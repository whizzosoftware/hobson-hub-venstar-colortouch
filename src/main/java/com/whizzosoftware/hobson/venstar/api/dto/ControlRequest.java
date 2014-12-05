/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.api.dto;

import org.apache.commons.httpclient.NameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the data needed to make control requests to a thermostat.
 *
 * @author Dan Noguerol
 */
public class ControlRequest {
    private Integer mode;
    private Integer fan;
    private Double heatTemp;
    private Double coolTemp;
    private Integer pin;

    public ControlRequest(ThermostatMode mode, FanMode fan, Double heatTemp, Double coolTemp, Integer pin) {
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

    public NameValuePair[] getRequestBody() {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        if (mode != null) {
            pairs.add(new NameValuePair("mode", mode.toString()));
        }
        if (fan != null) {
            pairs.add(new NameValuePair("fan", fan.toString()));
        }
        if (heatTemp != null) {
            pairs.add(new NameValuePair("heattemp", heatTemp.toString()));
        }
        if (coolTemp != null) {
            pairs.add(new NameValuePair("cooltemp", coolTemp.toString()));
        }
        if (pin != null) {
            pairs.add(new NameValuePair("pin", pin.toString()));
        }
        return pairs.toArray(new NameValuePair[pairs.size()]);
    }
}
