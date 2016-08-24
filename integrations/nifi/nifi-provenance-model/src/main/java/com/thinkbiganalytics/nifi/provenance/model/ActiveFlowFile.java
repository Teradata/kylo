package com.thinkbiganalytics.nifi.provenance.model;

import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventRecordDTOComparator;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/11/16.
 *
 * Hold on to a Flowfile and get its graph of events
 */
public class ActiveFlowFile implements FlowFile<ActiveFlowFile, ProvenanceEventRecordDTO> {

    /**
     * FlowFile UUID
     */
    private String id;

    private Set<ActiveFlowFile> parents;

    private Set<ActiveFlowFile> children;

    //   private List<Long> events;

    private Set<String> completedProcessorIds;

    private transient List<ProvenanceEventRecordDTO> completedEvents;

    private ProvenanceEventRecordDTO firstEvent;

    private ActiveFlowFile rootFlowFile;

    private AtomicLong completedEndingProcessors = new AtomicLong();

    private DateTime timeCompleted;

    private boolean isFirstEventStream;

    /**
     * marker to determine if the Flow has Received a DROP event.
     */
    private boolean currentFlowFileComplete = false;


    private String feedName;
    private String feedProcessGroupId;

    private boolean isRootFlowFile = false;


    @Override
    public void assignFeedInformation(String feedName, String feedProcessGroupId) {
        this.feedName = feedName;
        this.feedProcessGroupId = feedProcessGroupId;
    }

    @Override
    public boolean hasFeedInformationAssigned() {
        return getFeedName() != null && getFeedProcessGroupId() != null;
    }

    @Override
    public String getFeedName() {
        //if the name is blank attempt to get it from the root file
        if (StringUtils.isBlank(feedName) && !this.isRootFlowFile()) {
            FlowFile root = getRootFlowFile();
            if (root != null) {
                this.feedName = root.getFeedName();
            }
        }
        return this.feedName;
    }

    @Override
    public String getFeedProcessGroupId() {
        //if the id is blank attempt to get it from the root file
        if (StringUtils.isBlank(feedProcessGroupId) && !this.isRootFlowFile()) {
            FlowFile root = getRootFlowFile();
            if (root != null) {
                this.feedProcessGroupId = root.getFeedProcessGroupId();
            }
        }
        return this.feedProcessGroupId;


    }


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
    @Override
    public ActiveFlowFile addParent(ActiveFlowFile flowFile) {
        if (!flowFile.equals(this)) {
            getParents().add(flowFile);
        }
        return flowFile;
    }

    @Override
    public ActiveFlowFile getRootFlowFile() {
        ActiveFlowFile root = null;
        //if the pointer to the root file is already set, short circuity the check and return
        if (this.rootFlowFile != null) {
            return this.rootFlowFile;
        }
        if (isRootFlowFile()) {
            root = this;
        } else {
            if (hasParents()) {
                for (ActiveFlowFile parent : getParents()) {
                    root = parent.getRootFlowFile();
                    if (root != null) {
                        break;
                    }
                }
            }
        }
        //assign the pointer to the root for quick discovery
        if (root != null) {
            this.rootFlowFile = root;
        }
        return root;
    }




    /**
     * add and return the child
     */
    @Override
    public ActiveFlowFile addChild(ActiveFlowFile flowFile) {
        if (!flowFile.equals(this)) {
            getChildren().add(flowFile);
        }
        return flowFile;
    }

    @Override
    public ActiveFlowFile getFirstParent() {
        return getParents().stream().findFirst().orElse(null);
    }

    @Override
    public boolean hasParents() {
        return !getParents().isEmpty();
    }

    @Override
    public Set<ActiveFlowFile> getParents() {
        if (parents == null) {
            parents = new HashSet<>();
        }
        return parents;
    }

    @Override
    public Set<ActiveFlowFile> getChildren() {
        if (children == null) {
            children = new HashSet<>();
        }
        return children;
    }

