/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar;

import com.sun.org.apache.xpath.internal.operations.Variable;
import com.whizzosoftware.hobson.api.device.AbstractHobsonDevice;
import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.venstar.api.ColorTouchChannel;
import com.whizzosoftware.hobson.venstar.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private ColorTouchChannel channel;
    private URI uri;
    private String defaultName;
    private Double lastTempF;
    private Double lastCoolTempF;
    private Double lastHeatTempF;
    private String lastMode;
    private String lastFanMode;
    private ControlRequest pendingControlRequest;

    public ColorTouchThermostat(HobsonPlugin plugin, ColorTouchChannel channel, URI uri, InfoResponse info) {
        super(plugin, uri.getHost().replace('.', '-'));

        this.channel = channel;
        this.uri = uri;
        if (info != null) {
            this.defaultName = info.getName();
            this.lastTempF = info.getSpaceTemp();
            this.lastCoolTempF = info.getCoolTemp();
            this.lastHeatTempF = info.getHeatTemp();
            this.lastMode = info.getMode().toString();
            this.lastFanMode = info.getFanMode().toString();
        }
    }

    @Override
    public void onStartup() {
        publishVariable(VariableConstants.TEMP_F, lastTempF, HobsonVariable.Mask.READ_ONLY);
        publishVariable(VariableConstants.TARGET_COOL_TEMP_F, lastCoolTempF, HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.TARGET_HEAT_TEMP_F, lastHeatTempF, HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.TSTAT_MODE, lastMode, HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.TSTAT_FAN_MODE, lastFanMode, HobsonVariable.Mask.READ_WRITE);
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
        return new String[] {VariableConstants.TEMP_F, VariableConstants.TARGET_HEAT_TEMP_F, VariableConstants.TARGET_COOL_TEMP_F};
    }

    @Override
    public DeviceType getType() {
        return DeviceType.THERMOSTAT;
    }

    @Override
    public void onSetVariable(String name, Object value) {
        ThermostatMode mode = null;
        FanMode fanMode = null;
        Double heatTemp = null;
        Double coolTemp = null;

        if (name.equals(VariableConstants.TARGET_COOL_TEMP_F)) {
            coolTemp = getDouble(value);
        } else if (name.equals(VariableConstants.TARGET_HEAT_TEMP_F)) {
            heatTemp = getDouble(value);
        } else if (name.equals(VariableConstants.TSTAT_MODE)) {
            mode = ThermostatMode.valueOf(value.toString());
        } else if (name.equals(VariableConstants.TSTAT_FAN_MODE)) {
            fanMode = FanMode.valueOf(value.toString());
        }

        // the ColorTouch thermostat doesn't have a way to just update a single property. We therefore query the
        // thermostat for its latest values (in case the thermostat was changed through other means in between refresh
        // intervals) and will send a full control request when a response is received
        try {
            pendingControlRequest = new ControlRequest(uri, getId(), mode, fanMode, heatTemp, coolTemp, null);
            channel.sendInfoRequest(new InfoRequest(getBaseURI(), getId()));
        } catch (URISyntaxException e) {
            logger.error("Error refreshing thermostat: " + getId(), e);
        }
    }

    public URI getBaseURI() {
        return uri;
    }

    public void refresh() {
        try {
            channel.sendInfoRequest(new InfoRequest(getBaseURI(), getId()));
        } catch (URISyntaxException e) {
            logger.error("Error refreshing thermostat: " + getId(), e);
        }
    }

    public Double getDouble(Object value) {
        if (value instanceof Double) {
            return (Double)value;
        } else if (value instanceof Integer) {
            return (double)(Integer)value;
        } else {
            return Double.parseDouble(value.toString());
        }
    }

    protected boolean hasPendingControlRequest() {
        return (pendingControlRequest != null);
    }

    protected Double getLastTempF() {
        return lastTempF;
    }

    protected Double getLastCoolTempF() {
        return lastCoolTempF;
    }

    protected Double getLastHeatTempF() {
        return lastHeatTempF;
    }

    protected String getLastMode() {
        return lastMode;
    }

    protected String getLastFanMode() {
        return lastFanMode;
    }

    /**
     * Callback when an InfoResponse is received for this device.
     *
     * @param response an InfoResponse object
     * @param error a Throwable if an HTTP protocol-level error occurred
     */
    public void onInfoResponse(InfoResponse response, Throwable error) {
        if (response != null) {
            String newMode = response.getMode().toString();
            String newFanMode = response.getFanMode().toString();
            Double newTempF = response.getSpaceTemp();
            Double newHeatTempF = response.getHeatTemp();
            Double newCoolTempF = response.getCoolTemp();

            List<VariableUpdate> updates = new ArrayList<>();
            if (lastMode == null || !lastMode.equals(newMode)) {
                updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TSTAT_MODE, newMode));
            }
            if (lastFanMode == null || !lastFanMode.equals(newFanMode)) {
                updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TSTAT_FAN_MODE, newFanMode));
            }
            if (lastTempF == null || !lastTempF.equals(newTempF)) {
                updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TEMP_F, newTempF));
            }
            if (lastCoolTempF == null || !lastCoolTempF.equals(newCoolTempF)) {
                updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TARGET_COOL_TEMP_F, newCoolTempF));
            }
            if (lastHeatTempF == null || !lastHeatTempF.equals(newHeatTempF)) {
                updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TARGET_HEAT_TEMP_F, newHeatTempF));
            }
            if (updates.size() > 0) {
                fireVariableUpdateNotifications(updates);
            }

            lastMode = newMode;
            lastFanMode = newFanMode;
            lastTempF = newTempF;
            lastCoolTempF = newCoolTempF;
            lastHeatTempF = newHeatTempF;

            // if there's a pending control request, send it now that we have the latest status from the thermostat
            if (hasPendingControlRequest()) {
                pendingControlRequest.updateIfNull(response.getMode(), response.getFanMode(), newHeatTempF, newCoolTempF, null);
                channel.sendControlRequest(pendingControlRequest);
                pendingControlRequest = null;
            }
        } else if (error != null) {
            logger.error("Error retrieving state info for device: " + getId(), error);
        }
    }

    /**
     * Callback when a ControlResponse is received for this device. Note that there are two types of errors that can
     * occur here. One is a response error which would be in the ControlResponse object if it occurred. The other is
     * a HTTP protocol error which would be the error param.
     *
     * @param response the ControlResponse object
     * @param error a Throwable if an HTTP protocol-level error occurred
     */
    public void onControlResponse(ControlResponse response, Throwable error) {
        if (response != null) {
            logger.trace("Received successful control response");
        } else if (error != null) {
            logger.error("Error sending control request for device " + getId(), error);
        }
    }
}
