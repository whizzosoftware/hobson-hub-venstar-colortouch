/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.api;

import com.whizzosoftware.hobson.venstar.api.dto.ControlRequest;
import com.whizzosoftware.hobson.venstar.api.dto.ControlResponse;
import com.whizzosoftware.hobson.venstar.api.dto.InfoResponse;
import com.whizzosoftware.hobson.venstar.api.dto.RootResponse;

/**
 * Interface that represents a communication channel with a ColorTouch thermostat.
 *
 * @author Dan Noguerol
 */
public interface ColorTouchChannel {
    public String getId();
    public String getHost();
    public RootResponse sendRootRequest() throws ColorTouchChannelException;
    public InfoResponse sendInfoRequest() throws ColorTouchChannelException;
    public ControlResponse sendControlRequest(ControlRequest request) throws ColorTouchChannelException;
    public void close();
}
