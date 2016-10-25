package com.thinkbiganalytics.metadata.api.jobrepo.step;

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;

/**
 * Listener called when a Step is marked as a Failure This is called before it is persisted. Listeners have the ability to modify/add data to the StepExecutionContext before it is persisted.
 */
public interface FailedStepExecutionListener {

    void failedStep(BatchJobExecution jobExecution, BatchStepExecution stepExecution, String flowFileId, String componentId);

}
