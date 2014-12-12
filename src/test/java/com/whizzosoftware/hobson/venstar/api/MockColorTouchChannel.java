package com.whizzosoftware.hobson.venstar.api;

import com.whizzosoftware.hobson.venstar.api.dto.ControlRequest;
import com.whizzosoftware.hobson.venstar.api.dto.InfoRequest;
import com.whizzosoftware.hobson.venstar.api.dto.RootRequest;

import java.util.ArrayList;
import java.util.List;

public class MockColorTouchChannel implements ColorTouchChannel {
    private final List<InfoRequest> infoRequests = new ArrayList<>();
    private final List<ControlRequest> controlRequests = new ArrayList<>();

    @Override
    public void sendRootRequest(RootRequest request) {
    }

    @Override
    public void sendInfoRequest(InfoRequest request) {
        infoRequests.add(request);
    }

    @Override
    public void sendControlRequest(ControlRequest request) {
        controlRequests.add(request);
    }

    public List<InfoRequest> getInfoRequests() {
        return infoRequests;
    }

    public void clearInfoRequests() {
        infoRequests.clear();
    }

    public List<ControlRequest> getControlRequests() {
        return controlRequests;
    }

    public void clearControlRequests() {
        controlRequests.clear();
    }
}
