package com.thinkbiganalytics.metadata.api.jobrepo.job;

/**
 * Created by sr186054 on 12/1/16.
 */
public interface BatchJobExecutionFilters {

    String RUNNING_FILTER = "endTime==NULL";
    String ABANDONED_FILTER = "status=="+BatchJobExecution.JobStatus.ABANDONED.name();
    String COMPLETED_FILTER = "status=="+BatchJobExecution.JobStatus.COMPLETED.name();
    String STARTED_FILTER = "status=="+BatchJobExecution.JobStatus.STARTED.name();
    String STOPPED_FILTER = "status=="+BatchJobExecution.JobStatus.STOPPED.name();
    String FAILED_FILTER = "status=="+BatchJobExecution.JobStatus.FAILED.name();
    String RUNNING_OR_FAILED_FILTER = "status==\""+BatchJobExecution.JobStatus.STARTED.name()+","+BatchJobExecution.JobStatus.STARTING.name()+","+BatchJobExecution.JobStatus.FAILED.name()+"\"";

}
