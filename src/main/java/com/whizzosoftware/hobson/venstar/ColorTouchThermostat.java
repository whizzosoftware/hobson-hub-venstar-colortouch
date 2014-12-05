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
import com.whizzosoftware.hobson.venstar.api.ColorTouchChannelException;
import com.whizzosoftware.hobson.venstar.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private String defaultName;
    private Double initialTempF;
    private Double initialCoolTempF;
    private Double initialHeatTempF;
    private String initialTstatMode;
    private String initialTstatFanMode;

    public ColorTouchThermostat(HobsonPlugin plugin, ColorTouchChannel channel, InfoResponse info) {
        super(plugin, channel.getId());

        this.channel = channel;
        if (info != null) {
            this.defaultName = info.getName();
            this.initialTempF = info.getSpaceTemp();
            this.initialCoolTempF = info.getCoolTemp();
            this.initialHeatTempF = info.getHeatTemp();
            this.initialTstatMode = info.getMode().toString();
            this.initialTstatFanMode = info.getFan().toString();
        }
    }

    @Override
    public void onStartup() {
        publishVariable(VariableConstants.TEMP_F, initialTempF, HobsonVariable.Mask.READ_ONLY);
        publishVariable(VariableConstants.TARGET_COOL_TEMP_F, initialCoolTempF, HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.TARGET_HEAT_TEMP_F, initialHeatTempF, HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.TSTAT_MODE, initialTstatMode, HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.TSTAT_FAN_MODE, initialTstatFanMode, HobsonVariable.Mask.READ_WRITE);

        initialTempF = null;
        initialCoolTempF = null;
        initialHeatTempF = null;
        initialTstatMode = null;
        initialTstatFanMode = null;
    }

    @Override
    public void onShutdown() {
        channel.close();
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
    public DeviceType getType() {
        return DeviceType.THERMOSTAT;
    }

    @Override
    public void onSetVariable(String name, Object value) {
        try {
            InfoResponse info = channel.sendInfoRequest();
            Double heatTemp = info.getHeatTemp();
            Double coolTemp = info.getCoolTemp();
            ThermostatMode mode = info.getMode();
            FanMode fanMode = info.getFan();

            if (name.equals(VariableConstants.TARGET_COOL_TEMP_F)) {
                coolTemp = getDouble(value);
            } else if (name.equals(VariableConstants.TARGET_HEAT_TEMP_F)) {
                heatTemp = getDouble(value);
            } else if (name.equals(VariableConstants.TSTAT_MODE)) {
                mode = ThermostatMode.valueOf(value.toString());
            } else if (name.equals(VariableConstants.TSTAT_FAN_MODE)) {
                fanMode = FanMode.valueOf(value.toString());
            }

            ControlResponse res = channel.sendControlRequest(
                new ControlRequest(mode, fanMode, heatTemp, coolTemp, null)
            );

            if (res.isError()) {
                logger.error("Error setting variable " + name + ": " + res.getErrorReason());
            }
        } catch (ColorTouchChannelException e) {
            logger.error("Error setting variable " + name, e);
        }
    }

    public String getHost() {
        return channel.getHost();
    }

    public void refresh() {
        try {
            InfoResponse info = channel.sendInfoRequest();
            List<VariableUpdate> updates = new ArrayList<VariableUpdate>();
            updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TEMP_F, info.getSpaceTemp()));
            updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TARGET_COOL_TEMP_F, info.getCoolTemp()));
            updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TARGET_HEAT_TEMP_F, info.getHeatTemp()));
            updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TSTAT_MODE, info.getMode().toString()));
            updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.TSTAT_FAN_MODE, info.getFan().toString()));
            fireVariableUpdateNotifications(updates);
        } catch (ColorTouchChannelException e) {
            logger.error("Error refreshing device " + getId(), e);
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
}
