package com.whizzosoftware.hobson.venstar.state;

import org.junit.Test;
import static org.junit.Assert.*;

public class RunningStateTest {
    @Test
    public void testOnRefresh() {
        MockStateContext context = new MockStateContext();
        RunningState state = new RunningState();
        assertFalse(context.getRefreshFlag());
        state.onRefresh(context);
        assertTrue(context.getRefreshFlag());
    }

    @Test
    public void testOnThermostatFound() {
        MockStateContext context = new MockStateContext();
        RunningState state = new RunningState();
        assertNull(context.getState());
        state.onThermostatFound(context);
        assertTrue(context.getState() instanceof DiscoveryState);
    }

    @Test
    public void testOnVariableUpdateRequest() {
        MockStateContext context = new MockStateContext();
        RunningState state = new RunningState();
        assertNull(context.getState());
        assertEquals(0, context.getUpdateRequests().size());

        state.onSetDeviceVariable(context, "device", "foo", "bar");
        assertNull(context.getState());
        assertEquals(1, context.getUpdateRequests().size());
        MockStateContext.VariableUpdateRequest e = context.getUpdateRequests().iterator().next();
        assertEquals("device", e.deviceId);
        assertEquals("foo", e.name);
        assertEquals("bar", e.value);
    }
}
