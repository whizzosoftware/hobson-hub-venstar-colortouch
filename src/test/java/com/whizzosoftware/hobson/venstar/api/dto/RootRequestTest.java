package com.whizzosoftware.hobson.venstar.api.dto;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class RootRequestTest {
    @Test
    public void testConstructor() throws Exception {
        RootRequest request = new RootRequest(new URI("http://192.168.0.129"));
        assertEquals("http://192.168.0.129", request.getURI().toString());
    }
}
