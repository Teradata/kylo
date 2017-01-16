package com.thinkbiganalytics.nifi.provenance.model;

import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventRecordDTOComparator;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/11/16.
 *
 * Hold on to a Flowfile and get its graph of events
 */
public class ActiveFlowFile {

    private static final Logger log = LoggerFactory.getLogger(ActiveFlowFile.class);

    /**
     * FlowFile UUID
     */
    private String id;

    private Set<ActiveFlowFile> parents;

    private Set<ActiveFlowFile> children;

    //   private List<Long> events;

    private Set<String> completedProcessorIds;

    private transient List<Long> eventIds;

    private ProvenanceEventRecordDTO firstEvent;

    private transient ProvenanceEventRecordDTO lastEvent;

    private transient ProvenanceEventRecordDTO previousEvent;

    private Set<Long> failedEvents;

    private RootFlowFile rootFlowFile;

    private AtomicLong completedEndingProcessors = new AtomicLong();

    private DateTime timeCompleted;
    /**
     * marker to determine if the Flow has Received a DROP event.
     */
    private boolean currentFlowFileComplete = false;

    private String feedProcessGroupId;

    private boolean isRootFlowFile = false;

    private AtomicBoolean flowFileCompletionStatsCollected = new AtomicBoolean(false);

    private boolean isBuiltFromIdReferenceFlowFile;


    //Information gained from walking the Nifi Flow Graph
    private String feedName;


    public ActiveFlowFile(String id) {
        this.id = id;
        this.failedEvents = new HashSet<>();
    }


    public void assignFeedInformation(String feedName, String feedProcessGroupId) {
        this.feedName = feedName;
        this.feedProcessGroupId = feedProcessGroupId;
    }


    public boolean hasFeedInformationAssigned() {
        return getFeedName() != null && getFeedProcessGroupId() != null;
    }


    public String getFeedName() {
        //if the name is blank attempt to get it from the root file
        if (StringUtils.isBlank(feedName) && !this.isRootFlowFile()) {
            ActiveFlowFile root = getRootFlowFile();
            if (root != null) {
                this.feedName = root.getFeedName();
            }
        }
        return this.feedName;
    }


    public String getFeedProcessGroupId() {
        //if the id is blank attempt to get it from the root file
        if (StringUtils.isBlank(feedProcessGroupId) && !this.isRootFlowFile()) {
            ActiveFlowFile root = getRootFlowFile();
            if (root != null) {
                this.feedProcessGroupId = root.getFeedProcessGroupId();
            }
        }
        return this.feedProcessGroupId;


    }


    public RootFlowFile getRootFlowFile() {
        RootFlowFile root = null;
        //if the pointer to the root file is already set, short circuity the check and return
        if (this.rootFlowFile != null) {
            return this.rootFlowFile;
        }
        if (isRootFlowFile()) {
            //shouldnt get here.. but just in case
            log.debug("marking root flow in getRootFlowFile for {} ", this.getId());
            markAsRootFlowFile();
            root = this.rootFlowFile;
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
            log.debug("Adding {} as a child to {} ", flowFile.getId(), getId());
            getChildren().add(flowFile);
        }
        return flowFile;
    }


    public ActiveFlowFile getFirstParent() {
        return getParents().stream().findFirst().orElse(null);
    }


    public boolean hasParents() {
        return !getParents().isEmpty();
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
        if (firstEvent == null && !isRootFlowFile()) {
            ActiveFlowFile root = getRootFlowFile();
            if (root != null) {
                firstEvent = root.getFirstEvent();
            }
        }
        return firstEvent;
    }


    public void setFirstEvent(ProvenanceEventRecordDTO firstEvent) {
        this.firstEvent = firstEvent;
    }


    public boolean hasFirstEvent() {
        return firstEvent != null;
    }


    public void completeEndingProcessor() {
        completedEndingProcessors.incrementAndGet();
    }


    public void markAsRootFlowFile() {
        this.isRootFlowFile = true;
        this.rootFlowFile = new RootFlowFile(this);
    }


