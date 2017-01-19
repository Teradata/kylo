package com.thinkbiganalytics.nifi.rest.model.flow;

/**
 * Created by sr186054 on 1/19/17.
 */
public class NifiFlowConnection {

    private String connectionIdentifier;
    private String name;
    private String sourceIdentifier;
    private String destinationIdentifier;

    public NifiFlowConnection() {

    }

    public NifiFlowConnection(String connectionIdentifier, String name, String sourceIdentifier, String destinationIdentifier) {
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

        NifiFlowConnection that = (NifiFlowConnection) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (sourceIdentifier != null ? !sourceIdentifier.equals(that.sourceIdentifier) : that.sourceIdentifier != null) {
            return false;
        }
        return destinationIdentifier != null ? destinationIdentifier.equals(that.destinationIdentifier) : that.destinationIdentifier == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (sourceIdentifier != null ? sourceIdentifier.hashCode() : 0);
        result = 31 * result + (destinationIdentifier != null ? destinationIdentifier.hashCode() : 0);
        return result;
    }
}
