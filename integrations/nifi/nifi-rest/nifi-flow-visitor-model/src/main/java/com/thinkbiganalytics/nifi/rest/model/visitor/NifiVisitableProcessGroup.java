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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 2/14/16.
 */
public class NifiVisitableProcessGroup implements NifiVisitable {

    private ProcessGroupDTO dto;

    private ProcessGroupDTO parentProcessGroup;


    private Set<NifiVisitableProcessor> startingProcessors;

    private Set<NifiVisitableProcessor> endingProcessors;

    public Set<NifiVisitableProcessor> processors;


    private Map<String, Set<NifiVisitableProcessor>> outputPortProcessors;
    private Map<String, Set<NifiVisitableProcessor>> inputPortProcessors;

    private Set<ConnectionDTO> connections;

    /**
     * Cached map of the ConnectionId to a list of all processors that this connection is coming from of which the connection is indicated as being a "failure"
     *
     * used for lookups to determine if an event has failed or not
     */
    private Map<String, Set<String>> failureConnectionIdToSourceProcessorIds;

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

    private NifiVisitableProcessGroup(NifiVisitableProcessGroup group) {
        this.dto = group.getDto();
        Map<String, NifiVisitableProcessor> processorMap = group.getProcessors().stream()
            .map(NifiVisitableProcessor::asCacheableProcessor)
            .collect(Collectors.toMap(processor1 -> processor1.getDto().getId(), Function.identity()));
        this.processors = new HashSet<>(processorMap.values());
        this.startingProcessors = group.getStartingProcessors().stream().map(processor -> processorMap.get(processor.getId())).collect(Collectors.toSet());
        this.endingProcessors = group.getEndingProcessors().stream().map(processor -> processorMap.get(processor.getId())).collect(Collectors.toSet());

        this.inputPortProcessors = transform(group.getInputPortProcessors(), set -> set.stream().map(p -> processorMap.get(p.getId())).collect(Collectors.toSet()));

        this.outputPortProcessors = transform(group.getOutputPortProcessors(), set -> set.stream().map(p -> processorMap.get(p.getId())).collect(Collectors.toSet()));

        this.connections = new HashSet<>(group.getConnections());

    }

    public NifiVisitableProcessGroup asCacheableProcessGroup() {
        return new NifiVisitableProcessGroup(this);
    }

    static <X, Y, Z> Map<X, Z> transform(Map<? extends X, ? extends Y> input,
                                         Function<Y, Z> function) {
        return input.keySet().stream()
            .collect(Collectors.toMap(Function.identity(),
                                      key -> function.apply(input.get(key))));
    }

    public void clearConnectedProcessors() {
        processors.stream().forEach(processor -> processor.clearConnectedProcessors());
    }


    private NifiVisitableProcessor getOrCreateProcessor(NifiFlowVisitor nifiVisitor, ProcessorDTO processorDTO) {
        NifiVisitableProcessor processor = nifiVisitor.getProcessor(processorDTO.getId());
        if (processor == null) {
            processor = new NifiVisitableProcessor(processorDTO);
            addProcessor(processor);
        }
        return processor;
    }

    private NifiVisitableProcessGroup getOrCreateProcessGroup(NifiFlowVisitor nifiVisitor, ProcessGroupDTO processGroupDTO) {
        NifiVisitableProcessGroup group = nifiVisitor.getProcessGroup(processGroupDTO.getId());
        if (group == null) {
            group = new NifiVisitableProcessGroup(processGroupDTO);
        }
        return group;
    }

    public ProcessGroupDTO getParentProcessGroup() {
        return parentProcessGroup;
    }

    public void setParentProcessGroup(ProcessGroupDTO parentProcessGroup) {
        this.parentProcessGroup = parentProcessGroup;
    }

