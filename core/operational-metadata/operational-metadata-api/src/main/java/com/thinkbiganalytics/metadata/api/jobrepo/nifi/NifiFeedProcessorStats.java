package com.thinkbiganalytics.metadata.api.jobrepo.nifi;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface NifiFeedProcessorStats {

    String getProcessorName();

    void setProcessorName(String processorName);

    String getFeedName();

    void setFeedName(String feedName);

    String getProcessorId();

    void setProcessorId(String processorId);

    String getId();

    void setId(String id);

    String getFeedProcessGroupId();

    void setFeedProcessGroupId(String feedProcessGroupId);

    String getCollectionId();

    void setCollectionId(String collectionId);

    Long getDuration();

    void setDuration(Long duration);

    Long getBytesIn();

    void setBytesIn(Long bytesIn);

    Long getBytesOut();

    void setBytesOut(Long bytesOut);

    Long getTotalCount();

    void setTotalCount(Long totalCount);

    Long getJobsStarted();

    void setJobsStarted(Long jobsStarted);

    Long getJobsFinished();

    void setJobsFinished(Long jobsFinished);

    Long getJobsFailed();

    void setJobsFailed(Long jobsFailed);

    Long getJobDuration();

    void setJobDuration(Long jobDuration);

    Long getSuccessfulJobDuration();

    void setSuccessfulJobDuration(Long successfulJobDuration);

    Long getProcessorsFailed();

    void setProcessorsFailed(Long processorsFailed);

    Long getFlowFilesStarted();

    void setFlowFilesStarted(Long flowFilesStarted);

    Long getFlowFilesFinished();

    void setFlowFilesFinished(Long flowFilesFinished);

    DateTime getCollectionTime();

    void setCollectionTime(DateTime collectionTime);

    DateTime getMinEventTime();

    void setMinEventTime(DateTime minEventTime);

    DateTime getMaxEventTime();

    void setMaxEventTime(DateTime maxEventTime);

    Long getResultSetCount();

    void setResultSetCount(Long resultSetCount);
}
