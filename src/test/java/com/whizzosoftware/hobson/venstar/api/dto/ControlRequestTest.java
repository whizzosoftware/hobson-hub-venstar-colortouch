package com.whizzosoftware.hobson.venstar.api.dto;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class ControlRequestTest {
    @Test
    public void testConstructor() throws Exception {
        ControlRequest request = new ControlRequest(new URI("http://192.168.0.129"), null, null, null, null, null, null);
        assertEquals("http://192.168.0.129/control", request.getURI().toString());
    }
}
