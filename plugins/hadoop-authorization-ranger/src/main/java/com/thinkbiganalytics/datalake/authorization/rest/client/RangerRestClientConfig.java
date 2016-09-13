package com.thinkbiganalytics.datalake.authorization.rest.client;


import com.thinkbiganalytics.rest.JerseyClientConfig;


/**
 * Setting Ranger REST client configuration.
 *
 * @author sv186029
 * @version 1.0
 * @category ranger security
 */

public class RangerRestClientConfig extends JerseyClientConfig {

// apiPath not require here value is set in RangerRestClient 

    private String apiPath = "/service";


    public RangerRestClientConfig(String apiPath) {
        this.apiPath = apiPath;
    }

    public RangerRestClientConfig() {

    }

    public RangerRestClientConfig(String host, String username, String password) {
        super(host, username, password);
        //this.apiPath = apiPath;

    }

    public RangerRestClientConfig(String host, String username, String password, boolean https, boolean keystoreOnClasspath, String keystorePath, String keystorePassword, String apiPath) {
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
