/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.state;

/**
 * Represents the "normal" running state of the plugin.
 *
 * @author Dan Noguerol
 */
public class RunningState implements State {
    @Override
    public void onRefresh(StateContext context) {
        context.refreshAllThermostats();
    }

    @Override
    public void onThermostatFound(StateContext context) {
        context.setState(new DiscoveryState(context.getChannelFactory()));
    }

    @Override
    public void onSetDeviceVariable(StateContext context, String deviceId, String name, Object value) {
        context.doSetDeviceVariable(deviceId, name, value);
    }
}
