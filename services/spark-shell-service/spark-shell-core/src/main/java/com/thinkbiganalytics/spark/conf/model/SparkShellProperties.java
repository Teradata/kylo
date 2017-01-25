package com.thinkbiganalytics.spark.conf.model;

/*-
 * #%L
 * Spark Shell Core
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

/**
 *
 */
public class SparkShellProperties {

    /** Startup timeout in seconds */
    private int clientTimeout = 60;

    /** Spark deploy mode */
    private String deployMode = "client";

    /** Request timeout in seconds */
    private int idleTimeout = 900;

    /** Spark master */
    private String master = "local";

    /** Maximum port number */
    private int portMax = 45999;

    /** Minimum port number */
    private int portMin = 45000;

    /** Enables user impersonation */
    private boolean proxyUser = false;

    /** Externally managed process */
    private SparkShellServerProperties server;

    /** Additional command-line options */
    private String sparkOptions;

    public int getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(int clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public int getPortMax() {
        return portMax;
    }

    public void setPortMax(int portMax) {
        this.portMax = portMax;
    }

    public int getPortMin() {
        return portMin;
    }

    public void setPortMin(int portMin) {
        this.portMin = portMin;
    }

    public boolean isProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(boolean proxyUser) {
        this.proxyUser = proxyUser;
    }

    public SparkShellServerProperties getServer() {
        return server;
    }

    public void setServer(SparkShellServerProperties server) {
        this.server = server;
    }

    public String getSparkOptions() {
        return sparkOptions;
    }

    public void setSparkOptions(String sparkOptions) {
        this.sparkOptions = sparkOptions;
    }
}
