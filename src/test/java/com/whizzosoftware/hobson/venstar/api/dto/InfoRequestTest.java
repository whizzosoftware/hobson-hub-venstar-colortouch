package com.whizzosoftware.hobson.venstar.api.dto;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class InfoRequestTest {
    @Test
    public void testConstructor() throws Exception {
        InfoRequest request = new InfoRequest(new URI("http://192.168.0.129"));
        assertEquals("http://192.168.0.129/query/info", request.getURI().toString());
    }
}
