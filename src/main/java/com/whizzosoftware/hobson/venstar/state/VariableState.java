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
    public static final String TARGET_COOL_TEMP_F = "targetCoolTempF";
    public static final String TARGET_HEAT_TEMP_F = "targetHeatTempF";

    private String mode;
    private String fanMode;
    private Double tempF;
    private Double coolTempF;
    private Double heatTempF;
    private Double targetTempF;

    public VariableState() {}

    public VariableState(String mode, String fanMode, Double tempF, Double coolTempF, Double heatTempF, Double targetTempF) {
        update(mode, fanMode, tempF, coolTempF, heatTempF, targetTempF);
    }

    public void update(String mode, String fanMode, Double tempF, Double coolTempF, Double heatTempF, Double targetTempF) {
        this.mode = mode;
        this.fanMode = fanMode;
        this.tempF = tempF;
        this.coolTempF = coolTempF;
        this.heatTempF = heatTempF;
        this.targetTempF = targetTempF;
    }

    public void clear() {
        this.mode = null;
        this.fanMode = null;
        this.tempF = null;
        this.coolTempF = null;
        this.heatTempF = null;
        this.targetTempF = null;
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

    public boolean hasTargetTempF() {
        return (targetTempF != null);
    }

    public Double getTargetTempF() {
        return targetTempF;
    }

    public void setTargetTempF(Double targetTempF) {
        this.targetTempF = targetTempF;
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
        } else if (VariableConstants.TARGET_TEMP_F.equals(name)) {
            this.targetTempF = getDouble(value);
        } else if (TARGET_COOL_TEMP_F.equals(name)) {
            this.coolTempF = getDouble(value);
        } else if (TARGET_HEAT_TEMP_F.equals(name)) {
            this.heatTempF = getDouble(value);
        }
    }

    /**
     * Indicates whether the state has any variable values defined.
     *
     * @return a boolean
     */
    public boolean hasValues() {
        return (getMode() != null || getFanMode() != null || getTempF() != null || getTargetTempF() != null || getCoolTempF() != null || getHeatTempF() != null);
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
            (getHeatTempF() == null || state.getHeatTempF() == null || getHeatTempF().equals(state.getHeatTempF())) &&
            (getTargetTempF() == null || state.getTargetTempF() == null || getTargetTempF().equals(state.getTargetTempF()))
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
