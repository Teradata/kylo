package com.thinkbiganalytics.metadata.api.jobrepo.job;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchJobExecutionContext {

    Long getJobExecutionId();

    String getShortContext();

    String getSerializedContext();
}
