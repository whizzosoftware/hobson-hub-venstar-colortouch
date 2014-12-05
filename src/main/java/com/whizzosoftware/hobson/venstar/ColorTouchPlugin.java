/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar;

import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.plugin.AbstractHobsonPlugin;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.venstar.api.ColorTouchChannel;
import com.whizzosoftware.hobson.venstar.api.ColorTouchChannelFactory;
import com.whizzosoftware.hobson.venstar.api.HttpColorTouchChannelFactory;
import com.whizzosoftware.hobson.venstar.api.dto.InfoResponse;
import com.whizzosoftware.hobson.venstar.disco.ColorTouchAdvertisementListener;
import com.whizzosoftware.hobson.venstar.disco.ColorTouchThermostatListener;
import com.whizzosoftware.hobson.venstar.state.DiscoveryState;
import com.whizzosoftware.hobson.venstar.state.State;
import com.whizzosoftware.hobson.venstar.state.StateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Dictionary;

/**
 * The Venstar ColorTouch plugin. This uses a REST client to communicate with ColorTouch thermostats.
 *
 * @author Dan Noguerol
 */
public class ColorTouchPlugin extends AbstractHobsonPlugin implements ColorTouchThermostatListener, StateContext {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long DEFAULT_REFRESH_INTERVAL_IN_SECONDS = 30;

    private ColorTouchChannelFactory channelFactory;
    private long refreshIntervalInSeconds;
    private ColorTouchAdvertisementListener detector;
    private State state;
    private boolean hasAtLeastOneThermostat;

    public ColorTouchPlugin(String pluginId) {
        this(pluginId, new HttpColorTouchChannelFactory(), DEFAULT_REFRESH_INTERVAL_IN_SECONDS);
    }

    public ColorTouchPlugin(String pluginId, HttpColorTouchChannelFactory channelFactory, long refreshIntervalInSeconds) {
        super(pluginId);
        this.channelFactory = channelFactory;
        this.refreshIntervalInSeconds = refreshIntervalInSeconds;
        this.state = new DiscoveryState(channelFactory);
    }

    // ***
    // HobsonPlugin methods
    // ***

    @Override
    public void onStartup(Dictionary config) {
        // publish an analyzer that can detect Hue bridges via SSDP
        detector = new ColorTouchAdvertisementListener(this);
        publishDeviceAdvertisementListener(ColorTouchAdvertisementListener.PROTOCOL_ID, detector);

        // set to running status
        setStatus(new PluginStatus(PluginStatus.Status.RUNNING));
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public String getName() {
        return "Venstar ColorTouch";
    }

    @Override
    public long getRefreshInterval() {
        return refreshIntervalInSeconds;
    }

    @Override
    public void onRefresh() {
        state.onRefresh(this);
    }

    @Override
    public void onPluginConfigurationUpdate(Dictionary config) {
    }

    @Override
    public void onSetDeviceVariable(String deviceId, String variableName, Object value) {
        state.onSetDeviceVariable(this, deviceId, variableName, value);
    }

    // ***
    // StateContext methods
    // ***

    @Override
    public ColorTouchChannelFactory getChannelFactory() {
        return channelFactory;
    }

    @Override
    public Collection<String> getDiscoveredHosts() {
        return detector.getDiscoveredAddresses();
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
        for (HobsonDevice device : devices) {
            if (device instanceof ColorTouchThermostat) {
                ColorTouchThermostat ctt = (ColorTouchThermostat)device;
                if (ctt.getHost().equals(host)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void addThermostat(ColorTouchChannel channel, InfoResponse info) {
        publishDevice(new ColorTouchThermostat(this, channel, info));
        hasAtLeastOneThermostat = true;
        logger.debug("Added thermostat: {}", info.getName());
    }

    @Override
    public void refreshAllThermostats() {
        Collection<HobsonDevice> devices = getAllDevices();
        for (HobsonDevice device : devices) {
            if (device instanceof ColorTouchThermostat) {
                ((ColorTouchThermostat)device).refresh();
            } else {
                logger.error("Unable to refresh unknown device: {}", device.getId());
            }
        }
    }

    @Override
    public void doSetDeviceVariable(String deviceId, String name, Object value) {
        getDevice(deviceId).onSetVariable(name, value);
    }

    // ***
    // ColorTouchListener methods
    // ***

    @Override
    public void onThermostatFound(String host) {
        state.onThermostatFound(this);
    }
}
