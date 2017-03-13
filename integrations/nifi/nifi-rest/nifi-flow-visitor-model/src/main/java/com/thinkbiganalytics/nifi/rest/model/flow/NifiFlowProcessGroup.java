package com.thinkbiganalytics.nifi.rest.model.flow;

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


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * the graph starting with the Set of startingProcessors, or lookup into the graph using hte processorMap and then traverse it back and forth by a specific processor.
 *
 * When walking the flow the system removes the internal Processgroups and connects the processors together.
 *
 * If Deserialized from JSON you will need to call the NifiFlowDeserializer to construct the full graph
 *
 * @see NifiFlowDeserializer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NifiFlowProcessGroup {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowProcessGroup.class);

    private String id;
    private String name;

    private String parentGroupId;
    private String parentGroupName;
    private String feedName;


    private Map<String, NifiFlowProcessor> processorMap;

    private Map<String, NifiFlowProcessor> endingProcessors;

    /**
     * Connection Identifier to Connection object
     */
    private Map<String, NifiFlowConnection> connectionIdMap;

    private Set<NifiFlowProcessor> startingProcessors;

    public NifiFlowProcessGroup() {

    }

    public NifiFlowProcessGroup(String id, String name) {

        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NifiFlowProcessor getProcessor(String processorId) {
        return getProcessorMap().get(processorId);
    }

    public Set<NifiFlowProcessor> getStartingProcessors() {
        if (startingProcessors == null) {
            startingProcessors = new HashSet<>();
        }
        return startingProcessors;
    }

    public void setStartingProcessors(Set<NifiFlowProcessor> startingProcessors) {
        this.startingProcessors = startingProcessors;
    }

    public Map<String, NifiFlowProcessor> getEndingProcessors() {
        if (endingProcessors == null) {
            endingProcessors = new HashMap<>();
        }
        return endingProcessors;
    }

    public void setEndingProcessors(Map<String, NifiFlowProcessor> endingProcessors) {
        this.endingProcessors = endingProcessors;
    }

    public void print() {
        for (NifiFlowProcessor processor : getStartingProcessors()) {
            processor.print();
        }
    }


    public List<NifiFlowProcessor> getSortedStartingProcessors() {
        List<NifiFlowProcessor> startingProcessors = Lists.newArrayList(getStartingProcessors());
        Collections.sort(startingProcessors, new NifiFlowProcessor.FlowIdComparator());
        return startingProcessors;
    }


    public void assignFlowIds() {
        Integer flowId = 0;
        for (NifiFlowProcessor processor : getSortedStartingProcessors()) {
            flowId = processor.assignFlowIds(flowId);
        }
    }

    public Map<String, NifiFlowProcessor> getProcessorMap() {
        if (processorMap == null) {
            processorMap = new HashMap<>();
        }
        return processorMap;
    }

    public void setProcessorMap(Map<String, NifiFlowProcessor> processorMap) {
        this.processorMap = processorMap;

        this.endingProcessors =
            processorMap.values().stream().filter(simpleNifiFlowProcessor -> simpleNifiFlowProcessor.isEnd()).collect(Collectors.toMap(processor -> processor.getId(), processor -> processor));

        processorMap.values().forEach(processor -> processor.setProcessGroup(this));


    }


    public String getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(String parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public String getParentGroupName() {
        return parentGroupName;
    }

    public void setParentGroupName(String parentGroupName) {
        this.parentGroupName = parentGroupName;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }


    public Map<String, NifiFlowConnection> getConnectionIdMap() {
        if (connectionIdMap == null) {
            connectionIdMap = new HashMap<>();
        }
        return connectionIdMap;
    }

    public void setConnectionIdMap(Map<String, NifiFlowConnection> connectionIdMap) {
        this.connectionIdMap = connectionIdMap;
    }

    public void addConnection(ConnectionDTO connectionDTO) {
        NifiFlowConnection nifiFlowConnection = NiFiFlowConnectionConverter.toNiFiFlowConnection(connectionDTO);
        if (nifiFlowConnection != null) {
            getConnectionIdMap().put(connectionDTO.getId(), nifiFlowConnection);
        }
    }

    public void addConnections(Collection<ConnectionDTO> connectionDTOs) {
        if (connectionDTOs != null) {
            connectionDTOs.stream().forEach(connectionDTO -> addConnection(connectionDTO));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NifiFlowProcessGroup group = (NifiFlowProcessGroup) o;
        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NifiFlowProcessGroup{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", parentGroupId='").append(parentGroupId).append('\'');
        sb.append(", parentGroupName='").append(parentGroupName).append('\'');
        sb.append(", feedName='").append(feedName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
