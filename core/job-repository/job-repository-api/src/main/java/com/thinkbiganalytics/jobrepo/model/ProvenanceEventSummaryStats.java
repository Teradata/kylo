package com.thinkbiganalytics.jobrepo.model;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface ProvenanceEventSummaryStats {

    String getProcessorName();

    String getFeedName();

    String getProcessorId();

    String getId();

    String getFeedProcessGroupId();

    String getCollectionId();

    Long getDuration();

    Long getBytesIn();

    Long getBytesOut();

    Long getTotalCount();

    Long getJobsStarted();

    Long getJobsFinished();

    Long getJobsFailed();

    Long getJobDuration();

    Long getSuccessfulJobDuration();

    Long getProcessorsFailed();

    Long getFlowFilesStarted();

    Long getFlowFilesFinished();

    DateTime getCollectionTime();

    DateTime getMinEventTime();

    DateTime getMaxEventTime();
}
