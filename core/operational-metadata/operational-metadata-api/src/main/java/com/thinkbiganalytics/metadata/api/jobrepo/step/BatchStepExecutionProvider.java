package com.thinkbiganalytics.metadata.api.jobrepo.step;

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import java.util.List;

/**
 * Created by sr186054 on 10/24/16.
 */
public interface BatchStepExecutionProvider {


    /**
     * Update a StepExecution record
     */
    BatchStepExecution update(BatchStepExecution stepExecution);

    /**
     * For a given JobExecution ensure the steps matching the nifi failed events are failed, if the Job is indicated as having failures
     */
    boolean ensureFailureSteps(BatchJobExecution jobExecution);

    /**
     * Create a new StepExecution record  from a Provenance Event Record
     */
    BatchStepExecution createStepExecution(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event);

    /**
     * When a step fails, get notified of the failure
     */
    void subscribeToFailedSteps(FailedStepExecutionListener listener);


    List<? extends BatchStepExecution> getSteps(Long jobExecutionId);

}
