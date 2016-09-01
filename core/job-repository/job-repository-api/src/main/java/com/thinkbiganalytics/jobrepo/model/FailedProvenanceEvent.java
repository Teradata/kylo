package com.thinkbiganalytics.jobrepo.model;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 8/30/16.
 */
public interface FailedProvenanceEvent {

    Long getEventId();

    String getFeedName();

    void setFeedName(String feedName);

    String getProcessorId();

    String getFlowFileId();

    String getProcessorName();

    String getFeedProcessGroupId();

    String getEventDetails();

    DateTime getEventTime();
}
