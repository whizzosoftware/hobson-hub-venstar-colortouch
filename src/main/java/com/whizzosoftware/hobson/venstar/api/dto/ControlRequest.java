/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.api.dto;

import com.whizzosoftware.hobson.api.plugin.http.URLEncoderUtil;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the data needed to make control requests to a thermostat.
 *
 * @author Dan Noguerol
 */
public class ControlRequest {
    private URI baseURI;
    private URI uri;
    private String deviceId;
    private Integer mode;
    private Integer fan;
    private Double heatTemp;
    private Double coolTemp;
    private Integer pin;

    public ControlRequest(URI baseURI, String deviceId, ThermostatMode mode, FanMode fan, Double heatTemp, Double coolTemp, Integer pin) throws URISyntaxException {
        this.baseURI = baseURI;
        this.uri = new URI(baseURI.getScheme(), baseURI.getHost(), "/control", null);
        this.deviceId = deviceId;
        if (mode != null) {
            this.mode = mode.ordinal();
        }
        if (fan != null) {
            this.fan = fan.ordinal();
        }
        this.heatTemp = heatTemp;
        this.coolTemp = coolTemp;
        this.pin = pin;
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public URI getURI() {
        return uri;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Integer getMode() {
        return mode;
    }

    public Integer getFanMode() {
        return fan;
    }

    public Double getHeatTemp() {
        return heatTemp;
    }

    public Double getCoolTemp() {
        return coolTemp;
    }

    public Integer getPin() {
        return pin;
    }

    public void updateIfNull(ThermostatMode mode, FanMode fan, Double heatTemp, Double coolTemp, Integer pin) {
        if (this.mode == null && mode != null) {
            this.mode = mode.ordinal();
        }
        if (this.fan == null && fan != null) {
            this.fan = fan.ordinal();
        }
        if (this.heatTemp == null) {
            this.heatTemp = heatTemp;
        }
        if (this.coolTemp == null) {
            this.coolTemp = coolTemp;
        }
        if (this.pin == null) {
            this.pin = pin;
        }
    }

    public String getRequestBody() throws UnsupportedEncodingException {
        Map<String,String> pairs = new HashMap<>();
        if (mode != null) {
            pairs.put("mode", mode.toString());
        }
        if (fan != null) {
            pairs.put("fan", fan.toString());
        }
        if (heatTemp != null) {
            pairs.put("heattemp", heatTemp.toString());
        }
        if (coolTemp != null) {
            pairs.put("cooltemp", coolTemp.toString());
        }
        if (pin != null) {
            pairs.put("pin", pin.toString());
        }
        return URLEncoderUtil.format(pairs, null);
    }
}
