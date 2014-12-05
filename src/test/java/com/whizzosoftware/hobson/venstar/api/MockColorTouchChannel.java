package com.whizzosoftware.hobson.venstar.api;

import com.whizzosoftware.hobson.venstar.api.dto.ControlRequest;
import com.whizzosoftware.hobson.venstar.api.dto.ControlResponse;
import com.whizzosoftware.hobson.venstar.api.dto.InfoResponse;
import com.whizzosoftware.hobson.venstar.api.dto.RootResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MockColorTouchChannel implements ColorTouchChannel {
    private String host;
    private Integer apiVersion;
    private final List<ControlRequest> controlRequests = new ArrayList<ControlRequest>();

    public MockColorTouchChannel(String host, Integer apiVersion) {
        this.host = host;
        this.apiVersion = apiVersion;
    }

    @Override
    public String getId() {
        return host;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public RootResponse sendRootRequest() throws ColorTouchChannelException {
        return new RootResponse(apiVersion, "residential");
    }

    @Override
    public InfoResponse sendInfoRequest() throws ColorTouchChannelException {
        return new InfoResponse();
    }

    @Override
    public ControlResponse sendControlRequest(ControlRequest request) throws ColorTouchChannelException {
        controlRequests.add(request);
        return new ControlResponse();
    }

    public Collection<ControlRequest> getControlRequests() {
        return controlRequests;
    }

    @Override
    public void close() {
    }
}
