/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar;

import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.disco.MockDiscoManager;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Dictionary;
import java.util.Hashtable;

public class ColorTouchPluginTest {
    @Test
    public void testOnPluginConfigurationUpdated() {
        MockDiscoManager discoManager = new MockDiscoManager();
        MockDeviceManager deviceManager = new MockDeviceManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("id");
        plugin.setDiscoManager(discoManager);
        plugin.setDeviceManager(deviceManager);
        assertEquals(0, plugin.getDiscoveredURIs().size());

        // start with manually configured thermostat host
        Dictionary config = new Hashtable();
        config.put(ColorTouchPlugin.PROP_THERMOSTAT_HOST, "192.168.0.10");
        plugin.onStartup(config);
        assertEquals(1, plugin.getDiscoveredURIs().size());

        // make sure configuration update doesn't create a second identical host
        plugin.onPluginConfigurationUpdate(config);
        assertEquals(1, plugin.getDiscoveredURIs().size());
    }
}
