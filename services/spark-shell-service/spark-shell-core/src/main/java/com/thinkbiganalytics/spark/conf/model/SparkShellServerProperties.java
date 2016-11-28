package com.thinkbiganalytics.spark.conf.model;

/**
 * Properties for an externally managed Spark Shell service.
 */
public class SparkShellServerProperties {

    /** Host where Spark Shell is running */
    private String host;

    /** Port where Spark Shell is listening */
    private int port = 8450;

    /** Username for basic authentication */
    private String username;

    /** Password for basic authentication */
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
