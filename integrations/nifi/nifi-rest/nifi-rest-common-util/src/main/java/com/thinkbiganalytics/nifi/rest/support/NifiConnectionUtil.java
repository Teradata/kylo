package com.thinkbiganalytics.nifi.rest.support;

/*-
 * #%L
 * thinkbig-nifi-rest-common-util
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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NifiConnectionUtil {

    public static List<String> getInputProcessorIds(Collection<ConnectionDTO> connections) {
        List<String> processorIds = new ArrayList<>();
        SourcesAndDestinations connectionItems = new SourcesAndDestinations(connections);
        //find all sources that are not in a destination
        for (String source : connectionItems.getSourceConnections()) {
            if (!connectionItems.getDestinationConnections().contains(source)) {
                processorIds.add(source);
            }
        }
        return processorIds;

    }

    public static List<String> getInputPortIds(Collection<ConnectionDTO> connections) {
        List<String> inputPortIds = new ArrayList<>();
        if (connections != null) {
            for (ConnectionDTO connectionDTO : connections) {
                if (connectionDTO.getSource().getType().equals(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name())) {
                    inputPortIds.add(connectionDTO.getSource().getId());
                }
            }
        }
        return inputPortIds;
    }


    private static class SourcesAndDestinations {

        private List<String> destinationConnections = new ArrayList<>();
        private List<String> sourceConnections = new ArrayList<>();
        private List<String> selfReferencingConnections = new ArrayList<>();

        public SourcesAndDestinations(Collection<ConnectionDTO> connections) {
            if (connections != null) {
                for (ConnectionDTO connection : connections) {
                    sourceConnections.add(connection.getSource().getId());
                    if (!connection.getSource().getId().equalsIgnoreCase(connection.getDestination().getId())) {
                        destinationConnections.add(connection.getDestination().getId());
                    }
                }
            }
        }

        public List<String> getSelfReferencingConnections() {
            return selfReferencingConnections;
        }

        public List<String> getDestinationConnections() {
            return destinationConnections;
        }

        public List<String> getSourceConnections() {
            return sourceConnections;
        }
    }

    public static List<String> getEndingProcessorIds(Collection<ConnectionDTO> connections) {
        List<String> processorIds = new ArrayList<>();
        SourcesAndDestinations connectionItems = new SourcesAndDestinations(connections);

        //find all destinations that are not in a source

        for (String dest : connectionItems.getDestinationConnections()) {
            if (!connectionItems.getSourceConnections().contains(dest)) {
                processorIds.add(dest);
            }
        }
        return processorIds;
    }

    public static List<ConnectionDTO> findConnectionsMatchingSourceGroupId(Collection<ConnectionDTO> connections, final String sourceProcessGroupId) {
        return Lists.newArrayList(Iterables.filter(connections, new Predicate<ConnectionDTO>() {
            @Override
            public boolean apply(ConnectionDTO connectionDTO) {
                return connectionDTO.getSource().getGroupId().equals(sourceProcessGroupId);
            }
        }));
    }

    public static List<ConnectionDTO> findConnectionsMatchingDestinationGroupId(Collection<ConnectionDTO> connections, final String destProcessGroupId) {
        return Lists.newArrayList(Iterables.filter(connections, new Predicate<ConnectionDTO>() {
            @Override
            public boolean apply(ConnectionDTO connectionDTO) {
                return connectionDTO.getDestination().getGroupId().equals(destProcessGroupId);
            }
        }));
    }

    public static List<ConnectionDTO> findConnectionsMatchingDestinationId(Collection<ConnectionDTO> connections, final String destId) {
        return Lists.newArrayList(Iterables.filter(connections, new Predicate<ConnectionDTO>() {
            @Override
            public boolean apply(ConnectionDTO connectionDTO) {
                return connectionDTO.getDestination().getId().equals(destId);
            }
        }));
    }

    public static List<ConnectionDTO> findConnectionsMatchingSourceId(Collection<ConnectionDTO> connections, final String sourceId) {
        return Lists.newArrayList(Iterables.filter(connections, new Predicate<ConnectionDTO>() {
            @Override
            public boolean apply(ConnectionDTO connectionDTO) {
                return connectionDTO.getSource().getId().equals(sourceId);
            }
        }));
    }

    public static ConnectionDTO findConnection(Collection<ConnectionDTO> connections, final String sourceId, final String destId) {
        ConnectionDTO connection = null;
        connection = Iterables.tryFind(connections, new Predicate<ConnectionDTO>() {
            @Override
            public boolean apply(ConnectionDTO connectionDTO) {
                return connectionDTO.getSource().getId().equals(sourceId) && connectionDTO.getDestination()
                        .getId().equalsIgnoreCase(destId);
            }
        }).orNull();

        return connection;

    }

    public static PortDTO findPortMatchingName(Collection<PortDTO> ports, final String name) {

        return Iterables.tryFind(ports, new Predicate<PortDTO>() {
            @Override
            public boolean apply(PortDTO portDTO) {
                return portDTO.getName().equalsIgnoreCase(name);
            }
        }).orNull();
    }


    public static Set<ConnectionDTO> getAllConnections(ProcessGroupDTO group) {
        Set<ConnectionDTO> connections = new HashSet<>();
        if (group != null) {
            connections.addAll(group.getContents().getConnections());
            if (group.getContents().getProcessGroups() != null) {
                for (ProcessGroupDTO groupDTO : group.getContents().getProcessGroups()) {
                    connections.addAll(getAllConnections(groupDTO));
                }
            }
        }
        return connections;
    }



}
