package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import org.apache.nifi.provenance.ProvenanceEventRecord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 8/11/16.
 *
 * Hold on to a Flowfile and get its graph of events
 */
public class ActiveFlowFile {



    /**
     * FlowFile UUID
     */
    private String id;

    private Set<ActiveFlowFile> parents;

    private Set<ActiveFlowFile> children;

    private List<Long> events;

    private ProvenanceEventRecord firstEvent;

    private ActiveFlowFile rootFlowFile;

    private AtomicLong completedEndingProcessors = new AtomicLong();



    //track failed events in this flow
    private Set<ProvenanceEventRecord> failedEvents;

    public ActiveFlowFile(String id) {
        this.id = id;
        this.failedEvents = new HashSet<>();
    }

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

    public Set<ActiveFlowFile> getParents() {
        if (parents == null) {
            parents = new HashSet<>();
        }
        return parents;
    }

    public Set<ActiveFlowFile> getChildren() {
        if (children == null) {
            children = new HashSet<>();
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

    public ProvenanceEventRecord getFirstEvent() {
        return firstEvent;
    }

    public void setFirstEvent(ProvenanceEventRecord firstEvent) {
        this.firstEvent = firstEvent;
    }

    public boolean hasFirstEvent() {
        return firstEvent != null;
    }


    public void completeEndingProcessor(){
        completedEndingProcessors.incrementAndGet();
    }


    public ActiveFlowFile getRootFlowFile() {
        return rootFlowFile;
    }

    public void setRootFlowFile(ActiveFlowFile rootFlowFile) {
        this.rootFlowFile = rootFlowFile;
    }

    public boolean isRootFlowFile(){
        return this.rootFlowFile != null && this.rootFlowFile.equals(this);
    }

    public void addFailedEvent(ProvenanceEventRecord event){
        failedEvents.add(event);
    }



    /**
     * gets the flow files failed events.
     * if inclusive then get all children
     * @param inclusive
     * @return
     */
    public Set<ProvenanceEventRecord> getFailedEvents(boolean inclusive){
        Set<ProvenanceEventRecord> failedEvents = new HashSet<>();
        failedEvents.addAll(failedEvents);
        if(inclusive) {
            for (ActiveFlowFile child : getChildren()) {
                failedEvents.addAll(child.getFailedEvents(inclusive));
            }
        }
        return failedEvents;
    }

    public String getId() {
        return id;
    }

    public String summary(){
        Set<ProvenanceEventRecord> failedEvents = getFailedEvents(true);
       return "Flow File ("+id+"), with first Event of ("+firstEvent+") processed "+getEvents().size()+" events. "+failedEvents.size()+" were failure events. "+completedEndingProcessors.longValue()+" where leaf ending events";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActiveFlowFile that = (ActiveFlowFile) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
