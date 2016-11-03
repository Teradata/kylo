package com.thinkbiganalytics.servicemonitor.rest.client.ambari;

import com.thinkbiganalytics.rest.JerseyClientConfig;

/**
 * Created by sr186054 on 10/16/15.
 */
public class AmbariJerseyRestClientConfig extends JerseyClientConfig {

    private String apiPath = "/api/v1";

    public AmbariJerseyRestClientConfig(String apiPath) {
        this.apiPath = apiPath;
    }

    public AmbariJerseyRestClientConfig() {

    }

    public AmbariJerseyRestClientConfig(String host, String username, String password, String apiPath) {
        super(host, username, password);
        this.apiPath = apiPath;
    }

    public AmbariJerseyRestClientConfig(String host, String username, String password, boolean https, boolean keystoreOnClasspath, String keystorePath, String keystorePassword, String apiPath) {
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
