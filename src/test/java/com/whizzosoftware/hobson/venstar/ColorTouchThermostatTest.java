/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar;

import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.device.MockDevicePublisher;
import com.whizzosoftware.hobson.api.disco.MockDiscoManager;
import com.whizzosoftware.hobson.api.util.UserUtil;
import com.whizzosoftware.hobson.api.variable.*;
import com.whizzosoftware.hobson.venstar.api.MockColorTouchChannel;
import com.whizzosoftware.hobson.venstar.api.dto.*;
import org.junit.Test;

import java.net.URI;
import java.util.Collection;

import static org.junit.Assert.*;

public class ColorTouchThermostatTest {
    @Test
    public void testConstructor() throws Exception {
        InfoResponse info = new InfoResponse("thermo", ThermostatMode.AUTO, FanMode.ON, 100, 1.0, 2.0, 3.0, 2.0);
        ColorTouchThermostat t = new ColorTouchThermostat(null, null, new URI("http://192.168.0.129"), info);
        assertEquals("192-168-0-129", t.getId());
        assertEquals("thermo", t.getDefaultName());
        assertEquals("AUTO", t.getLastMode());
        assertEquals("ON", t.getLastFanMode());
        assertEquals(1.0, t.getLastTempF(), 0);
        assertEquals(2.0, t.getLastCoolTempF(), 0);
        assertEquals(3.0, t.getLastHeatTempF(), 0);
    }

