/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.state;

import com.whizzosoftware.hobson.venstar.api.dto.InfoRequest;
import com.whizzosoftware.hobson.venstar.api.dto.InfoResponse;
import com.whizzosoftware.hobson.venstar.api.dto.RootRequest;
import com.whizzosoftware.hobson.venstar.api.dto.RootResponse;

/**
 * An interface that represents a single state in this plugin's finite state machine. The methods in the interface
 * represent all the possible events that can occur in a state.
 *
 * @author Dan Noguerol
 */
public interface State {
    /**
     * Callback when the Hobson runtime invokes a refresh of the plugin.
     *
     * @param context the current state context
     */
    public void onRefresh(StateContext context);

    /**
     * Callback when a new thermostat is found.
     *
     * @param context the current state context
     */
    public void onThermostatFound(StateContext context);

    /**
     * Callback when a request to set a device variable is received.
     *
     * @param context the current state context
     * @param deviceId the ID of the device for which the request was made
     * @param name the variable name
     * @param value the variable value
     */
    public void onSetDeviceVariable(StateContext context, String deviceId, String name, Object value);

    /**
     * Called when a "root response" is received from a thermostat.
     *
     * @param context the current state context
     * @param request a RootRequest object
     * @param response a RootResponse object (or null if it was an error)
     * @param error an Exception (or null if a successful response)
     */
    public void onRootResponse(StateContext context, RootRequest request, RootResponse response, Throwable error);

    /**
     * Called when an "info response" is received from a thermostat.
     *
     * @param context the current state context
     * @param request an InfoRequest object
     * @param response an InfoResponse object (or null if it was an error)
     * @param error an Exception (or null if a successful response)
     */
    public void onInfoResponse(StateContext context, InfoRequest request, InfoResponse response, Throwable error);
}
