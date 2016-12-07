package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 12/6/16.
 */
public interface FeedHealth {

    OpsManagerFeed.ID getFeedId();

    String getFeedName();

    Long getJobExecutionId();

    Long getJobInstanceId();

    DateTime getStartTime();

    DateTime getEndTime();

    BatchJobExecution.JobStatus getStatus();

    ExecutionConstants.ExitCode getExitCode();

    String getExitMessage();

    Long getAllCount();

    Long getFailedCount();

    Long getCompletedCount();

    Long getAbandonedCount();

    Long getRunningCount();
}
