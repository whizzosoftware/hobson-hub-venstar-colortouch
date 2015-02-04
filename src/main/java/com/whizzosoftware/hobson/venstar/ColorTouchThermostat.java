/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar;

import com.whizzosoftware.hobson.api.device.AbstractHobsonDevice;
import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.venstar.api.ColorTouchChannel;
import com.whizzosoftware.hobson.venstar.api.dto.*;
import com.whizzosoftware.hobson.venstar.state.PendingConfirmation;
import com.whizzosoftware.hobson.venstar.state.VariableState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * A HobsonDevice implementation for ColorTouch thermostats.
 *
 * @author Dan Noguerol
 */
public class ColorTouchThermostat extends AbstractHobsonDevice {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected static final long DEFAULT_REFRESH_INTERVAL_IN_MS_NO_PENDING_CONFIRMS = 10000;
    protected static final long DEFAULT_REFRESH_INTERVAL_IN_MS_PENDING_CONFIRMS = 1000;
    protected static final long DEFAULT_INFO_RESPONSE_TIMEOUT = 5000;

    private ColorTouchChannel channel;
    private URI uri;
    private String defaultName;
    /**
     * This represents the current state of the thermostat (based on the last info response received)
     */
    private VariableState currentState;
    /**
     * Indicates the last time the refresh() method was called
     */
    private long lastRefresh;
    /**
     * Indicates the last time an info request was made
     */
    private Long pendingInfoRequestTime;
    /**
     * This represents a pending confirmation we are awaiting (based on the last control request sent)
     */
    private final PendingConfirmation pendingConfirmation = new PendingConfirmation();

    public ColorTouchThermostat(HobsonPlugin plugin, ColorTouchChannel channel, URI uri, InfoResponse info) {
        super(plugin, uri.getHost().replace('.', '-'));

        this.channel = channel;
        this.uri = uri;
        this.currentState = new VariableState();
        if (info != null) {
            this.defaultName = info.getName();
            // set the current state to the InfoResponse argument
            this.currentState.update(
                    info.getMode().toString(),
                    info.getFanMode().toString(),
                    info.getSpaceTemp(),
                    info.getCoolTemp(),
                    info.getHeatTemp(),
                    calculateTargetTemp(info.getMode().toString(), info.getCoolTemp(), info.getHeatTemp())
            );
        }
    }

