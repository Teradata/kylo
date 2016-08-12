package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/11/16.
 *
 * Hold on to a Flowfile and get its graph of events
 */
public class ActiveFlowFile {

    public ActiveFlowFile(String id) {
        this.id = id;
    }

    /**
     * FlowFile UUID
     */
    private String id;

    private List<ActiveFlowFile> parents;

    private List<ActiveFlowFile> children;

    private List<Long> events;

    private Long firstEvent;


    /**
     * Add and return the parent
     */
    public ActiveFlowFile addParent(ActiveFlowFile flowFile) {
        if (!flowFile.equals(this)) {
            getParents().add(flowFile);
        }
        return flowFile;
    }

    /**
     * add and return the child
     */
    public ActiveFlowFile addChild(ActiveFlowFile flowFile) {
        if (!flowFile.equals(this)) {
            getChildren().add(flowFile);
        }
        return flowFile;
    }

    public List<ActiveFlowFile> getParents() {
        if (parents == null) {
            parents = new ArrayList<>();
        }
        return parents;
    }

    public List<ActiveFlowFile> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public void addEvent(Long eventId) {
        getEvents().add(eventId);
    }

    public List<Long> getEvents() {
        if (events == null) {
            events = new ArrayList<>();
        }
        return events;
    }

    public Long getFirstEvent() {
        return firstEvent;
    }

    public void setFirstEvent(Long firstEvent) {
        this.firstEvent = firstEvent;
    }

    public boolean hasFirstEvent() {
        return firstEvent != null;
    }
}
