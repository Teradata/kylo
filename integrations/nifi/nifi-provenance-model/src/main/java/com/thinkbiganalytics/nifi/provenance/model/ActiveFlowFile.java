package com.thinkbiganalytics.nifi.provenance.model;

import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventRecordDTOComparator;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    /**
     * Events that match the ProvenanceEventUtil.isCompletionEvent CLONE, ROUTE, SEND eventTypes are events which generate new flow files and are not to be processed as completion of a Processor
     */
    private transient List<ProvenanceEventRecordDTO> completedEvents;

    /**
     * Store all events regardless of eventType
     */
    private transient List<ProvenanceEventRecordDTO> allEvents;


    private transient Map<String, List<ProvenanceEventRecordDTO>> completedEventsByProcessorId = new ConcurrentHashMap<>();

    private ProvenanceEventRecordDTO firstEvent;

    private transient ProvenanceEventRecordDTO lastEvent;

    private RootFlowFile rootFlowFile;

    private AtomicLong completedEndingProcessors = new AtomicLong();

    private DateTime timeCompleted;
    /**
     * marker to determine if the Flow has Received a DROP event.
     */
    private boolean currentFlowFileComplete = false;



    private String feedProcessGroupId;

    private boolean isRootFlowFile = false;

    private boolean flowFileCompletionStatsCollected = false;


    //Information gained from walking the Nifi Flow Graph
    private String feedName;



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


    //track failed events in this flow
    //change to ConcurrentSkipListSet ???
    private Set<ProvenanceEventRecordDTO> failedEvents;

    public ActiveFlowFile(String id) {
        this.id = id;
        this.failedEvents = new HashSet<>();
    }


    public RootFlowFile getRootFlowFile() {
        RootFlowFile root = null;
        //if the pointer to the root file is already set, short circuity the check and return
        if (this.rootFlowFile != null) {
            return this.rootFlowFile;
        }
        if (isRootFlowFile()) {
            //shouldnt get here.. but just in case
            log.info("marking root flow in getRootFlowFile for {} ", this.getId());
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
        failedEvents.add(event);
    }


    /**
     * gets the flow files failed events. if inclusive then get all children
     */

    public Set<ProvenanceEventRecordDTO> getFailedEvents(boolean inclusive) {
        if (inclusive) {
            Set<ProvenanceEventRecordDTO> failedEvents = new HashSet<>();
            failedEvents.addAll(this.failedEvents);
            for (ActiveFlowFile child : getChildren()) {
                failedEvents.addAll(child.getFailedEvents(inclusive));
            }
            return failedEvents;
        } else {
            return failedEvents;
        }
    }


    public String getId() {
        return id;
    }

    public boolean isFlowFileCompletionStatsCollected() {
        return flowFileCompletionStatsCollected;
    }

    public void setFlowFileCompletionStatsCollected(boolean flowFileCompletionStatsCollected) {
        this.flowFileCompletionStatsCollected = flowFileCompletionStatsCollected;
    }

    public boolean isStartOfCurrentFlowFile(ProvenanceEventRecordDTO event) {
        Integer index = getCompletedEvents().indexOf(event);
        return index == 0;
    }

    private ProvenanceEventRecordDTO getPreviousEvent(ProvenanceEventRecordDTO event) {
        if (event.getPreviousEvent() == null) {
            setPreviousEvent(event);
        }
        return event.getPreviousEvent();
    }

    /**
     * set the Previous Event to the prev (Completion Event. @see ProvenanceEventUtil.isCompletionEvent Parent/children are added to non completion events. We need to add the relationships
     */
    public void setPreviousEvent(ProvenanceEventRecordDTO event) {
        if (event.getPreviousEvent() == null) {
            Integer index = getCompletedEvents().indexOf(event);
            if (index > 0) {
                    event.setPreviousEvent(getCompletedEvents().get(index - 1));
            }
            if (event.getPreviousEvent() == null && getParents() != null && !getParents().isEmpty()) {
                List<ProvenanceEventRecordDTO> previousEvents = getParents().stream()
                        .filter(flowFile -> event.getParentFlowFileIds().contains(flowFile.getId()))
                        .flatMap(flow -> flow.getCompletedEvents().stream()).sorted(new ProvenanceEventRecordDTOComparator().reversed())
                        .collect(Collectors.toList());
                    if (previousEvents != null && !previousEvents.isEmpty()) {
                        ProvenanceEventRecordDTO previousEvent = previousEvents.get(0);
                        event.setPreviousEvent(previousEvent);
                        if (event.getParentUuids() == null && event.getParentUuids().isEmpty()) {
                            event.setParentUuids(getParents().stream().map(ff -> ff.getId()).collect(Collectors.toList()));
                            log.info("Assigned Parent Flow File ids as {}, to event {} ", event.getParentUuids(), event);
                            //set children?
                            //previousEvents.addChild(event)
                        }
                        previousEvent.addChildUuid(this.getId());
                    }
            }
        }
    }


    private Long calculateEventDuration(ProvenanceEventRecordDTO event) {

        //lookup the flow file to get the prev event
        ProvenanceEventRecordDTO prev = getPreviousEvent(event);
        if (prev != null) {
            long dur = event.getEventTime().getMillis() - prev.getEventTime().getMillis();
            if(dur <0){
                log.warn("The Duration for event {} is <0.  prev event {}.  dur: {} ", event, prev, dur);
                dur = 0L;
            }
            event.setEventDuration(dur);
            return dur;
        } else if (event.getEventDuration() == null || event.getEventDuration() < 0L) {
            event.setEventDuration(0L);
            return 0L;
        } else {
            return event.getEventDuration() != null ? event.getEventDuration() : 0L;
        }

    }


    public String summary() {
        Set<ProvenanceEventRecordDTO> failedEvents = getFailedEvents(true);
        return "Flow File (" + id + "), with first Event of (" + firstEvent + ") processed " + getCompletedEvents().size() + " events. " + failedEvents.size() + " were failure events. "
               + completedEndingProcessors.longValue() + " were leaf ending events";
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


    public List<ProvenanceEventRecordDTO> getCompletedEvents() {
        if (completedEvents == null) {
            completedEvents = new LinkedList<>();
        }
        return completedEvents;
    }

    public List<ProvenanceEventRecordDTO> getCompletedEventsForProcessorId(String processorId) {
        return completedEventsByProcessorId.getOrDefault(processorId, new ArrayList<>());
    }

    public ProvenanceEventRecordDTO getFirstCompletedEventsForProcessorId(String processorId) {
        List<ProvenanceEventRecordDTO> processorEvents = completedEventsByProcessorId.get(processorId);
        if (processorEvents != null && !processorEvents.isEmpty()) {
            return processorEvents.get(0);
        }
        return null;
    }

    public void addCompletionEvent(ProvenanceEventRecordDTO event) {
        getCompletedEvents().add(event);
        getCompletedProcessorIds().add(event.getComponentId());
        completedEventsByProcessorId.computeIfAbsent(event.getComponentId(), (processorId) -> new ArrayList<>()).add(event);
        setPreviousEvent(event);
        calculateEventDuration(event);
        checkAndMarkIfFlowFileIsComplete(event);
    }


    public void checkAndMarkIfFlowFileIsComplete(ProvenanceEventRecordDTO event) {
        if ("DROP".equalsIgnoreCase(event.getEventType())) {
            currentFlowFileComplete = true;
            log.info("Marking flow file {} as complete for event {}/{} with root file: {} ", this.getId(), event.getEventId(), event.getEventType(),
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
                    log.info("**** Failed isFlowComplete for {} because child was not complete: {} ", this.getId(), child.getId());
                    if (isRootFlowFile()) {
                        log.info("Root flow failed completion for {} with active children of {} ", getId(), this.getRootFlowFile().getRootFlowFileActiveChildren().size());
                    }
                    break;
                }
            }
        }
        if(complete && isRootFlowFile()){
            complete = !this.getRootFlowFile().hasActiveRootChildren();
            log.info(" isComplete for root file {} = {} with activechildren of {} ", getId(), complete, this.getRootFlowFile().getRootFlowFileActiveChildren().size());
        }
        if (complete && timeCompleted == null) {
           // log.info("****** COMPLETING FLOW FILE {} ",this);
            timeCompleted = new DateTime();
        }
        return complete;
    }


    public DateTime getTimeCompleted() {
        return timeCompleted;
    }


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

        if(getRootFlowFile() != null) {
            Long firstEventId = getRootFlowFile().getFirstEvent().getEventId();
            idReferenceFlowFile.setRootFlowFileFirstEventId(firstEventId);
        }
        return idReferenceFlowFile;

    }

    public void findEventMatchingDestinationConnection(String connectionIdentifier) {

    }

}
