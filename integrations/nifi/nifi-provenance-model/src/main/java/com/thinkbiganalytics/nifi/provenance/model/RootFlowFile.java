package com.thinkbiganalytics.nifi.provenance.model;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sr186054 on 9/8/16.
 */
public class RootFlowFile extends ActiveFlowFile {

    private static final Logger log = LoggerFactory.getLogger(RootFlowFile.class);

    private ActiveFlowFile flowFile;

    private boolean canExpire;

    private DateTime minimiumExpireTime;


    private Set<String> rootFlowFileActiveChildren = new HashSet<>();

    public enum FIRST_EVENT_TYPE {
        BATCH, STREAM, UNKNOWN;
    }

    private FIRST_EVENT_TYPE firstEventType;

    public FIRST_EVENT_TYPE getFirstEventType() {
        return firstEventType;
    }


    public void setFirstEventType(FIRST_EVENT_TYPE firstEventType) {
        this.firstEventType = firstEventType;
    }

    public boolean isBatch() {
        boolean isBatch = false;
        if (getFirstEventType() != null) {

            isBatch = getFirstEventType().equals(FIRST_EVENT_TYPE.BATCH);
        }
        return isBatch;
    }

    public boolean isStream() {
        return getFirstEventType().equals(FIRST_EVENT_TYPE.STREAM);
    }

    private Set<RootFlowFile> relatedRootFlowFiles = new HashSet<>();


    public void addRootFileActiveChild(String flowFileId) {
        if (this.isRootFlowFile()) {
            getRootFlowFileActiveChildren().add(flowFileId);
            log.debug("adding active child {} to root {}. size: {} ", flowFileId, this.getId(), getRootFlowFileActiveChildren().size());
        }
    }

    public void removeRootFileActiveChild(String flowFileId) {
        if (this.isRootFlowFile()) {
            getRootFlowFileActiveChildren().remove(flowFileId);
            log.debug("removing active child {} from root {}. size: {} ", flowFileId, this.getId(), getRootFlowFileActiveChildren().size());
        }
    }

    public boolean hasActiveRootChildren() {
        return this.isRootFlowFile() && !getRootFlowFileActiveChildren().isEmpty();
    }

    public Set<String> getRootFlowFileActiveChildren() {
        if (rootFlowFileActiveChildren == null) {
            rootFlowFileActiveChildren = new HashSet<>();
        }
        return rootFlowFileActiveChildren;
    }

    public void setRootFlowFileActiveChildren(Set<String> rootFlowFileActiveChildren) {
        this.rootFlowFileActiveChildren = rootFlowFileActiveChildren;
    }


    public RootFlowFile(ActiveFlowFile flowFile) {
        super(flowFile.getId());
        this.rootFlowFileActiveChildren = new HashSet<>();
        this.flowFile = flowFile;
    }

    @Override
    public void assignFeedInformation(String feedName, String feedProcessGroupId) {
        flowFile.assignFeedInformation(feedName, feedProcessGroupId);
    }

    @Override
    public boolean hasFeedInformationAssigned() {
        return flowFile.hasFeedInformationAssigned();
    }

    @Override
    public String getFeedName() {
        return flowFile.getFeedName();
    }

    @Override
    public String getFeedProcessGroupId() {
        return flowFile.getFeedProcessGroupId();
    }

    @Override
    public RootFlowFile getRootFlowFile() {
        return flowFile.getRootFlowFile();
    }

    @Override
    public ActiveFlowFile addParent(ActiveFlowFile flowFile) {
        return this.flowFile.addParent(flowFile);
    }

    @Override
    public ActiveFlowFile addChild(ActiveFlowFile flowFile) {
        return this.flowFile.addChild(flowFile);
    }

    @Override
    public ActiveFlowFile getFirstParent() {
        return flowFile.getFirstParent();
    }

    @Override
    public boolean hasParents() {
        return flowFile.hasParents();
    }

    @Override
    public Set<ActiveFlowFile> getParents() {
        return flowFile.getParents();
    }

    @Override
    public Set<ActiveFlowFile> getChildren() {
        return flowFile.getChildren();
    }

    @Override
    public Set<ActiveFlowFile> getAllChildren() {
        return flowFile.getAllChildren();
    }

    @Override
    public ProvenanceEventRecordDTO getFirstEvent() {
        return flowFile.getFirstEvent();
    }

    @Override
    public void setFirstEvent(ProvenanceEventRecordDTO firstEvent) {
        flowFile.setFirstEvent(firstEvent);
    }

    @Override
    public boolean hasFirstEvent() {
        return flowFile.hasFirstEvent();
    }

    @Override
    public void completeEndingProcessor() {
        flowFile.completeEndingProcessor();
    }

    @Override
    public void markAsRootFlowFile() {
        flowFile.markAsRootFlowFile();
    }

    @Override
    public boolean isRootFlowFile() {
        return flowFile.isRootFlowFile();
    }

    @Override
    public void addFailedEvent(ProvenanceEventRecordDTO event) {
        flowFile.addFailedEvent(event);
    }

    @Override
    public Set<ProvenanceEventRecordDTO> getFailedEvents(boolean inclusive) {
        return flowFile.getFailedEvents(inclusive);
    }

    public boolean hasFailedEvents() {
        return !getFailedEvents(false).isEmpty();
    }

    @Override
    public String getId() {
        return flowFile.getId();
    }

