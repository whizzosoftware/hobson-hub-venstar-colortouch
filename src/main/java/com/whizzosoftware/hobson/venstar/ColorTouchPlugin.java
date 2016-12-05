/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.venstar;

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.disco.DeviceAdvertisement;
import com.whizzosoftware.hobson.api.event.DeviceAdvertisementEvent;
import com.whizzosoftware.hobson.api.event.EventTopics;
import com.whizzosoftware.hobson.api.event.HobsonEvent;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.http.AbstractHttpClientPlugin;
import com.whizzosoftware.hobson.api.plugin.http.HttpRequest;
import com.whizzosoftware.hobson.api.plugin.http.HttpResponse;
import com.whizzosoftware.hobson.api.property.PropertyConstraintType;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.ssdp.SSDPPacket;
import com.whizzosoftware.hobson.venstar.api.ColorTouchChannel;
import com.whizzosoftware.hobson.venstar.api.dto.*;
import com.whizzosoftware.hobson.venstar.state.DiscoveryState;
import com.whizzosoftware.hobson.venstar.state.State;
import com.whizzosoftware.hobson.venstar.state.StateContext;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * The Venstar ColorTouch plugin. This uses a REST client to communicate with ColorTouch thermostats.
 *
 * @author Dan Noguerol
 */
public class ColorTouchPlugin extends AbstractHttpClientPlugin implements StateContext, ColorTouchChannel {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    static final String PROP_THERMOSTAT_HOST = "thermostat.host";
    private static final long DEFAULT_REFRESH_INTERVAL_IN_SECONDS = 5;

    private State state;
    private final List<URI> discoveredURIs = new ArrayList<>();
    private boolean hasAtLeastOneThermostat;

    public ColorTouchPlugin(String pluginId) {
        super(pluginId);

        this.state = new DiscoveryState();
    }

    // ***
    // HobsonPlugin methods
    // ***

    @Override
    public void onStartup(PropertyContainer config) {
        // set to running status
        setStatus(PluginStatus.running());

        // request SSDP device advertisements events that occurred before this plugin started
        requestDeviceAdvertisementSnapshot(SSDPPacket.PROTOCOL_ID);

        // check if a thermostat has been manually configured
        addManualHostIfNotDiscovered((String)config.getPropertyValue(PROP_THERMOSTAT_HOST));
    }

    @Override
    public void onShutdown() {
    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return new TypedProperty[] {
            new TypedProperty.Builder(
                PROP_THERMOSTAT_HOST,
                "Thermostat Host",
                "The hostname or IP address of a ColorTouch thermostat. This should be detected automatically but you can enter it manually here if necessary. You must have the API enabled on the thermostat.",
                TypedProperty.Type.STRING).
                    constraint(PropertyConstraintType.required, true).
                    build()
        };
    }

    @Override
    public String getName() {
        return "Venstar ColorTouch";
    }

    @Override
    public long getRefreshInterval() {
        return DEFAULT_REFRESH_INTERVAL_IN_SECONDS;
    }

    @Override
    public String[] getEventTopics() {
        // make sure we subscribe to SSDP device advertisement events
        return new String[] { EventTopics.createDiscoTopic(SSDPPacket.PROTOCOL_ID) };
    }

    @Override
    public void onRefresh() {
        state.onRefresh(this, System.currentTimeMillis());
    }

    @Override
    public void onPluginConfigurationUpdate(PropertyContainer config) {
        addManualHostIfNotDiscovered((String)config.getPropertyValue(PROP_THERMOSTAT_HOST));
    }

    @Override
    public void onSetDeviceVariable(DeviceContext context, String variableName, Object value) {
        // we override this to run the set variable request through the state machine
        state.onSetDeviceVariable(this, context, variableName, value);
    }

    @Override
    public void onHobsonEvent(HobsonEvent event) {
        super.onHobsonEvent(event);

        if (event instanceof DeviceAdvertisementEvent) {
            DeviceAdvertisement advertisement = ((DeviceAdvertisementEvent)event).getAdvertisement();
            if ("ssdp".equals(advertisement.getProtocolId())) {
                final SSDPPacket ssdp = (SSDPPacket)advertisement.getObject();
                if (ssdp != null && ssdp.getLocation() != null) {
                    try {
                        URI uri = new URI(ssdp.getLocation());
                        if (logger.isTraceEnabled()) {
                            logger.trace("Found device at {}: {}", uri.toASCIIString(), ssdp.toString());
                        }
                        if (ssdp.getNT() != null && ssdp.getNT().equals("colortouch:ecp") && !discoveredURIs.contains(uri)) {
                            logger.info("Found ColorTouch thermostat at {}", ssdp.getLocation());
                            discoveredURIs.add(uri);
                            state.onThermostatFound(this);
                            // TODO: make sure not to overwrite this property
                            setPluginConfigurationProperty(getContext(), PROP_THERMOSTAT_HOST, uri.getHost());
                        }
                    } catch (URISyntaxException e) {
                        logger.error("ColorTouch thermostat location is not a valid URI; ignoring", e);
                    }
                } else {
                    logger.warn("Received device advertisement with no SSDP packet");
                }
            }
        }
    }

