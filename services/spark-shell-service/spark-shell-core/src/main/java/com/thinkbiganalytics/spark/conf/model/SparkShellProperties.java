package com.thinkbiganalytics.spark.conf.model;

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
