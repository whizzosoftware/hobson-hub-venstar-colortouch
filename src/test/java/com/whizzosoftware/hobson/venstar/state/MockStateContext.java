package com.whizzosoftware.hobson.venstar.state;

import com.whizzosoftware.hobson.venstar.api.ColorTouchChannel;
import com.whizzosoftware.hobson.venstar.api.ColorTouchChannelFactory;
import com.whizzosoftware.hobson.venstar.api.dto.InfoResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MockStateContext implements StateContext {
    private State state;
    private List<String> foundHosts = new ArrayList<String>();
    private List<String> createdHosts = new ArrayList<String>();
    private boolean refreshFlag;
    private List<VariableUpdateRequest> updateRequests = new ArrayList<VariableUpdateRequest>();

    @Override
    public ColorTouchChannelFactory getChannelFactory() {
        return null;
    }

    @Override
    public Collection<String> getDiscoveredHosts() {
        return foundHosts;
    }

    public void addDiscoveredHost(String host) {
        foundHosts.add(host);
    }

    public int getDiscoveredHostCount() {
        return foundHosts.size();
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    @Override
    public boolean hasThermostats() {
        return createdHosts.size() > 0;
    }

    @Override
    public boolean hasThermostatWithHost(String host) {
        return createdHosts.contains(host);
    }

    public int getCreatedHostCount() {
        return createdHosts.size();
    }

    @Override
    public void addThermostat(ColorTouchChannel channel, InfoResponse info) {
        createdHosts.add(channel.getHost());
    }

    @Override
    public void refreshAllThermostats() {
        refreshFlag = true;
    }

    @Override
    public void doSetDeviceVariable(String deviceId, String name, Object value) {
        updateRequests.add(new VariableUpdateRequest(deviceId, name, value));
    }

    public Collection<VariableUpdateRequest> getUpdateRequests() {
        return updateRequests;
    }

    public boolean getRefreshFlag() {
        return refreshFlag;
    }

    public class VariableUpdateRequest {
        public String deviceId;
        public String name;
        public Object value;

        public VariableUpdateRequest(String deviceId, String name, Object value) {
            this.deviceId = deviceId;
            this.name = name;
            this.value = value;
        }
    }
}
