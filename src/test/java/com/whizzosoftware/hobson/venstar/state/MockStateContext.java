package com.whizzosoftware.hobson.venstar.state;

import com.whizzosoftware.hobson.venstar.api.dto.ControlRequest;
import com.whizzosoftware.hobson.venstar.api.dto.InfoRequest;
import com.whizzosoftware.hobson.venstar.api.dto.InfoResponse;
import com.whizzosoftware.hobson.venstar.api.dto.RootRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MockStateContext implements StateContext {
    private State state;
    private List<URI> foundURIs = new ArrayList<>();
    private List<String> createdThermostats = new ArrayList<>();
    private boolean refreshFlag;
    private List<VariableUpdateRequest> updateRequests = new ArrayList<>();
    private List<RootRequest> rootRequests = new ArrayList<>();
    private List<InfoRequest> infoRequests = new ArrayList<>();
    private List<ControlRequest> controlRequests = new ArrayList<>();

    @Override
    public Collection<URI> getDiscoveredURIs() {
        return foundURIs;
    }

    public void addDiscoveredHost(URI uri) {
        foundURIs.add(uri);
    }

    public int getDiscoveredHostCount() {
        return foundURIs.size();
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
        return createdThermostats.size() > 0;
    }

    @Override
    public boolean hasThermostatWithHost(String host) {
        return createdThermostats.contains(host);
    }

    public int getCreatedThermostatCount() {
        return createdThermostats.size();
    }

    @Override
    public void addThermostat(URI uri, InfoResponse info) {
        createdThermostats.add(uri.getHost());
    }

    @Override
    public void refreshAllThermostats() {
        refreshFlag = true;
    }

    @Override
    public void doSetDeviceVariable(String deviceId, String name, Object value) {
        updateRequests.add(new VariableUpdateRequest(deviceId, name, value));
    }

    @Override
    public void sendRootRequest(RootRequest request) {
        rootRequests.add(request);
    }

    public Collection<RootRequest> getRootRequests() {
        return rootRequests;
    }

    @Override
    public void sendInfoRequest(InfoRequest request) {
        infoRequests.add(request);
    }

    public Collection<InfoRequest> getInfoRequests() {
        return infoRequests;
    }

    @Override
    public void sendControlRequest(ControlRequest request) {
        controlRequests.add(request);
    }

    public Collection<ControlRequest> getControlRequests() {
        return controlRequests;
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
