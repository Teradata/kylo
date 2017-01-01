package com.thinkbiganalytics.nifi.rest.model.flow;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.common.constants.KyloProcessorFlowType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/11/16. Simple object to store Graph of a Nifi flow. . This is populated via the Nifi Rest Client Each NifiFlowProcessor has a pointer to its parent/children. You can walk
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

    private Map<String, NifiFlowProcessor> failureProcessors;

    private Map<String, NifiFlowProcessor> endingProcessors;


    /**
     * Cached map of the ConnectionId to a list of all processors that this connection is coming from of which the connection is indicated as being a "failure"
     *
     * used for lookups to determine if an event has failed or not
     */
    private Map<String, List<NifiFlowProcessor>> failureConnectionIdToSourceProcessorMap;
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
        Integer maxCount = 0;
        for (NifiFlowProcessor processor : getSortedStartingProcessors()) {
            flowId = processor.assignFlowIds(flowId);
            Integer count = processor.countNodes();
            if (count > maxCount) {
                maxCount = count;
            }
        }
        log.info("Total Processors {} :  {}", getName(), maxCount);
    }

    public Map<String, NifiFlowProcessor> getProcessorMap() {
        if (processorMap == null) {
            processorMap = new HashMap<>();
        }
        return processorMap;
    }

    public void setProcessorMap(Map<String, NifiFlowProcessor> processorMap) {
        this.processorMap = processorMap;
        this.failureProcessors =
            processorMap.values().stream().filter(simpleNifiFlowProcessor -> simpleNifiFlowProcessor.isFailure()).collect(Collectors.toMap(processor -> processor.getId(), processor -> processor));

        this.endingProcessors =
            processorMap.values().stream().filter(simpleNifiFlowProcessor -> simpleNifiFlowProcessor.isEnd()).collect(Collectors.toMap(processor -> processor.getId(), processor -> processor));

        processorMap.values().forEach(processor -> processor.setProcessGroup(this));


    }

    public Map<String, NifiFlowProcessor> getFailureProcessors() {
        return failureProcessors;
    }

    public void setFailureProcessors(Map<String, NifiFlowProcessor> failureProcessors) {
        this.failureProcessors = failureProcessors;
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

    public Map<String, List<NifiFlowProcessor>> getFailureConnectionIdToSourceProcessorMap() {
        if (failureConnectionIdToSourceProcessorMap == null) {
            failureConnectionIdToSourceProcessorMap = new HashMap<>();
        }
        return failureConnectionIdToSourceProcessorMap;
    }

    public void setFailureConnectionIdToSourceProcessorMap(
        Map<String, List<NifiFlowProcessor>> failureConnectionIdToSourceProcessorMap) {
        this.failureConnectionIdToSourceProcessorMap = failureConnectionIdToSourceProcessorMap;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }


    public void resetProcessorsFlowType(final Map<String, KyloProcessorFlowType> processorFlowTypeMap) {
        if (processorFlowTypeMap != null) {
            getProcessorMap().values().stream()
                .forEach(flowProcessor -> flowProcessor.setProcessorFlowType(processorFlowTypeMap.getOrDefault(flowProcessor.getFlowId(), KyloProcessorFlowType.NORMAL_FLOW)));
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
        final StringBuilder sb = new StringBuilder("SimpleNifiFlowProcessGroup{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", parentGroupId='").append(parentGroupId).append('\'');
        sb.append(", parentGroupName='").append(parentGroupName).append('\'');
        sb.append(", feedName='").append(feedName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
