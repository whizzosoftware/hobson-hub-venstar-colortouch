/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar;

/**
 * Stores information about a pending set variable request. This is used by a ColorTouchThermostat to correlate
 * an async HTTP request for current state with the set variable request that prompted it.
 *
 * @author Dan Noguerol
 */
public class PendingSetVariableRequest {
    private String name;
    private Object value;

    public PendingSetVariableRequest(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
