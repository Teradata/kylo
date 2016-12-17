package com.thinkbiganalytics.nifi.rest.client.layout;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sr186054 on 12/16/16.
 */
public class ProcessGroupAndConnections {

    private Set<ConnectionDTO> connections;

    private ProcessGroupDTO processGroup;

    private Set<PortDTO> ports;

    public ProcessGroupAndConnections(ProcessGroupDTO processGroup, Set<ConnectionDTO> connections) {
        this.connections = connections;
        this.processGroup = processGroup;
    }

    public ProcessGroupAndConnections(ProcessGroupDTO processGroup) {
        this.processGroup = processGroup;
    }

    public Set<ConnectionDTO> getConnections() {
        if(connections == null){
            connections = new HashSet<>();
        }
        return connections;
    }

    public ProcessGroupAndConnections addConnection(ConnectionDTO connection){
        getConnections().add(connection);
        return this;
    }

    public ProcessGroupAndConnections addPort(PortDTO portDTO){
        getPorts().add(portDTO);
        return this;
    }


    public ProcessGroupDTO getProcessGroup() {
        return processGroup;
    }

    public Set<PortDTO> getPorts() {
        if(ports == null){
            ports = new HashSet<>();
        }
        return ports;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProcessGroupAndConnections that = (ProcessGroupAndConnections) o;

        return !(processGroup != null ? !processGroup.equals(that.processGroup) : that.processGroup != null);

    }

    @Override
    public int hashCode() {
        return processGroup != null ? processGroup.hashCode() : 0;
    }
}
