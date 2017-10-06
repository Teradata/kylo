package com.thinkbiganalytics.nifi.rest.visitor;

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


import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowVisitor;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableConnection;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessor;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorConfigDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.RemoteProcessGroupDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class NifiConnectionOrderVisitor implements NifiFlowVisitor {

    private static final Logger log = LoggerFactory.getLogger(NifiConnectionOrderVisitor.class);

    protected NifiVisitableProcessGroup currentProcessGroup;

    private NifiVisitableProcessGroup processGroup;

    private Map<String, ProcessorDTO> processorsMap = new HashMap<>();

    private Map<String, NifiVisitableProcessor> visitedProcessors = new HashMap<>();

    private Map<String, NifiVisitableProcessGroup> visitedProcessGroups = new HashMap<>();

    private Set<NifiVisitableConnection> allConnections = new HashSet<>();

    private NiFiRestClient restClient;

    private NifiConnectionOrderVisitorCache cache;


    public NifiConnectionOrderVisitor(NiFiRestClient restClient, NifiVisitableProcessGroup processGroup, NifiConnectionOrderVisitorCache cache) {
        this.restClient = restClient;
        this.processGroup = processGroup;
        this.currentProcessGroup = processGroup;
        this.processorsMap = NifiProcessUtil.getProcessorsMap(processGroup.getDto());
        this.cache = cache;
        if (cache == null) {
            cache = new NifiConnectionOrderVisitorCache();
        }
    }

    @Override
    public void visitProcessor(NifiVisitableProcessor processor) {

        visitedProcessors.put(processor.getDto().getId(), processor);
        //add the pointer to the ProcessGroup
        currentProcessGroup.addProcessor(processor);
    }

    @Override
    public NifiVisitableProcessor getProcessor(String id) {
        return visitedProcessors.get(id);
    }

    @Override
    public NifiVisitableProcessGroup getProcessGroup(String id) {
        return visitedProcessGroups.get(id);
    }


    @Override
    public void visitConnection(NifiVisitableConnection connection) {
        Set<String> relationships = connection.getDto().getSelectedRelationships();
        String sourceType = connection.getDto().getSource().getType();
        String destType = connection.getDto().getDestination().getType();

        List<NifiVisitableProcessor> destinationProcessors = getDestinationProcessors(connection.getDto(), true);

        List<NifiVisitableProcessor> sourceProcessors = getSourceProcessors(connection.getDto());

        if (destinationProcessors != null) {
            destinationProcessors.forEach(destinationProcessor -> destinationProcessor.addSourceConnectionIdentifier(connection.getDto()));
        }
        if (destinationProcessors != null && sourceProcessors != null) {
            for (NifiVisitableProcessor destination : destinationProcessors) {
                for (NifiVisitableProcessor source : sourceProcessors) {
                    destination.addSource(source);
                    source.addDestination(destination);
                }
            }
        }

        for (NifiVisitableProcessor sourceProcessor : sourceProcessors) {
            sourceProcessor.addDestinationConnectionIdentifier(connection.getDto());
        }

        allConnections.add(connection);

    }

    @Override
    public void visitProcessGroup(NifiVisitableProcessGroup processGroup) {

        log.debug(" Visit Process Group: {}, ({}) ", processGroup.getDto().getName(), processGroup.getDto().getId());

        NifiVisitableProcessGroup group = visitedProcessGroups.get(processGroup.getDto().getId());

        if (group == null) {
            group = processGroup;
        }
        this.currentProcessGroup = group;

        if (processGroup.getParentProcessGroup() == null) {
            try {
                ProcessGroupDTO parent = fetchProcessGroupForNameAndIdentifier(processGroup.getDto().getParentGroupId());
                if (parent != null) {
                    group.setParentProcessGroup(parent);
                }
            } catch (NifiComponentNotFoundException e) {
                //cant find the parent
            }
        }

        group.accept(this);
        this.visitedProcessGroups.put(group.getDto().getId(), group);


    }


    public List<NifiVisitableProcessor> getDestinationProcessors(ConnectionDTO connection, boolean getSource) {
        ConnectableDTO dest = connection.getDestination();
        List<NifiVisitableProcessor> destinationProcessors = new ArrayList<>();
        if (dest != null) {

            if ("INPUT_PORT".equalsIgnoreCase(dest.getType())) {
                boolean isNew = false;
                NifiVisitableProcessGroup group = visitedProcessGroups.get(dest.getGroupId());
                if (group == null) {
                    group = fetchProcessGroup(dest.getGroupId());
                }
                ConnectionDTO conn = group.getConnectionMatchingSourceId(dest.getId());
                if (conn != null) {
                    destinationProcessors = getDestinationProcessors(conn, getSource);

                    if (getSource) {
                        List<NifiVisitableProcessor> outputProcessors = getSourceProcessors(connection);
                        if (outputProcessors != null) {
                            for (NifiVisitableProcessor outputProcessor : outputProcessors) {
                                //outputProcessor.setOutputPortId(dest.getId());
                                currentProcessGroup.addOutputPortProcessor(dest.getId(), outputProcessor);
                            }
                        }
                    }
                }


            }if ("REMOTE_INPUT_PORT".equalsIgnoreCase(dest.getType())) {
                boolean isNew = false;
                //treat this like a Processor for the connection graph
                NifiVisitableProcessor processorDto = getRemoteProcessGroupAsVisitableProcessor(dest.getGroupId(),connection.getParentGroupId());
                destinationProcessors.add(processorDto);

            }  else if ("OUTPUT_PORT".equals(dest.getType())) {
                boolean isNew = false;
                //get parent processgroup connection to input port
                NifiVisitableProcessGroup group = visitedProcessGroups.get(dest.getGroupId());
                if (group == null) {
                    group = fetchProcessGroup(dest.getGroupId());
                }
                ConnectionDTO conn = group.getConnectionMatchingSourceId(dest.getId());
                if (conn == null) {
                    conn = searchConnectionMatchingSource(group.getDto().getParentGroupId(), dest.getId());
                }
                if (conn != null) {
                    //get the processor whos source matches this connection Id
                    List<NifiVisitableProcessor> destinations = getDestinationProcessors(conn, getSource);
                    if (destinations != null) {
                        destinationProcessors.addAll(destinations);
                    }
                    if (getSource) {
                        List<NifiVisitableProcessor> outputProcessors = getSourceProcessors(connection);
                        if (outputProcessors != null) {
                            for (NifiVisitableProcessor outputProcessor : outputProcessors) {
                                currentProcessGroup.addOutputPortProcessor(dest.getId(), outputProcessor);
                            }
                        }
                    }
                }

            } else if ("FUNNEL".equals(dest.getType())) {
                List<ConnectionDTO>
                    passThroughConnections =
                    NifiConnectionUtil.findConnectionsMatchingSourceId(currentProcessGroup.getDto().getContents().getConnections(), connection.getDestination().getId());
                if (passThroughConnections != null) {
                    for (ConnectionDTO dto : passThroughConnections) {
                        ConnectionDTO newConnection = new ConnectionDTO();
                        newConnection.setId(connection.getSource().getId());
                        newConnection.setSource(connection.getSource());
                        newConnection.setDestination(dto.getDestination());
                        List<NifiVisitableProcessor> destinations = getDestinationProcessors(newConnection, getSource);
                        if (destinations != null) {
                            //  destinationProcessor.setOutputPortId(dest.getId());
                            destinationProcessors.addAll(destinations);
                        }
                    }
                }
            } else if ("PROCESSOR".equals(dest.getType())) {
                NifiVisitableProcessor destinationProcessor = getConnectionProcessor(dest.getGroupId(), dest.getId());
                destinationProcessors.add(destinationProcessor);
            }
        }
        for (NifiVisitableProcessor destinationProcessor : destinationProcessors) {
            destinationProcessor.addSourceConnectionIdentifier(connection);
        }
        return destinationProcessors;
    }


    public List<NifiVisitableProcessor> getSourceProcessors(ConnectionDTO connection) {

        ConnectableDTO source = connection.getSource();
        List<NifiVisitableProcessor> sourceProcessors = new ArrayList<>();
        if (source != null) {
            if ("INPUT_PORT".equalsIgnoreCase(source.getType())) {
                NifiVisitableProcessGroup group = visitedProcessGroups.get(source.getGroupId());
                if (group == null) {
                    group = processGroup;
                }
                NifiVisitableProcessGroup parent = visitedProcessGroups.get(group.getDto().getParentGroupId());
                //if the parent is null the parent is the starting process group
                if (parent == null) {
                    parent = processGroup;
                }

                ConnectionDTO conn = parent.getConnectionMatchingDestinationId(source.getId());
                if (conn == null) {
                    //if its null get it from the cache and process with that.
                    conn =
                        cache.getProcessGroupCache().values().stream().flatMap(g -> g.getContents().getConnections().stream())
                            .filter(connectionDTO -> connectionDTO.getDestination().getId().equalsIgnoreCase(source.getId())).findFirst().orElse(null);
                }
                if (conn != null && conn != connection) {
                    //get the processor whos source matches this connection Id
                    sourceProcessors = getSourceProcessors(conn);
                    //assign the inputPortProcessor == the the destination of this connection
                }
                List<NifiVisitableProcessor> inputProcessors = getDestinationProcessors(connection, false);
                if (inputProcessors != null) {
                    for (NifiVisitableProcessor inputProcessor : inputProcessors) {
                        currentProcessGroup.addInputPortProcessor(source.getId(), inputProcessor);
                    }
                }

            } else if ("OUTPUT_PORT".equals(source.getType())) {
                //get the sources group id then get the ending processor for that group
                NifiVisitableProcessGroup group = visitedProcessGroups.get(source.getGroupId());
                if (group != null) {
                    Set<NifiVisitableProcessor> sources = group.getOutputPortProcessors(source.getId());
                    if (sourceProcessors != null && sources != null) {
                        sourceProcessors.addAll(sources);
                    }
                  /*
                   If a process group is connected to another process group without any processors there will be no source processors
                    having sourceProcessors as null here is ok.
                    */
                }
            } else if ("FUNNEL".equalsIgnoreCase(source.getType())) {
                List<ConnectionDTO> passThroughConnections = NifiConnectionUtil.findConnectionsMatchingDestinationId(currentProcessGroup.getDto().getContents().getConnections(),
                                                                                                                     connection.getSource().getId());
                if (passThroughConnections != null) {
                    for (ConnectionDTO dto : passThroughConnections) {
                        ConnectionDTO newConnection = new ConnectionDTO();
                        newConnection.setSource(dto.getSource());
                        newConnection.setId(connection.getSource().getId());
                        newConnection.setDestination(connection.getDestination());
                        visitConnection(new NifiVisitableConnection(currentProcessGroup, newConnection));
                    }
                }

            } else if ("PROCESSOR".equals(source.getType())) {
                NifiVisitableProcessor sourceProcessor = getConnectionProcessor(source.getGroupId(), source.getId());
                sourceProcessors.add(sourceProcessor);
            }
            for (NifiVisitableProcessor sourceProcessor : sourceProcessors) {
                sourceProcessor.addDestinationConnectionIdentifier(connection);
            }
        }
        return sourceProcessors;
    }


    private ProcessGroupDTO fetchProcessGroupForNameAndIdentifier(String groupId) {
        //fetch it
        ProcessGroupDTO processGroupEntity = null;
        try {
            try {
                log.debug("fetchProcessGroup {} ", groupId);
                processGroupEntity = getGroup(groupId);
            } catch (NifiComponentNotFoundException e) {
                log.debug("Unable to find the process group " + groupId);
            }
        } catch (Exception e) {
            log.error("Exception fetching the process group " + groupId);
        }
        return processGroupEntity;
    }


    private NifiVisitableProcessGroup fetchProcessGroup(String groupId) {
        NifiVisitableProcessGroup group = processGroup;
        //fetch it
        ProcessGroupDTO processGroupEntity = null;
        try {
            try {
                log.debug("fetchProcessGroup {} ", groupId);
                processGroupEntity = getGroup(groupId);

            } catch (NifiComponentNotFoundException e) {
                log.debug("Unable to find the process group " + groupId);
            }
            //if the parent is null the parent is the starting process group
            if (processGroupEntity != null) {
                group = new NifiVisitableProcessGroup(processGroupEntity);
            }
        } catch (Exception e) {
            log.error("Exception fetching the process group " + groupId);
        }
        return group;
    }


    private ConnectionDTO searchConnectionMatchingSource(String parentGroupId, String destinationId) {
        //search up to find the connection that matches this dest id
        try {
            ProcessGroupDTO parent = null;
            try {
                log.debug("fetch ProcessGroup for searchConnectionMatchingSource {} ", parentGroupId);
                parent = getGroup(parentGroupId);
            } catch (NifiComponentNotFoundException e) {
                log.debug("Exception searching Connection matching the source. Parent Group ID: " + parentGroupId + ", and destinationId of  " + destinationId);
            }
            if (parent != null) {
                //processGroup.getDto().setParent(parentParent.getProcessGroup());
                //get Contents of this parent
                NifiVisitableProcessGroup visitableProcessGroup = new NifiVisitableProcessGroup(parent);
                ConnectionDTO conn = visitableProcessGroup.getConnectionMatchingSourceId(destinationId);
                if (conn != null) {
                    return conn;
                }
                if (conn == null && parent.getParentGroupId() != null) {
                    return searchConnectionMatchingSource(parent.getParentGroupId(), destinationId);
                }
            }

        } catch (Exception e) {
            log.error("Exception searching Connection matching the source.  Parent Group ID: " + parentGroupId + ", and destinationId of  " + destinationId);
        }
        return null;

    }

    private ConnectionDTO searchConnectionMatchingDestination(String parentGroupId, String sourceId) {
        //search up to find the connectoin that matches this dest id

        try {
            ProcessGroupDTO parent = null;
            try {
                parent = getGroup(parentGroupId);
            } catch (NifiComponentNotFoundException e) {
                log.debug("Exception searching Connection matching the destination. Parent Group ID: " + parentGroupId + ", and destinationId of  " + sourceId);
            }
            if (parent != null) {
                //get Contents of this parent
                NifiVisitableProcessGroup visitableProcessGroup = new NifiVisitableProcessGroup(parent);
                ConnectionDTO conn = visitableProcessGroup.getConnectionMatchingDestinationId(sourceId);
                if (conn != null) {
                    return conn;
                }
                if (conn == null && parent.getParentGroupId() != null) {
                    return searchConnectionMatchingSource(parent.getParentGroupId(), sourceId);
                }
            }

        } catch (Exception e) {
            log.error("Exception searching Connection matching the destination.  Parent Group ID: " + parentGroupId + ", and source of  " + sourceId);
        }
        return null;

    }


    private NifiVisitableProcessor getConnectionProcessor(String groupId, String id) {
        NifiVisitableProcessor processor = visitedProcessors.get(id);
        if (processor == null) {
            if (!this.processorsMap.containsKey(id)) {
                //if the current group is not related to this processgroup then attempt to walk this processors processgroup
                try {
                    log.debug("fetch ProcessGroup for getConnectionProcessor {} ", groupId);
                    ProcessGroupDTO processGroupEntity = getGroup(groupId);
                    if (processGroupEntity != null) {

                        ProcessorDTO processorDTO = NifiProcessUtil.findFirstProcessorsById(
                            processGroupEntity.getContents().getProcessors(), id);
                        if (processorDTO != null) {
                            this.processorsMap.put(id, processorDTO);
                        }
                        if (processGroup.getDto().getId() != groupId && !visitedProcessGroups.containsKey(processGroupEntity.getId())) {
                            visitProcessGroup(new NifiVisitableProcessGroup(processGroupEntity));
                        }
                    } else {
                        log.error("getConnectionProcessor error.  Unable to find Process Group for process group id: {}, and processor: {} {} ", groupId, id);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            //
            processor = visitedProcessors.get(id);
            if (processor == null) {
                processor = new NifiVisitableProcessor(this.processorsMap.get(id));
                //visit the group?
                processor.accept(this);
            }


        }
        return processor;
    }


    public NifiVisitableProcessGroup getProcessGroup() {
        return processGroup;
    }

    public Map<String, ProcessorDTO> getProcessorsMap() {
        return processorsMap;
    }

    public Map<String, NifiVisitableProcessor> getVisitedProcessors() {
        return visitedProcessors;
    }

    public Map<String, NifiVisitableProcessGroup> getVisitedProcessGroups() {
        return visitedProcessGroups;
    }

    public Set<NifiVisitableConnection> getAllConnections() {
        return allConnections;
    }

    public NifiConnectionOrderVisitorCachedItem toCachedItem() {
        return new NifiConnectionOrderVisitorCachedItem(this);
    }


    public Integer getNumberOfSplits() {
        int count = 0;
        for (NifiVisitableProcessor processor : visitedProcessors.values()) {
            Set<NifiVisitableProcessor> destinations = processor.getDestinations();
            if (destinations != null && !destinations.isEmpty()) {
                count += (destinations.size() - 1);
            }
        }
        return count;
    }

    /**
     * inspect the current status and determine if it has data in queue
     */
    public boolean isProcessingData() {
        return false;
    }

    public void printOrder() {
        for (NifiVisitableProcessor processor : processGroup.getStartingProcessors()) {
            processor.print();
        }
    }

    private ProcessGroupDTO getGroup(String processGroupId) {
        ProcessGroupDTO group = null;
        if (cache.getProcessGroup(processGroupId).isPresent()) {
            group = cache.getProcessGroup(processGroupId).get();
        } else {
            group = getRestClient().processGroups().findById(processGroupId, true, true).orElse(null);
            if (group != null) {
                cache.add(group);
                NifiProcessUtil.getProcessGroups(group).stream().forEach(processGroupDTO -> cache.add(processGroupDTO));
            }
        }
        return group;
    }

    private RemoteProcessGroupDTO getRemoteGroup(String processGroupId) {
        RemoteProcessGroupDTO group = null;
        if (cache.getRemoteProcessGroup(processGroupId).isPresent()) {
            group = cache.getRemoteProcessGroup(processGroupId).get();
        } else {
            group = getRestClient().remoteProcessGroups().findById(processGroupId).orElse(null);
            if (group != null) {
                cache.add(group);
            }
        }
        return group;
    }

    private NifiVisitableProcessor getRemoteProcessGroupAsVisitableProcessor(String processGroupId, String parentGroupId) {

        NifiVisitableProcessor processor = visitedProcessors.get(processGroupId);
        if(processor == null) {
            ProcessorDTO processorDTO = processorsMap.get(processGroupId);
            if (processorDTO == null) {
                RemoteProcessGroupDTO remoteProcessGroupDTO = getRemoteGroup(processGroupId);
                if (remoteProcessGroupDTO != null) {
                    processorDTO = new ProcessorDTO();
                    processorDTO.setType("NiFi.RemoteProcessGroup");
                    processorDTO.setId(processGroupId);
                    processorDTO.setParentGroupId(parentGroupId);
                    processorDTO.setName(remoteProcessGroupDTO.getName());
                    processorDTO.setConfig(new ProcessorConfigDTO());
                    processorsMap.put(processGroupId, processorDTO);
                }
            }
            if(processorDTO != null){
                processor = new NifiVisitableProcessor(processorDTO);
            }
        }
       return processor;
    }

    private NiFiRestClient getRestClient() {
        return restClient;
    }
}
