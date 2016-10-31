package com.thinkbiganalytics.metadata.api.jobrepo.job;

import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.joda.time.DateTime;

import java.util.Set;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchJobExecutionProvider {

    String NIFI_JOB_TYPE_PROPERTY = "tb.jobType";
    String NIFI_FEED_PROPERTY = "feed";
    String NIFI_CATEGORY_PROPERTY = "category";

    BatchJobInstance createJobInstance(ProvenanceEventRecordDTO event);

    BatchJobExecution save(ProvenanceEventRecordDTO event, NifiEvent nifiEvent);

    BatchJobExecution save(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event, NifiEvent nifiEvent);

    BatchJobExecution findByEventAndFlowFile(Long eventId, String flowfileid);

    //BatchJobExecution failStepsInJobThatNeedToBeFailed(BatchJobExecution jobExecution);

    BatchJobExecution findByJobExecutionId(Long jobExecutionId);

    BatchJobExecution save(BatchJobExecution jobExecution);

    /**
     *
     * @param event
     * @return
     */
    BatchJobExecution getOrCreateJobExecution(ProvenanceEventRecordDTO event);

    /**
     * Returns all completed JobExecution records that were started since {@code sinceDate}
     * @param feedName
     * @param sinceDate
     * @return
     */
    Set<? extends BatchJobExecution> findJobsForFeedCompletedSince(String feedName,  DateTime sinceDate);

    /**
     * Returns the Latest Completed JobExecution record for a feed
     * @param feedName
     * @return
     */
    BatchJobExecution findLatestCompletedJobForFeed(String feedName);
}
