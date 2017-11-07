package com.thinkbiganalytics.nifi.rest.model.visitor;

/*-
 * #%L
 * thinkbig-nifi-flow-visitor-model
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
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the NiFi Process Group that can be visited and linked building a graph of connecting parents and children {@link NifiVisitableProcessor}
 * These are built from the {@link NifiFlowVisitor}
 */
public class NifiVisitableProcessGroup implements NifiVisitable {

    /**
     * all the processors visited in this group
     */
    private Set<NifiVisitableProcessor> processors;
    /**
     * the Nifi process group
     */
    private ProcessGroupDTO dto;
    /**
     * The parent process group for which this process group resides
     */
    private ProcessGroupDTO parentProcessGroup;
    /**
     * the starting processors in this group which dont have any source connections in front of them
     */
    private Set<NifiVisitableProcessor> startingProcessors;
    /**
     * the ending processors in this group that dont have any destination connections from them
     */
    private Set<NifiVisitableProcessor> endingProcessors;
    /**
     * Map of the output port id to set of connecting processor ids
     */
    private Map<String, Set<NifiVisitableProcessor>> outputPortProcessors;
    /**
     * Map of the input port id to set of connecting processor ids
     */
    private Map<String, Set<NifiVisitableProcessor>> inputPortProcessors;

    private Set<ConnectionDTO> connections;

    /**
     * Create a new visitable group from a given NiFi process group
     *
     * @param dto a NiFi process group
     */
    public NifiVisitableProcessGroup(ProcessGroupDTO dto) {
        this.dto = dto;
        startingProcessors = new HashSet<>();
        endingProcessors = new HashSet<>();
        outputPortProcessors = new HashMap<>();
        inputPortProcessors = new HashMap<>();
        processors = new HashSet<>();
        connections = new HashSet<>();
        if (dto.getContents() != null) {
            this.connections = dto.getContents().getConnections();
        }

    }


    /**
     * Return a visitable processor, first looking at those that have been visited already by this process group, if not it will create a new one.
     *
     * @param nifiVisitor  the visitor
     * @param processorDTO the processor to find
     * @return a visitable processor, wrapping the incoming processordto, to process or link to as a source/destination
     */
    private NifiVisitableProcessor getOrCreateProcessor(NifiFlowVisitor nifiVisitor, ProcessorDTO processorDTO) {
        NifiVisitableProcessor processor = nifiVisitor.getProcessor(processorDTO.getId());
        if (processor == null) {
            processor = new NifiVisitableProcessor(processorDTO);
            addProcessor(processor);
        }
        return processor;
    }

    /**
     * Return a visitable process group first looking at those that have been visited already by this parent process group, if not it will create a new one.
     *
     * @param nifiVisitor     the visitor
     * @param processGroupDTO the group to find
     * @return the visitable process group wrapping the incoming processgroup
     */
    private NifiVisitableProcessGroup getOrCreateProcessGroup(NifiFlowVisitor nifiVisitor, ProcessGroupDTO processGroupDTO) {
        NifiVisitableProcessGroup group = nifiVisitor.getProcessGroup(processGroupDTO.getId());
        if (group == null) {
            group = new NifiVisitableProcessGroup(processGroupDTO);
        }
        return group;
    }

    /**
     * Return the parent process group for this group
     *
     * @return the parent process group for this group
     */
    public ProcessGroupDTO getParentProcessGroup() {
        return parentProcessGroup;
    }

    /**
     * set the parent process group
     *
     * @param parentProcessGroup the group that is the parent group to this group
     */
    public void setParentProcessGroup(ProcessGroupDTO parentProcessGroup) {
        this.parentProcessGroup = parentProcessGroup;
    }

    /**
     * Visit this process group using the supplied visitor
     *
     * @param nifiVisitor the visitor
     */
    @Override
    public void accept(NifiFlowVisitor nifiVisitor) {
        if (dto.getContents() != null) {

            if (dto.getContents().getProcessors() != null) {
                for (ProcessorDTO processorDTO : dto.getContents().getProcessors()) {
                    NifiVisitableProcessor processor = getOrCreateProcessor(nifiVisitor, processorDTO);
                    nifiVisitor.visitProcessor(processor);
                }
            }

            if (dto.getContents().getProcessGroups() != null) {
                for (ProcessGroupDTO processGroupDTO : dto.getContents().getProcessGroups()) {
                    if (processGroupDTO != null) {
                        nifiVisitor.visitProcessGroup(getOrCreateProcessGroup(nifiVisitor, processGroupDTO));
                    }
                }
            }

            if (dto.getContents().getConnections() != null) {
                for (ConnectionDTO connectionDTO : dto.getContents().getConnections()) {
                    nifiVisitor.visitConnection(new NifiVisitableConnection(this, connectionDTO));
                }
            }

            populateStartingAndEndingProcessors();
        }

    }


    /**
     * Return the  NiFi ProcessGroup that this class wraps
     *
     * @return the NiFi process group item
     */
    public ProcessGroupDTO getDto() {
        return dto;
    }


