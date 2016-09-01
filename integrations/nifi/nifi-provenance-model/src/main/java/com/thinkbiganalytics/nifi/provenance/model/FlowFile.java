package com.thinkbiganalytics.nifi.provenance.model;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Created by sr186054 on 8/20/16.
 */
public interface FlowFile<P extends FlowFile, E extends FlowFileEvent> extends Serializable {

    void assignFeedInformation(String feedName, String feedProcessGroupId);

    boolean hasFeedInformationAssigned();

    String getFeedName();

    String getFeedProcessGroupId();

    P addParent(P flowFile);

    P getRootFlowFile();

    P addChild(P flowFile);

    P getFirstParent();

    boolean hasParents();

    Set<P> getParents();

    Set<P> getChildren();

    Set<P> getAllChildren();

    E getFirstEvent();

    void setFirstEvent(E firstEvent);

    boolean hasFirstEvent();

    void completeEndingProcessor();

    void markAsRootFlowFile();

    boolean isRootFlowFile();

    void addFailedEvent(E event);

    Set<E> getFailedEvents(boolean inclusive);

    String getId();

    boolean isStartOfCurrentFlowFile(E event);

    String summary();

    Set<String> getCompletedProcessorIds();

    List<E> getCompletedEvents();

    void addCompletedEvent(E event);

    void checkAndMarkIfFlowFileIsComplete(E event);

    boolean isCurrentFlowFileComplete();

    boolean isFlowComplete();

    DateTime getTimeCompleted();

    IdReferenceFlowFile toIdReferenceFlowFile();

    List<ProvenanceEventRecordDTO> getPreviousEvents(ProvenanceEventRecordDTO event);
}