    @Override
    public Set<ActiveFlowFile> getAllChildren() {
        Set<ActiveFlowFile> allChildren = new HashSet<>();
        for (ActiveFlowFile child : getChildren()) {
            allChildren.add(child);
            allChildren.addAll(child.getAllChildren());
        }
        return allChildren;

    }

    @Override
    public ProvenanceEventRecordDTO getFirstEvent() {
        if (firstEvent == null && !isRootFlowFile()) {
            ActiveFlowFile root = getRootFlowFile();
            if (root != null) {
                firstEvent = root.getFirstEvent();
            }
        }
        return firstEvent;
    }

    @Override
    public void setFirstEvent(ProvenanceEventRecordDTO firstEvent) {
        this.firstEvent = firstEvent;
    }

    @Override
    public boolean hasFirstEvent() {
        return firstEvent != null;
    }


    @Override
    public void completeEndingProcessor() {
        completedEndingProcessors.incrementAndGet();
    }

    @Override
    public void markAsRootFlowFile() {

        this.isRootFlowFile = true;
        this.rootFlowFile = this;
    }

    @Override
    public boolean isRootFlowFile() {
        return isRootFlowFile;
    }

    @Override
    public void addFailedEvent(ProvenanceEventRecordDTO event) {
        failedEvents.add(event);
    }


