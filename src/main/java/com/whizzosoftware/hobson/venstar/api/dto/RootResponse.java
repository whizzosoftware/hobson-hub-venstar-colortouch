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
 * Encapsulates the result of a thermostat "root request". This provides high-level information about the
 * thermostat including its API version.
 *
 * @author Dan Noguerol
 */
public class RootResponse {
    private Integer apiVersion;
    private String thermostatType;

    public RootResponse(JSONObject json) {
        if (json.has("api_ver")) {
            apiVersion = json.getInt("api_ver");
        }
        if (json.has("type")) {
            thermostatType = json.getString("type");
        }
    }

    public RootResponse(Integer apiVersion, String thermostatType) {
        this.apiVersion = apiVersion;
        this.thermostatType = thermostatType;
    }

    public Integer getApiVersion() {
        return apiVersion;
    }

    public String getThermostatType() {
        return thermostatType;
    }
}
