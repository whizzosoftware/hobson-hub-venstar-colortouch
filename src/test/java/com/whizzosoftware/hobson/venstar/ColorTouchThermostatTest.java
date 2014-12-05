package com.whizzosoftware.hobson.venstar;

import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.venstar.api.MockColorTouchChannel;
import org.junit.Test;
import static org.junit.Assert.*;

public class ColorTouchThermostatTest {
    @Test
    public void testApplySetVariableToControlledHardwareWithInteger() {
        MockColorTouchChannel channel = new MockColorTouchChannel("localhost", 3);
        ColorTouchThermostat tstat = new ColorTouchThermostat(null, channel, null);
        tstat.onSetVariable(VariableConstants.TARGET_COOL_TEMP_F, 75);
        assertEquals(1, channel.getControlRequests().size());
    }

    @Test
    public void testApplySetVariableToControlledHardwareWithDouble() {
        MockColorTouchChannel channel = new MockColorTouchChannel("localhost", 3);
        ColorTouchThermostat tstat = new ColorTouchThermostat(null, channel, null);
        tstat.onSetVariable(VariableConstants.TARGET_COOL_TEMP_F, 75.0);
        assertEquals(1, channel.getControlRequests().size());
    }

    @Test
    public void testApplySetVariableToControlledHardwareWithString() {
        MockColorTouchChannel channel = new MockColorTouchChannel("localhost", 3);
        ColorTouchThermostat tstat = new ColorTouchThermostat(null, channel, null);
        tstat.onSetVariable(VariableConstants.TARGET_COOL_TEMP_F, "75");
        assertEquals(1, channel.getControlRequests().size());
    }
}
