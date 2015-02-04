/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.api.dto;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.Map;

public class ControlRequestTest {
    @Test
    public void testConstructor() throws Exception {
        ControlRequest request = new ControlRequest(new URI("http://192.168.0.129"), null, (String)null, null, null, null, null, null);
        assertEquals("http://192.168.0.129/control", request.getURI().toString());
    }

    @Test
    public void testTargetTempF() throws Exception {
        ControlRequest request = new ControlRequest(new URI("http://192.168.0.129"), null, ThermostatMode.COOL, null, 70.0, 72.0, 2.0, null);
        Map<String,String> map = request.getRequestBodyMap();
        assertEquals(3, map.size());
        assertEquals("70.0", map.get("heattemp"));
        assertEquals("72.0", map.get("cooltemp"));
        assertEquals("2", map.get("mode"));

        request = new ControlRequest(new URI("http://192.168.0.129"), null, ThermostatMode.HEAT, null, 71.0, 73.0, 2.0, null);
        map = request.getRequestBodyMap();
        assertEquals(3, map.size());
        assertEquals("71.0", map.get("heattemp"));
        assertEquals("73.0", map.get("cooltemp"));
        assertEquals("1", map.get("mode"));

        request = new ControlRequest(new URI("http://192.168.0.129"), null, ThermostatMode.AUTO, null, 74.0, 76.0, 2.0, null);
        map = request.getRequestBodyMap();
        assertEquals(3, map.size());
        assertEquals("74.0", map.get("heattemp"));
        assertEquals("76.0", map.get("cooltemp"));
        assertEquals("3", map.get("mode"));

        try {
            new ControlRequest(new URI("http://192.168.0.129"), null, ThermostatMode.AUTO, null, 74.0, 74.0, 2.0, null);
            fail("Should have thrown exception");
        } catch (HobsonRuntimeException hre) {
        }

        try {
            new ControlRequest(new URI("http://192.168.0.129"), null, ThermostatMode.AUTO, null, 74.0, 75.0, 2.0, null);
            fail("Should have thrown exception");
        } catch (HobsonRuntimeException hre) {
        }
    }

    @Test
    public void testCreate() throws Exception {
        URI uri = new URI("http://192.168.0.129");

        // test cool mode with target temp lower than current temp
        ControlRequest cr = ControlRequest.create(uri, "id", "COOL", "AUTO", 72.0, 73.0, 2.0, 71.0, null);
        Map<String,String> map = cr.getRequestBodyMap();
        assertEquals(4, map.size());
        assertEquals("71.0", map.get("cooltemp"));
        assertEquals("72.0", map.get("heattemp"));
        assertEquals("2", map.get("mode"));
        assertEquals("0", map.get("fan"));

        // test cool mode with target temp higher than current temp
        cr = ControlRequest.create(uri, "id", "COOL", "AUTO", 72.0, 73.0, 2.0, 71.0, null);
        map = cr.getRequestBodyMap();
        assertEquals(4, map.size());
        assertEquals("71.0", map.get("cooltemp"));
        assertEquals("72.0", map.get("heattemp"));
        assertEquals("2", map.get("mode"));
        assertEquals("0", map.get("fan"));

        // test cool mode with target temp equal to current temp
        cr = ControlRequest.create(uri, "id", "COOL", "AUTO", 72.0, 73.0, 2.0, 70.0, null);
        map = cr.getRequestBodyMap();
        assertEquals(4, map.size());
        assertEquals("70.0", map.get("cooltemp"));
        assertEquals("72.0", map.get("heattemp"));
        assertEquals("2", map.get("mode"));
        assertEquals("0", map.get("fan"));

        // test heat mode with target temp higher than current temp
        cr = ControlRequest.create(uri, "id", "HEAT", "AUTO", 72.0, 73.0, 2.0, 71.0, null);
        map = cr.getRequestBodyMap();
        assertEquals(4, map.size());
        assertEquals("71.0", map.get("heattemp"));
        assertEquals("73.0", map.get("cooltemp"));
        assertEquals("1", map.get("mode"));
        assertEquals("0", map.get("fan"));

        // test heat mode with target temp lower than current temp
        cr = ControlRequest.create(uri, "id", "HEAT", "AUTO", 72.0, 73.0, 2.0, 71.0, null);
        map = cr.getRequestBodyMap();
        assertEquals(4, map.size());
        assertEquals("71.0", map.get("heattemp"));
        assertEquals("73.0", map.get("cooltemp"));
        assertEquals("1", map.get("mode"));
        assertEquals("0", map.get("fan"));

        // test heat mode with target temp equal to current temp
        cr = ControlRequest.create(uri, "id", "HEAT", "AUTO", 72.0, 73.0, 2.0, 71.0, null);
        map = cr.getRequestBodyMap();
        assertEquals(4, map.size());
        assertEquals("71.0", map.get("heattemp"));
        assertEquals("73.0", map.get("cooltemp"));
        assertEquals("1", map.get("mode"));
        assertEquals("0", map.get("fan"));

        // test auto mode with target temp higher than current temp
        cr = ControlRequest.create(uri, "id", "AUTO", "AUTO", 72.0, 73.0, 2.0, 71.0, null);
        map = cr.getRequestBodyMap();
        assertEquals(4, map.size());
        assertEquals("70.0", map.get("heattemp"));
        assertEquals("72.0", map.get("cooltemp"));
        assertEquals("3", map.get("mode"));
        assertEquals("0", map.get("fan"));

        // test auto mode with target temp lower than current temp
        cr = ControlRequest.create(uri, "id", "AUTO", "AUTO", 72.0, 73.0, 2.0, 72.0, null);
        map = cr.getRequestBodyMap();
        assertEquals(4, map.size());
        assertEquals("73.0", map.get("cooltemp"));
        assertEquals("71.0", map.get("heattemp"));
        assertEquals("3", map.get("mode"));
        assertEquals("0", map.get("fan"));

        // test auto mode with target temp lower than current temp
        cr = ControlRequest.create(uri, "id", "AUTO", "AUTO", 72.0, 73.0, 2.0, 70.0, null);
        map = cr.getRequestBodyMap();
        assertEquals(4, map.size());
        assertEquals("71.0", map.get("cooltemp"));
        assertEquals("69.0", map.get("heattemp"));
        assertEquals("3", map.get("mode"));
        assertEquals("0", map.get("fan"));
    }
}
