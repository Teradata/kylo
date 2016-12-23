package com.thinkbiganalytics.nifi.rest.model;

/**
 * Status of a NiFi node.
 */
public class NiFiClusterSummary {

    private Integer connectedNodeCount;
    private String connectedNodes;
    private Boolean isClustered;
    private Boolean isConnectedToCluster;
    private Integer totalNodeCount;

    public Integer getConnectedNodeCount() {
        return connectedNodeCount;
    }

    public void setConnectedNodeCount(Integer connectedNodeCount) {
        this.connectedNodeCount = connectedNodeCount;
    }

    public String getConnectedNodes() {
        return connectedNodes;
    }

    public void setConnectedNodes(String connectedNodes) {
        this.connectedNodes = connectedNodes;
    }

    public Boolean getClustered() {
        return isClustered;
    }

    public void setClustered(Boolean clustered) {
        isClustered = clustered;
    }

    public Boolean getConnectedToCluster() {
        return isConnectedToCluster;
    }

    public void setConnectedToCluster(Boolean connectedToCluster) {
        isConnectedToCluster = connectedToCluster;
    }

    public Integer getTotalNodeCount() {
        return totalNodeCount;
    }

    public void setTotalNodeCount(Integer totalNodeCount) {
        this.totalNodeCount = totalNodeCount;
    }
}
