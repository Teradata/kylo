package com.thinkbiganalytics.metadata.api.jobrepo.job;

import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchJobExecutionProvider {

    String NIFI_JOB_TYPE_PROPERTY = "tb.jobType";
    String NIFI_FEED_PROPERTY = "feed";
    String NIFI_CATEGORY_PROPERTY = "category";

    BatchJobInstance createJobInstance(ProvenanceEventRecordDTO event);

    BatchStepExecution save(ProvenanceEventRecordDTO event, NifiEvent nifiEvent);

    BatchJobExecution findByEventAndFlowFile(Long eventId, String flowfileid);

    BatchJobExecution failStepsInJobThatNeedToBeFailed(BatchJobExecution jobExecution);

    BatchJobExecution findByJobExecutionId(Long jobExecutionId);

    BatchJobExecution save(BatchJobExecution jobExecution);
}