    @Override
    public AtomicBoolean getFlowFileCompletionStatsCollected() {
        return flowFile.getFlowFileCompletionStatsCollected();
    }

    @Override
    public void setFlowFileCompletionStatsCollected(boolean flowFileCompletionStatsCollected) {
        flowFile.setFlowFileCompletionStatsCollected(flowFileCompletionStatsCollected);
    }

    @Override
    public boolean isStartOfCurrentFlowFile(ProvenanceEventRecordDTO event) {
        return flowFile.isStartOfCurrentFlowFile(event);
    }

    @Override
    public void setPreviousEvent(ProvenanceEventRecordDTO event) {
        flowFile.setPreviousEvent(event);
    }

    @Override
    public String summary() {
        return flowFile.summary();
    }

    @Override
    public boolean equals(Object o) {
        return flowFile.equals(o);
    }

    @Override
    public int hashCode() {
        return flowFile.hashCode();
    }

    @Override
    public Set<String> getCompletedProcessorIds() {
        return flowFile.getCompletedProcessorIds();
    }

    @Override
    public List<ProvenanceEventRecordDTO> getCompletedEvents() {
        return flowFile.getCompletedEvents();
    }

    @Override
    public List<ProvenanceEventRecordDTO> getCompletedEventsForProcessorId(String processorId) {
        return flowFile.getCompletedEventsForProcessorId(processorId);
    }

    @Override
    public ProvenanceEventRecordDTO getFirstCompletedEventsForProcessorId(String processorId) {
        return flowFile.getFirstCompletedEventsForProcessorId(processorId);
    }

    @Override
    public void addCompletionEvent(ProvenanceEventRecordDTO event) {
        flowFile.addCompletionEvent(event);
    }

    @Override
    public void checkAndMarkIfFlowFileIsComplete(ProvenanceEventRecordDTO event) {
        flowFile.checkAndMarkIfFlowFileIsComplete(event);
    }

    @Override
    public boolean isCurrentFlowFileComplete() {
        return flowFile.isCurrentFlowFileComplete();
    }

    @Override
    public void setCurrentFlowFileComplete(boolean currentFlowFileComplete) {
        flowFile.setCurrentFlowFileComplete(currentFlowFileComplete);
    }

    @Override
    public ProvenanceEventRecordDTO getLastEvent() {
        return flowFile.getLastEvent();
    }

    @Override
    public boolean isFlowComplete() {
        return flowFile.isFlowComplete();
    }

    @Override
    public DateTime getTimeCompleted() {
        return flowFile.getTimeCompleted();
    }

    @Override
    public String toString() {
        return flowFile.toString();
    }

    @Override
    public IdReferenceFlowFile toIdReferenceFlowFile() {
        return flowFile.toIdReferenceFlowFile();
    }

    @Override
    public void findEventMatchingDestinationConnection(String connectionIdentifier) {
        flowFile.findEventMatchingDestinationConnection(connectionIdentifier);
    }


    public Set<RootFlowFile> getRelatedRootFlowFiles() {
        return relatedRootFlowFiles;
    }

    public void addRelatedRootFlowFiles(Set<RootFlowFile> rootFlowFiles) {
        this.relatedRootFlowFiles.addAll(rootFlowFiles);
    }

    public void addRelatedRootFlowFile(RootFlowFile rootFlowFile) {
        this.relatedRootFlowFiles.add(rootFlowFile);
    }

    public boolean areRelatedRootFlowFilesCompleteOld() {
        if (getRelatedRootFlowFiles() != null || getRelatedRootFlowFiles().isEmpty()) {
            return true;
        } else {
            boolean allComplete = isFlowComplete();
            if (allComplete) {
                for (RootFlowFile rootFlowFile : getRelatedRootFlowFiles()) {
                    allComplete &= !rootFlowFile.equals(this) && rootFlowFile.isFlowComplete();
                }
            }
            return allComplete;

        }
    }

    public boolean isFlowAndRelatedRootFlowFilesComplete() {
        boolean thisComplete = isFlowComplete();
        if (thisComplete) {

            boolean relatedCompleted = false;
            if (getRootFlowFile().getRelatedRootFlowFiles().isEmpty()) {
                relatedCompleted = true;
            } else {
                relatedCompleted = getRelatedRootFlowFiles().stream().filter(ff -> !ff.equals(this)).allMatch(ff2 -> ff2.isFlowComplete());
                if (relatedCompleted) {
                    DateTime now = DateTime.now();
                    getRootFlowFile().getRelatedRootFlowFiles().stream().forEach(ff -> ff.setMinimiumExpireTime(now));
                    this.setMinimiumExpireTime(now);
                }
            }
            thisComplete &= relatedCompleted;
        }
        return thisComplete;
    }

    /**
     * Checks this flow and any related flow files for failure events
     */
    public boolean hasAnyFailures() {
        boolean failures = this.hasFailedEvents();
        if (!failures && !getRootFlowFile().getRelatedRootFlowFiles().isEmpty()) {

            failures &= getRelatedRootFlowFiles().stream().anyMatch(ff -> ff.hasFailedEvents());
        }
        return failures;
    }


    public void setMinimiumExpireTime(DateTime minimiumExpireTime) {
        if (minimiumExpireTime == null) {
            this.minimiumExpireTime = minimiumExpireTime;
        }
    }

    public DateTime getMinimiumExpireTime() {
        return minimiumExpireTime;
    }
}
