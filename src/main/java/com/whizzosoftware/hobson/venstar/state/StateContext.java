/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.state;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.venstar.ColorTouchThermostat;
import com.whizzosoftware.hobson.venstar.api.ColorTouchChannel;
import com.whizzosoftware.hobson.venstar.api.dto.InfoResponse;

import java.net.URI;
import java.util.Collection;

/**
 * A context interface for state callbacks.
 *
 * @author Dan Noguerol
 */
public interface StateContext extends ColorTouchChannel {
    Collection<URI> getDiscoveredURIs();
    void setState(State state);
    boolean hasThermostats();
    boolean hasThermostatWithHost(String host);
    void addThermostat(URI baseURI, InfoResponse info);
    void refreshAllThermostats(long now);
    void doSetDeviceVariable(DeviceContext ctx, String name, Object value);
    ColorTouchThermostat getThermostatDevice(DeviceContext ctx);
}