    @Override
    public void accept(NifiFlowVisitor nifiVisitor) {
        if (dto.getContents() != null) {
            //GET DATA IN THIS ORDER
            //1. Get Processor Info
            //2. get Process Group info
            //3. get Connections and make relationships between processors

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
        setFailureConnectionIdToSourceProcessorIds(nifiVisitor.getFailureConnectionIdToSourceProcessorIds());

    }


    public ProcessGroupDTO getDto() {
        return dto;
    }


    public void addProcessor(NifiVisitableProcessor processor) {
        processors.add(processor);
    }

    public Set<NifiVisitableProcessor> getStartingProcessors() {
        return startingProcessors;
    }

    public Set<NifiVisitableProcessor> getEndingProcessors() {
        return endingProcessors;
    }

    public NifiVisitableProcessor getProcessorMatchingId(final String processorId) {
        return Iterables.tryFind(processors, new Predicate<NifiVisitableProcessor>() {
            @Override
            public boolean apply(NifiVisitableProcessor processor) {
                return processor.getDto().getId().equals(processorId);
            }
        }).orNull();
    }

    public boolean containsProcessor(String processorId) {
        return getProcessorMatchingId(processorId) != null;
    }

    public ConnectionDTO getConnectionMatchingSourceId(final String connectionId) {
        if (connections != null) {
            return Iterables.tryFind(connections, new Predicate<ConnectionDTO>() {
                @Override
                public boolean apply(ConnectionDTO connection) {
                    return connection.getSource() != null && connection.getSource().getId().equals(connectionId);
                }
            }).orNull();
        }
        return null;
    }

    public ConnectionDTO getConnectionMatchingDestinationId(final String connectionId) {
        if (connections != null) {
            return Iterables.tryFind(connections, new Predicate<ConnectionDTO>() {
                @Override
                public boolean apply(ConnectionDTO connection) {
                    return connection.getDestination() != null && connection.getDestination().getId().equals(connectionId);
                }
            }).orNull();
        }
        return null;
    }


    public ConnectionDTO getConnectionMatchingId(final String connectionId) {
        if (connections != null) {
            return Iterables.tryFind(connections, new Predicate<ConnectionDTO>() {
                @Override
                public boolean apply(ConnectionDTO connection) {
                    return connection.getId().equals(connectionId);
                }
            }).orNull();
        }
        return null;
    }

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


    public Set<NifiVisitableProcessor> getInputPortProcessors(String inputProcessorId) {
        return inputPortProcessors.get(inputProcessorId);
    }

    public void addInputPortProcessor(String id, NifiVisitableProcessor inputPortProcessor) {
        if (!this.inputPortProcessors.containsKey(id)) {
            this.inputPortProcessors.put(id, new HashSet<>());
        }
        this.inputPortProcessors.get(id).add(inputPortProcessor);
    }

    public Set<NifiVisitableProcessor> getOutputPortProcessors(String connectionSourceId) {
        return outputPortProcessors.get(connectionSourceId);
    }

    public NifiVisitableProcessor getOutputPortProcessor(String connectionSourceId) {
        if (outputPortProcessors.containsKey(connectionSourceId)) {
            return Lists.newArrayList(outputPortProcessors.get(connectionSourceId)).get(0);
        }
        return null;
    }

    public void addOutputPortProcessor(String id, NifiVisitableProcessor outputPortProcessor) {
        if (!this.outputPortProcessors.containsKey(id)) {
            this.outputPortProcessors.put(id, new HashSet<>());
        }
        this.outputPortProcessors.get(id).add(outputPortProcessor);
    }

    public Map<String, Set<String>> getFailureConnectionIdToSourceProcessorIds() {
        return failureConnectionIdToSourceProcessorIds;
    }

    public void setFailureConnectionIdToSourceProcessorIds(Map<String, Set<String>> failureConnectionIdToSourceProcessorIds) {
        this.failureConnectionIdToSourceProcessorIds = failureConnectionIdToSourceProcessorIds;
    }


    public Map<String, Set<NifiVisitableProcessor>> getInputPortProcessors() {
        return inputPortProcessors;
    }

    public Map<String, Set<NifiVisitableProcessor>> getOutputPortProcessors() {
        return outputPortProcessors;
    }


    public Set<NifiVisitableProcessor> getProcessors() {
        return processors;
    }

    public Set<ConnectionDTO> getConnections() {
        return connections;
    }
}
