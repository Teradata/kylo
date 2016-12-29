package com.thinkbiganalytics.nifi.rest.model.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.common.constants.KyloProcessorFlowType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by sr186054 on 8/11/16. Object in a NifiFlow that has pointers to all its sources (parents)  and children (destinations)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NifiFlowProcessor implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowProcessor.class);

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;

    private String type;

    private String parentGroupId;

    private boolean isFailure;
    private boolean isEnd;

    private String flowId;

    private KyloProcessorFlowType processorFlowType;


    private NifiFlowProcessGroup processGroup;

    @JsonIgnore
    private Set<NifiFlowProcessor> sources; //parents

    @JsonIgnore
    private Set<NifiFlowProcessor> destinations; //children

    private Set<String> sourceIds;

    private Set<String> destinationIds;

    private Set<NiFiFlowProcessorConnection> sourceConnectionIds;

    private Set<NiFiFlowProcessorConnection> destinationConnectionIds;


    private Set<NifiFlowProcessor> failureProcessors;

    public NifiFlowProcessor() {

    }

    public NifiFlowProcessor(@JsonProperty("id") String id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

    public NifiFlowProcessor(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("type") String type) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    public Set<NifiFlowProcessor> getDestinations() {
        if (destinations == null) {
            destinations = new HashSet<>();
        }
        return destinations;
    }

    public void setDestinations(Set<NifiFlowProcessor> destinations) {
        this.destinations = destinations;
    }

    public Set<NifiFlowProcessor> getSources() {
        if (sources == null) {
            sources = new HashSet<>();
        }
        return sources;
    }

    public void setSources(Set<NifiFlowProcessor> sources) {
        this.sources = sources;
    }

    public Set<NifiFlowProcessor> getFailureProcessors() {
        if (failureProcessors == null) {
            failureProcessors = new HashSet<>();
        }
        return failureProcessors;
    }

    public void setFailureProcessors(Set<NifiFlowProcessor> failureProcessors) {
        this.failureProcessors = failureProcessors;
    }

    public NifiFlowProcessGroup getProcessGroup() {
        return processGroup;
    }

    public void setProcessGroup(NifiFlowProcessGroup processGroup) {
        this.processGroup = processGroup;
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
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isFailure() {
        return isFailure;
    }

    public void setIsFailure(boolean isFailure) {
        this.isFailure = isFailure;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setIsEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public boolean isStart() {
        return this.getSources().isEmpty();
    }


    public Set<String> getSourceIds() {
        if (sourceIds == null) {
            sourceIds = new HashSet<>();
        }
        return sourceIds;
    }

    public void setSourceIds(Set<String> sourceIds) {
        this.sourceIds = sourceIds;
    }

    public Set<String> getDestinationIds() {
        if (destinationIds == null) {
            destinationIds = new HashSet<>();
        }
        return destinationIds;
    }

    public void setDestinationIds(Set<String> destinationIds) {
        this.destinationIds = destinationIds;
    }

    public Set<String> getAllDestinationIds() {
        Set<String> destinationIds = new HashSet<>();
        destinationIds.addAll(getDestinationIds());
        for (NifiFlowProcessor destination : getDestinations()) {
            destinationIds.addAll(destination.getAllDestinationIds());
        }
        return destinationIds;

    }

    public Set<NiFiFlowProcessorConnection> getSourceConnectionIds() {
        if (sourceConnectionIds == null) {
            sourceConnectionIds = new HashSet<>();
        }
        return sourceConnectionIds;
    }

    public void setSourceConnectionIds(Set<NiFiFlowProcessorConnection> sourceConnectionIds) {
        this.sourceConnectionIds = sourceConnectionIds;
    }

    public Set<NiFiFlowProcessorConnection> getDestinationConnectionIds() {
        if (destinationConnectionIds == null) {
            destinationConnectionIds = new HashSet<>();
        }
        return destinationConnectionIds;
    }

    public void setDestinationConnectionIds(Set<NiFiFlowProcessorConnection> destinationConnectionIds) {
        this.destinationConnectionIds = destinationConnectionIds;
    }

    public void print() {

        print(0);
    }

    public void print(Integer level) {

        log.info(flowId + ", " + level + ". " + getName());
        System.out.println(level + ". " + getName());
        Set<String> printed = new HashSet<>();
        printed.add(this.getId());
        Integer nextLevel = level + 1;

        for (NifiFlowProcessor child : getDestinations()) {
            if (!child.containsDestination(this) && !child.containsDestination(child) && !child.equals(this) && !printed.contains(child.getId())) {
                child.print(nextLevel);
                printed.add(child.getId());
            }
        }
    }

    public Integer assignFlowIds(Integer flowId) {
        flowId++;
        setFlowId(StringUtils.substringAfterLast(this.type, ".") + "__" + flowId);
        //   log.info(getFlowId() + " - " + getName() + " (" + getId() + ")");
        Set<String> printed = new HashSet<>();
        printed.add(this.getId());

        for (NifiFlowProcessor child : getDestinations()) {
            if (!child.containsDestination(this) && !child.containsDestination(child) && !child.equals(this) && !printed.contains(child.getId())) {
                flowId = child.assignFlowIds(flowId);
                printed.add(child.getId());
            }
        }
        return flowId;
    }

    public Integer countNodes() {
        Integer count = getDestinations().size();
        Set<String> printed = new HashSet<>();
        printed.add(this.getId());
        for (NifiFlowProcessor child : getDestinations()) {
            if (!child.containsDestination(this) && !child.containsDestination(child) && !child.equals(this) && !printed.contains(child.getId())) {
                count += child.countNodes();
                printed.add(child.getId());
            }
        }
        return count;
    }

    public KyloProcessorFlowType getProcessorFlowType() {
        return processorFlowType != null ? processorFlowType : KyloProcessorFlowType.NORMAL_FLOW;
    }

    public void setProcessorFlowType(KyloProcessorFlowType processorFlowType) {
        this.processorFlowType = processorFlowType;
        if (KyloProcessorFlowType.CRITICAL_FAILURE.equals(processorFlowType)) {
            setIsFailure(true);
        } else {
            setIsFailure(false);
        }
    }

    public boolean containsDestination(NifiFlowProcessor parent) {
        final String thisId = getId();
        final String parentId = parent.getId();

        return getDestinations().stream().anyMatch(processor -> processor.getId().equalsIgnoreCase(thisId) || processor.getId()
            .equalsIgnoreCase(parentId));
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public boolean isLeaf(){
        return getDestinationIds().isEmpty();
    }

    public boolean isRoot(){
        return getSourceIds().isEmpty();
    }

    public String getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(String parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NifiFlowProcessor{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
