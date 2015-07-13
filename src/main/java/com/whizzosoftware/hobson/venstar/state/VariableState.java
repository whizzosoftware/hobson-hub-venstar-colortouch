/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.state;

import com.whizzosoftware.hobson.api.variable.VariableConstants;

/**
 * Represents the current values of a thermostat's variables.
 *
 * @author Dan Noguerol
 */
public class VariableState {
    private Boolean on;
    private String mode;
    private String fanMode;
    private Double tempF;
    private Double coolTempF;
    private Double heatTempF;

    public VariableState() {}

    public VariableState(Boolean on, String mode, String fanMode, Double tempF, Double coolTempF, Double heatTempF) {
        update(on, mode, fanMode, tempF, coolTempF, heatTempF);
    }

    public void update(Boolean on, String mode, String fanMode, Double tempF, Double coolTempF, Double heatTempF) {
        this.on = on;
        this.mode = mode;
        this.fanMode = fanMode;
        this.tempF = tempF;
        this.coolTempF = coolTempF;
        this.heatTempF = heatTempF;
    }

    public void clear() {
        this.mode = null;
        this.fanMode = null;
        this.tempF = null;
        this.coolTempF = null;
        this.heatTempF = null;
    }

    public boolean hasOn() {
        return (on != null);
    }

    public Boolean getOn() {
        return on;
    }

    public void setOn(Boolean on) {
        this.on = on;
    }

    public boolean hasMode() {
        return (mode != null);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean hasFanMode() {
        return (fanMode != null);
    }

    public String getFanMode() {
        return fanMode;
    }

    public void setFanMode(String fanMode) {
        this.fanMode = fanMode;
    }

    public boolean hasTempF() {
        return (tempF != null);
    }

    public Double getTempF() {
        return tempF;
    }

    public void setTempF(Double tempF) {
        this.tempF = tempF;
    }

    public boolean hasCoolTempF() {
        return (coolTempF != null);
    }

    public Double getCoolTempF() {
        return coolTempF;
    }

    public void setCoolTempF(Double coolTempF) {
        this.coolTempF = coolTempF;
    }

    public boolean hasHeatTempF() {
        return (heatTempF != null);
    }

    public Double getHeatTempF() {
        return heatTempF;
    }

    public void setHeatTempF(Double heatTempF) {
        this.heatTempF = heatTempF;
    }

    /**
     * Set a variable value based on variable name.
     *
     * @param name the variable name
     * @param value the variable value
     */
    public void setValue(String name, Object value) {
        if (VariableConstants.TSTAT_MODE.equals(name)) {
            this.mode = value.toString();
        } else if (VariableConstants.TSTAT_FAN_MODE.equals(name)) {
            this.fanMode = value.toString();
        } else if (VariableConstants.TEMP_F.equals(name)) {
            this.tempF = (Double)value;
        } else if (VariableConstants.TARGET_COOL_TEMP_F.equals(name)) {
            this.coolTempF = getDouble(value);
        } else if (VariableConstants.TARGET_HEAT_TEMP_F.equals(name)) {
            this.heatTempF = getDouble(value);
        }
    }

    /**
     * Indicates whether the state has any variable values defined.
     *
     * @return a boolean
     */
    public boolean hasValues() {
        return (getMode() != null || getFanMode() != null || getTempF() != null || getCoolTempF() != null || getHeatTempF() != null);
    }

    /**
     * Indicates whether this state is equal to another state.
     *
     * @param state the state to compare with
     *
     * @return a boolean
     */
    public boolean equals(VariableState state) {
        return (
            (getMode() == null || state.getMode() == null || getMode().equals(state.getMode())) &&
            (getFanMode() == null || state.getFanMode() == null || getFanMode().equals(state.getFanMode())) &&
            (getCoolTempF() == null || state.getCoolTempF() == null || getCoolTempF().equals(state.getCoolTempF())) &&
            (getHeatTempF() == null || state.getHeatTempF() == null || getHeatTempF().equals(state.getHeatTempF()))
        );
    }

    private Double getDouble(Object value) {
        if (value instanceof Double) {
            return (Double)value;
        } else if (value instanceof Integer) {
            return (double)(Integer)value;
        } else {
            return Double.parseDouble(value.toString());
        }
    }
}