    private void addManualHostIfNotDiscovered(String manualHost) {
        // check if a thermostat has been manually configured
        if (manualHost != null) {
            try {
                URI uri = new URI("http://" + manualHost + "/");
                if (!discoveredURIs.contains(uri)) {
                    discoveredURIs.add(uri);
                    state.onThermostatFound(this);
                }
            } catch (URISyntaxException e) {
                logger.error("Invalid thermostat host configured; ignoring", e);
            }
        }
    }

    // ***
    // StateContext methods
    // ***

    @Override
    public Collection<URI> getDiscoveredURIs() {
        return discoveredURIs;
    }

    @Override
    public void setState(State state) {
        if (this.state != state) {
            logger.debug("Changing to state: " + state);
            this.state = state;
            onRefresh();
        }
    }

    @Override
    public boolean hasThermostats() {
        return hasAtLeastOneThermostat;
    }

    @Override
    public boolean hasThermostatWithHost(String host) {
        Collection<HobsonDevice> devices = getAllDevices();
        if (devices != null) {
            for (HobsonDevice device : devices) {
                if (device instanceof ColorTouchThermostat) {
                    ColorTouchThermostat ctt = (ColorTouchThermostat) device;
                    if (ctt.getBaseURI().getHost().equals(host)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void addThermostat(URI baseURI, InfoResponse info) {
        publishDevice(new ColorTouchThermostat(this, this, baseURI, info));
        hasAtLeastOneThermostat = true;
        logger.debug("Added thermostat: {}", info.getName());
    }

    @Override
    public void refreshAllThermostats(long now) {
        Collection<HobsonDevice> devices = getAllPluginDevices();
        if (devices != null) {
            for (HobsonDevice device : devices) {
                if (device instanceof ColorTouchThermostat) {
                    ((ColorTouchThermostat) device).onRefresh(now);
                } else {
                    logger.error("Unable to refresh unknown device: {}", device.getContext());
                }
            }
        }
    }

    @Override
    public void doSetDeviceVariable(DeviceContext context, String name, Object value) {
        getDevice(context).getRuntime().onSetVariable(name, value);
    }

    @Override
    public void sendRootRequest(RootRequest request) {
        sendHttpRequest(request.getURI(), HttpRequest.Method.GET, request);
    }

    @Override
    public void sendInfoRequest(InfoRequest request) {
        sendHttpRequest(request.getURI(), HttpRequest.Method.GET, null, request);
    }

    @Override
    public void sendControlRequest(ControlRequest request) {
        try {
            sendHttpRequest(request.getURI(), HttpRequest.Method.POST, null, null, request.getRequestBody().getBytes(), request);
        } catch (UnsupportedEncodingException e) {
            throw new HobsonRuntimeException("Error sending control request", e);
        }
    }

    // ***
    // AbstractHttpClientPlugin methods
    // ***

    @Override
    public void onHttpResponse(HttpResponse response, Object context) {
        logger.trace("Got HTTP response {} with context: {}", response.getStatusCode(), context.getClass().getSimpleName());

        try {
            if (context instanceof RootRequest) {
                state.onRootResponse(this, (RootRequest) context, new RootResponse(new JSONObject(new JSONTokener(response.getBodyAsStream()))), null);
            } else if (context instanceof InfoRequest) {
                state.onInfoResponse(this, (InfoRequest)context, new InfoResponse(new JSONObject(new JSONTokener(response.getBodyAsStream()))), null);
            } else if (context instanceof ControlRequest) {
                state.onControlResponse(this, (ControlRequest) context, new ControlResponse(new JSONObject(new JSONTokener(response.getBodyAsStream()))), null);
            } else {
                logger.error("Unknown HTTP response: " + context);
            }
        } catch (IOException e) {
            logger.error("Error reading HTTP response", e);
        }
    }

    @Override
    public void onHttpRequestFailure(Throwable cause, Object context) {
        logger.debug("HTTP request failure", cause);
        if (context instanceof RootRequest) {
            state.onRootResponse(this, (RootRequest) context, null, cause);
        } else if (context instanceof InfoRequest) {
            state.onInfoResponse(this, (InfoRequest)context, null, cause);
        } else if (context instanceof ControlRequest) {
            state.onControlResponse(this, (ControlRequest) context, null, cause);
        } else {
            logger.error("Unknown HTTP request failure: " + context, cause);
        }
    }

    // ***
    // StateContext methods
    // ***

    @Override
    public ColorTouchThermostat getThermostatDevice(DeviceContext context) {
        HobsonDevice device = getDevice(context);
        if (!(device instanceof ColorTouchThermostat)) {
            throw new HobsonNotFoundException("Device " + context + " found but it's not a ColorTouch thermostat: " + device);
        }
        return (ColorTouchThermostat)device;
    }
}
