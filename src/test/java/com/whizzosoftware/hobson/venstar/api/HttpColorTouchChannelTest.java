package com.whizzosoftware.hobson.venstar.api;

import org.junit.Test;
import static org.junit.Assert.*;

public class HttpColorTouchChannelTest {
    @Test
    public void testConstructor() {
        HttpColorTouchChannel ch = new HttpColorTouchChannel("192.168.0.167");
        assertEquals("192.168.0.167", ch.getHost());
        assertEquals("192-168-0-167", ch.getId());
    }
}
