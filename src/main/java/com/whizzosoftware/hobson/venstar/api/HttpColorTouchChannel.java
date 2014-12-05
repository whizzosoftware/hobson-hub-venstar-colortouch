/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.venstar.api;

import com.whizzosoftware.hobson.venstar.api.dto.ControlRequest;
import com.whizzosoftware.hobson.venstar.api.dto.ControlResponse;
import com.whizzosoftware.hobson.venstar.api.dto.InfoResponse;
import com.whizzosoftware.hobson.venstar.api.dto.RootResponse;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

public class HttpColorTouchChannel implements ColorTouchChannel {
    private String host;
    private String id;
    private String urlRoot;
    private HttpClient httpClient = new HttpClient(new SimpleHttpConnectionManager());

    public HttpColorTouchChannel(String host) {
        this.host = host;
        this.id = host.replace('.', '-');
        this.urlRoot = "http://" + host;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getHost() {
        return host;
    }

    public RootResponse sendRootRequest() throws ColorTouchChannelException {
        try {
            GetMethod get = new GetMethod(urlRoot);
            int statusCode = httpClient.executeMethod(get);
            if (statusCode == 200) {
                return new RootResponse(new JSONObject(new JSONTokener(get.getResponseBodyAsStream())));
            } else {
                throw new ColorTouchChannelException("Received unexpected response code: " + statusCode);
            }
        } catch (IOException e) {
            throw new ColorTouchChannelException("Error retrieving root", e);
        }
    }

    public InfoResponse sendInfoRequest() throws ColorTouchChannelException {
        try {
            GetMethod get = new GetMethod(urlRoot + "/query/info");
            int statusCode = httpClient.executeMethod(get);
            if (statusCode == 200) {
                return new InfoResponse(new JSONObject(new JSONTokener(get.getResponseBodyAsStream())));
            } else {
                throw new ColorTouchChannelException("Received unexpected response code: " + statusCode);
            }
        } catch (IOException e) {
            throw new ColorTouchChannelException("Error querying for info", e);
        }
    }

    @Override
    public ControlResponse sendControlRequest(ControlRequest request) throws ColorTouchChannelException {
        try {
            PostMethod post = new PostMethod(urlRoot + "/control");
            post.setRequestBody(request.getRequestBody());
            int statusCode = httpClient.executeMethod(post);
            if (statusCode == 200) {
                return new ControlResponse(new JSONObject(new JSONTokener(post.getResponseBodyAsStream())));
            } else {
                throw new ColorTouchChannelException("Received unexpected response code: " + statusCode);
            }
        } catch (IOException e) {
            throw new ColorTouchChannelException("Error sending control request", e);
        }
    }

    @Override
    public void close() {
        ((SimpleHttpConnectionManager)httpClient.getHttpConnectionManager()).shutdown();
    }
}
