package com.whizzosoftware.hobson.venstar.state;

import com.whizzosoftware.hobson.venstar.api.MockColorTouchChannelFactory;
import org.junit.Test;
import static org.junit.Assert.*;

public class DiscoveryStateTest {
    @Test
    public void testNoFoundAddress() {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState(new MockColorTouchChannelFactory(3));

        assertNull(context.getState());
        assertEquals(0, context.getDiscoveredHostCount());

        state.onRefresh(context);

        // make sure the state didn't change and no devices were created
        assertNull(context.getState());
        assertEquals(0, context.getCreatedHostCount());
    }

    @Test
    public void testOneFoundAddress() {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState(new MockColorTouchChannelFactory(3));

        assertNull(context.getState());
        context.addDiscoveredHost("localhost");
        assertEquals(1, context.getDiscoveredHostCount());

        state.onRefresh(context);

        assertTrue(context.getState() instanceof RunningState);
        assertEquals(1, context.getCreatedHostCount());
    }

    @Test
    public void testOneFoundAddressWithUnsupportedApiVersion() {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState(new MockColorTouchChannelFactory(2));

        assertNull(context.getState());
        context.addDiscoveredHost("localhost");
        assertEquals(1, context.getDiscoveredHostCount());

        state.onRefresh(context);

        assertNull(context.getState());
        assertEquals(0, context.getCreatedHostCount());
    }

    @Test
    public void testDuplicateFoundAddress() {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState(new MockColorTouchChannelFactory(3));

        assertNull(context.getState());
        context.addDiscoveredHost("localhost");
        assertEquals(1, context.getDiscoveredHostCount());

        state.onRefresh(context);

        assertTrue(context.getState() instanceof RunningState);
        assertEquals(1, context.getCreatedHostCount());

        context.setState(null);
        assertNull(context.getState());
        state.onRefresh(context);

        assertTrue(context.getState() instanceof RunningState);
        assertEquals(1, context.getCreatedHostCount());
    }

    @Test
    public void testTwoFoundAddresses() {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState(new MockColorTouchChannelFactory(3));

        assertNull(context.getState());
        context.addDiscoveredHost("host1");
        context.addDiscoveredHost("host2");
        assertEquals(2, context.getDiscoveredHostCount());

        state.onRefresh(context);

        assertTrue(context.getState() instanceof RunningState);
        assertEquals(2, context.getCreatedHostCount());
    }

    @Test
    public void testOnThermostatFound() {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState(new MockColorTouchChannelFactory(3));

        assertNull(context.getState());
        context.addDiscoveredHost("localhost");
        assertEquals(1, context.getDiscoveredHostCount());
        assertEquals(0, context.getCreatedHostCount());

        state.onThermostatFound(context);

        assertEquals(1, context.getCreatedHostCount());
    }
}
