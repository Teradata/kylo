package com.thinkbiganalytics.nifi.rest.client.layout;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;

import java.util.HashSet;
import java.util.Set;

/**
 * Hold nifi components together for layout render processing
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
        if (connections == null) {
            connections = new HashSet<>();
        }
        return connections;
    }

    public ProcessGroupAndConnections addConnection(ConnectionDTO connection) {
        getConnections().add(connection);
        return this;
    }

    public ProcessGroupAndConnections addPort(PortDTO portDTO) {
        getPorts().add(portDTO);
        return this;
    }


    public ProcessGroupDTO getProcessGroup() {
        return processGroup;
    }

    public Set<PortDTO> getPorts() {
        if (ports == null) {
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
