/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.state;

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
    public Collection<URI> getDiscoveredURIs();
    public void setState(State state);
    public boolean hasThermostats();
    public boolean hasThermostatWithHost(String host);
    public void addThermostat(URI baseURI, InfoResponse info);
    public void refreshAllThermostats();
    public void doSetDeviceVariable(String deviceId, String name, Object value);
    public ColorTouchThermostat getThermostatDevice(String deviceId);
}