    public boolean isRootFlowFile() {
        return isRootFlowFile;
    }


    public void addFailedEvent(ProvenanceEventRecordDTO event) {
        getFailedEvents().add(event.getEventId());
    }

    public Set<Long> getFailedEvents() {
        if (failedEvents == null) {
            failedEvents = new HashSet<>();
        }
        return failedEvents;
    }


    /**
     * gets the flow files failed events. if inclusive then get all children
     */

    public Set<Long> getFailedEvents(boolean inclusive) {
        if (inclusive) {
            Set<Long> failedEvents = new HashSet<>();
            failedEvents.addAll(this.getFailedEvents());
            for (ActiveFlowFile child : getChildren()) {
                failedEvents.addAll(child.getFailedEvents(inclusive));
            }
            return failedEvents;
        } else {
            return getFailedEvents();
        }
    }


    public String getId() {
        return id;
    }

    public AtomicBoolean getFlowFileCompletionStatsCollected() {
        return flowFileCompletionStatsCollected;
    }

    public void setFlowFileCompletionStatsCollected(boolean flowFileCompletionStatsCollected) {
        this.flowFileCompletionStatsCollected.set(flowFileCompletionStatsCollected);
    }

    public boolean isStartOfCurrentFlowFile(ProvenanceEventRecordDTO event) {
        return getEventIds().indexOf(event.getEventId()) == 0;
    }


    public ProvenanceEventRecordDTO getPreviousEvent() {
        return previousEvent;
    }

    public void setPreviousEvent(ProvenanceEventRecordDTO previousEvent) {
        this.previousEvent = previousEvent;
    }

    private ProvenanceEventRecordDTO getPreviousEventForEvent(ProvenanceEventRecordDTO event) {
        if (event.getPreviousEvent() == null) {
            setPreviousEventForEvent(event);
        }
        return event.getPreviousEvent();
    }

    /**
     * looks up to the parent flow files and finds the last event to use as this previous event
     */
    public ProvenanceEventRecordDTO findPreviousEventInParentFlowFile(ProvenanceEventRecordDTO event) {
        ProvenanceEventRecordDTO previousEvent = null;
        if (hasParents()) {
            List<ProvenanceEventRecordDTO> previousEvents = getParents().stream()
                .filter(flowFile -> flowFile.getPreviousEvent() != null)
                .map(flow -> flow.getPreviousEvent())
                .collect(Collectors.toList());
            if (previousEvents != null && !previousEvents.isEmpty()) {
                Collections.sort(previousEvents, new ProvenanceEventRecordDTOComparator().reversed());
                previousEvent = previousEvents.get(0);

                if (event.getParentUuids() == null || event.getParentUuids().isEmpty()) {
                    event.setParentUuids(getParents().stream().map(ff -> ff.getId()).collect(Collectors.toList()));
                }
                previousEvent.addChildUuid(this.getId());
            }
        }
        return previousEvent;
    }

    /**
     * set the Previous Event to the prev (Completion Event. @see ProvenanceEventUtil.isCompletionEvent Parent/children are added to non completion events. We need to add the relationships
     */
    public void setPreviousEventForEvent(ProvenanceEventRecordDTO event) {
        if (event.getPreviousEvent() == null && !event.isStartOfJob()) {
            Integer index = getEventIds().indexOf(event.getEventId());
            //if this event is not first in the flow file then its prev is the pointer on this flow file
            if (index > 0) {
                event.setPreviousEvent(previousEvent);
            }

            if (event.getPreviousEvent() == null) {
                ProvenanceEventRecordDTO previousEvent = findPreviousEventInParentFlowFile(event);
                if (previousEvent != null) {
                    event.setPreviousEvent(previousEvent);
                    //cehck to ensure the prev is set
                    if (previousEvent.getPreviousEvent() == null && !previousEvent.isStartOfJob()) {
                        //check against if previous event came in before this one
                        ProvenanceEventRecordDTO previousEvent1 = findPreviousEventInParentFlowFile(previousEvent);
                        previousEvent.setPreviousEvent(previousEvent1);
                    }
                } else {
                    log.error("Unable to find the previous event for {}.  setting start time == event time  ", event);
                    event.setStartTime(event.getEventTime());
                }

            }

        }

    }

