/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.state;

import com.whizzosoftware.hobson.venstar.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents the state when the plugin is performing discovery.
 *
 * This class assumes that all of its onXXX methods will be called from the same thread (the plugin event loop thread)
 * so it makes no allowances for synchronization or thread safety.
 *
 * @author Dan Noguerol
 */
public class DiscoveryState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<RootRequest> pendingRootRequests = new ArrayList<>();
    private List<InfoRequest> pendingInfoRequests = new ArrayList<>();

    @Override
    public void onRefresh(StateContext context) {
        // check if any discovered addresses are ones without an associated thermostat
        Collection<URI> uris = context.getDiscoveredURIs();
        for (URI uri : uris) {
            // if so, send a root request to the host for its information
            if (!context.hasThermostatWithHost(uri.getHost())) {
                try {
                    logger.trace("Sending root request to thermostat at {}", uri);
                    RootRequest rr = new RootRequest(uri);
                    context.sendRootRequest(rr);
                    pendingRootRequests.add(rr);
                } catch (URISyntaxException e) {
                    logger.error("Found invalid host address when processing new thermostat", e);
                }
            }
        }

        // refresh all current thermostats
        context.refreshAllThermostats();

        switchStatesIfApplicable(context);
    }

    @Override
    public void onThermostatFound(StateContext context) {
        onRefresh(context);
    }

    @Override
    public void onSetDeviceVariable(StateContext context, String deviceId, String name, Object value) {
        logger.debug("Received set device variable request while in discovery state; ignoring");
    }

    @Override
    public void onRootResponse(StateContext context, RootRequest request, RootResponse response, Throwable error) {
        pendingRootRequests.remove(request);

        logger.trace("Received root response from thermostat at {}", request.getURI());

        if (response != null) {
            try {
                if (response.getApiVersion() == 3) {
                    logger.trace("Sending info request to thermostat at {}", request.getURI());
                    InfoRequest ir = new InfoRequest(request.getURI());
                    context.sendInfoRequest(ir);
                    pendingInfoRequests.add(ir);
                } else {
                    logger.warn("Found ColorTouch thermostat with unsupported API version " + response.getApiVersion() + "; ignoring");
                }
            } catch (URISyntaxException e) {
                logger.error("Found invalid host address when processing new thermostat", e);
            }
        } else if (error != null) {
            logger.error("Error requesting root information from host " + request.getURI(), error);
        }

        switchStatesIfApplicable(context);
    }

    @Override
    public void onInfoResponse(StateContext context, InfoRequest request, InfoResponse response, Throwable error) {
        pendingInfoRequests.remove(request);

        logger.trace("Received info response from thermostat at {}", request.getURI());

        if (response != null) {
            if (!context.hasThermostatWithHost(request.getURI().getHost())) {
                context.addThermostat(request.getURI(), response);
            } else if (request.getDeviceId() != null) {
                context.getThermostatDevice(request.getDeviceId()).onInfoResponse(request, response, error);
            }
        } else if (error != null) {
            logger.error("Error requesting info from host " + request.getURI(), error);
            if (request.getDeviceId() != null) {
                context.getThermostatDevice(request.getDeviceId()).onInfoResponse(request, null, error);
            }
        }

        switchStatesIfApplicable(context);
    }

    @Override
    public void onControlResponse(StateContext context, ControlRequest request, ControlResponse response, Throwable error) {
        if (request.getDeviceId() != null) {
            context.getThermostatDevice(request.getDeviceId()).onControlResponse(request, response, error);
        }
    }

    protected boolean hasPendingRequests() {
        return (pendingRootRequests.size() > 0 || pendingInfoRequests.size() > 0);
    }

    protected void switchStatesIfApplicable(StateContext context) {
        // if there are no in-flight requests and there is at least one found thermostat, move to the running state
        if (!hasPendingRequests() && context.hasThermostats()) {
            context.setState(new RunningState());
        }
    }
}
