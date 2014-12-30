package com.whizzosoftware.hobson.venstar.state;

import com.whizzosoftware.hobson.venstar.api.dto.*;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class DiscoveryStateTest {
    // TODO: need test cases for timeout situations

    @Test
    public void testRefreshDevices() {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState();
        assertFalse(context.getRefreshFlag());
        state.onRefresh(context);
        assertTrue(context.getRefreshFlag());
    }

    @Test
    public void testNoFoundAddress() {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState();

        assertNull(context.getState());
        assertEquals(0, context.getDiscoveredHostCount());

        state.onRefresh(context);

        // make sure the state didn't change and no devices were created
        assertNull(context.getState());
        assertEquals(0, context.getCreatedThermostatCount());
    }

    @Test
    public void testOneFoundAddress() throws Exception {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState();

        assertNull(context.getState());

        // add a discovered thermostat host
        context.addDiscoveredHost(new URI("http://localhost"));
        assertEquals(1, context.getDiscoveredHostCount());

        // make sure no requests went out
        assertEquals(0, context.getRootRequests().size());
        assertEquals(0, context.getInfoRequests().size());

        // call refresh
        state.onRefresh(context);

        // make sure one root request went out and it was to the correct address
        assertEquals(1, context.getRootRequests().size());
        assertEquals(0, context.getInfoRequests().size());
        RootRequest rr = context.getRootRequests().iterator().next();
        assertEquals("http://localhost", rr.getURI().toString());
        assertNull(context.getState());

        // send back async root response
        RootResponse rrr = new RootResponse(3, "ColorTouch");
        state.onRootResponse(context, rr, rrr, null);

        // make sure one info request went out and it was to the correct address
        assertEquals(1, context.getRootRequests().size());
        assertEquals(1, context.getInfoRequests().size());
        InfoRequest ir = context.getInfoRequests().iterator().next();
        assertEquals("http://localhost", ir.getBaseURI().toString());
        assertEquals("http://localhost/query/info", ir.getURI().toString());
        assertNull(context.getState());

        // send back an async response
        InfoResponse irr = new InfoResponse("foo", ThermostatMode.AUTO, FanMode.AUTO, 1, 1.0, 2.0, 3.0, 2.0);
        state.onInfoResponse(context, ir, irr, null);

        // validate plugin is now in running state with one created thermostat
        assertTrue(context.getState() instanceof RunningState);
        assertEquals(1, context.getCreatedThermostatCount());
    }

    @Test
    public void testOneFoundAddressWithRootRequestFailureAndNoFoundThermostats() throws Exception {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState();

        assertNull(context.getState());

        // add a discovered thermostat host
        context.addDiscoveredHost(new URI("http://localhost"));
        assertEquals(1, context.getDiscoveredHostCount());

        // make sure no requests went out
        assertEquals(0, context.getRootRequests().size());
        assertEquals(0, context.getInfoRequests().size());

        // call refresh
        state.onRefresh(context);

        // make sure one root request went out and it was to the correct address
        assertEquals(1, context.getRootRequests().size());
        assertEquals(0, context.getInfoRequests().size());
        RootRequest rr = context.getRootRequests().iterator().next();
        assertEquals("http://localhost", rr.getURI().toString());
        assertNull(context.getState());

        // send root request failure
        state.onRootResponse(context, rr, null, new Exception());

        // validate no state change occurred
        assertNull(context.getState());
        assertFalse(state.hasPendingRequests());
    }

    @Test
    public void testOneFoundAddressWithRootRequestFailureAndOneFoundThermostat() throws Exception {
        MockStateContext context = new MockStateContext();
        context.addThermostat(new URI("http://host1"), new InfoResponse("foo", ThermostatMode.AUTO, FanMode.AUTO, 1, 1.0, 2.0, 3.0, 2.0));

        DiscoveryState state = new DiscoveryState();
        assertNull(context.getState());

        // add a discovered thermostat host
        context.addDiscoveredHost(new URI("http://localhost"));
        assertEquals(1, context.getDiscoveredHostCount());

        // make sure no requests went out
        assertEquals(0, context.getRootRequests().size());
        assertEquals(0, context.getInfoRequests().size());

        // call refresh
        state.onRefresh(context);

        // make sure one root request went out and it was to the correct address
        assertEquals(1, context.getRootRequests().size());
        assertEquals(0, context.getInfoRequests().size());
        RootRequest rr = context.getRootRequests().iterator().next();
        assertEquals("http://localhost", rr.getURI().toString());
        assertNull(context.getState());

        // send root request failure
        state.onRootResponse(context, rr, null, new Exception());

        // validate plugin is now in running state
        assertTrue(context.getState() instanceof RunningState);
        assertFalse(state.hasPendingRequests());
    }

    @Test
    public void testOneFoundAddressWithInfoRequestFailureAndNoFoundThermostats() throws Exception {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState();

        assertNull(context.getState());

        // add a discovered thermostat host
        context.addDiscoveredHost(new URI("http://localhost"));
        assertEquals(1, context.getDiscoveredHostCount());

        // make sure no requests went out
        assertEquals(0, context.getRootRequests().size());
        assertEquals(0, context.getInfoRequests().size());

        // call refresh
        state.onRefresh(context);

        // make sure one root request went out and it was to the correct address
        assertEquals(1, context.getRootRequests().size());
        assertEquals(0, context.getInfoRequests().size());
        RootRequest rr = context.getRootRequests().iterator().next();
        assertEquals("http://localhost", rr.getURI().toString());
        assertNull(context.getState());

        // send back async root response
        RootResponse rrr = new RootResponse(3, "ColorTouch");
        state.onRootResponse(context, rr, rrr, null);

        // make sure one info request went out and it was to the correct address
        assertEquals(1, context.getRootRequests().size());
        assertEquals(1, context.getInfoRequests().size());
        InfoRequest ir = context.getInfoRequests().iterator().next();
        assertEquals("http://localhost", ir.getBaseURI().toString());
        assertEquals("http://localhost/query/info", ir.getURI().toString());
        assertNull(context.getState());

        // send back an async response
        state.onInfoResponse(context, ir, null, new Exception());

        // validate plugin is now in running state with one created thermostat
        assertNull(context.getState());
        assertEquals(0, context.getCreatedThermostatCount());
        assertFalse(state.hasPendingRequests());
    }

    @Test
    public void testOneFoundAddressWithUnsupportedApiVersionAndNoFoundThermostats() throws Exception {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState();
        assertNull(context.getState());

        // add a new discovered host
        context.addDiscoveredHost(new URI("localhost"));
        assertEquals(1, context.getDiscoveredHostCount());

        // make sure not info requests went out
        assertEquals(0, context.getRootRequests().size());

        // call refresh
        state.onRefresh(context);

        // make sure one info request went out
        assertEquals(1, context.getRootRequests().size());

        RootRequest rr = context.getRootRequests().iterator().next();

        // send async response with bad API version
        RootResponse rrr = new RootResponse(2, "ColorTouch");
        state.onRootResponse(context, rr, rrr, null);

        // make sure that no info request was sent, no thermostats were created and we've not changed states
        assertEquals(0, context.getInfoRequests().size());
        assertEquals(0, context.getCreatedThermostatCount());
        assertNull(context.getState());
    }

    @Test
    public void testOneFoundAddressWithUnsupportedApiVersionAndOneFoundThermostat() throws Exception {
        MockStateContext context = new MockStateContext();
        context.addThermostat(new URI("http://host1"), new InfoResponse("foo", ThermostatMode.AUTO, FanMode.AUTO, 1, 1.0, 2.0, 3.0, 2.0));
        assertEquals(1, context.getCreatedThermostatCount());

        DiscoveryState state = new DiscoveryState();
        assertNull(context.getState());

        // add a new discovered host
        context.addDiscoveredHost(new URI("http://localhost"));
        assertEquals(1, context.getDiscoveredHostCount());

        // make sure not info requests went out
        assertEquals(0, context.getRootRequests().size());

        // call refresh
        state.onRefresh(context);

        // make sure one info request went out
        assertEquals(1, context.getRootRequests().size());

        RootRequest rr = context.getRootRequests().iterator().next();

        // send async response with bad API version
        RootResponse rrr = new RootResponse(2, "ColorTouch");
        state.onRootResponse(context, rr, rrr, null);

        // make sure that no info request was sent, no thermostats were created and we've moved to the running state
        assertEquals(0, context.getInfoRequests().size());
        assertEquals(1, context.getCreatedThermostatCount());
        assertTrue(context.getState() instanceof RunningState);
    }

    @Test
    public void testDuplicateFoundAddress() throws Exception {
        // start with one thermostat already discovered
        MockStateContext context = new MockStateContext();
        context.addThermostat(new URI("http://localhost"), new InfoResponse("foo", ThermostatMode.AUTO, FanMode.AUTO, 1, 1.0, 2.0, 3.0, 2.0));

        // add a new discovered host with the same address
        DiscoveryState state = new DiscoveryState();
        assertNull(context.getState());
        context.addDiscoveredHost(new URI("http://localhost"));
        assertEquals(1, context.getDiscoveredHostCount());

        // refresh
        state.onRefresh(context);

        // we should be in the running state again with the number of created thermostats
        assertTrue(context.getState() instanceof RunningState);
        assertEquals(1, context.getCreatedThermostatCount());
    }

    @Test
    public void testTwoFoundAddresses() throws Exception {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState();

        // add two discovered hosts
        assertNull(context.getState());
        context.addDiscoveredHost(new URI("http://host1"));
        context.addDiscoveredHost(new URI("http://host2"));
        assertEquals(2, context.getDiscoveredHostCount());

        // refresh
        state.onRefresh(context);

        // confirm that two root requests went out
        assertEquals(2, context.getRootRequests().size());
        assertEquals(0, context.getInfoRequests().size());
        assertNull(context.getState());

        // send back two root responses
        for (RootRequest rr : context.getRootRequests()) {
            state.onRootResponse(context, rr, new RootResponse(3, "ColorTouch"), null);
        }

        // confirm that two info requests went out
        assertEquals(2, context.getRootRequests().size());
        assertEquals(2, context.getInfoRequests().size());

        // send back two info responses
        int i=0;
        for (InfoRequest ir : context.getInfoRequests()) {
            state.onInfoResponse(context, ir, new InfoResponse("thermo", ThermostatMode.AUTO, FanMode.AUTO, 1, 1.0, 2.0, 3.0, 2.0), null);
            // make sure we didn't jump states prematurely
            if (i == 0) {
                assertNull(context.getState());
            }
            i++;
        }

        // confirm that we're back in the running state and have two added thermostats
        assertTrue(context.getState() instanceof RunningState);
        assertEquals(2, context.getCreatedThermostatCount());
    }

    @Test
    public void testOnThermostatFound() throws Exception {
        MockStateContext context = new MockStateContext();
        DiscoveryState state = new DiscoveryState();

        assertNull(context.getState());
        context.addDiscoveredHost(new URI("http://localhost"));
        assertEquals(1, context.getDiscoveredHostCount());
        assertEquals(0, context.getCreatedThermostatCount());

        state.onThermostatFound(context);

        // make sure one root request went out and it was to the correct address
        assertEquals(1, context.getRootRequests().size());
        assertEquals(0, context.getInfoRequests().size());
        RootRequest rr = context.getRootRequests().iterator().next();
        assertEquals("http://localhost", rr.getURI().toString());
        assertNull(context.getState());
    }
}
