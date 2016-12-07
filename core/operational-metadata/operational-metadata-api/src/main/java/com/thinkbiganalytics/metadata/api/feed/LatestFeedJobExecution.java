package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 12/3/16.
 */
public interface LatestFeedJobExecution {

    OpsManagerFeed getFeed();

    String getFeedName();

    BatchJobExecution getJobExecution();

    DateTime getStartTime();

    DateTime getEndTime();

    BatchJobExecution.JobStatus getStatus();

    ExecutionConstants.ExitCode getExitCode();

    String getExitMessage();

    Long getJobExecutionId();

    Long getJobInstanceId();
}
