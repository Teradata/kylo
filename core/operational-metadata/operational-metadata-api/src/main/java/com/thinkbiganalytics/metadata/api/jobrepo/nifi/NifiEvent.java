package com.thinkbiganalytics.metadata.api.jobrepo.nifi;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface NifiEvent {

    Long getEventId();

    String getFeedName();

    String getProcessorId();

    String getProcessorName();

    String getFeedProcessGroupId();

    String getEventDetails();

    DateTime getEventTime();

    String getFileSize();

    Long getFileSizeBytes();

    String getParentFlowFileIds();

    String getChildFlowFileIds();

    String getAttributesJson();

    String getSourceConnectionId();

    String getEventType();

    Long getEventDuration();

    String getJobFlowFileId();

    boolean isStartOfJob();

    boolean isEndOfJob();

    boolean isFailure();

    boolean isBatchJob();

    boolean isFinalJobEvent();

    boolean isHasFailureEvents();

    String getClusterNodeId();

    String getClusterNodeAddress();

}
