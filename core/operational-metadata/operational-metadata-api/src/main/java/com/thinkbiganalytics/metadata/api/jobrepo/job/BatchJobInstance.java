package com.thinkbiganalytics.metadata.api.jobrepo.job;

import java.util.List;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchJobInstance {

    Long getJobInstanceId();

    Long getVersion();

    String getJobName();

    String getJobKey();

    List<BatchJobExecution> getJobExecutions();
}
