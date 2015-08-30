/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.state;

import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.venstar.api.dto.FanMode;
import com.whizzosoftware.hobson.venstar.api.dto.ThermostatMode;
import org.junit.Test;
import static org.junit.Assert.*;

public class VariableStateTest {
    @Test
    public void testClear() {
        VariableState state = new VariableState();
        assertFalse(state.hasValues());
        state.setTempF(72.0);
        assertTrue(state.hasValues());
        state.clear();
        assertFalse(state.hasValues());
    }

    @Test
    public void testSetValue() {
        VariableState state = new VariableState();

        assertNull(state.getMode());
        state.setValue(VariableConstants.TSTAT_MODE, ThermostatMode.AUTO.toString());
        assertEquals(ThermostatMode.AUTO.toString(), state.getMode());

        assertNull(state.getFanMode());
        state.setValue(VariableConstants.TSTAT_FAN_MODE, FanMode.AUTO.toString());
        assertEquals(FanMode.AUTO.toString(), state.getFanMode());

        assertNull(state.getTempF());
        state.setValue(VariableConstants.INDOOR_TEMP_F, 72.0);
        assertEquals(72.0, state.getTempF(), 0);

        assertNull(state.getCoolTempF());
        state.setValue(VariableConstants.TARGET_COOL_TEMP_F, 74.0);
        assertEquals(74.0, state.getCoolTempF(), 0);

        assertNull(state.getHeatTempF());
        state.setValue(VariableConstants.TARGET_HEAT_TEMP_F, 71.0);
        assertEquals(71.0, state.getHeatTempF(), 0);
    }

    @Test
    public void testEquals() {
        VariableState state = new VariableState();

        // two fully null states should be equal
        assertNull(state.getMode());
        assertNull(state.getFanMode());
        assertNull(state.getTempF());
        assertNull(state.getCoolTempF());
        assertNull(state.getHeatTempF());
        assertTrue(state.equals(new VariableState(null, null, null, null, null, null)));

        // one fully null state should be equal to a state with full values
        assertTrue(state.equals(new VariableState(null, "AUTO", "AUTO", 70.0, 71.0, 72.0)));

        // a state with only one populated value should be equal to a state with all values populated containing the same one value
        state.setMode("COOL");
        assertTrue(state.equals(new VariableState(null, "COOL", "AUTO", 70.0, 71.0, 72.0)));

        // a state with only one populated value should not be equal to a state with all values populated and containing a different one value
        assertFalse(state.equals(new VariableState(null, "AUTO", "AUTO", 70.0, 71.0, 72.0)));
        assertFalse(new VariableState(null, null, null, null, null, 72.0).equals(new VariableState(null, null, null, null, null, 73.0)));

        // a state with all populated values should be equal to a state with the same populated values
        assertTrue(new VariableState(null, "AUTO", "AUTO", 70.0, 71.0, 72.0).equals(new VariableState(null, "AUTO", "AUTO", 70.0, 71.0, 72.0)));

        // a state with all populated values should not be equal to a state with different populated values
        assertFalse(new VariableState(null, "COOL", "ON", 71.0, 72.0, 73.0).equals(new VariableState(null, "AUTO", "AUTO", 70.0, 71.0, 72.0)));
    }
}
