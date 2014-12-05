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
    private String name;
    private ThermostatMode mode;
    private FanMode fan;
    private Integer tempUnits;
    private Double spaceTemp;
    private Double coolTemp;
    private Double heatTemp;

    public InfoResponse() {}

    public InfoResponse(JSONObject json) {
        if (json.has("name")) {
            name = json.getString("name");
        }
        if (json.has("mode")) {
            mode = ThermostatMode.values()[json.getInt("mode")];
        }
        if (json.has("fan")) {
            fan = FanMode.values()[json.getInt("fan")];
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
    }

    public String getName() {
        return name;
    }

    public ThermostatMode getMode() {
        return mode;
    }

    public FanMode getFan() {
        return fan;
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
}
