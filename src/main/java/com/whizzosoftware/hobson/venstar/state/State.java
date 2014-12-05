/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.state;

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
}