    private Long calculateEventDuration(ProvenanceEventRecordDTO event) {

        Long dur = null;
        if (event.getStartTime() != null) {
            dur = event.getEventTime().getMillis() - event.getStartTime().getMillis();
            if (dur < 0) {
                log.warn("The Duration for event {} is <0.  dur: {} ", event, dur);
                dur = 0L;
            }

        } else if (event.getEventDuration() == null || event.getEventDuration() < 0L) {
            dur = 0L;
        } else {
            dur = event.getEventDuration() != null ? event.getEventDuration() : 0L;
        }
        if (dur == null) {
            log.warn("Event duration could not be determined.  returning 0L for duration on event: {} ", event);
            dur = 0L;
        }
        event.setEventDuration(dur);
        return dur;

    }


    public String summary() {
        Set<Long> failedEvents = getFailedEvents(true);
        return "Flow File (" + id + "), with first Event of (" + firstEvent + ") processed " + getEventIds().size() + " events. " + failedEvents.size() + " were failure events. "
               + completedEndingProcessors.longValue() + " were ending events";
    }


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


    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }


    public Set<String> getCompletedProcessorIds() {
        if (completedProcessorIds == null) {
            completedProcessorIds = new HashSet<>();
        }
        return completedProcessorIds;
    }


    public List<Long> getEventIds() {
        if (eventIds == null) {
            eventIds = new LinkedList<>();
        }
        return eventIds;
    }


    public void addCompletionEvent(ProvenanceEventRecordDTO event) {
        getEventIds().add(event.getEventId());
        getCompletedProcessorIds().add(event.getComponentId());
        //track the prev event for duration calcs
        setPreviousEventForEvent(event);

        //safeguard to set prev event
        if (event.getPreviousEvent() == null && previousEvent != null) {
            event.setPreviousEvent(previousEvent);
        }
        calculateEventDuration(event);
        //reset the pointer on this flow file to this event as the previous event
        setPreviousEvent(event);
        checkAndMarkIfFlowFileIsComplete(event);
    }


    public void checkAndMarkIfFlowFileIsComplete(ProvenanceEventRecordDTO event) {
        if ("DROP".equalsIgnoreCase(event.getEventType())) {
            currentFlowFileComplete = true;
            log.debug("Marking flow file {} as complete for event {}/{} with root file: {} ", this.getId(), event.getEventId(), event.getEventType(),
                      (getRootFlowFile() != null ? getRootFlowFile().getId() + "" : "null root"));
            lastEvent = event;
            getRootFlowFile().removeRootFileActiveChild(this.getId());
        }
    }


    public boolean isCurrentFlowFileComplete() {
        return currentFlowFileComplete;
    }

    public void setCurrentFlowFileComplete(boolean currentFlowFileComplete) {
        this.currentFlowFileComplete = currentFlowFileComplete;
    }

    public ProvenanceEventRecordDTO getLastEvent() {
        return lastEvent;
    }

    /**
     * Walks the graph of this flow and all children to see if there is a DROP event associated with each and every flow file
     */
    public boolean isFlowComplete() {
        if (timeCompleted != null) {
            return true;
        }
        boolean complete = isCurrentFlowFileComplete();
        Set<ActiveFlowFile> directChildren = getChildren();
        if (complete && !directChildren.isEmpty()) {
            for (ActiveFlowFile child : directChildren) {
                complete &= child.isCurrentFlowFileComplete();
                if (!complete) {
                    log.debug("**** Failed isFlowComplete for {} because child was not complete: {} ", this.getId(), child.getId());
                    if (isRootFlowFile()) {
                        log.debug("Root flow failed completion for {} with active children of {} ", getId(), this.getRootFlowFile().getRootFlowFileActiveChildren().size());
                    }
                    break;
                }
            }
        }
        if (complete && isRootFlowFile()) {
            complete = !this.getRootFlowFile().hasActiveRootChildren();
            log.debug(" isComplete for root file {} = {}.  Related flows complete: {}, with activechildren of {} ", getId(), complete, this.getRootFlowFile().getRootFlowFileActiveChildren().size());
        }
        if (complete && timeCompleted == null) {
            timeCompleted = new DateTime();
        }
        if (complete && isRootFlowFile()) {
            log.debug("****** COMPLETING ROOT FLOW FILE {} # of related {} ", this);
        }
        return complete;
    }

    public boolean isBuiltFromIdReferenceFlowFile() {
        return isBuiltFromIdReferenceFlowFile;
    }

    public void setIsBuiltFromIdReferenceFlowFile(boolean isBuiltFromIdReferenceFlowFile) {
        this.isBuiltFromIdReferenceFlowFile = isBuiltFromIdReferenceFlowFile;
    }

    public DateTime getTimeCompleted() {
        return timeCompleted;
    }

    public IdReferenceFlowFile toIdReferenceFlowFile() {

        IdReferenceFlowFile idReferenceFlowFile = new IdReferenceFlowFile(this.getId());
        idReferenceFlowFile.setFeedName(this.getFeedName());
        idReferenceFlowFile.setFeedProcessGroupId(this.getFeedProcessGroupId());
        idReferenceFlowFile.setRootFlowFile(isRootFlowFile());
        idReferenceFlowFile.setRootFlowFileId(getRootFlowFile() != null ? getRootFlowFile().getId() : null);
        idReferenceFlowFile.setIsComplete(isCurrentFlowFileComplete());
        idReferenceFlowFile.setPreviousEventId(this.getPreviousEvent() != null ? this.getPreviousEvent().getEventId() : null);
        idReferenceFlowFile.setPreviousEventTime(this.getPreviousEvent() != null && this.getPreviousEvent().getEventTime() != null ? this.getPreviousEvent().getEventTime().getMillis() : null);
        for (ActiveFlowFile parent : getParents()) {
            idReferenceFlowFile.addParentId(parent.getId());
        }

        for (ActiveFlowFile child : getChildren()) {
            IdReferenceFlowFile childIdRef = child.toIdReferenceFlowFile();
            idReferenceFlowFile.addChildId(childIdRef.getId());
        }

        if (getRootFlowFile() != null) {
            ProvenanceEventRecordDTO firstEvent = getRootFlowFile().getFirstEvent();
            if (firstEvent != null) {
                Long firstEventId = firstEvent.getEventId();
                idReferenceFlowFile.setRootFlowFileFirstEventId(firstEventId);
                idReferenceFlowFile.setRootFlowFileFirstEventComponentId(firstEvent.getComponentId());
                idReferenceFlowFile.setRootFlowFileFirstEventComponentName(firstEvent.getComponentName());
                idReferenceFlowFile.setRootFlowFileFirstEventTime(firstEvent.getEventTime().getMillis());
                idReferenceFlowFile.setRootFlowFileFirstEventType(firstEvent.getEventType());
                idReferenceFlowFile.setRootFlowFileFirstEventStartTime(firstEvent.getStartTime().getMillis());
            }
        }
        return idReferenceFlowFile;

    }


    public String toString() {
        final StringBuilder sb = new StringBuilder("ActiveFlowFile{");
        sb.append("id='").append(id).append('\'');
        sb.append("isRoot=").append(isRootFlowFile());
        sb.append(", parents=").append(getParents().size());
        sb.append(", children=").append(getChildren().size());
        sb.append(", completedProcessorIds=").append(getCompletedProcessorIds().size());
        sb.append(", completedEvents=").append(getEventIds().size());
        sb.append(", firstEvent=").append(firstEvent != null ? getFirstEvent().getEventId() : "NULL");
        sb.append(", timeCompleted=").append(timeCompleted);
        sb.append(", currentFlowFileComplete=").append(currentFlowFileComplete);
        sb.append(", feedName='").append(feedName).append('\'');
        sb.append(", feedProcessGroupId='").append(feedProcessGroupId).append('\'');
        sb.append('}');
        return sb.toString();
    }


}