    /**
     * gets the flow files failed events. if inclusive then get all children
     */
    @Override
    public Set<ProvenanceEventRecordDTO> getFailedEvents(boolean inclusive) {
        if (inclusive) {
            Set<ProvenanceEventRecordDTO> failedEvents = new HashSet<>();
            failedEvents.addAll(this.failedEvents);
            for (FlowFile child : getChildren()) {
                failedEvents.addAll(child.getFailedEvents(inclusive));
            }
            return failedEvents;
        } else {
            return failedEvents;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isStartOfCurrentFlowFile(ProvenanceEventRecordDTO event) {
        Integer index = getCompletedEvents().indexOf(event);
        return index == 0;
    }

    private ProvenanceEventRecordDTO getPreviousEvent(ProvenanceEventRecordDTO event) {
        if (event.getPreviousEvent() == null) {
            Integer index = getCompletedEvents().indexOf(event);
            if (index > 0) {
                event.setPreviousEvent(getCompletedEvents().get(index - 1));
            } else {
                //get parent flow file for this event
                if (getParents() != null && !getParents().isEmpty()) {
                    List<ProvenanceEventRecordDTO> previousEvents = getParents().stream()
                        .filter(flowFile -> event.getParentFlowFileIds().contains(flowFile.getId()))
                        .flatMap(flow -> flow.getCompletedEvents().stream()).sorted(new ProvenanceEventRecordDTOComparator().reversed())
                        .collect(Collectors.toList());
                    if (previousEvents != null && !previousEvents.isEmpty()) {
                        event.setPreviousEvent(previousEvents.get(0));
                    }
                }
            }
        }
        return event.getPreviousEvent();
    }

    private Long calculateEventDuration(ProvenanceEventRecordDTO event) {

        //lookup the flow file to get the prev event
        ProvenanceEventRecordDTO prev = getPreviousEvent(event);
        if (prev != null) {
            long dur = event.getEventTime().getMillis() - prev.getEventTime().getMillis();
            event.setEventDuration(dur);
            return dur;
        } else if (event.getEventDuration() == null || event.getEventDuration() < 0L) {
            event.setEventDuration(0L);
            return 0L;
        } else {
            return event.getEventDuration() != null ? event.getEventDuration() : 0L;
        }

    }

    @Override
    public String summary() {
        Set<ProvenanceEventRecordDTO> failedEvents = getFailedEvents(true);
        return "Flow File (" + id + "), with first Event of (" + firstEvent + ") processed " + getCompletedEvents().size() + " events. " + failedEvents.size() + " were failure events. "
               + completedEndingProcessors.longValue() + " were leaf ending events";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ActiveFlowFile flowFile = (ActiveFlowFile) o;

        return !(id != null ? !id.equals(flowFile.id) : flowFile.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public Set<String> getCompletedProcessorIds() {
        if (completedProcessorIds == null) {
            completedProcessorIds = new HashSet<>();
        }
        return completedProcessorIds;
    }

    @Override
    public List<ProvenanceEventRecordDTO> getCompletedEvents() {
        if (completedEvents == null) {
            completedEvents = new LinkedList<>();
        }
        return completedEvents;
    }

    @Override
    public void addCompletedEvent(ProvenanceEventRecordDTO event) {
        getCompletedEvents().add(event);
        getCompletedProcessorIds().add(event.getComponentId());
        calculateEventDuration(event);
        checkAndMarkIfFlowFileIsComplete(event);
    }

    @Override
    public void checkAndMarkIfFlowFileIsComplete(ProvenanceEventRecordDTO event) {
        if ("DROP".equalsIgnoreCase(event.getEventType())) {
            currentFlowFileComplete = true;
        }
    }

    @Override
    public boolean isCurrentFlowFileComplete() {
        return currentFlowFileComplete;
    }

    public void setCurrentFlowFileComplete(boolean currentFlowFileComplete) {
        this.currentFlowFileComplete = currentFlowFileComplete;
    }

    /**
     * Walks the graph of this flow and all children to see if there is a DROP event associated with each and every flow file
     */
    @Override
    public boolean isFlowComplete() {
        if (timeCompleted != null) {
            return true;
        }
        boolean complete = isCurrentFlowFileComplete();
        Set<ActiveFlowFile> directChildren = getChildren();
        if (complete && !directChildren.isEmpty()) {
            for (FlowFile child : directChildren) {
                complete &= child.isCurrentFlowFileComplete();
                if (!complete) {
                    break;
                }
            }
        }
        if (complete && timeCompleted == null) {
            timeCompleted = new DateTime();
        }
        return complete;
    }


    @Override
    public DateTime getTimeCompleted() {
        return timeCompleted;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActiveFlowFile{");
        sb.append("id='").append(id).append('\'');
        sb.append("isRoot=").append(isRootFlowFile());
        sb.append(", parents=").append(getParents().size());
        sb.append(", children=").append(getChildren().size());
        sb.append(", completedProcessorIds=").append(getCompletedProcessorIds().size());
        sb.append(", completedEvents=").append(getCompletedEvents().size());
        sb.append(", firstEvent=").append(firstEvent != null ? getFirstEvent().getEventId() : "NULL");
        sb.append(", timeCompleted=").append(timeCompleted);
        sb.append(", currentFlowFileComplete=").append(currentFlowFileComplete);
        sb.append(", feedName='").append(feedName).append('\'');
        sb.append(", feedProcessGroupId='").append(feedProcessGroupId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public IdReferenceFlowFile toIdReferenceFlowFile() {

        IdReferenceFlowFile idReferenceFlowFile = new IdReferenceFlowFile(this.getId());
        idReferenceFlowFile.setFeedName(this.getFeedName());
        idReferenceFlowFile.setFeedProcessGroupId(this.getFeedProcessGroupId());
        idReferenceFlowFile.setRootFlowFile(isRootFlowFile());
        idReferenceFlowFile.setRootFlowFileId(getRootFlowFile() != null ? getRootFlowFile().getId() : null);
        idReferenceFlowFile.setIsComplete(isCurrentFlowFileComplete());
        for (ActiveFlowFile parent : getParents()) {
            idReferenceFlowFile.addParentId(parent.getId());
        }

        for (ActiveFlowFile child : getChildren()) {
            IdReferenceFlowFile childIdRef = child.toIdReferenceFlowFile();
            idReferenceFlowFile.addChildId(childIdRef.getId());
        }
        return idReferenceFlowFile;

    }

}
