package com.whizzosoftware.hobson.venstar.api.dto;

import java.net.URI;
import java.net.URISyntaxException;

public class InfoRequest {
    private URI baseURI;
    private URI uri;
    private String deviceId;

    public InfoRequest(URI baseURI) throws URISyntaxException {
        this(baseURI, null);
    }

    public InfoRequest(URI baseURI, String deviceId) throws URISyntaxException {
        this.baseURI = baseURI;
        this.uri = new URI(baseURI.getScheme(), baseURI.getHost(), "/query/info", null);
        this.deviceId = deviceId;
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public URI getURI() {
        return uri;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean hasDeviceId() {
        return (deviceId != null);
    }
}
