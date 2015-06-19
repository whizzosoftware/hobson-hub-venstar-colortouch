package com.whizzosoftware.hobson.venstar.api.dto;

import com.whizzosoftware.hobson.api.device.DeviceContext;

import java.net.URI;
import java.net.URISyntaxException;

public class InfoRequest {
    private URI baseURI;
    private URI uri;
    private DeviceContext deviceContext;

    public InfoRequest(URI baseURI) throws URISyntaxException {
        this(baseURI, null);
    }

    public InfoRequest(URI baseURI, DeviceContext deviceContext) throws URISyntaxException {
        this.baseURI = baseURI;
        this.uri = new URI(baseURI.getScheme(), baseURI.getHost(), "/query/info", null);
        this.deviceContext = deviceContext;
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public URI getURI() {
        return uri;
    }

    public DeviceContext getDeviceContext() {
        return deviceContext;
    }

    public boolean hasDeviceId() {
        return (deviceContext != null);
    }
}
