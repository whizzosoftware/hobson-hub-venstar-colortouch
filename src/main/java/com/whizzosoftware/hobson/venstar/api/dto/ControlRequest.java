/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.api.dto;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
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
    private DeviceContext deviceContext;
    private Integer mode;
    private Integer fan;
    private Double heatTemp;
    private Double coolTemp;
    private Integer pin;

    public ControlRequest(URI baseURI, DeviceContext deviceContext, String mode, String fan, Double heatTemp, Double coolTemp, Double setPointDelta, Integer pin) {
        this(baseURI, deviceContext, (mode != null) ? ThermostatMode.valueOf(mode) : null, (fan != null) ? FanMode.valueOf(fan) : null, heatTemp, coolTemp, setPointDelta, pin);
    }

    public ControlRequest(URI baseURI, DeviceContext deviceContext, ThermostatMode mode, FanMode fan, Double heatTemp, Double coolTemp, Double setPointDelta, Integer pin) {
        try {
            // according to the documentation, when the thermostat mode is "AUTO", cooltemp has to be higher than
            // heattemp and they have to be setpointdelta units apart.
            if (mode == ThermostatMode.AUTO) {
                if (Math.abs(coolTemp - heatTemp) < setPointDelta) {
                    throw new HobsonRuntimeException("cooltemp must be " + setPointDelta + " units higher than heattemp when thermostat mode is AUTO");
                }
            }

            this.baseURI = baseURI;
            this.uri = new URI(baseURI.getScheme(), baseURI.getHost(), "/control", null);
            this.deviceContext = deviceContext;
            if (mode != null) {
                this.mode = mode.ordinal();
            }
            if (fan != null) {
                this.fan = fan.ordinal();
            }
            this.heatTemp = heatTemp;
            this.coolTemp = coolTemp;
            this.pin = pin;
        } catch (URISyntaxException e) {
            throw new HobsonRuntimeException("Invalid base request URI: " + baseURI.toString(), e);
        }
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public URI getURI() {
        return uri;
    }

    public DeviceContext getDeviceContext() {
        return deviceContext;
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

    public Map<String,String> getRequestBodyMap() {
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
        return pairs;
    }

    public String getRequestBody() throws UnsupportedEncodingException {
        return URLEncoderUtil.createQueryString(getRequestBodyMap(), null);
    }
}