    /**
     * After visiting a processor, add it to the cache of processors referenced by this process group
     *
     * @param processor the processor that has been visited
     */
    public void addProcessor(NifiVisitableProcessor processor) {
        processors.add(processor);
    }

    /**
     * Return a set of all the processors that dont have any incoming source connections
     *
     * @return a set of all the processors that dont have any incoming source connections
     */
    public Set<NifiVisitableProcessor> getStartingProcessors() {
        return startingProcessors;
    }

    /**
     * Return a set of all the ending/leaf processors that dont have destination connections coming from them
     *
     * @return a set of all the ending/leaf processors that dont have destination connections coming from them
     */
    public Set<NifiVisitableProcessor> getEndingProcessors() {
        return endingProcessors;
    }


    /**
     * Return the connection, if any, matching the incoming source connectionSourceId
     *
     * @param connectionSourceId the unique id for the connection source to find
     * @return a connection matching the incoming connectionSourceId, or Null if not found
     */
    public ConnectionDTO getConnectionMatchingSourceId(final String connectionSourceId) {
        if (connections != null) {
            return Iterables.tryFind(connections, new Predicate<ConnectionDTO>() {
                @Override
                public boolean apply(ConnectionDTO connection) {
                    return connection.getSource() != null && connection.getSource().getId().equals(connectionSourceId);
                }
            }).orNull();
        }
        return null;
    }

    /**
     * Return the connection, if any, matching the incoming destination connectionDestinationId
     *
     * @param connectionDestinationId the unique id for the connection destination to find
     * @return a connection matching the incoming connectionDestinationId, or Null if not found
     */
    public ConnectionDTO getConnectionMatchingDestinationId(final String connectionDestinationId) {
        if (connections != null) {
            return Iterables.tryFind(connections, new Predicate<ConnectionDTO>() {
                @Override
                public boolean apply(ConnectionDTO connection) {
                    return connection.getDestination() != null && connection.getDestination().getId().equals(connectionDestinationId);
                }
            }).orNull();
        }
        return null;
    }

    /**
     * populate the processors source/sink processors
     */
    private void populateStartingAndEndingProcessors() {

        for (NifiVisitableProcessor processor : processors) {
            if (processor.isStart()) {
                startingProcessors.add(processor);
            }
            if (processor.isEnd()) {
                endingProcessors.add(processor);
            }
        }

    }

    /**
     * Return the processors that are attached to input ports
     *
     * @return processors attaached to a specific input port
     */
    public Set<NifiVisitableProcessor> getInputPortProcessors(String inputPortId) {
        return inputPortProcessors.get(inputPortId);
    }

    /**
     * add the input port and processor to the map
     *
     * @param id                 the id of the input port
     * @param inputPortProcessor the processor connected to the input port
     */
    public void addInputPortProcessor(String id, NifiVisitableProcessor inputPortProcessor) {
        if (!this.inputPortProcessors.containsKey(id)) {
            this.inputPortProcessors.put(id, new HashSet<>());
        }
        this.inputPortProcessors.get(id).add(inputPortProcessor);
    }

    /**
     * Return the set of output port processors
     *
     * @param outputPortId the output port id
     * @return the processors connected to the supplied output port
     */
    public Set<NifiVisitableProcessor> getOutputPortProcessors(String outputPortId) {
        return outputPortProcessors.get(outputPortId);
    }

    /**
     * Return the processor connected to the supplied output port
     *
     * @return the processor
     */
    public NifiVisitableProcessor getOutputPortProcessor(String connectionSourceId) {
        if (outputPortProcessors.containsKey(connectionSourceId)) {
            return Lists.newArrayList(outputPortProcessors.get(connectionSourceId)).get(0);
        }
        return null;
    }

    /**
     * Add a processor connected to the supplied output port id
     *
     * @param id                  the output port id
     * @param outputPortProcessor the processor attached to this output port
     */
    public void addOutputPortProcessor(String id, NifiVisitableProcessor outputPortProcessor) {
        if (!this.outputPortProcessors.containsKey(id)) {
            this.outputPortProcessors.put(id, new HashSet<>());
        }
        this.outputPortProcessors.get(id).add(outputPortProcessor);
    }

    /**
     * Return the map of input port id to processors
     *
     * @return the map of input port id to processors
     */
    public Map<String, Set<NifiVisitableProcessor>> getInputPortProcessors() {
        return inputPortProcessors;
    }

    /**
     * Return the map of output port id to processors
     *
     * @return the map of output port id to processors
     */
    public Map<String, Set<NifiVisitableProcessor>> getOutputPortProcessors() {
        return outputPortProcessors;
    }


    /**
     * Return all the visited processors
     *
     * @return all the visited processors
     */
    public Set<NifiVisitableProcessor> getProcessors() {
        return processors;
    }

    /**
     * Return all the all connections under this process group
     *
     * @return all the all connections under this process group
     */
    public Set<ConnectionDTO> getConnections() {
        return connections;
    }
}