    @Test
    public void testOnStartupVariablesInHeatMode() throws Exception {
        ColorTouchPlugin plugin = new ColorTouchPlugin("id");
        MockVariableManager vm = new MockVariableManager();
        plugin.setVariableManager(vm);
        InfoResponse info = new InfoResponse("name", ThermostatMode.HEAT, FanMode.AUTO, 0, 71.0, 75.0, 70.0, 2.0);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, null, new URI("http://192.168.0.129"), info);
        t.onStartup();
        Collection<HobsonVariable> vars = vm.getDeviceVariables(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, "id", t.getId());
        assertEquals(4, vars.size());
        for (HobsonVariable v : vars) {
            assertTrue(
                (VariableConstants.TEMP_F.equals(v.getName()) && v.getValue().equals(71.0)) ||
                (VariableConstants.TARGET_TEMP_F.equals(v.getName()) && v.getValue().equals(70.0)) ||
                (VariableConstants.TSTAT_FAN_MODE.equals(v.getName()) && v.getValue().equals("AUTO")) ||
                (VariableConstants.TSTAT_MODE.equals(v.getName()) && v.getValue().equals("HEAT"))
            );
        }
    }

    @Test
    public void testOnStartupVariablesInCoolMode() throws Exception {
        ColorTouchPlugin plugin = new ColorTouchPlugin("id");
        MockVariableManager vm = new MockVariableManager();
        plugin.setVariableManager(vm);
        InfoResponse info = new InfoResponse("name", ThermostatMode.COOL, FanMode.AUTO, 0, 74.0, 75.0, 70.0, 2.0);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, null, new URI("http://192.168.0.129"), info);
        t.onStartup();
        Collection<HobsonVariable> vars = vm.getDeviceVariables(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, "id", t.getId());
        assertEquals(4, vars.size());
        for (HobsonVariable v : vars) {
            assertTrue(
                (VariableConstants.TEMP_F.equals(v.getName()) && v.getValue().equals(74.0)) ||
                (VariableConstants.TARGET_TEMP_F.equals(v.getName()) && v.getValue().equals(75.0)) ||
                (VariableConstants.TSTAT_FAN_MODE.equals(v.getName()) && v.getValue().equals("AUTO")) ||
                (VariableConstants.TSTAT_MODE.equals(v.getName()) && v.getValue().equals("COOL"))
            );
        }
    }

    @Test
    public void testOnStartupVariablesInAutoModeWithCoolpointLower() throws Exception {
        ColorTouchPlugin plugin = new ColorTouchPlugin("id");
        MockVariableManager vm = new MockVariableManager();
        plugin.setVariableManager(vm);
        InfoResponse info = new InfoResponse("name", ThermostatMode.AUTO, FanMode.AUTO, 0, 71.0, 70.0, 68.0, 2.0);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, null, new URI("http://192.168.0.129"), info);
        t.onStartup();
        Collection<HobsonVariable> vars = vm.getDeviceVariables(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, "id", t.getId());
        assertEquals(4, vars.size());
        for (HobsonVariable v : vars) {
            assertTrue(
                (VariableConstants.TEMP_F.equals(v.getName()) && v.getValue().equals(71.0)) ||
                (VariableConstants.TARGET_TEMP_F.equals(v.getName()) && v.getValue().equals(69.0)) ||
                (VariableConstants.TSTAT_FAN_MODE.equals(v.getName()) && v.getValue().equals("AUTO")) ||
                (VariableConstants.TSTAT_MODE.equals(v.getName()) && v.getValue().equals("AUTO"))
            );
        }
    }

    @Test
    public void testOnStartupVariablesInAutoModeWithCoolpointHigher() throws Exception {
        ColorTouchPlugin plugin = new ColorTouchPlugin("id");
        MockVariableManager vm = new MockVariableManager();
        plugin.setVariableManager(vm);
        InfoResponse info = new InfoResponse("name", ThermostatMode.AUTO, FanMode.AUTO, 0, 71.0, 73.0, 72.0, 2.0);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, null, new URI("http://192.168.0.129"), info);
        t.onStartup();
        Collection<HobsonVariable> vars = vm.getDeviceVariables(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, "id", t.getId());
        assertEquals(4, vars.size());
        for (HobsonVariable v : vars) {
            assertTrue(
                (VariableConstants.TEMP_F.equals(v.getName()) && v.getValue().equals(71.0)) ||
                (VariableConstants.TARGET_TEMP_F.equals(v.getName()) && v.getValue().equals(73.0)) ||
                (VariableConstants.TSTAT_FAN_MODE.equals(v.getName()) && v.getValue().equals("AUTO")) ||
                (VariableConstants.TSTAT_MODE.equals(v.getName()) && v.getValue().equals("AUTO"))
            );
        }
    }

    @Test
    public void testOnStartupVariablesInAutoModeWithCoolpointEqual() throws Exception {
        ColorTouchPlugin plugin = new ColorTouchPlugin("id");
        MockVariableManager vm = new MockVariableManager();
        plugin.setVariableManager(vm);
        InfoResponse info = new InfoResponse("name", ThermostatMode.AUTO, FanMode.AUTO, 0, 71.0, 71.0, 69.0, 2.0);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, null, new URI("http://192.168.0.129"), info);
        t.onStartup();
        Collection<HobsonVariable> vars = vm.getDeviceVariables(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, "id", t.getId());
        assertEquals(4, vars.size());
        for (HobsonVariable v : vars) {
            assertTrue(
                (VariableConstants.TEMP_F.equals(v.getName()) && v.getValue().equals(71.0)) ||
                (VariableConstants.TARGET_TEMP_F.equals(v.getName()) && v.getValue().equals(70.0)) ||
                (VariableConstants.TSTAT_FAN_MODE.equals(v.getName()) && v.getValue().equals("AUTO")) ||
                (VariableConstants.TSTAT_MODE.equals(v.getName()) && v.getValue().equals("AUTO"))
            );
        }
    }

    @Test
    public void testOnStartupVariablesInAutoModeWithHeatpointEqual() throws Exception {
        ColorTouchPlugin plugin = new ColorTouchPlugin("id");
        MockVariableManager vm = new MockVariableManager();
        plugin.setVariableManager(vm);
        InfoResponse info = new InfoResponse("name", ThermostatMode.AUTO, FanMode.AUTO, 0, 71.0, 73.0, 71.0, 2.0);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, null, new URI("http://192.168.0.129"), info);
        t.onStartup();
        Collection<HobsonVariable> vars = vm.getDeviceVariables(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, "id", t.getId());
        assertEquals(4, vars.size());
        for (HobsonVariable v : vars) {
            assertTrue(
                (VariableConstants.TEMP_F.equals(v.getName()) && v.getValue().equals(71.0)) ||
                (VariableConstants.TARGET_TEMP_F.equals(v.getName()) && v.getValue().equals(72.0)) ||
                (VariableConstants.TSTAT_FAN_MODE.equals(v.getName()) && v.getValue().equals("AUTO")) ||
                (VariableConstants.TSTAT_MODE.equals(v.getName()) && v.getValue().equals("AUTO"))
            );
        }
    }

    @Test
    public void testTargetTempFHeatMode() throws Exception {
        URI uri = new URI("http://192.168.0.129");
        InfoResponse info = new InfoResponse("name", ThermostatMode.HEAT, FanMode.AUTO, 0, 71.0, 71.0, 71.0, 2.0);
        MockVariableManager vm = new MockVariableManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("pluginId");
        plugin.setVariableManager(vm);
        MockColorTouchChannel channel = new MockColorTouchChannel();
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, channel, uri, info);
        assertEquals(0, channel.getControlRequests().size());
        t.onSetVariable(VariableConstants.TARGET_TEMP_F, 72.0);
        assertEquals(0, channel.getControlRequests().size());
        t.onInfoResponse(new InfoRequest(uri), info, null);
        assertEquals(1, channel.getControlRequests().size());
        ControlRequest cr = channel.getControlRequests().get(0);
        assertEquals(ThermostatMode.HEAT.ordinal(), (int)cr.getMode());
        assertEquals(FanMode.AUTO.ordinal(), (int)cr.getFanMode());
        assertEquals(71.0, cr.getCoolTemp(), 0);
        assertEquals(72.0, cr.getHeatTemp(), 0);
    }

    @Test
    public void testTargetTempFCoolMode() throws Exception {
        InfoResponse info = new InfoResponse("name", ThermostatMode.COOL, FanMode.AUTO, 0, 71.0, 71.0, 71.0, 2.0);
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("pluginId");
        plugin.setVariableManager(vm);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, channel, new URI("http://192.168.0.129"), info);
        assertEquals(0, channel.getControlRequests().size());
        t.onSetVariable(VariableConstants.TARGET_TEMP_F, 72.0);
        assertEquals(0, channel.getControlRequests().size());
        t.onInfoResponse(null, info, null);
        assertEquals(1, channel.getControlRequests().size());
        ControlRequest cr = channel.getControlRequests().get(0);
        assertEquals(ThermostatMode.COOL.ordinal(), (int)cr.getMode());
        assertEquals(FanMode.AUTO.ordinal(), (int)cr.getFanMode());
        assertEquals(72.0, cr.getCoolTemp(), 0);
        assertEquals(71.0, cr.getHeatTemp(), 0);
    }

    @Test
    public void testTargetTempFAutoMode() throws Exception {
        InfoResponse info = new InfoResponse("name", ThermostatMode.AUTO, FanMode.AUTO, 0, 70.0, 74.0, 70.0, 2.0);
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("pluginId");
        plugin.setVariableManager(vm);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, channel, new URI("http://192.168.0.129"), info);
        assertEquals(0, channel.getControlRequests().size());
        t.onSetVariable(VariableConstants.TARGET_TEMP_F, 72.0);
        assertEquals(0, channel.getControlRequests().size());
        t.onInfoResponse(null, info, null);
        assertEquals(1, channel.getControlRequests().size());
        ControlRequest cr = channel.getControlRequests().get(0);
        assertEquals(ThermostatMode.AUTO.ordinal(), (int)cr.getMode());
        assertEquals(FanMode.AUTO.ordinal(), (int)cr.getFanMode());
        assertEquals(73.0, cr.getCoolTemp(), 0); // this should be raised to 74
        assertEquals(71.0, cr.getHeatTemp(), 0); // this should be lowered to 70
    }

    @Test
    public void testVariableUpdates() throws Exception {
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariablePublisher vp = new MockVariablePublisher();
        MockVariableManager vm = new MockVariableManager(vp);
        MockDiscoManager dm = new MockDiscoManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("foo");
        plugin.setVariableManager(vm);
        plugin.setDiscoManager(dm);
        assertEquals(0, vp.getVariableUpdates().size());

        InfoResponse info = new InfoResponse("thermo", ThermostatMode.COOL, FanMode.ON, 100, 1.0, 2.0, 3.0, 2.0);
        ColorTouchThermostat tstat = new ColorTouchThermostat(plugin, channel, new URI("http://localhost"), info);
        tstat.onStartup();

        // verify that the appropriate variables were published
        assertEquals(4, vm.getDeviceVariables(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, "foo", "localhost").size());
        for (HobsonVariable v : vm.getDeviceVariables(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, "foo", "localhost")) {
            assertTrue(
                (VariableConstants.TEMP_F.equals(v.getName()) && v.getValue().equals(1.0)) ||
                (VariableConstants.TARGET_TEMP_F.equals(v.getName()) && v.getValue().equals(2.0)) ||
                (VariableConstants.TSTAT_FAN_MODE.equals(v.getName()) && v.getValue().equals("ON")) ||
                (VariableConstants.TSTAT_MODE.equals(v.getName()) && v.getValue().equals("COOL"))
            );
        }
        assertEquals(0, vp.getVariableUpdates().size());

        // update tstat information with two changes (TEMP_F and TARGET_TEMP_F)
        tstat.onInfoResponse(null, new InfoResponse("thermo", ThermostatMode.COOL, FanMode.ON, 100, 1.5, 2.0, 3.5, 2.0), null);

        // verify that only the two variable updates occurred
        assertEquals(2, vp.getVariableUpdates().size());
        for (VariableUpdate vu : vp.getVariableUpdates()) {
            assertTrue(
                (VariableConstants.TEMP_F.equals(vu.getName()) && vu.getValue().equals(1.5)) ||
                (VariableConstants.TARGET_TEMP_F.equals(vu.getName()) && vu.getValue().equals(2.0))
            );
        }
    }

    @Test
    public void testApplySetVariableToControlledHardwareWithInteger() throws Exception {
        testApplySetVariableToControlHardwareWithValue(75);
    }

    @Test
    public void testApplySetVariableToControlledHardwareWithDouble() throws Exception {
        testApplySetVariableToControlHardwareWithValue(75.0);
    }

    @Test
    public void testApplySetVariableToControlledHardwareWithString() throws Exception {
        testApplySetVariableToControlHardwareWithValue("75");
    }

    @Test
    public void testValueInvalidation() throws Exception {
        URI uri = new URI("http://192.168.0.129");
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariablePublisher vp = new MockVariablePublisher();
        MockVariableManager vm = new MockVariableManager(vp);
        MockDevicePublisher dp = new MockDevicePublisher();
        MockDeviceManager dm = new MockDeviceManager(dp);
        ColorTouchPlugin plugin = new ColorTouchPlugin("pluginId");
        plugin.setDeviceManager(dm);
        plugin.setVariableManager(vm);

        ColorTouchThermostat ctt = new ColorTouchThermostat(plugin, channel, uri, null);

        dp.publishDevice(plugin, ctt);

        // make sure no variable updates exist
        assertEquals(0, vp.getVariableUpdates().size());

        // send a successful InfoResponse
        plugin.onHttpResponse(200, null, "{\"name\": \"Office\",\"mode\": 3,\"state\": 0,\"fan\": 0,\"fanstate\": 0,\"tempunits\": 0,\"schedule\": 0,\"schedulepart\": 0,\"away\": 0,\"holiday\": 0,\"override\": 0,\"overridetime\": 0,\"forceunocc\": 0,\"spacetemp\": 79,\"heattemp\": 78,\"cooltemp\": 75,\"cooltempmin\": 35,\"cooltempmax\": 99,\"heattempmin\": 35,\"heattempmax\": 99,\"setpointdelta\": 2,\"availablemodes\": 0}", new InfoRequest(uri, ctt.getId()));
        assertEquals(4, vp.getVariableUpdates().size());

        vp.clearVariableUpdates();

        // send an InfoRequest failure
        plugin.onHttpRequestFailure(new RuntimeException(), new InfoRequest(uri, ctt.getId()));

        // make sure thermostat invalidated all values due to the failure
        assertEquals(4, vp.getVariableUpdates().size());
        for (VariableUpdate vu : vp.getVariableUpdates()) {
            assertNull(vu.getValue());
        }
        vp.clearVariableUpdates();

        // send an successful InfoResponse
        plugin.onHttpResponse(200, null, "{\"name\": \"Office\",\"mode\": 3,\"state\": 0,\"fan\": 0,\"fanstate\": 0,\"tempunits\": 0,\"schedule\": 0,\"schedulepart\": 0,\"away\": 0,\"holiday\": 0,\"override\": 0,\"overridetime\": 0,\"forceunocc\": 0,\"spacetemp\": 79,\"heattemp\": 73,\"cooltemp\": 75,\"cooltempmin\": 35,\"cooltempmax\": 99,\"heattempmin\": 35,\"heattempmax\": 99,\"setpointdelta\": 2,\"availablemodes\": 0}", new InfoRequest(uri, ctt.getId()));
        assertEquals(4, vp.getVariableUpdates().size());
        for (VariableUpdate vu : vp.getVariableUpdates()) {
            assertNotNull(vu.getValue());
        }
    }

    @Test
    public void testCalculateTargetTemp() throws Exception {
        ColorTouchThermostat t = new ColorTouchThermostat(null, null, new URI("http://192.168.0.129"), null);
        assertEquals(73.0, t.calculateTargetTemp(ThermostatMode.COOL.toString(), 73.0, 71.0), 0);
        assertEquals(71.0, t.calculateTargetTemp(ThermostatMode.COOL.toString(), 71.0, 77.0), 0);

        assertEquals(72.0, t.calculateTargetTemp(ThermostatMode.HEAT.toString(), 75.0, 72.0), 0);
        assertEquals(71.0, t.calculateTargetTemp(ThermostatMode.HEAT.toString(), 73.0, 71.0), 0);

        assertEquals(70.0, t.calculateTargetTemp(ThermostatMode.AUTO.toString(), 71.0, 69.0), 0);
        assertEquals(70.0, t.calculateTargetTemp(ThermostatMode.AUTO.toString(), 71.0, 69.0), 0);
        assertEquals(70.0, t.calculateTargetTemp(ThermostatMode.AUTO.toString(), 70.0, 69.0), 0);
        assertEquals(71.0, t.calculateTargetTemp(ThermostatMode.AUTO.toString(), 71.0, 71.0), 0);

        assertNull(t.calculateTargetTemp(ThermostatMode.OFF.toString(), 70.0, 69.0));
    }

    public void testApplySetVariableToControlHardwareWithValue(Object value) throws Exception {
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("foo");
        plugin.setVariableManager(vm);
        ColorTouchThermostat tstat = new ColorTouchThermostat(plugin, channel, new URI("http://localhost"), null);
        assertEquals(0, channel.getInfoRequests().size());
        assertEquals(0, channel.getControlRequests().size());
        assertFalse(tstat.hasPendingSetVariableRequest());

        // send a set variable request; we expect this to trigger an info request and no control request
        tstat.onSetVariable(VariableConstants.TARGET_TEMP_F, value);
        assertEquals(1, channel.getInfoRequests().size());
        assertEquals(0, channel.getControlRequests().size());
        assertTrue(tstat.hasPendingSetVariableRequest());

        // send a response to the info request; we expect this to trigger a control request
        tstat.onInfoResponse(null, new InfoResponse("foo", ThermostatMode.COOL, FanMode.AUTO, 1, 70.0, 71.0, 73.0, 2.0), null);
        assertEquals(1, channel.getInfoRequests().size());
        assertEquals(1, channel.getControlRequests().size());
        assertFalse(tstat.hasPendingSetVariableRequest());

        // validate that the control request is identical to the info response except for COOL_TEMP_F which we're setting
        ControlRequest cr = channel.getControlRequests().get(0);
        assertEquals(ThermostatMode.COOL.ordinal(), (int)cr.getMode());
        assertEquals(FanMode.AUTO.ordinal(), (int)cr.getFanMode());
        assertEquals(75.0, cr.getCoolTemp(), 0);
        assertEquals(73.0, cr.getHeatTemp(), 0);

        // send a successful control response
        ControlResponse crr = new ControlResponse();
        tstat.onControlResponse(cr, crr, null);
        assertFalse(tstat.hasPendingSetVariableRequest());
    }
}
