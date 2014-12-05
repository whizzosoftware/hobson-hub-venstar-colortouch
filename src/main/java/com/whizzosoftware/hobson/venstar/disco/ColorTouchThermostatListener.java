/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.disco;

/**
 * A local listener for thermostat discoveries.
 *
 * @author Dan Noguerol
 */
public interface ColorTouchThermostatListener {
    /**
     * Callback when a new thermostat is found.
     *
     * @param host the host of the discovered thermostat
     */
    public void onThermostatFound(String host);
}
