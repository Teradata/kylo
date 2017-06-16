package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.google.common.collect.ImmutableList;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.common.constants.CheckDataStepConstants;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.metadata.api.SearchCriteria;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobInstance;
import com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecutionProvider;
import com.thinkbiganalytics.metadata.config.RoleSetExposingSecurityExpressionRoot;
import com.thinkbiganalytics.metadata.jpa.feed.FeedAclIndexQueryAugmentor;
import com.thinkbiganalytics.metadata.jpa.feed.JpaOpsManagerFeed;
import com.thinkbiganalytics.metadata.jpa.feed.OpsManagerFeedRepository;
import com.thinkbiganalytics.metadata.jpa.feed.QJpaOpsManagerFeed;
import com.thinkbiganalytics.metadata.jpa.feed.QOpsManagerFeedId;
import com.thinkbiganalytics.metadata.jpa.feed.security.JpaFeedOpsAclEntry;
import com.thinkbiganalytics.metadata.jpa.feed.security.QJpaFeedOpsAclEntry;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiEventJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiRelatedRootFlowFiles;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiRelatedRootFlowFilesRepository;
import com.thinkbiganalytics.metadata.jpa.support.CommonFilterTranslations;
import com.thinkbiganalytics.metadata.jpa.support.GenericQueryDslFilter;
import com.thinkbiganalytics.metadata.jpa.support.QueryDslFetchJoin;
import com.thinkbiganalytics.metadata.jpa.support.QueryDslPagingSupport;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;


/**
 * Provider for the {@link JpaBatchJobExecution}
 */
@Service
public class JpaBatchJobExecutionProvider extends QueryDslPagingSupport<JpaBatchJobExecution> implements BatchJobExecutionProvider {

    private static final Logger log = LoggerFactory.getLogger(JpaBatchJobExecutionProvider.class);

    private static String PARAM_TB_JOB_TYPE = "tb.jobType";


    @Autowired
    private JPAQueryFactory factory;

    private BatchJobExecutionRepository jobExecutionRepository;

    private BatchJobInstanceRepository jobInstanceRepository;

    private BatchJobParametersRepository jobParametersRepository;


    private OpsManagerFeedRepository opsManagerFeedRepository;


    private NifiRelatedRootFlowFilesRepository relatedRootFlowFilesRepository;

    @Inject
    private BatchStepExecutionProvider batchStepExecutionProvider;

    @Inject
    private AccessController controller;

    @Autowired
    public JpaBatchJobExecutionProvider(BatchJobExecutionRepository jobExecutionRepository, BatchJobInstanceRepository jobInstanceRepository,
                                        NifiRelatedRootFlowFilesRepository relatedRootFlowFilesRepository,
                                        BatchJobParametersRepository jobParametersRepository,
                                        OpsManagerFeedRepository opsManagerFeedRepository
    ) {
        super(JpaBatchJobExecution.class);
        this.jobExecutionRepository = jobExecutionRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.relatedRootFlowFilesRepository = relatedRootFlowFilesRepository;
        this.jobParametersRepository = jobParametersRepository;
        this.opsManagerFeedRepository = opsManagerFeedRepository;

    }


    @Override
    public BatchJobInstance createJobInstance(ProvenanceEventRecordDTO event) {

        JpaBatchJobInstance jobInstance = new JpaBatchJobInstance();
        jobInstance.setJobKey(jobKeyGenerator(event));
        jobInstance.setJobName(event.getFeedName());
        //wire this instance to the Feed
        OpsManagerFeed feed = opsManagerFeedRepository.findByName(event.getFeedName());
        jobInstance.setFeed(feed);
        BatchJobInstance batchJobInstance = this.jobInstanceRepository.save(jobInstance);
        return batchJobInstance;
    }


    /**
     * Generate a Unique key for the Job Instance table This code is similar to what was used by Spring Batch
     */
    private String jobKeyGenerator(ProvenanceEventRecordDTO event) {

        StringBuffer stringBuffer = new StringBuffer(event.getEventTime() + "").append(event.getFlowFileUuid());
        MessageDigest digest1;
        try {
            digest1 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException var10) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
        }

