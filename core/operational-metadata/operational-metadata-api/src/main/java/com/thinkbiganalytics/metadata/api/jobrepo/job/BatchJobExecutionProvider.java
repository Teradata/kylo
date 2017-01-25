package com.thinkbiganalytics.metadata.api.jobrepo.job;

/*-
 * #%L
 * thinkbig-operational-metadata-api
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchJobExecutionProvider extends BatchJobExecutionFilters{

    @Deprecated
    String NIFI_JOB_TYPE_PROPERTY = "tb.jobType";
    String NIFI_JOB_TYPE_PROPERTY2 = "kylo.jobType";
    String NIFI_FEED_PROPERTY = "feed";
    String NIFI_CATEGORY_PROPERTY = "category";
    String NIFI_JOB_EXIT_DESCRIPTION_PROPERTY = "kylo.jobExitDescription";



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

    Boolean isFeedRunning(String feedName);

    Page<? extends BatchJobExecution> findAll(String filter, Pageable pageable);

    Page<? extends BatchJobExecution> findAllForFeed(String feedName,String filter, Pageable pageable);

    List<JobStatusCount> getJobStatusCountByDate();

    List<JobStatusCount> getJobStatusCountByDateFromNow(ReadablePeriod period, String filter);

    List<JobStatusCount> getJobStatusCount(String filter);


}
