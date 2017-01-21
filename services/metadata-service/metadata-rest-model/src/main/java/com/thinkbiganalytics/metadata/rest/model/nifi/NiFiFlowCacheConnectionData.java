package com.thinkbiganalytics.metadata.rest.model.nifi;

/**
 * Created by sr186054 on 1/19/17.
 */
public class NiFiFlowCacheConnectionData {

    private String connectionIdentifier;
    private String name;
    private String sourceIdentifier;
    private String destinationIdentifier;

    public NiFiFlowCacheConnectionData() {

    }

    public NiFiFlowCacheConnectionData(String connectionIdentifier, String name, String sourceIdentifier, String destinationIdentifier) {
        this.connectionIdentifier = connectionIdentifier;
        this.name = name;
        this.sourceIdentifier = sourceIdentifier;
        this.destinationIdentifier = destinationIdentifier;
    }

    public String getConnectionIdentifier() {
        return connectionIdentifier;
    }

    public void setConnectionIdentifier(String connectionIdentifier) {
        this.connectionIdentifier = connectionIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }

    public String getDestinationIdentifier() {
        return destinationIdentifier;
    }

    public void setDestinationIdentifier(String destinationIdentifier) {
        this.destinationIdentifier = destinationIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NiFiFlowCacheConnectionData that = (NiFiFlowCacheConnectionData) o;

        return connectionIdentifier != null ? connectionIdentifier.equals(that.connectionIdentifier) : that.connectionIdentifier == null;
    }

    @Override
    public int hashCode() {
        return connectionIdentifier != null ? connectionIdentifier.hashCode() : 0;
    }
}
