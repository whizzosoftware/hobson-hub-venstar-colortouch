/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.api.dto;

import org.json.JSONObject;

/**
 * Encapsulates the result of a thermostat "info request". This provides current state information for a thermostat.
 *
 * @author Dan Noguerol
 */
public class InfoResponse {
    private Boolean on;
    private String name;
    private ThermostatMode mode;
    private FanMode fanMode;
    private Integer tempUnits;
    private Double spaceTemp;
    private Double coolTemp;
    private Double heatTemp;
    private Double setPointDelta;

    public InfoResponse(Boolean on, String name, ThermostatMode mode, FanMode fanMode, Integer tempUnits, Double spaceTemp, Double coolTemp, Double heatTemp, Double setPointDelta) {
        this.on = on;
        this.name = name;
        this.mode = mode;
        this.fanMode = fanMode;
        this.tempUnits = tempUnits;
        this.spaceTemp = spaceTemp;
        this.coolTemp = coolTemp;
        this.heatTemp = heatTemp;
        this.setPointDelta = setPointDelta;
    }

    public InfoResponse(JSONObject json) {
        if (json.has("name")) {
            name = json.getString("name");
        }
        if (json.has("state")) {
            int s = json.getInt("state");
            on = (s >= 1 && s <= 2);
        }
        if (json.has("mode")) {
            mode = ThermostatMode.values()[json.getInt("mode")];
        }
        if (json.has("fan")) {
            fanMode = FanMode.values()[json.getInt("fan")];
        }
        if (json.has("tempunits")) {
            tempUnits = json.getInt("tempunits");
        }
        if (json.has("spacetemp")) {
            spaceTemp = json.getDouble("spacetemp");
        }
        if (json.has("cooltemp")) {
            coolTemp = json.getDouble("cooltemp");
        }
        if (json.has("heattemp")) {
            heatTemp = json.getDouble("heattemp");
        }
        if (json.has("setpointdelta")) {
            setPointDelta = json.getDouble("setpointdelta");
        }
    }

    public String getName() {
        return name;
    }

    public Boolean getOn() {
        return on;
    }

    public ThermostatMode getMode() {
        return mode;
    }

    public FanMode getFanMode() {
        return fanMode;
    }

    public Integer getTempUnits() {
        return tempUnits;
    }

    public Double getSpaceTemp() {
        return spaceTemp;
    }

    public Double getCoolTemp() {
        return coolTemp;
    }

    public Double getHeatTemp() {
        return heatTemp;
    }

    public Double getSetPointDelta() {
        return setPointDelta;
    }
}
