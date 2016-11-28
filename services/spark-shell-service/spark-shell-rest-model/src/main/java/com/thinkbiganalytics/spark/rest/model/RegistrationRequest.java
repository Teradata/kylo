package com.thinkbiganalytics.spark.rest.model;

/**
 * Request to register a Spark Shell process with Kylo.
 */
public class RegistrationRequest {

    /** Remote hostname */
    private String host;

    /** Remote port number */
    private int port;

    /**
     * Gets the hostname for communicating with the Spark Shell client.
     *
     * @return the remote hostname
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the hostname for communicating with the Spark Shell client.
     *
     * @param host the remote hostname
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the port number for communicating with the Spark Shell client.
     *
     * @return the remote port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number for communicating with the Spark Shell client.
     *
     * @param port the remote port number
     */
    public void setPort(int port) {
        this.port = port;
    }
}
