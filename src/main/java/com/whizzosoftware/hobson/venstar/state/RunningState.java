/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.state;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.venstar.api.dto.*;

/**
 * Represents the "normal" running state of the plugin.
 *
 * @author Dan Noguerol
 */
public class RunningState implements State {
    @Override
    public void onRefresh(StateContext context, long now) {
        context.refreshAllThermostats(now);
    }

    @Override
    public void onThermostatFound(StateContext context) {
        context.setState(new DiscoveryState());
    }

    @Override
    public void onSetDeviceVariable(StateContext context, DeviceContext deviceContext, String name, Object value) {
        context.doSetDeviceVariable(deviceContext, name, value);
    }

    @Override
    public void onRootResponse(StateContext context, RootRequest request, RootResponse response, Throwable error) {

    }

    @Override
    public void onInfoResponse(StateContext context, InfoRequest request, InfoResponse response, Throwable error) {
        if (request.getDeviceContext() != null) {
            context.getThermostatDevice(request.getDeviceContext()).onInfoResponse(request, response, error, System.currentTimeMillis());
        }
    }

    @Override
    public void onControlResponse(StateContext context, ControlRequest request, ControlResponse response, Throwable error) {
        if (request.getDeviceContext() != null) {
            context.getThermostatDevice(request.getDeviceContext()).onControlResponse(request, response, error);
        }
    }
}
