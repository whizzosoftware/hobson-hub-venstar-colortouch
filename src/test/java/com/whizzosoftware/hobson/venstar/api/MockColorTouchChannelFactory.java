package com.whizzosoftware.hobson.venstar.api;

public class MockColorTouchChannelFactory implements ColorTouchChannelFactory {
    private Integer apiVersion;

    public MockColorTouchChannelFactory(Integer apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public ColorTouchChannel createColorTouchChannel(String host) {
        return new MockColorTouchChannel(host, apiVersion);
    }
}
