package com.thinkbiganalytics.nifi.rest.model.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by sr186054 on 8/11/16.
 */
public class SimpleNifiFlowProcessor {

    private static final Logger log = LoggerFactory.getLogger(SimpleNifiFlowProcessor.class);

    private String id;
    private String name;
    private boolean isFailure;
    private boolean isEnd;

    private SimpleNifiFlowProcessGroup processGroup;

    private Set<SimpleNifiFlowProcessor> sources; //parents
    private Set<SimpleNifiFlowProcessor> destinations; //children

    private Set<SimpleNifiFlowProcessor> failureProcessors;

    public SimpleNifiFlowProcessor(String id, String name) {
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

    public Set<SimpleNifiFlowProcessor> getDestinations() {
        if (destinations == null) {
            destinations = new HashSet<>();
        }
        return destinations;
    }

    public void setDestinations(Set<SimpleNifiFlowProcessor> destinations) {
        this.destinations = destinations;
    }

    public Set<SimpleNifiFlowProcessor> getSources() {
        if (sources == null) {
            sources = new HashSet<>();
        }
        return sources;
    }

    public void setSources(Set<SimpleNifiFlowProcessor> sources) {
        this.sources = sources;
    }

    public Set<SimpleNifiFlowProcessor> getFailureProcessors() {
        return failureProcessors;
    }

    public void setFailureProcessors(Set<SimpleNifiFlowProcessor> failureProcessors) {
        this.failureProcessors = failureProcessors;
    }

    public SimpleNifiFlowProcessGroup getProcessGroup() {
        return processGroup;
    }

    public void setProcessGroup(SimpleNifiFlowProcessGroup processGroup) {
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
        SimpleNifiFlowProcessor that = (SimpleNifiFlowProcessor) o;
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

    public void print() {

        print(0);
    }

    public void print(Integer level) {

        log.info(level + ". " + getName());
        System.out.println(level + ". " + getName());
        Set<String> printed = new HashSet<>();
        printed.add(this.getId());
        Integer nextLevel = level + 1;

        for (SimpleNifiFlowProcessor child : getDestinations()) {
            if (!child.containsDestination(this) && !child.containsDestination(child) && !child.equals(this) && !printed.contains(child.getId())) {
                child.print(nextLevel);
                printed.add(child.getId());
            }
        }
    }

    //used for connections with input/output ports

    public boolean containsDestination(SimpleNifiFlowProcessor parent) {
        final String thisId = getId();
        final String parentId = parent.getId();

        return getDestinations().stream().anyMatch(processor -> processor.getId().equalsIgnoreCase(thisId) || processor.getId()
            .equalsIgnoreCase(parentId));
    }


}
