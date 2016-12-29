package com.thinkbiganalytics.nifi.rest.model.flow;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the ConnectionDTO id and the related Name Created by sr186054 on 8/30/16.
 */
public class NiFiFlowProcessorConnection {

    private String connectionIdentifier;
    private String name;
    private Set<String> selectedRelationships;

    public NiFiFlowProcessorConnection() {
    }

    public NiFiFlowProcessorConnection(String connectionIdentifier, String name, Set<String> selectedRelationships) {
        this.connectionIdentifier = connectionIdentifier;
        this.name = name;
        this.selectedRelationships = selectedRelationships;
    }

    public String getConnectionIdentifier() {
        return connectionIdentifier;
    }

    public void setConnectionIdentifier(String connectionIdentifier) {
        this.connectionIdentifier = connectionIdentifier;
    }

    public Set<String> getSelectedRelationships() {
        if (selectedRelationships == null) {
            this.selectedRelationships = new HashSet<>();
        }
        return selectedRelationships;
    }

    public void setSelectedRelationships(Set<String> selectedRelationships) {
        this.selectedRelationships = selectedRelationships;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NiFiFlowProcessorConnection that = (NiFiFlowProcessorConnection) o;

        return connectionIdentifier.equals(that.connectionIdentifier);

    }

    @Override
    public int hashCode() {
        return connectionIdentifier.hashCode();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NiFiFlowProcessorConnection{");
        sb.append("connectionIdentifier='").append(connectionIdentifier).append('\'');
        sb.append(", selectedRelationships='").append(selectedRelationships).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