        try {
            byte[] e1 = digest1.digest(stringBuffer.toString().getBytes("UTF-8"));
            return String.format("%032x", new Object[]{new BigInteger(1, e1)});
        } catch (UnsupportedEncodingException var9) {
            throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
        }

    }

    /**
     * Crate a new job exection from a provenance event
     *
     * @param event the provenance event indicating it is the start of a job
     * @return the job execution
     */
    private JpaBatchJobExecution createNewJobExecution(ProvenanceEventRecordDTO event) {
        BatchJobInstance jobInstance = createJobInstance(event);
        JpaBatchJobExecution jobExecution = createJobExecution(jobInstance, event);

        return jobExecution;
    }


    /**
     * Create a new Job Execution record from a given Provenance Event
     *
     * @param jobInstance the job instance to relate this job execution to
     * @param event       the event that started this job execution
     * @return the job execution
     */
    private JpaBatchJobExecution createJobExecution(BatchJobInstance jobInstance, ProvenanceEventRecordDTO event) {

        JpaBatchJobExecution jobExecution = new JpaBatchJobExecution();
        jobExecution.setJobInstance(jobInstance);
        //add in the parameters from the attributes
        jobExecution.setCreateTime(DateTimeUtil.getNowUTCTime());
        jobExecution.setStartTime(DateTimeUtil.convertToUTC(event.getEventTime()));
        jobExecution.setStatus(BatchJobExecution.JobStatus.STARTED);
        jobExecution.setExitCode(ExecutionConstants.ExitCode.EXECUTING);
        jobExecution.setLastUpdated(DateTimeUtil.getNowUTCTime());

        //create the job params
        Map<String, Object> jobParameters = new HashMap<>();
        if (event.isStartOfJob() && event.getAttributeMap() != null) {
            jobParameters = new HashMap<>(event.getAttributeMap());
        } else {
            jobParameters = new HashMap<>();
        }

        //save the params
        JpaNifiEventJobExecution eventJobExecution = new JpaNifiEventJobExecution(jobExecution, event.getEventId(), event.getJobFlowFileId());
        jobExecution.setNifiEventJobExecution(eventJobExecution);
        jobExecution = this.jobExecutionRepository.save(jobExecution);
        //bootstrap the feed parameters
        jobParameters.put(FeedConstants.PARAM__FEED_NAME, event.getFeedName());
        jobParameters.put(FeedConstants.PARAM__JOB_TYPE, FeedConstants.PARAM_VALUE__JOB_TYPE_FEED);
        Set<JpaBatchJobExecutionParameter> jpaJobParameters = addJobParameters(jobExecution, jobParameters);
        this.jobParametersRepository.save(jpaJobParameters);
        return jobExecution;
    }

    /**
     * add parameters to a job execution
     *
     * @param jobExecution  the job execution
     * @param jobParameters the parameters to add to the {@code jobExecution}
     * @return the newly created job parameters
     */
    private Set<JpaBatchJobExecutionParameter> addJobParameters(JpaBatchJobExecution jobExecution, Map<String, Object> jobParameters) {
        Set<JpaBatchJobExecutionParameter> jobExecutionParametersList = new HashSet<>();
        for (Map.Entry<String, Object> entry : jobParameters.entrySet()) {
            JpaBatchJobExecutionParameter jobExecutionParameters = jobExecution.addParameter(entry.getKey(), entry.getValue());
            jobExecutionParametersList.add(jobExecutionParameters);
        }
        return jobExecutionParametersList;
    }


    /**
     * if a event is a Merge (JOIN event) that merges other Root flow files (other JobExecutions) it will contain this relationship. These files need to be related together to determine when the final
     * job is complete.
     *
     * @param event     the event that indicates it is related to other job executions
     * @param nifiEvent the persisted event
     */
    private void checkAndRelateJobs(ProvenanceEventRecordDTO event, NifiEvent nifiEvent) {
        //if (event.getFeedFlowFile() != null && event.getFeedFlowFile().hasRelatedBatchFlows() && event.isFinalJobEvent() && event.getJobFlowFileId().equalsIgnoreCase(event.getStreamingBatchFeedFlowFileId())) {
            //relate the files together
            List<JpaNifiRelatedRootFlowFiles> relatedRootFlowFiles = new ArrayList<>();
            String relationId = UUID.randomUUID().toString();
            //for (String flowFile : event.getFeedFlowFile().getRelatedBatchFeedFlows()) {
            //    JpaNifiRelatedRootFlowFiles nifiRelatedRootFlowFile = new JpaNifiRelatedRootFlowFiles(nifiEvent, flowFile, relationId);
            //    relatedRootFlowFiles.add(nifiRelatedRootFlowFile);
           // }
            relatedRootFlowFilesRepository.save(relatedRootFlowFiles);
      //  }
    }

    /**
     * Check to see if the NifiEvent has the attributes indicating it is a Check Data Job
     */
    private boolean isCheckDataJob(ProvenanceEventRecordDTO event) {
        if (event.getAttributeMap() != null) {
            String jobType = event.getAttributeMap().get(NIFI_JOB_TYPE_PROPERTY);
            if (StringUtils.isBlank(jobType)) {
                jobType = event.getAttributeMap().get(NIFI_KYLO_JOB_TYPE_PROPERTY);
            }
            return StringUtils.isNotBlank(jobType) && FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK.equalsIgnoreCase(jobType);
        }
        return false;
    }

    /**
     * Sets the Job Execution params to either Check Data or Feed Jobs
     */
    private boolean updateJobType(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {

        if (event.getUpdatedAttributes() != null && (event.getUpdatedAttributes().containsKey(NIFI_JOB_TYPE_PROPERTY) || event.getUpdatedAttributes().containsKey(NIFI_KYLO_JOB_TYPE_PROPERTY))) {
            String jobType = event.getUpdatedAttributes().get(NIFI_JOB_TYPE_PROPERTY);
            if (StringUtils.isBlank(jobType)) {
                jobType = event.getUpdatedAttributes().get(NIFI_KYLO_JOB_TYPE_PROPERTY);
            }
            String nifiCategory = event.getAttributeMap().get(NIFI_CATEGORY_PROPERTY);
            String nifiFeedName = event.getAttributeMap().get(NIFI_FEED_PROPERTY);
            String feedName = FeedNameUtil.fullName(nifiCategory, nifiFeedName);
            if (FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK.equalsIgnoreCase(jobType)) {
                Set<JpaBatchJobExecutionParameter> updatedParams = ((JpaBatchJobExecution) jobExecution).setAsCheckDataJob(feedName);
                jobParametersRepository.save(updatedParams);

                //update feed type
                JpaOpsManagerFeed checkDataFeed = (JpaOpsManagerFeed) opsManagerFeedRepository.findByName(event.getFeedName());
                checkDataFeed.setFeedType(OpsManagerFeed.FeedType.CHECK);
                //relate to this feed
                JpaOpsManagerFeed feedToCheck = (JpaOpsManagerFeed) opsManagerFeedRepository.findByName(feedName);
                feedToCheck.getCheckDataFeeds().add(checkDataFeed);

                return true;
            }
        }
        return false;
    }

    /**
     * When the job is complete determine its status, write out execution context, and determine if all related jobs are complete
     */
    private void finishJob(ProvenanceEventRecordDTO event, JpaBatchJobExecution jobExecution) {

        if (jobExecution.getJobExecutionId() == null) {
            log.error("Warning execution id is null for ending event {} ", event);
        }
        if (event.isFailure()) {  //event.hasFailureEvents
            jobExecution.failJob();
        } else {
            jobExecution.completeJob();
        }

        //ensure check data jobs are property failed if they dont pass
        if (isCheckDataJob(event)) {
            String valid = event.getAttributeMap().get(CheckDataStepConstants.VALIDATION_KEY);
            String msg = event.getAttributeMap().get(CheckDataStepConstants.VALIDATION_MESSAGE_KEY);
            if (StringUtils.isNotBlank(valid)) {
                if (!BooleanUtils.toBoolean(valid)) {
                    jobExecution.failJob();
                }
            }
            if (StringUtils.isNotBlank(msg)) {
                jobExecution.setExitMessage(msg);
            }
        }

        jobExecution.setEndTime(DateTimeUtil.convertToUTC(event.getEventTime()));
        log.info("Finishing Job: {} with a status of: {} for event: {} ", jobExecution.getJobExecutionId(), jobExecution.getStatus(), event.getEventId());
        //add in execution contexts
        Map<String, String> allAttrs = event.getAttributeMap();
        if (allAttrs != null && !allAttrs.isEmpty()) {
            for (Map.Entry<String, String> entry : allAttrs.entrySet()) {
                JpaBatchJobExecutionContextValue executionContext = new JpaBatchJobExecutionContextValue(jobExecution, entry.getKey());
                executionContext.setStringVal(entry.getValue());
                jobExecution.addJobExecutionContext(executionContext);

                if (entry.getKey().equals(BatchJobExecutionProvider.NIFI_JOB_EXIT_DESCRIPTION_PROPERTY)) {
                    String msg = jobExecution.getExitMessage() != null ? jobExecution.getExitMessage() + "\n" : "";
                    msg += entry.getValue();
                    jobExecution.setExitMessage(msg);
                }
            }
        }

    }

    public JpaBatchJobExecution findJobExecution(ProvenanceEventRecordDTO event) {
        return jobExecutionRepository.findByFlowFile(event.getJobFlowFileId());
    }


    /**
     * Get or Create the JobExecution for a given ProvenanceEvent
     */
    @Override
    public synchronized JpaBatchJobExecution getOrCreateJobExecution(ProvenanceEventRecordDTO event) {
        if(event.isStream()){
            return getOrCreateStreamJobExecution(event);
        }
        else {
            return getOrCreateBatchJobExecution(event);
        }

    }



    private JpaBatchJobExecution getOrCreateBatchJobExecution(ProvenanceEventRecordDTO event) {
        JpaBatchJobExecution jobExecution = null;
        boolean isNew = false;
        try {
            jobExecution = jobExecutionRepository.findByFlowFile(event.getJobFlowFileId());
            if (jobExecution == null) {
                jobExecution = createNewJobExecution(event);
                isNew = true;
            }
        } catch (OptimisticLockException e) {
            //read
            jobExecution = jobExecutionRepository.findByFlowFile(event.getJobFlowFileId());
        }

        //if the attrs coming in change the type to a CHECK job then update the entity
        boolean updatedJobType = updateJobType(jobExecution, event);
        boolean save = isNew || updatedJobType;
        if (event.isFinalJobEvent()) {
            finishJob(event, jobExecution);
            save = true;
        }

        //if the event is the start of the Job, but the job execution was created from another downstream event, ensure the start time and event are related correctly
        if (event.isStartOfJob() && !isNew) {
            jobExecution.getNifiEventJobExecution().setEventId(event.getEventId());
            jobExecution.setStartTime(DateTimeUtil.convertToUTC(event.getEventTime()));
            //create the job params
            Map<String, Object> jobParameters = new HashMap<>();
            if (event.isStartOfJob() && event.getAttributeMap() != null) {
                jobParameters = new HashMap<>(event.getAttributeMap());
            } else {
                jobParameters = new HashMap<>();
            }

            this.jobParametersRepository.save(addJobParameters(jobExecution, jobParameters));
            save = true;
        }
        if (save) {
            jobExecutionRepository.save(jobExecution);
        }
        return jobExecution;
    }


    private JpaBatchJobExecution getOrCreateStreamJobExecution(ProvenanceEventRecordDTO event) {
        JpaBatchJobExecution jobExecution = null;
        boolean isNew = false;
        try {
            List<JpaBatchJobExecution> jobExecutions = jobExecutionRepository.findLatestJobForFeed(event.getFeedName());
            if (jobExecution == null && jobExecutions.isEmpty()) {
                jobExecution = createNewJobExecution(event);
                isNew = true;
            }
            else {
                if(jobExecutions != null){
                    jobExecution = jobExecutions.get(0);
                }
            }
        } catch (OptimisticLockException e) {
            //read
            List<JpaBatchJobExecution> jobExecutions = jobExecutionRepository.findLatestJobForFeed(event.getFeedName());
            if(jobExecutions != null && !jobExecutions.isEmpty()){
                jobExecution = jobExecutions.get(0);
            }
        }

        //if the attrs coming in change the type to a CHECK job then update the entity
        boolean updatedJobType = updateJobType(jobExecution, event);
        boolean save = isNew || updatedJobType;
        if (event.isFinalJobEvent()) {
            finishJob(event, jobExecution);
            save = true;
        }

        //if the event is the start of the Job, but the job execution was created from another downstream event, ensure the start time and event are related correctly
        if (event.isStartOfJob() && !isNew && jobExecution != null) {
            jobExecution.getNifiEventJobExecution().setEventId(event.getEventId());
            jobExecution.setStartTime(DateTimeUtil.convertToUTC(event.getEventTime()));
            //create the job params
            Map<String, Object> jobParameters = new HashMap<>();
            if (event.isStartOfJob() && event.getAttributeMap() != null) {
                jobParameters = new HashMap<>(event.getAttributeMap());
            } else {
                jobParameters = new HashMap<>();
            }

            this.jobParametersRepository.save(addJobParameters(jobExecution, jobParameters));
            save = true;
        }
        if (save) {
            jobExecutionRepository.save(jobExecution);
        }
        return jobExecution;
    }





    @Override
    public BatchJobExecution save(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event, NifiEvent nifiEvent) {
        if (jobExecution == null) {
            return null;
        }
        JpaBatchJobExecution jpaBatchJobExecution = (JpaBatchJobExecution) jobExecution;
      //  checkAndRelateJobs(event, nifiEvent);
        batchStepExecutionProvider.createStepExecution(jobExecution, event);
        if (jobExecution.isFinished()) {
            //ensure failures
            batchStepExecutionProvider.ensureFailureSteps(jpaBatchJobExecution);
        }
        return jobExecution;
    }

    /**
     * Save the job execution in the database
     *
     * @return the saved job execution
     */
    @Override
    public BatchJobExecution save(ProvenanceEventRecordDTO event, NifiEvent nifiEvent) {
        JpaBatchJobExecution jobExecution = getOrCreateJobExecution(event);
        return save(jobExecution, event, nifiEvent);

    }

    /**
     * Save a job execution to the database
     *
     * @return the saved job execution
     */
    @Override
    public BatchJobExecution save(BatchJobExecution jobExecution) {
        return jobExecutionRepository.save((JpaBatchJobExecution) jobExecution);
    }

    @Override
    public BatchJobExecution findByJobExecutionId(Long jobExecutionId) {
        return jobExecutionRepository.findOne(jobExecutionId);
    }

    /**
     * Find all the job executions for a feed that have been completed since a given date
     *
     * @param feedName  the feed to check
     * @param sinceDate the min end date for the jobs on the {@code feedName}
     * @return the job executions for a feed that have been completed since a given date
     */
    @Override
    public Set<? extends BatchJobExecution> findJobsForFeedCompletedSince(String feedName, DateTime sinceDate) {
        return jobExecutionRepository.findJobsForFeedCompletedSince(feedName, sinceDate);
    }

    @Override
    public BatchJobExecution findLatestCompletedJobForFeed(String feedName) {
        List<JpaBatchJobExecution> jobExecutions = jobExecutionRepository.findLatestCompletedJobForFeed(feedName);
        if (jobExecutions != null && !jobExecutions.isEmpty()) {
            return jobExecutions.get(0);
        } else {
            return null;
        }
    }

    @Override
    public BatchJobExecution findLatestJobForFeed(String feedName) {
        List<JpaBatchJobExecution> jobExecutions = jobExecutionRepository.findLatestJobForFeed(feedName);
        if (jobExecutions != null && !jobExecutions.isEmpty()) {
            return jobExecutions.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Boolean isFeedRunning(String feedName) {
        return jobExecutionRepository.isFeedRunning(feedName);
    }


    /**
     * Find all BatchJobExecution objects with the provided filter. the filter needs to match
     *
     * @return a paged result set of all the job executions matching the incoming filter
     */
    @Override
    public Page<? extends BatchJobExecution> findAll(String filter, Pageable pageable) {
        QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;
        //if the filter contains a filter on the feed then delegate to the findAllForFeed method to include any check data jobs
        List<SearchCriteria> searchCriterias = GenericQueryDslFilter.parseFilterString(filter);
        SearchCriteria feedFilter = searchCriterias.stream().map(searchCriteria -> searchCriteria.withKey(CommonFilterTranslations.resolvedFilter(jobExecution, searchCriteria.getKey()))).filter(
            sc -> sc.getKey().equalsIgnoreCase(CommonFilterTranslations.jobExecutionFeedNameFilterKey)).findFirst().orElse(null);
        if (feedFilter != null && feedFilter.getPreviousSearchCriteria() != null && !feedFilter.isValueCollection()) {
            //remove the feed filter from the list and filter by this feed
            searchCriterias.remove(feedFilter.getPreviousSearchCriteria());
            String feedValue = feedFilter.getValue().toString();
            //remove any quotes around the feedValue
            feedValue = feedValue.replaceAll("^\"|\"$", "");
            return findAllForFeed(feedValue, searchCriterias, pageable);
        } else {
            pageable = CommonFilterTranslations.resolveSortFilters(jobExecution, pageable);
            QJpaBatchJobInstance jobInstancePath = new QJpaBatchJobInstance("jobInstance");
            QJpaOpsManagerFeed feedPath = new QJpaOpsManagerFeed("feed");

            return findAllWithFetch(jobExecution,
                                    GenericQueryDslFilter.buildFilter(jobExecution, filter).and(augment(feedPath.id)),
                                    pageable,
                                    QueryDslFetchJoin.innerJoin(jobExecution.nifiEventJobExecution),
                                    QueryDslFetchJoin.innerJoin(jobExecution.jobInstance, jobInstancePath),
                                    QueryDslFetchJoin.innerJoin(jobInstancePath.feed, feedPath)
            );
        }

    }

    private Predicate augment(QOpsManagerFeedId id) {
        return FeedAclIndexQueryAugmentor.generateExistsExpression(id, controller.isEntityAccessControlled());
    }


    private RoleSetExposingSecurityExpressionRoot getUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new RoleSetExposingSecurityExpressionRoot(authentication);
    }

    private Page<? extends BatchJobExecution> findAllForFeed(String feedName, List<SearchCriteria> filters, Pageable pageable) {
        QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;
        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        QJpaOpsManagerFeed checkDataFeed = new QJpaOpsManagerFeed("checkDataFeed");
        QJpaBatchJobInstance jobInstance = QJpaBatchJobInstance.jpaBatchJobInstance;
        JPQLQuery checkFeedQuery = JPAExpressions.select(checkDataFeed.id).from(feed).join(feed.checkDataFeeds, checkDataFeed).where(feed.name.eq(feedName));


        JPAQuery
            query = factory.select(jobExecution)
            .from(jobExecution)
            .join(jobExecution.jobInstance, jobInstance)
            .join(jobInstance.feed, feed)
            .where((feed.name.eq(feedName).or(feed.id.in(checkFeedQuery)))
                       .and(GenericQueryDslFilter.buildFilter(jobExecution, filters)
                       .and(augment(feed.id))))
                .fetchAll();

        pageable = CommonFilterTranslations.resolveSortFilters(jobExecution, pageable);
        return findAll(query, pageable);
    }


    /**
     * Get count of Jobs grouped by Status
     */
    @Override
    public List<JobStatusCount> getJobStatusCount(String filter) {

        QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;

        QJpaBatchJobInstance jobInstance = QJpaBatchJobInstance.jpaBatchJobInstance;

        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;

        List<BatchJobExecution.JobStatus> runningStatus = ImmutableList.of(BatchJobExecution.JobStatus.STARTED, BatchJobExecution.JobStatus.STARTING);

        com.querydsl.core.types.dsl.StringExpression jobState = new CaseBuilder().when(jobExecution.status.eq(BatchJobExecution.JobStatus.FAILED)).then("FAILED")
            .when(jobExecution.status.in(runningStatus)).then("RUNNING")
            .otherwise(jobExecution.status.stringValue());

        BooleanBuilder whereBuilder = new BooleanBuilder();
        if (StringUtils.isNotBlank(filter)) {
            whereBuilder.and(GenericQueryDslFilter.buildFilter(jobExecution, filter));
        }

        ConstructorExpression<JpaBatchJobExecutionStatusCounts> expr =
            Projections.constructor(JpaBatchJobExecutionStatusCounts.class,
                                    jobState.as("status"),
                                    jobExecution.jobExecutionId.count().as("count"));

        JPAQuery<?> query = factory.select(expr)
            .from(jobExecution)
            .innerJoin(jobInstance).on(jobExecution.jobInstance.jobInstanceId.eq(jobInstance.jobInstanceId))
            .innerJoin(feed).on(jobInstance.feed.id.eq(feed.id))
            .where(whereBuilder
            .and(FeedAclIndexQueryAugmentor.generateExistsExpression(feed.id, controller.isEntityAccessControlled())))
            .groupBy(jobExecution.status);

        return (List<JobStatusCount>) query.fetch();
    }

    @Override
    public List<JobStatusCount> getJobStatusCountByDate() {

        QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;

        QJpaBatchJobInstance jobInstance = QJpaBatchJobInstance.jpaBatchJobInstance;

        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;

        List<BatchJobExecution.JobStatus> runningStatus = ImmutableList.of(BatchJobExecution.JobStatus.STARTED, BatchJobExecution.JobStatus.STARTING);

        com.querydsl.core.types.dsl.StringExpression jobState = new CaseBuilder().when(jobExecution.status.eq(BatchJobExecution.JobStatus.FAILED)).then("FAILED")
            .when(jobExecution.status.in(runningStatus)).then("RUNNING")
            .otherwise(jobExecution.status.stringValue());

        JPAQuery
            query = factory.select(
            Projections.constructor(JpaBatchJobExecutionStatusCounts.class,
                                    jobState.as("status"),
                                    jobExecution.startYear,
                                    jobExecution.startMonth,
                                    jobExecution.startDay,
                                    jobExecution.count().as("count")))
            .from(jobExecution)
            .innerJoin(jobInstance).on(jobExecution.jobInstance.jobInstanceId.eq(jobInstance.jobInstanceId))
            .innerJoin(feed).on(jobInstance.feed.id.eq(feed.id))
            .where(FeedAclIndexQueryAugmentor.generateExistsExpression(feed.id, controller.isEntityAccessControlled()))
            .groupBy(jobExecution.status, jobExecution.startYear, jobExecution.startMonth, jobExecution.startDay);

        return (List<JobStatusCount>) query.fetch();

    }

    /**
     * gets job executions grouped by status and Day looking back from Now - the supplied {@code period}
     *
     * @param period period to look back from the current time to get job execution status
     */
    @Override
    public List<JobStatusCount> getJobStatusCountByDateFromNow(ReadablePeriod period, String filter) {

        QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;

        QJpaBatchJobInstance jobInstance = QJpaBatchJobInstance.jpaBatchJobInstance;

        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;

        List<BatchJobExecution.JobStatus> runningStatus = ImmutableList.of(BatchJobExecution.JobStatus.STARTED, BatchJobExecution.JobStatus.STARTING);

        com.querydsl.core.types.dsl.StringExpression jobState = new CaseBuilder().when(jobExecution.status.eq(BatchJobExecution.JobStatus.FAILED)).then("FAILED")
            .when(jobExecution.status.in(runningStatus)).then("RUNNING")
            .otherwise(jobExecution.status.stringValue());

        BooleanBuilder whereBuilder = new BooleanBuilder();
        whereBuilder.and(jobExecution.startTime.goe(DateTimeUtil.getNowUTCTime().minus(period)));
        if (StringUtils.isNotBlank(filter)) {
            whereBuilder.and(GenericQueryDslFilter.buildFilter(jobExecution, filter));
        }

        JPAQuery
            query = factory.select(
            Projections.constructor(JpaBatchJobExecutionStatusCounts.class,
                                    jobState.as("status"),
                                    jobExecution.startYear,
                                    jobExecution.startMonth,
                                    jobExecution.startDay,
                                    jobExecution.count().as("count")))
            .from(jobExecution)
            .innerJoin(jobInstance).on(jobExecution.jobInstance.jobInstanceId.eq(jobInstance.jobInstanceId))
            .innerJoin(feed).on(jobInstance.feed.id.eq(feed.id))
            .where(whereBuilder
            .and(FeedAclIndexQueryAugmentor.generateExistsExpression(feed.id, controller.isEntityAccessControlled())))
            .groupBy(jobExecution.status, jobExecution.startYear, jobExecution.startMonth, jobExecution.startDay);

        return (List<JobStatusCount>) query.fetch();

    }


    public List<String> findRelatedFlowFiles(String flowFileId) {
        return relatedRootFlowFilesRepository.findRelatedFlowFiles(flowFileId);
    }



    /*
    public Page<? extends BatchJobExecution> findAllByExample(String filter, Pageable pageable){

        //construct new instance of JpaBatchExecution for searching
        JpaBatchJobExecution example = new JpaBatchJobExecution();
        example.setStatus(BatchJobExecution.JobStatus.valueOf("status"));
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withIgnoreCase().withIgnoreCase(true);

      return   jobExecutionRepository.findAll(Example.of(example,matcher),pageable);

    }
    */


}
