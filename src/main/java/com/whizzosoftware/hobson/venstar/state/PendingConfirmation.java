/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.state;

/**
 * Represents a pending confirmation of new state from the thermostat. These are created when a "set variable" request
 * is sent to a thermostat. Confirmation will come in the form of an info response that meets the awaited confirmation
 * variable state.
 *
 * @author Dan Noguerol
 */
public class PendingConfirmation {
    public static final long PENDING_CONTROL_REQUEST_TIMEOUT = 10000;

    private VariableState state = new VariableState();
    private Long controlRequestTime;

    public PendingConfirmation() {
        this.state = new VariableState();
    }

    public void clear() {
        controlRequestTime = null;
        state.clear();
    }

    public VariableState getState() {
        return state;
    }

    public void flagControlRequestSent(long now) {
        controlRequestTime = now;
    }

    public boolean wasControlRequestSent() {
        return (controlRequestTime != null);
    }

    public boolean hasTimeout(long now) {
        return (controlRequestTime > 0 && now - controlRequestTime >= PENDING_CONTROL_REQUEST_TIMEOUT);
    }
}
