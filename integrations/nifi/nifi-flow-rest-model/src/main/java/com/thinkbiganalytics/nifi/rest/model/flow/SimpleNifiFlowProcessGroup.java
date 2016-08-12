package com.thinkbiganalytics.nifi.rest.model.flow;


import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/11/16. Simple object to store Graph of a Nifi flow. This is populated via the Nifi Rest Client Each SimpleNifiFlowProcessor has a pointer to its parent/children You can
 * walk the graph starting with the Set of startingProcessors, or lookup into the graph using hte processorMap and then traverse it back and forth by a specific processor.
 *
 * When walking the flow the system removes the internal Processgroups and connects the processors together.
 */
public class SimpleNifiFlowProcessGroup {


    private String id;
    private String name;

    private String parentGroupId;
    private String parentGroupName;

    private Map<String, SimpleNifiFlowProcessor> processorMap;

    private Map<String, SimpleNifiFlowProcessor> failureProcessors;

    private Map<String, SimpleNifiFlowProcessor> endingProcessors;


    public SimpleNifiFlowProcessGroup(String id, String name) {
        this.id = id;
        this.name = name;
    }

    private Set<SimpleNifiFlowProcessor> startingProcessors;

    public Set<SimpleNifiFlowProcessor> processors;

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

    public Set<SimpleNifiFlowProcessor> getStartingProcessors() {
        return startingProcessors;
    }

    public void setStartingProcessors(Set<SimpleNifiFlowProcessor> startingProcessors) {
        this.startingProcessors = startingProcessors;
    }

    public Map<String, SimpleNifiFlowProcessor> getEndingProcessors() {
        return endingProcessors;
    }


    public Set<SimpleNifiFlowProcessor> getProcessors() {
        return processors;
    }

    public void setProcessors(Set<SimpleNifiFlowProcessor> processors) {
        this.processors = processors;
    }

    public void print() {
        for (SimpleNifiFlowProcessor processor : getStartingProcessors()) {
            processor.print();
        }
    }

    public Map<String, SimpleNifiFlowProcessor> getProcessorMap() {
        return processorMap;
    }

    public void setProcessorMap(Map<String, SimpleNifiFlowProcessor> processorMap) {
        this.processorMap = processorMap;
        this.failureProcessors = processorMap.values().stream().filter(simpleNifiFlowProcessor ->
                                                                           simpleNifiFlowProcessor.isFailure()
        ).collect(Collectors.toMap(processor -> processor.getId(), processor -> processor));

        this.endingProcessors = processorMap.values().stream().filter(simpleNifiFlowProcessor ->
                                                                          simpleNifiFlowProcessor.isEnd()
        ).collect(Collectors.toMap(processor -> processor.getId(), processor -> processor));

        processorMap.values().forEach(processor -> processor.setProcessGroup(this));
    }

    public Map<String, SimpleNifiFlowProcessor> getFailureProcessors() {
        return failureProcessors;
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
}
