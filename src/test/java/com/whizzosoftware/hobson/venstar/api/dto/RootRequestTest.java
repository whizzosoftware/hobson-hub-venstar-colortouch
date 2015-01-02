/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
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
