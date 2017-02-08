package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.rest.JerseyClientConfig;

/**
 */
public class NifiRestClientConfig extends JerseyClientConfig {

    private String apiPath = "/nifi-api/";
    private String clusterType = "NODE";

    public NifiRestClientConfig(String apiPath) {
        this.apiPath = apiPath;
    }

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
