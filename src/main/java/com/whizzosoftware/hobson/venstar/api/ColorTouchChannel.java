package com.whizzosoftware.hobson.venstar.api;

import com.whizzosoftware.hobson.venstar.api.dto.ControlRequest;
import com.whizzosoftware.hobson.venstar.api.dto.InfoRequest;
import com.whizzosoftware.hobson.venstar.api.dto.RootRequest;

/**
 * An interface for sending requests to ColorTouch thermostats.
 *
 * @author Dan Noguerol
 */
public interface ColorTouchChannel {
    public void sendRootRequest(RootRequest request);
    public void sendInfoRequest(InfoRequest request);
    public void sendControlRequest(ControlRequest request);
}
