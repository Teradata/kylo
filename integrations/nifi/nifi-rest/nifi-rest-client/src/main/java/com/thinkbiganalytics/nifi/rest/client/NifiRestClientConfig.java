/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.nifi.rest.client;

import com.thinkbiganalytics.rest.JerseyClientConfig;

/**
 * Created by sr186054 on 10/16/15.
 */
public class NifiRestClientConfig extends JerseyClientConfig {

    private String apiPath = "/nifi-api/";

    public NifiRestClientConfig(String apiPath) {
        this.apiPath = apiPath;
    }

    private String clusterType = "NODE";

    public NifiRestClientConfig() {

    }

    public NifiRestClientConfig(String host, String username, String password, String apiPath) {
        super(host, username, password);
        this.apiPath = apiPath;
    }

    public NifiRestClientConfig(String host, String username, String password, boolean https, boolean keystoreOnClasspath,
                                String keystorePath, String keystorePassword, String apiPath) {
        super(host, username, password, https, keystoreOnClasspath, keystorePath, keystorePassword);
        this.apiPath = apiPath;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }
}
