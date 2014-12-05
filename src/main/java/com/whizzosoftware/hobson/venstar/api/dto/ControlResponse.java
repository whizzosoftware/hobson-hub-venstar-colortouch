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
 * Encapsulates the result of a thermostat "control request". This provides information about whether the
 * request was successful or not (including error information if present).
 *
 * @author Dan Noguerol
 */
public class ControlResponse {
    private boolean isError;
    private String errorReason;

    public ControlResponse() {}

    public ControlResponse(JSONObject json) {
        if (json.has("error") && json.getBoolean("error")) {
            isError = true;
            errorReason = json.getString("reason");
        } else if (!json.has("success")) {
            isError = true;
            errorReason = "No success or error keys in response";
        }
    }

    public boolean isError() {
        return isError;
    }

    public String getErrorReason() {
        return errorReason;
    }
}
