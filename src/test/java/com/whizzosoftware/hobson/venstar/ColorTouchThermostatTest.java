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
import com.whizzosoftware.hobson.api.variable.*;
import com.whizzosoftware.hobson.venstar.api.MockColorTouchChannel;
import com.whizzosoftware.hobson.venstar.api.dto.*;
import com.whizzosoftware.hobson.venstar.state.PendingConfirmation;
import org.junit.Test;

import java.net.URI;
import java.util.Collection;

import static org.junit.Assert.*;

public class ColorTouchThermostatTest {
    @Test
    public void testConstructor() throws Exception {
        InfoResponse info = new InfoResponse(null, "thermo", ThermostatMode.AUTO, FanMode.ON, 100, 1.0, 2.0, 3.0, 2.0);
        ColorTouchPlugin plugin = new ColorTouchPlugin("plugin");
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, null, new URI("http://192.168.0.129"), info);
        assertEquals("192-168-0-129", t.getContext().getDeviceId());
        assertEquals("thermo", t.getDefaultName());
        assertEquals("AUTO", t.getCurrentState().getMode());
        assertEquals("ON", t.getCurrentState().getFanMode());
        assertEquals(1.0, t.getCurrentState().getTempF(), 0);
        assertEquals(2.0, t.getCurrentState().getCoolTempF(), 0);
        assertEquals(3.0, t.getCurrentState().getHeatTempF(), 0);
    }

    @Test
    public void testOnStartupVariablesInHeatMode() throws Exception {
        ColorTouchPlugin plugin = new ColorTouchPlugin("id");
        MockVariableManager vm = new MockVariableManager();
        plugin.setVariableManager(vm);
        InfoResponse info = new InfoResponse(null, "name", ThermostatMode.HEAT, FanMode.AUTO, 0, 71.0, 75.0, 70.0, 2.0);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, null, new URI("http://192.168.0.129"), info);
        t.onStartup(null);
        Collection<HobsonVariable> vars = vm.getDeviceVariables(t.getContext()).getCollection();
        assertEquals(6, vars.size());
        for (HobsonVariable v : vars) {
            assertTrue(
                (VariableConstants.ON.equals(v.getName()) && v.getValue() == null) ||
                (VariableConstants.INDOOR_TEMP_F.equals(v.getName()) && v.getValue().equals(71.0)) ||
                (VariableConstants.TARGET_COOL_TEMP_F.equals(v.getName()) && v.getValue().equals(75.0)) ||
                (VariableConstants.TARGET_HEAT_TEMP_F.equals(v.getName()) && v.getValue().equals(70.0)) ||
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
        InfoResponse info = new InfoResponse(null, "name", ThermostatMode.COOL, FanMode.AUTO, 0, 74.0, 75.0, 70.0, 2.0);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, null, new URI("http://192.168.0.129"), info);
        t.onStartup(null);
        Collection<HobsonVariable> vars = vm.getDeviceVariables(t.getContext()).getCollection();
        assertEquals(6, vars.size());
        for (HobsonVariable v : vars) {
            assertTrue(
                (VariableConstants.ON.equals(v.getName()) && v.getValue() == null) ||
                (VariableConstants.INDOOR_TEMP_F.equals(v.getName()) && v.getValue().equals(74.0)) ||
                (VariableConstants.TARGET_COOL_TEMP_F.equals(v.getName()) && v.getValue().equals(75.0)) ||
                (VariableConstants.TARGET_HEAT_TEMP_F.equals(v.getName()) && v.getValue().equals(70.0)) ||
                (VariableConstants.TSTAT_FAN_MODE.equals(v.getName()) && v.getValue().equals("AUTO")) ||
                (VariableConstants.TSTAT_MODE.equals(v.getName()) && v.getValue().equals("COOL"))
            );
        }
    }

    @Test
    public void testOnStartupVariablesInAutoMode() throws Exception {
        ColorTouchPlugin plugin = new ColorTouchPlugin("id");
        MockVariableManager vm = new MockVariableManager();
        plugin.setVariableManager(vm);
        InfoResponse info = new InfoResponse(null, "name", ThermostatMode.AUTO, FanMode.AUTO, 0, 71.0, 70.0, 68.0, 2.0);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, null, new URI("http://192.168.0.129"), info);
        t.onStartup(null);
        Collection<HobsonVariable> vars = vm.getDeviceVariables(t.getContext()).getCollection();
        assertEquals(6, vars.size());
        for (HobsonVariable v : vars) {
            assertTrue(
                (VariableConstants.ON.equals(v.getName()) && v.getValue() == null) ||
                (VariableConstants.INDOOR_TEMP_F.equals(v.getName()) && v.getValue().equals(71.0)) ||
                (VariableConstants.TARGET_COOL_TEMP_F.equals(v.getName()) && v.getValue().equals(70.0)) ||
                (VariableConstants.TARGET_HEAT_TEMP_F.equals(v.getName()) && v.getValue().equals(68.0)) ||
                (VariableConstants.TSTAT_FAN_MODE.equals(v.getName()) && v.getValue().equals("AUTO")) ||
                (VariableConstants.TSTAT_MODE.equals(v.getName()) && v.getValue().equals("AUTO"))
            );
        }
    }

    @Test
    public void testTargetTempFHeatMode() throws Exception {
        URI uri = new URI("http://192.168.0.129");
        InfoResponse info = new InfoResponse(null, "name", ThermostatMode.HEAT, FanMode.AUTO, 0, 71.0, 71.0, 71.0, 2.0);
        MockVariableManager vm = new MockVariableManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("pluginId");
        plugin.setVariableManager(vm);
        MockColorTouchChannel channel = new MockColorTouchChannel();
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, channel, uri, info);
        assertEquals(0, channel.getControlRequests().size());
        t.onSetVariable(VariableConstants.TARGET_HEAT_TEMP_F, 72.0);
        assertEquals(0, channel.getControlRequests().size());
        t.onInfoResponse(new InfoRequest(uri), info, null, System.currentTimeMillis());
        assertEquals(1, channel.getControlRequests().size());
        ControlRequest cr = channel.getControlRequests().get(0);
        assertEquals(ThermostatMode.HEAT.ordinal(), (int)cr.getMode());
        assertEquals(FanMode.AUTO.ordinal(), (int)cr.getFanMode());
        assertEquals(71.0, cr.getCoolTemp(), 0);
        assertEquals(72.0, cr.getHeatTemp(), 0);
    }

    @Test
    public void testTargetTempFCoolMode() throws Exception {
        InfoResponse info = new InfoResponse(null, "name", ThermostatMode.COOL, FanMode.AUTO, 0, 71.0, 71.0, 71.0, 2.0);
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("pluginId");
        plugin.setVariableManager(vm);
        ColorTouchThermostat t = new ColorTouchThermostat(plugin, channel, new URI("http://192.168.0.129"), info);
        assertEquals(0, channel.getControlRequests().size());
        t.onSetVariable(VariableConstants.TARGET_COOL_TEMP_F, 72.0);
        assertEquals(0, channel.getControlRequests().size());
        t.onInfoResponse(null, info, null, System.currentTimeMillis());
        assertEquals(1, channel.getControlRequests().size());
        ControlRequest cr = channel.getControlRequests().get(0);
        assertEquals(ThermostatMode.COOL.ordinal(), (int)cr.getMode());
        assertEquals(FanMode.AUTO.ordinal(), (int)cr.getFanMode());
        assertEquals(72.0, cr.getCoolTemp(), 0);
        assertEquals(71.0, cr.getHeatTemp(), 0);
    }

    @Test
    public void testVariableUpdates() throws Exception {
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        MockDiscoManager dm = new MockDiscoManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("foo");
        plugin.setVariableManager(vm);
        plugin.setDiscoManager(dm);
        assertEquals(0, vm.getVariableUpdates().size());

        InfoResponse info = new InfoResponse(null, "thermo", ThermostatMode.COOL, FanMode.ON, 100, 1.0, 2.0, 3.0, 2.0);
        ColorTouchThermostat tstat = new ColorTouchThermostat(plugin, channel, new URI("http://localhost"), info);
        tstat.onStartup(null);

        // verify that the appropriate variables were published
        assertEquals(6, vm.getDeviceVariables(tstat.getContext()).getCollection().size());
        for (HobsonVariable v : vm.getDeviceVariables(tstat.getContext()).getCollection()) {
            assertTrue(
                (VariableConstants.ON.equals(v.getName()) && v.getValue() == null) ||
                (VariableConstants.INDOOR_TEMP_F.equals(v.getName()) && v.getValue().equals(1.0)) ||
                (VariableConstants.TARGET_COOL_TEMP_F.equals(v.getName()) && v.getValue().equals(2.0)) ||
                (VariableConstants.TARGET_HEAT_TEMP_F.equals(v.getName()) && v.getValue().equals(3.0)) ||
                (VariableConstants.TSTAT_FAN_MODE.equals(v.getName()) && v.getValue().equals("ON")) ||
                (VariableConstants.TSTAT_MODE.equals(v.getName()) && v.getValue().equals("COOL"))
            );
        }
        assertEquals(0, vm.getVariableUpdates().size());

        // update tstat information with two changes (TEMP_F and TARGET_TEMP_COOL_F)
        tstat.onInfoResponse(null, new InfoResponse(true, "thermo", ThermostatMode.COOL, FanMode.ON, 100, 1.5, 3.5, 3.0, 2.0), null, System.currentTimeMillis());

        // verify that only the two variable updates occurred
        assertEquals(3, vm.getVariableUpdates().size());
        for (VariableUpdate vu : vm.getVariableUpdates()) {
            assertTrue(
                (VariableConstants.ON.equals(vu.getName()) && vu.getValue().equals(true)) ||
                (VariableConstants.INDOOR_TEMP_F.equals(vu.getName()) && vu.getValue().equals(1.5)) ||
                (VariableConstants.TARGET_COOL_TEMP_F.equals(vu.getName()) && vu.getValue().equals(3.5))
            );
        }
    }

    @Test
    public void testMultipleVariableUpdates() throws Exception {
        URI baseURI = new URI("http://localhost");
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        MockDiscoManager dm = new MockDiscoManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("foo");
        plugin.setVariableManager(vm);
        plugin.setDiscoManager(dm);
        assertEquals(0, vm.getVariableUpdates().size());

        InfoResponse info = new InfoResponse(null, "thermo", ThermostatMode.COOL, FanMode.ON, 100, 1.0, 2.0, 4.0, 2.0);
        ColorTouchThermostat tstat = new ColorTouchThermostat(plugin, channel, baseURI, info);
        tstat.onStartup(null);

        // set one variable and confirm that the info request was sent
        assertEquals(0, channel.getInfoRequests().size());
        tstat.onSetVariable(VariableConstants.TSTAT_FAN_MODE, FanMode.AUTO);
        assertEquals(1, channel.getInfoRequests().size());

        // set another variable before the info response comes back and make sure another info request isn't sent
        tstat.onSetVariable(VariableConstants.TSTAT_MODE, ThermostatMode.AUTO);
        assertEquals(1, channel.getInfoRequests().size());

        // process the info response
        assertEquals(0, channel.getControlRequests().size());
        tstat.onInfoResponse(new InfoRequest(baseURI), info, null, System.currentTimeMillis());

        // verify the control request was sent with correct data
        assertEquals(1, channel.getControlRequests().size());
        ControlRequest ctr = channel.getControlRequests().get(0);
        assertEquals(0, (int)ctr.getFanMode());
        assertEquals(3, (int)ctr.getMode());
    }

    @Test
    public void testDuplicateVariableUpdates() throws Exception {
        URI baseURI = new URI("http://localhost");
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        MockDiscoManager dm = new MockDiscoManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("foo");
        plugin.setVariableManager(vm);
        plugin.setDiscoManager(dm);
        assertEquals(0, vm.getVariableUpdates().size());

        InfoResponse info = new InfoResponse(null, "thermo", ThermostatMode.COOL, FanMode.ON, 100, 1.0, 2.0, 3.0, 2.0);
        ColorTouchThermostat tstat = new ColorTouchThermostat(plugin, channel, baseURI, info);
        tstat.onStartup(null);

        // send two variable requests
        assertEquals(0, channel.getInfoRequests().size());
        tstat.onSetVariable(VariableConstants.TSTAT_MODE, ThermostatMode.COOL);
        tstat.onSetVariable(VariableConstants.TSTAT_FAN_MODE, FanMode.ON);
        assertEquals(1, channel.getInfoRequests().size());

        // send the info response with same values as the variable set requests
        assertEquals(0, channel.getControlRequests().size());
        tstat.onInfoResponse(new InfoRequest(baseURI), info, null, System.currentTimeMillis());
        assertEquals(0, channel.getControlRequests().size());

        // send another info response with the same values; no control request should be sent
        tstat.onInfoResponse(new InfoRequest(baseURI), info, null, System.currentTimeMillis());
        assertEquals(0, channel.getControlRequests().size());
    }

    @Test
    public void testNoDuplicateControlRequests() throws Exception {
        URI baseURI = new URI("http://localhost");
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        MockDiscoManager dm = new MockDiscoManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("foo");
        plugin.setVariableManager(vm);
        plugin.setDiscoManager(dm);
        assertEquals(0, vm.getVariableUpdates().size());

        InfoResponse info = new InfoResponse(null, "thermo", ThermostatMode.COOL, FanMode.ON, 100, 1.0, 2.0, 3.0, 2.0);
        ColorTouchThermostat tstat = new ColorTouchThermostat(plugin, channel, baseURI, info);
        tstat.onStartup(null);

        // send two variable requests
        assertEquals(0, channel.getInfoRequests().size());
        long now = System.currentTimeMillis();
        tstat.onSetVariable(VariableConstants.TSTAT_MODE, ThermostatMode.COOL);
        assertEquals(1, channel.getInfoRequests().size());
        assertTrue(tstat.hasPendingControlConfirmation());

        // send the info response with different values as the variable set requests
        assertEquals(0, channel.getControlRequests().size());
        tstat.onInfoResponse(new InfoRequest(baseURI), new InfoResponse(null, "thermo", ThermostatMode.AUTO, FanMode.ON, 100, 1.0, 2.0, 3.0, 2.0), null, System.currentTimeMillis());
        assertEquals(1, channel.getControlRequests().size());
        assertTrue(tstat.hasPendingControlConfirmation());

        // send another info response with the same values; no additional control request should be sent
        tstat.onInfoResponse(new InfoRequest(baseURI), new InfoResponse(null, "thermo", ThermostatMode.AUTO, FanMode.ON, 100, 1.0, 2.0, 3.0, 2.0), null, System.currentTimeMillis());
        assertEquals(1, channel.getControlRequests().size());
        assertTrue(tstat.hasPendingControlConfirmation());

        // perform a refresh before the pending timeout interval and make sure no further control requests were sent
        tstat.onRefresh(now + 100);
        assertEquals(1, channel.getControlRequests().size());
        assertEquals(2, channel.getInfoRequests().size());
        assertTrue(tstat.hasPendingControlConfirmation());

        // perform a refresh after the pending timeout interval and make sure there is no longer a pending state
        tstat.onRefresh(now + PendingConfirmation.PENDING_CONTROL_REQUEST_TIMEOUT + 1);
        assertFalse(tstat.hasPendingControlConfirmation());
    }

    @Test
    public void testMultipleOnRefreshCallsBeforeNextInfoRequestInterval() throws Exception {
        URI baseURI = new URI("http://localhost");
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        MockDiscoManager dm = new MockDiscoManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("foo");
        plugin.setVariableManager(vm);
        plugin.setDiscoManager(dm);
        assertEquals(0, vm.getVariableUpdates().size());

        InfoResponse info = new InfoResponse(null, "thermo", ThermostatMode.COOL, FanMode.ON, 100, 1.0, 2.0, 3.0, 2.0);
        ColorTouchThermostat tstat = new ColorTouchThermostat(plugin, channel, baseURI, info);
        tstat.onStartup(null);

        long now = System.currentTimeMillis();

        // perform initial refresh and make sure the info request was sent
        assertEquals(0, channel.getInfoRequests().size());
        tstat.onRefresh(now);
        assertEquals(1, channel.getInfoRequests().size());

        // perform three additional (1ms apart) refreshes and make sure an info request was not sent
        tstat.onRefresh(now + 1);
        tstat.onRefresh(now + 2);
        tstat.onRefresh(now + 3);
        assertEquals(1, channel.getInfoRequests().size());

        // perform a fourth refresh after the 10 second interval and make sure an info request was sent
        tstat.onRefresh(now + 10001);
        assertEquals(2, channel.getInfoRequests().size());
    }

    @Test
    public void testMultipleOnRefreshCallsWithPendingVariable() throws Exception {
        URI baseURI = new URI("http://localhost");
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        MockDiscoManager dm = new MockDiscoManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("foo");
        plugin.setVariableManager(vm);
        plugin.setDiscoManager(dm);
        assertEquals(0, vm.getVariableUpdates().size());

        InfoResponse info = new InfoResponse(null, "thermo", ThermostatMode.COOL, FanMode.ON, 100, 1.0, 2.0, 3.0, 2.0);
        ColorTouchThermostat tstat = new ColorTouchThermostat(plugin, channel, baseURI, info);
        tstat.onStartup(null);

        long now = System.currentTimeMillis();

        // perform initial refresh and make sure the info request was sent
        assertEquals(0, channel.getInfoRequests().size());
        tstat.onRefresh(now);
        assertEquals(1, channel.getInfoRequests().size());

        // perform three additional (1ms apart) refreshes and make sure an info request was not sent
        tstat.onRefresh(now + 1);
        tstat.onRefresh(now + 2);
        tstat.onRefresh(now + 3);
        assertEquals(1, channel.getInfoRequests().size());

        // perform a fourth refresh after the default info response timeout interval and make sure an info request was sent
        tstat.onRefresh(now + ColorTouchThermostat.DEFAULT_REFRESH_INTERVAL_IN_MS_NO_PENDING_CONFIRMS);
        assertEquals(2, channel.getInfoRequests().size());
    }

    @Test
    public void testOnSetVariableWithInteger() throws Exception {
        testOnSetVariableWithValue(75);
    }

    @Test
    public void testOnSetVariableWithDouble() throws Exception {
        testOnSetVariableWithValue(75.0);
    }

    @Test
    public void testOnSetVariableWithString() throws Exception {
        testOnSetVariableWithValue("75");
    }

    @Test
    public void testValueInvalidation() throws Exception {
        URI uri = new URI("http://192.168.0.129");
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        MockDeviceManager dm = new MockDeviceManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("pluginId");
        plugin.setDeviceManager(dm);
        plugin.setVariableManager(vm);

        ColorTouchThermostat ctt = new ColorTouchThermostat(plugin, channel, uri, null);

        dm.publishDevice(ctt);

        // make sure no variable updates exist
        assertEquals(0, vm.getVariableUpdates().size());

        // send a successful InfoResponse
        plugin.onHttpResponse(200, null, "{\"name\": \"Office\",\"mode\": 3,\"state\": 0,\"fan\": 0,\"fanstate\": 0,\"tempunits\": 0,\"schedule\": 0,\"schedulepart\": 0,\"away\": 0,\"holiday\": 0,\"override\": 0,\"overridetime\": 0,\"forceunocc\": 0,\"spacetemp\": 79,\"heattemp\": 78,\"cooltemp\": 75,\"cooltempmin\": 35,\"cooltempmax\": 99,\"heattempmin\": 35,\"heattempmax\": 99,\"setpointdelta\": 2,\"availablemodes\": 0}", new InfoRequest(uri, ctt.getContext()));
        assertEquals(6, vm.getVariableUpdates().size());

        vm.clearVariableUpdates();

        // send an InfoRequest failure
        plugin.onHttpRequestFailure(new RuntimeException(), new InfoRequest(uri, ctt.getContext()));

        // make sure thermostat invalidated all values due to the failure
        assertEquals(4, vm.getVariableUpdates().size());
        for (VariableUpdate vu : vm.getVariableUpdates()) {
            assertNull(vu.getValue());
        }
        vm.clearVariableUpdates();

        // send an successful InfoResponse
        plugin.onHttpResponse(200, null, "{\"name\": \"Office\",\"mode\": 3,\"state\": 0,\"fan\": 0,\"fanstate\": 0,\"tempunits\": 0,\"schedule\": 0,\"schedulepart\": 0,\"away\": 0,\"holiday\": 0,\"override\": 0,\"overridetime\": 0,\"forceunocc\": 0,\"spacetemp\": 79,\"heattemp\": 73,\"cooltemp\": 75,\"cooltempmin\": 35,\"cooltempmax\": 99,\"heattempmin\": 35,\"heattempmax\": 99,\"setpointdelta\": 2,\"availablemodes\": 0}", new InfoRequest(uri, ctt.getContext()));
        assertEquals(5, vm.getVariableUpdates().size());
        for (VariableUpdate vu : vm.getVariableUpdates()) {
            assertNotNull(vu.getValue());
        }
    }

    public void testOnSetVariableWithValue(Object value) throws Exception {
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("foo");
        plugin.setVariableManager(vm);
        ColorTouchThermostat tstat = new ColorTouchThermostat(plugin, channel, new URI("http://localhost"), null);
        assertEquals(0, channel.getInfoRequests().size());
        assertEquals(0, channel.getControlRequests().size());
        assertFalse(tstat.hasPendingControlConfirmation());

        // send a set variable request; we expect this to trigger an info request and no control request
        tstat.onSetVariable(VariableConstants.TARGET_COOL_TEMP_F, value);
        assertEquals(1, channel.getInfoRequests().size());
        assertEquals(0, channel.getControlRequests().size());
        assertTrue(tstat.hasPendingControlConfirmation());

        // send a response to the info request; we expect this to trigger a control request
        tstat.onInfoResponse(null, new InfoResponse(null, "foo", ThermostatMode.COOL, FanMode.AUTO, 1, 70.0, 71.0, 73.0, 2.0), null, System.currentTimeMillis());
        assertEquals(1, channel.getInfoRequests().size());
        assertEquals(1, channel.getControlRequests().size());
        assertTrue(tstat.hasPendingControlConfirmation()); // the pending state is still in effect since we haven't confirmed the change

        // validate that the control request is identical to the info response except for COOL_TEMP_F which we're setting
        ControlRequest cr = channel.getControlRequests().get(0);
        assertEquals(ThermostatMode.COOL.ordinal(), (int)cr.getMode());
        assertEquals(FanMode.AUTO.ordinal(), (int)cr.getFanMode());
        assertEquals(75.0, cr.getCoolTemp(), 0);
        assertEquals(73.0, cr.getHeatTemp(), 0);

        // send a successful control response
        ControlResponse crr = new ControlResponse();
        tstat.onControlResponse(cr, crr, null);
        assertTrue(tstat.hasPendingControlConfirmation());

        // send an info response indicating the requested values have changed
        tstat.onInfoResponse(null, new InfoResponse(null, "foo", ThermostatMode.COOL, FanMode.AUTO, 1, 70.0, cr.getCoolTemp(), cr.getHeatTemp(), 2.0), null, System.currentTimeMillis());
        assertFalse(tstat.hasPendingControlConfirmation());
    }
}
