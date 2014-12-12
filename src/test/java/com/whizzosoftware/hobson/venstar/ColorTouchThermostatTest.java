package com.whizzosoftware.hobson.venstar;

import com.whizzosoftware.hobson.api.disco.MockDiscoManager;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.MockVariableManager;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.venstar.api.MockColorTouchChannel;
import com.whizzosoftware.hobson.venstar.api.dto.*;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class ColorTouchThermostatTest {
    @Test
    public void testConstructor() throws Exception {
        InfoResponse info = new InfoResponse("thermo", ThermostatMode.AUTO, FanMode.ON, 100, 1.0, 2.0, 3.0);
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
    public void testVariableUpdates() throws Exception {
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        MockDiscoManager dm = new MockDiscoManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("foo");
        plugin.setVariableManager(vm);
        plugin.setDiscoManager(dm);
        assertEquals(0, vm.getVariableUpdates().size());

        InfoResponse info = new InfoResponse("thermo", ThermostatMode.AUTO, FanMode.ON, 100, 1.0, 2.0, 3.0);
        ColorTouchThermostat tstat = new ColorTouchThermostat(plugin, channel, new URI("http://localhost"), info);
        tstat.onStartup();

        // verify that the appropriate variables were published
        assertEquals(5, vm.getPublishedDeviceVariables().size());
        for (HobsonVariable v : vm.getPublishedDeviceVariables()) {
            assertTrue(
                (VariableConstants.TEMP_F.equals(v.getName()) && v.getValue().equals(1.0)) ||
                (VariableConstants.TARGET_COOL_TEMP_F.equals(v.getName()) && v.getValue().equals(2.0)) ||
                (VariableConstants.TARGET_HEAT_TEMP_F.equals(v.getName()) && v.getValue().equals(3.0)) ||
                (VariableConstants.TSTAT_FAN_MODE.equals(v.getName()) && v.getValue().equals("ON")) ||
                (VariableConstants.TSTAT_MODE.equals(v.getName()) && v.getValue().equals("AUTO"))
            );
        }
        assertEquals(0, vm.getVariableUpdates().size());

        // update tstat information with two changes (TEMP_F and TARGET_HEAT_TEMP_F)
        tstat.onInfoResponse(new InfoResponse("thermo", ThermostatMode.AUTO, FanMode.ON, 100, 1.5, 2.0, 3.5), null);

        // verify that only the two appropriate variable updates occurred
        assertEquals(2, vm.getVariableUpdates().size());
        for (VariableUpdate vu : vm.getVariableUpdates()) {
            assertTrue(
                (VariableConstants.TEMP_F.equals(vu.getName()) && vu.getValue().equals(1.5)) ||
                (VariableConstants.TARGET_HEAT_TEMP_F.equals(vu.getName()) && vu.getValue().equals(3.5))
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

    public void testApplySetVariableToControlHardwareWithValue(Object value) throws Exception {
        MockColorTouchChannel channel = new MockColorTouchChannel();
        MockVariableManager vm = new MockVariableManager();
        ColorTouchPlugin plugin = new ColorTouchPlugin("foo");
        plugin.setVariableManager(vm);
        ColorTouchThermostat tstat = new ColorTouchThermostat(plugin, channel, new URI("http://localhost"), null);
        assertEquals(0, channel.getInfoRequests().size());
        assertEquals(0, channel.getControlRequests().size());
        assertFalse(tstat.hasPendingControlRequest());

        // send a set variable request; we expect this to trigger an info request and no control request
        tstat.onSetVariable(VariableConstants.TARGET_COOL_TEMP_F, value);
        assertEquals(1, channel.getInfoRequests().size());
        assertEquals(0, channel.getControlRequests().size());
        assertTrue(tstat.hasPendingControlRequest());

        // send a response to the info request; we expect this to trigger a control request
        tstat.onInfoResponse(new InfoResponse("foo", ThermostatMode.AUTO, FanMode.AUTO, 1, 72.0, 71.0, 73.0), null);
        assertEquals(1, channel.getInfoRequests().size());
        assertEquals(1, channel.getControlRequests().size());
        assertFalse(tstat.hasPendingControlRequest());

        // validate that the control request is identical to the info response except for COOL_TEMP_F which we're setting
        ControlRequest cr = channel.getControlRequests().get(0);
        assertEquals(ThermostatMode.AUTO.ordinal(), (int)cr.getMode());
        assertEquals(FanMode.AUTO.ordinal(), (int)cr.getFanMode());
        assertEquals(75.0, cr.getCoolTemp(), 0);
        assertEquals(73.0, cr.getHeatTemp(), 0);

        // send a successful control response
        ControlResponse crr = new ControlResponse();
        tstat.onControlResponse(crr, null);
        assertFalse(tstat.hasPendingControlRequest());
    }
}
