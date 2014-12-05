/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.state;

import com.whizzosoftware.hobson.venstar.api.ColorTouchChannel;
import com.whizzosoftware.hobson.venstar.api.ColorTouchChannelException;
import com.whizzosoftware.hobson.venstar.api.ColorTouchChannelFactory;
import com.whizzosoftware.hobson.venstar.api.dto.InfoResponse;
import com.whizzosoftware.hobson.venstar.api.dto.RootResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Represents the state when the plugin is performing discovery.
 *
 * @author Dan Noguerol
 */
public class DiscoveryState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ColorTouchChannelFactory factory;

    public DiscoveryState(ColorTouchChannelFactory factory) {
        this.factory = factory;
    }

    @Override
    public void onRefresh(StateContext context) {
        Collection<String> hosts = context.getDiscoveredHosts();
        for (String host : hosts) {
            if (!context.hasThermostatWithHost(host)) {
                processHost(context, host);
            }
        }
        if (context.hasThermostats()) {
            context.setState(new RunningState());
        }
    }

    @Override
    public void onThermostatFound(StateContext context) {
        onRefresh(context);
    }

    @Override
    public void onSetDeviceVariable(StateContext context, String deviceId, String name, Object value) {
        logger.debug("Received set device variable request while in discovery state; ignoring");
    }

    protected void processHost(StateContext context, String host) {
        try {
            ColorTouchChannel channel = factory.createColorTouchChannel(host);
            RootResponse root = channel.sendRootRequest();
            if (root.getApiVersion() == 3) {
                InfoResponse info = channel.sendInfoRequest();
                context.addThermostat(channel, info);
            } else {
                logger.error("Unsupported API version for device at {}; ignoring", host);
            }
        } catch (ColorTouchChannelException e) {
            logger.error("Error interrogating ColorTouch thermostat", e);
        }
    }
}
