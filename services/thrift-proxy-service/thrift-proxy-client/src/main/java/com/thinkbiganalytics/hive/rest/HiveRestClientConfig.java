/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.hive.rest;

import com.thinkbiganalytics.rest.JerseyClientConfig;

/**
 * Created by sr186054 on 10/16/15.
 */
public class HiveRestClientConfig extends JerseyClientConfig {

    private String apiPath = "/api/v1/hive";


    public HiveRestClientConfig(String apiPath) {
        this.apiPath = apiPath;
    }
    public HiveRestClientConfig() {

    }

    public HiveRestClientConfig(String host, String username, String password, String apiPath) {
        super(host, username, password);
        this.apiPath = apiPath;

    }

    public HiveRestClientConfig(String host, String username, String password, boolean https, boolean keystoreOnClasspath, String keystorePath, String keystorePassword, String apiPath) {
        super(host, username, password, https, keystoreOnClasspath, keystorePath, keystorePassword);
        this.apiPath = apiPath;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

}