    @Override
    public void onStartup() {
        // publish necessary variables
        publishVariable(VariableConstants.TEMP_F, currentState.getTempF(), HobsonVariable.Mask.READ_ONLY);
        publishVariable(VariableConstants.TSTAT_MODE, currentState.getMode(), HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.TARGET_TEMP_F, currentState.getTargetTempF(), HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.TSTAT_FAN_MODE, currentState.getFanMode(), HobsonVariable.Mask.READ_WRITE);
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public String getPreferredVariableName() {
        return VariableConstants.TEMP_F;
    }

    @Override
    public String[] getTelemetryVariableNames() {
        return new String[] {VariableConstants.TEMP_F, VariableConstants.TARGET_TEMP_F};
    }

    @Override
    public DeviceType getType() {
        return DeviceType.THERMOSTAT;
    }

    @Override
    public void onSetVariable(String name, Object value) {
        // the ColorTouch thermostat doesn't have a way to just update a single property. We therefore query the
        // thermostat for its latest values (in case the thermostat was changed through other means in between refresh
        // intervals) and will send a full control request when a response is received
        try {
            // set the pending confirmation state to new value
            pendingConfirmation.getState().setValue(name, value);

            // if we're not already waiting on an info response, send a new info request
            if (pendingInfoRequestTime == null) {
                channel.sendInfoRequest(new InfoRequest(getBaseURI(), getId()));
                pendingInfoRequestTime = System.currentTimeMillis();
            }
        } catch (URISyntaxException e) {
            logger.error("Error refreshing thermostat: " + getId(), e);
        }
    }

    public URI getBaseURI() {
        return uri;
    }

    public void onRefresh(long now) {
        // by default, our check interval assumes no pending control confirmations
        long checkInterval = DEFAULT_REFRESH_INTERVAL_IN_MS_NO_PENDING_CONFIRMS;

        // if we're waiting on a control request confirmation...
        if (hasPendingControlConfirmation()) {
            // if there's been a timout, stop waiting for the confirmation
            if (pendingConfirmation.hasTimeout(now)) {
                pendingConfirmation.clear();
                logger.warn("A timeout occurred waiting for a control request confirmation");
            // otherwise, set the check interval appropriately (basically, we want to check with the thermostat more
            // frequently (up to a timeout interval) when there's a control request that's awaiting confirmation
            } else {
                checkInterval = DEFAULT_REFRESH_INTERVAL_IN_MS_PENDING_CONFIRMS;
            }
        }

        // if we've exceeded the refresh interval and there's no pending info request or the last request timed out...
        if (now - lastRefresh >= checkInterval && (pendingInfoRequestTime == null || now - pendingInfoRequestTime >= DEFAULT_INFO_RESPONSE_TIMEOUT)) {
            // send a new info request to the thermostat
            try {
                channel.sendInfoRequest(new InfoRequest(getBaseURI(), getId()));
                pendingInfoRequestTime = System.currentTimeMillis();
                lastRefresh = now;
            } catch (URISyntaxException e) {
                logger.error("Error refreshing thermostat: " + getId(), e);
            }
        }
    }

    protected VariableState getCurrentState() {
        return currentState;
    }

    protected boolean hasPendingControlConfirmation() {
        return pendingConfirmation.getState().hasValues();
    }

    /**
     * Callback when an InfoResponse is received for this device.
     *
     * @param response an InfoResponse object
     * @param error a Throwable if an HTTP protocol-level error occurred
     */
    public void onInfoResponse(InfoRequest request, InfoResponse response, Throwable error, long now) {
        pendingInfoRequestTime = null;

        // if it's a good response, process it
        if (response != null) {
            // create a new variable state based on the response
            VariableState responseState = new VariableState(
                response.getMode().toString(),
                response.getFanMode().toString(),
                response.getSpaceTemp(),
                response.getCoolTemp(),
                response.getHeatTemp(),
                calculateTargetTemp(response.getMode().toString(), response.getCoolTemp(), response.getHeatTemp())
            );

            // if the response state is not equal to the pending confirmation state, send a control request
            if (!responseState.equals(pendingConfirmation.getState())) {
                if (!pendingConfirmation.wasControlRequestSent()) {
                    channel.sendControlRequest(ControlRequest.create(
                        uri,
                        getId(),
                        pendingConfirmation.getState().hasMode() ? pendingConfirmation.getState().getMode() : responseState.getMode(),
                        pendingConfirmation.getState().hasFanMode() ? pendingConfirmation.getState().getFanMode() : responseState.getFanMode(),
                        pendingConfirmation.getState().hasHeatTempF() ? pendingConfirmation.getState().getHeatTempF() : responseState.getHeatTempF(),
                        pendingConfirmation.getState().hasCoolTempF() ? pendingConfirmation.getState().getCoolTempF() : responseState.getCoolTempF(),
                        response.getSetPointDelta(),
                        pendingConfirmation.getState().hasTargetTempF() ? pendingConfirmation.getState().getTargetTempF() : responseState.getTargetTempF(),
                        null)
                    );
                    pendingConfirmation.flagControlRequestSent(now);
                }
            } else {
                pendingConfirmation.clear();
            }

            // build a list of variable updates based on any changes differences between current and response state
            List<VariableUpdate> updates = new ArrayList<>();
            if (!currentState.hasMode() || !currentState.getMode().equals(responseState.getMode())) {
                updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TSTAT_MODE, responseState.getMode()));
            }
            if (!currentState.hasFanMode() || !currentState.getFanMode().equals(responseState.getFanMode())) {
                updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TSTAT_FAN_MODE, responseState.getFanMode()));
            }
            if (!currentState.hasTempF() || !currentState.getTempF().equals(responseState.getTempF())) {
                updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TEMP_F, responseState.getTempF()));
            }
            if (!currentState.hasTargetTempF() || !currentState.getTargetTempF().equals(responseState.getTargetTempF())) {
                updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TARGET_TEMP_F, responseState.getTargetTempF()));
            }

            // fire variable update notifications if necessary
            if (updates.size() > 0) {
                fireVariableUpdateNotifications(updates);
            }

            // update the current state to reflect the response state
            currentState.update(
                responseState.getMode(),
                responseState.getFanMode(),
                responseState.getTempF(),
                responseState.getCoolTempF(),
                responseState.getHeatTempF(),
                responseState.getTargetTempF()
            );
        // if it's an error, clear the current state
        } else if (error != null) {
            logger.error("Error retrieving state info for device " + getId() + " at " + request.getURI(), error);

            // reset the last recorded values to force an update when new values are received
            currentState.clear();

            // post a null variable update to indicate we no longer know the current values
            List<VariableUpdate> updates = new ArrayList<>();
            updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TSTAT_MODE, null));
            updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TSTAT_FAN_MODE, null));
            updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TEMP_F, null));
            updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TARGET_TEMP_F, null));
            fireVariableUpdateNotifications(updates);
        }
    }

    /**
     * Callback when a ControlResponse is received for this device. Note that there are two types of errors that can
     * occur here. One is a response error which would be in the ControlResponse object if it occurred. The other is
     * a HTTP protocol error which would be the error param.
     *
     * @param request the ControlRequest object
     * @param response the ControlResponse object
     * @param error a Throwable if an HTTP protocol-level error occurred
     */
    public void onControlResponse(ControlRequest request, ControlResponse response, Throwable error) {
        if (response != null) {
            logger.trace("Received successful control response");
        } else if (error != null) {
            logger.error("Error sending control request for device " + getId(), error);
        }
    }

    protected Double calculateTargetTemp(String mode, Double coolTempF, Double heatTempF) {
        Double temp = null;
        if (mode != null) {
            if (mode.equals(ThermostatMode.COOL.toString())) {
                temp = coolTempF;
            } else if (mode.equals(ThermostatMode.HEAT.toString())) {
                temp = heatTempF;
            } else if (mode.equals(ThermostatMode.AUTO.toString())) {
                return round(heatTempF + ((coolTempF - heatTempF) / 2.0), 0);
            } else if (mode.equals(ThermostatMode.OFF.toString())) {
                temp = null;
            }
        }
        return temp;
    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
