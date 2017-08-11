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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Object in a NifiFlow that has pointers to all its sources (parents)  and children (destinations)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NifiFlowProcessor implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowProcessor.class);

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("type")
    private String type;

    private String parentGroupId;

    private boolean isFailure;
    private boolean isEnd;

    private String flowId;

    private NifiFlowProcessGroup processGroup;

    @JsonIgnore
    private Set<NifiFlowProcessor> sources; //parents

    @JsonIgnore
    private Set<NifiFlowProcessor> destinations; //children

    private Set<String> sourceIds;

    private Set<String> destinationIds;

    private Set<NiFiFlowProcessorConnection> sourceConnectionIds;

    private Set<NiFiFlowProcessorConnection> destinationConnectionIds;


    public NifiFlowProcessor() {

    }


    public NifiFlowProcessor(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("type") String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    /**
     * Return the processor id
     *
     * @return the processor id
     */
    public String getId() {
        return id;
    }

    /**
     * set the processor id
     *
     * @param id the processor id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Return the processor name
     *
     * @return the processor name
     */
    public String getName() {
        return name;
    }

    /**
     * set the processor name
     *
     * @param name the processor name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the set of destinations coming from this processor
     *
     * @return the set of destination processors
     */
    public Set<NifiFlowProcessor> getDestinations() {
        if (destinations == null) {
            destinations = new HashSet<>();
        }
        return destinations;
    }

    /**
     * set the destination processors
     *
     * @param destinations the destination processors
     */
    public void setDestinations(Set<NifiFlowProcessor> destinations) {
        this.destinations = destinations;
    }

    /**
     * Return the source processors
     *
     * @return the source processors
     */
    public Set<NifiFlowProcessor> getSources() {
        if (sources == null) {
            sources = new HashSet<>();
        }
        return sources;
    }

    /**
     * set the source processors
     *
     * @param sources the source processors
     */
    public void setSources(Set<NifiFlowProcessor> sources) {
        this.sources = sources;
    }

    /**
     * Return the group associated with this processor
     *
     * @return the group
     */
    public NifiFlowProcessGroup getProcessGroup() {
        return processGroup;
    }

    /**
     * Set the group associated with this processor
     *
     * @param processGroup the group
     */
    public void setProcessGroup(NifiFlowProcessGroup processGroup) {
        this.processGroup = processGroup;
    }


    /**
     * Return the set of ids that are incoming source processors
     *
     * @return the set of ids that are incoming source processors
     */
    public Set<String> getSourceIds() {
        if (sourceIds == null) {
            sourceIds = new HashSet<>();
        }
        return sourceIds;
    }

    /**
     * set the set of ids that are incoming source processors
     *
     * @param sourceIds the set of ids that are incoming source processors
     */
    public void setSourceIds(Set<String> sourceIds) {
        this.sourceIds = sourceIds;
    }

    /**
     * Return the set of ids that are destination processors
     *
     * @return the set of ids that are destination processors
     */
    public Set<String> getDestinationIds() {
        if (destinationIds == null) {
            destinationIds = new HashSet<>();
        }
        return destinationIds;
    }

    /**
     * set  the set of ids that are destination processors
     *
     * @param destinationIds the set of ids that are destination processors
     */
    public void setDestinationIds(Set<String> destinationIds) {
        this.destinationIds = destinationIds;
    }

    /**
     * Return the set of this processors destination ids, and all of its children destination ids
     *
     * @return the set of this processors destination ids, and all of its children destination ids
     */
    public Set<String> getAllDestinationIds() {
        Set<String> destinationIds = new HashSet<>();
        destinationIds.addAll(getDestinationIds());
        for (NifiFlowProcessor destination : getDestinations()) {
            destinationIds.addAll(destination.getAllDestinationIds());
        }
        return destinationIds;

    }

    /**
     * Return the set of connection identifiers for the incoming  connections
     *
     * @return the set of connection identifiers for the incoming  connections
     */
    public Set<NiFiFlowProcessorConnection> getSourceConnectionIds() {
        if (sourceConnectionIds == null) {
            sourceConnectionIds = new HashSet<>();
        }
        return sourceConnectionIds;
    }

    /**
     * set the set of connection identifiers for the incoming  connections
     *
     * @param sourceConnectionIds the set of connection identifiers for the incoming  connections
     */
    public void setSourceConnectionIds(Set<NiFiFlowProcessorConnection> sourceConnectionIds) {
        this.sourceConnectionIds = sourceConnectionIds;
    }

    /**
     * Return the set of outgoing destination connection ids
     *
     * @return the set of outgoing destination connection ids
     */
    public Set<NiFiFlowProcessorConnection> getDestinationConnectionIds() {
        if (destinationConnectionIds == null) {
            destinationConnectionIds = new HashSet<>();
        }
        return destinationConnectionIds;
    }

    /**
     * set the set of outgoing destination connection ids
     *
     * @param destinationConnectionIds the set of outgoing destination connection ids
     */
    public void setDestinationConnectionIds(Set<NiFiFlowProcessorConnection> destinationConnectionIds) {
        this.destinationConnectionIds = destinationConnectionIds;
    }

    /**
     * prints the flow id for the processor and its destinations
     */
    public void print() {

        print(0, null);
    }

    /**
     * prints the flow id for the processor and its destinations
     */
    public void print(Integer level, Set<String> printed) {

        log.info(flowId + ", " + level + ". " + getName());
        System.out.println(level + ". " + getName());
        printed = printed == null ? new HashSet<>() : printed;
        printed.add(this.getId());
        Integer nextLevel = level + 1;

        for (NifiFlowProcessor child : getDestinations()) {
            if (!child.containsDestination(this) && !child.containsDestination(child) && !child.equals(this) && !printed.contains(child.getId())) {
                child.print(nextLevel, printed);
                printed.add(child.getId());
            }
        }
    }

    /**
     * Sort the destination processors so the flow id generation will be the same each time a similar flow of the same template is walked
     *
     * @return the sorted destinations
     */
    public List<NifiFlowProcessor> getSortedDestinations() {
        if (destinations == null) {
            destinations = new HashSet<>();
        }
        List<NifiFlowProcessor> list = Lists.newArrayList(destinations);
        Collections.sort(list, new NifiFlowProcessor.FlowIdComparator());
        return list;
    }

    /**
     * Assign a flow identifier to the processor. Flow ids are an attempt to assign a id relative the the location of the processor in the graph as it is walked so different instances of the same
     * flow/template can relate given processors to each other
     *
     * @param flowId the id representing its placement in the graph
     * @return the assigned numeric id
     */
    public Integer assignFlowIds(Integer flowId) {
        flowId++;
        setFlowId(flowId + "__" + StringUtils.substringAfterLast(this.type, "."));
        Set<String> printed = new HashSet<>();
        printed.add(this.getId());

        for (NifiFlowProcessor child : getSortedDestinations()) {
            if (StringUtils.isBlank(child.getFlowId()) && !child.containsDestination(this) && !child.containsDestination(child) && !child.equals(this) && !printed.contains(child.getId())) {
                flowId = child.assignFlowIds(flowId);
                printed.add(child.getId());
            }
        }
        return flowId;
    }

    /**
     * Count the number of distinct destination processor nodes
     *
     * @return the number of distinct destination processor nodes
     */
    public Integer countNodes() {
        return countNodes(null);
    }

    /**
     * Count the number of distinct destination processor nodes
     *
     * @param printed a set containing those nodes that were already printed
     * @return the number of distinct destination processor nodes
     */
    public Integer countNodes(Set<String> printed) {
        Integer count = getDestinations().size();
        printed = printed == null ? new HashSet<>() : printed;
        printed.add(this.getId());
        for (NifiFlowProcessor child : getDestinations()) {
            if (!child.containsDestination(this) && !child.containsDestination(child) && !child.equals(this) && !printed
                .contains(child.getId())) {
                count += child.countNodes(printed);
                printed.add(child.getId());
            }
        }
        return count;
    }

    /**
     * Check to see if the supplied processor is part of this processors graph
     *
     * @param parent a processor
     * @return {@code true} if the processor is already part of this destination set, {@code false} if the processor is not part of the destination set
     */
    public boolean containsDestination(NifiFlowProcessor parent) {
        final String thisId = getId();
        final String parentId = parent.getId();

        return getDestinations().stream().anyMatch(processor -> processor.getId().equalsIgnoreCase(thisId) || processor.getId()
            .equalsIgnoreCase(parentId));
    }

    /**
     * Return the unique flow id for this processor
     *
     * @return the flow if
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * set the flow id for the processor
     *
     * @param flowId the flow id
     */
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }


    /**
     * Return the type of processor
     *
     * @return the type of processor
     */
    public String getType() {
        return type;
    }

    /**
     * set the type of processor
     *
     * @param type the type of processor
     */
    public void setType(String type) {
        this.type = type;
    }


    /**
     * check if there are any destinations
     *
     * @return {@code true} if this is a ending processor with no more connections, {@code false} if there are other processors connected to this
     */
    public boolean isLeaf() {
        return getDestinationIds().isEmpty();
    }

    /**
     * check if there are any sources
     *
     * @return {@code true} if this is a starting processor with no incoming connections, {@code false} if it is connected to other incoming processors
     */
    public boolean isStart() {
        return getSourceIds().isEmpty();
    }

    /**
     * check to see if its a leaf processor.  same check as the {@link #isLeaf()}, but supplied when building the object in the {@link
     * com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowBuilder}
     */
    public boolean isEnd() {
        return isEnd;
    }

    /**
     * set the processor as being a leaf/ending processor,  supplied when building the object in the {@link com.thinkbiganalytics.nifi.rest.model.visitor.NifiFlowBuilder}
     */
    public void setIsEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }


    /**
     * return the process group id for this processor
     *
     * @return the process group id
     */
    public String getParentGroupId() {
        return parentGroupId;
    }

    /**
     * set the process group id
     *
     * @param parentGroupId the process group id
     */
    public void setParentGroupId(String parentGroupId) {
        this.parentGroupId = parentGroupId;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NifiFlowProcessor that = (NifiFlowProcessor) o;
        return Objects.equals(id, that.id);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NifiFlowProcessor{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }


    public static class FlowIdComparator implements Comparator<NifiFlowProcessor> {

        @Override
        public int compare(NifiFlowProcessor o1, NifiFlowProcessor o2) {
            int compare = 0;

            if (o1 == null && o2 == null) {
                compare = 0;
            } else if (o1 == null && o2 != null) {
                compare = -1;
            } else if (o1 != null && o2 == null) {
                compare = 1;
            } else if (o1.getName() != null && o2.getName() != null) {
                compare = o1.getName().compareTo(o2.getName());
            }

            if (compare == 0 && o1.getType() != null && o2.getType() != null) {
                compare = o1.getType().compareTo(o2.getType());
            }
            return compare;
        }
    }


}
