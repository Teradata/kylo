package com.thinkbiganalytics.nifi.provenance.model;

import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventUtil;

import org.apache.nifi.provenance.ProvenanceEventType;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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

    //   private List<Long> events;

    private Set<String> completedProcessorIds;

    private List<ProvenanceEventRecordDTO> completedEvents;

    private ProvenanceEventRecordDTO firstEvent;

    private ActiveFlowFile rootFlowFile;

    private AtomicLong completedEndingProcessors = new AtomicLong();

    /**
     * marker to determine if the Flow has Received a DROP event.
     */
    private boolean currentFlowFileComplete = false;



    //track failed events in this flow
    //change to ConcurrentSkipListSet ???
    private Set<ProvenanceEventRecordDTO> failedEvents;

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

    public Set<ActiveFlowFile> getAllChildren() {
        Set<ActiveFlowFile> allChildren = new HashSet<>();
        for (ActiveFlowFile child : getChildren()) {
            allChildren.add(child);
            allChildren.addAll(child.getAllChildren());
        }
        return allChildren;

    }

    public ProvenanceEventRecordDTO getFirstEvent() {
        return firstEvent;
    }

    public void setFirstEvent(ProvenanceEventRecordDTO firstEvent) {
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

    public void addFailedEvent(ProvenanceEventRecordDTO event){
        failedEvents.add(event);
    }



    /**
     * gets the flow files failed events.
     * if inclusive then get all children
     * @param inclusive
     * @return
     */
    public Set<ProvenanceEventRecordDTO> getFailedEvents(boolean inclusive){
        Set<ProvenanceEventRecordDTO> failedEvents = new HashSet<>();
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

    public ProvenanceEventRecordDTO getPreviousEvent(ProvenanceEventRecordDTO event) {
        if (event.getPreviousEvent() == null) {
            Integer index = getCompletedEvents().indexOf(event);
            if (index > 0) {
                event.setPreviousEvent(getCompletedEvents().get(index - 1));
            } else {
                //get parent flow file for this event
                if (getParents() != null && !getParents().isEmpty()) {
                    List<ProvenanceEventRecordDTO> previousEvents = getParents().stream()
                        .filter(flowFile -> event.getParentUuids().contains(flowFile.getId()))
                        .flatMap(flow -> flow.getCompletedEvents().stream()).sorted(ProvenanceEventUtil.provenanceEventRecordDTOComparator().reversed())
                        .collect(Collectors.toList());
                    if (previousEvents != null && !previousEvents.isEmpty()) {
                        event.setPreviousEvent(previousEvents.get(0));
                    }
                }
            }
        }
        return event.getPreviousEvent();
    }

    public Long calculateEventDuration(ProvenanceEventRecordDTO event) {

        //lookup the flow file to get the prev event
        ProvenanceEventRecordDTO prev = getPreviousEvent(event);
        if (prev != null) {
            long dur = event.getEventTime().getTime() - prev.getEventTime().getTime();
            event.setEventDuration(dur);
            return dur;
        } else {
            event.setEventDuration(0L);
            return 0L;
        }


    }

    public String summary(){
        Set<ProvenanceEventRecordDTO> failedEvents = getFailedEvents(true);
        return "Flow File (" + id + "), with first Event of (" + firstEvent + ") processed " + getCompletedEvents().size() + " events. " + failedEvents.size() + " were failure events. "
               + completedEndingProcessors.longValue() + " where leaf ending events";
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


    public Set<String> getCompletedProcessorIds() {
        if (completedProcessorIds == null) {
            completedProcessorIds = new HashSet<>();
        }
        return completedProcessorIds;
    }

    public List<ProvenanceEventRecordDTO> getCompletedEvents() {
        if (completedEvents == null) {
            completedEvents = new LinkedList<>();
        }
        return completedEvents;
    }

    public void addCompletedEvent(ProvenanceEventRecordDTO event) {
        getCompletedEvents().add(event);
        getCompletedProcessorIds().add(event.getComponentId());
        calculateEventDuration(event);
        checkAndMarkIfFlowFileIsComplete(event);
    }

    public void checkAndMarkIfFlowFileIsComplete(ProvenanceEventRecordDTO event) {
        if (ProvenanceEventType.DROP.name().equalsIgnoreCase(event.getEventType())) {
            currentFlowFileComplete = true;
        }
    }

    public boolean isCurrentFlowFileComplete() {
        return currentFlowFileComplete;
    }

    /**
     * Walks the graph of this flow and all children to see if there is a DROP event associated with each and every flow file
     */
    public boolean isFlowComplete() {
        boolean complete = isCurrentFlowFileComplete();
        Set<ActiveFlowFile> directChildren = getChildren();
        if (complete && !directChildren.isEmpty()) {
            for (ActiveFlowFile child : directChildren) {
                complete &= child.isCurrentFlowFileComplete();
                if (!complete) {
                    break;
                }
            }
        }
        return complete;
    }
}
