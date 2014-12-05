/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.disco;

import com.whizzosoftware.hobson.api.disco.DeviceAdvertisement;
import com.whizzosoftware.hobson.api.disco.DeviceAdvertisementListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An DeviceAdvertisementListener implementation that can detect ColorTouch thermostats via SSDP.
 *
 * @author Dan Noguerol
 */
public class ColorTouchAdvertisementListener implements DeviceAdvertisementListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public final static String PROTOCOL_ID = "ssdp";

    private final List<String> discoveredAddresses = new ArrayList<String>();
    private ColorTouchThermostatListener listener;

    public ColorTouchAdvertisementListener(ColorTouchThermostatListener listener) {
        this.listener = listener;
    }

    public Collection<String> getDiscoveredAddresses() {
        return discoveredAddresses;
    }

    @Override
    public void onDeviceAdvertisement(DeviceAdvertisement advertisement) {
        String data = advertisement.getData();
        if (advertisement.getProtocolId().equals(PROTOCOL_ID) && data.startsWith("NOTIFY *") && data.contains("colortouch:ecp")) {
            int ix1 = data.indexOf("Location: ");
            if (ix1 > -1) {
                ix1 += 10;
                int ix2 = data.indexOf("\n", ix1);
                URI uri = URI.create(data.substring(ix1, ix2).trim());
                String host = uri.getHost();
                if (host != null) {
                    boolean isNew = false;
                    synchronized (discoveredAddresses) {
                        if (!discoveredAddresses.contains(host)) {
                            discoveredAddresses.add(host);
                            isNew = true;
                        }
                    }
                    if (isNew) {
                        logger.info("Found Venstar ColorTouch at {}", host);
                        if (listener != null) {
                            listener.onThermostatFound(host);
                        }
                    }
                }
            } else {
                logger.debug("Found possible Venstar ColorTouch but no LOCATION header found");
            }
        }
    }
}
