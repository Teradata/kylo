package com.thinkbiganalytics.es;

/**
 * Created by sr186054 on 5/13/16.
 */
public class ElasticSearchClientConfig {

    private String host;
    private String clusterName;
    private Integer port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
