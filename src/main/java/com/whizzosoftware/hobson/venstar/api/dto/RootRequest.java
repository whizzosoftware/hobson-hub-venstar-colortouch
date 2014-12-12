package com.whizzosoftware.hobson.venstar.api.dto;

import java.net.URI;
import java.net.URISyntaxException;

public class RootRequest {
    private URI baseURI;

    public RootRequest(URI baseURI) throws URISyntaxException {
        this.baseURI = baseURI;
    }

    public URI getURI() {
        return baseURI;
    }
}
