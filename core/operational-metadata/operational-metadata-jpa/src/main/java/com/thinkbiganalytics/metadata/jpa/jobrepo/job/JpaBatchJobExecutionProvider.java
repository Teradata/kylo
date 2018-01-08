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

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.DefaultAlertChangeEventContent;
import com.thinkbiganalytics.cluster.ClusterMessage;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.ClusterServiceMessageReceiver;
import com.thinkbiganalytics.jobrepo.common.constants.CheckDataStepConstants;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.SearchCriteria;
import com.thinkbiganalytics.metadata.api.alerts.OperationalAlerts;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationBatchStatusChange;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchAndStreamingJobStatusCount;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobInstance;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchRelatedFlowFile;
import com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStats;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecutionProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.config.RoleSetExposingSecurityExpressionRoot;
import com.thinkbiganalytics.metadata.jpa.feed.FeedAclIndexQueryAugmentor;
import com.thinkbiganalytics.metadata.jpa.feed.JpaOpsManagerFeed;
import com.thinkbiganalytics.metadata.jpa.feed.OpsManagerFeedRepository;
import com.thinkbiganalytics.metadata.jpa.feed.QJpaOpsManagerFeed;
import com.thinkbiganalytics.metadata.jpa.feed.QOpsManagerFeedId;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiEventJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiRelatedRootFlowFilesRepository;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.QJpaNifiFeedStats;
import com.thinkbiganalytics.metadata.jpa.support.CommonFilterTranslations;
import com.thinkbiganalytics.metadata.jpa.support.GenericQueryDslFilter;
import com.thinkbiganalytics.metadata.jpa.support.JobStatusDslQueryExpressionBuilder;
import com.thinkbiganalytics.metadata.jpa.support.QueryDslFetchJoin;
import com.thinkbiganalytics.metadata.jpa.support.QueryDslPagingSupport;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.role.SecurityRole;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
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

    private BatchRelatedFlowFileRepository batchRelatedFlowFileRepository;

    @Inject
    private AccessController controller;

    @Inject
    private MetadataEventService eventService;

    @Inject
    private NifiFeedStatisticsProvider feedStatisticsProvider;


    @Inject
    @Named("kyloAlertManager")
    protected AlertManager alertManager;

    @Inject
    private AlertProvider provider;

    @Inject
    private JobExecutionChangedNotifier jobExecutionChangedNotifier;


    @Inject
    private MetadataAccess metadataAccess;

    private Map<String, BatchJobExecution> latestStreamingJobByFeedName = new ConcurrentHashMap<>();

    @Inject
    private ClusterService clusterService;

    private BatchStatusChangeReceiver batchStatusChangeReceiver = new BatchStatusChangeReceiver();


    /**
     * Latest start time for feed.
     * This is used to speed up the findLatestJobForFeed query limiting the result set by the last known start time
     */
    private Map<String, Long> latestStartTimeByFeedName = new ConcurrentHashMap<>();


    @Autowired
    public JpaBatchJobExecutionProvider(BatchJobExecutionRepository jobExecutionRepository, BatchJobInstanceRepository jobInstanceRepository,
                                        NifiRelatedRootFlowFilesRepository relatedRootFlowFilesRepository,
                                        BatchJobParametersRepository jobParametersRepository,
                                        OpsManagerFeedRepository opsManagerFeedRepository,
                                        BatchRelatedFlowFileRepository batchRelatedFlowFileRepository
    ) {
        super(JpaBatchJobExecution.class);
        this.jobExecutionRepository = jobExecutionRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.relatedRootFlowFilesRepository = relatedRootFlowFilesRepository;
        this.jobParametersRepository = jobParametersRepository;
        this.opsManagerFeedRepository = opsManagerFeedRepository;
        this.batchRelatedFlowFileRepository = batchRelatedFlowFileRepository;

    }

    @PostConstruct
    private void init() {
        clusterService.subscribe(batchStatusChangeReceiver, FeedOperationBatchStatusChange.CLUSTER_MESSAGE_TYPE);
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
    private JpaBatchJobExecution createNewJobExecution(ProvenanceEventRecordDTO event, OpsManagerFeed feed) {
        BatchJobInstance jobInstance = createJobInstance(event);
        JpaBatchJobExecution jobExecution = createJobExecution(jobInstance, event, feed);

        return jobExecution;
    }


    /**
     * Create a new Job Execution record from a given Provenance Event
     *
     * @param jobInstance the job instance to relate this job execution to
     * @param event       the event that started this job execution
     * @return the job execution
     */
    private JpaBatchJobExecution createJobExecution(BatchJobInstance jobInstance, ProvenanceEventRecordDTO event, OpsManagerFeed feed) {

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
        jobExecution = (JpaBatchJobExecution) save(jobExecution);

        jobExecutionChangedNotifier.notifyStarted(jobExecution, feed, null);
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
        if (event.isStream()) {
            jobExecution.finishStreamingJob();
        } else {
            if (event.isFailure()) {  //event.hasFailureEvents
                jobExecution.failJob();
            } else {
                jobExecution.completeOrFailJob();
            }
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
    public synchronized JpaBatchJobExecution getOrCreateJobExecution(ProvenanceEventRecordDTO event, OpsManagerFeed feed) {
        JpaBatchJobExecution jobExecution = null;
        if (event.isStream()) {
            //Streams only care about start/stop events to track.. otherwise we can disregard the events)
            if (event.isStartOfJob() || event.isFinalJobEvent()) {
                jobExecution = getOrCreateStreamJobExecution(event, feed);
            }
        } else {
            if (feed == null) {
                feed = opsManagerFeedRepository.findByName(event.getFeedName());
            }
            if (isProcessBatchEvent(event, feed)) {
                jobExecution = getOrCreateBatchJobExecution(event, feed);
            }
        }

        return jobExecution;

    }

    @Override
    public void updateFeedJobStartTime(BatchJobExecution jobExecution,OpsManagerFeed feed){
        if(jobExecution != null){
            //add the starttime to the map
            Long startTime = jobExecution.getStartTime().getMillis();
            if(startTime != null) {
                if (!latestStartTimeByFeedName.containsKey(startTime)) {
                    latestStartTimeByFeedName.put(feed.getName(), startTime);
                } else {
                    Long previousStartTime = latestStartTimeByFeedName.get(startTime);
                    if (startTime > previousStartTime) {
                        latestStartTimeByFeedName.put(feed.getName(), startTime);
                    }
                }
            }
        }
    }

    private BatchRelatedFlowFile getOtherBatchJobFlowFile(ProvenanceEventRecordDTO event) {

        BatchRelatedFlowFile relatedFlowFile = batchRelatedFlowFileRepository.findOne(event.getJobFlowFileId());
        return relatedFlowFile;
    }

    private Long timeBetweenStartingJobs(OpsManagerFeed feed) {
        Long time = null;
        if (feed != null) {
            time = feed.getTimeBetweenBatchJobs();
        }
        if (time == null) {
            time = 1000L;
        }
        return time;
    }


    private BatchRelatedFlowFile relateFlowFiles(String eventFlowFileId, String batchJobExecutionFlowFile, Long batchJobExecutionId) {
        JpaBatchRelatedFlowFile relatedFlowFile = new JpaBatchRelatedFlowFile(eventFlowFileId, batchJobExecutionFlowFile, batchJobExecutionId);
        return batchRelatedFlowFileRepository.save(relatedFlowFile);
    }


    private boolean isProcessBatchEvent(ProvenanceEventRecordDTO event, OpsManagerFeed feed) {

        //if we have a job already for this event then let it pass
        JpaBatchJobExecution jobExecution = jobExecutionRepository.findByFlowFile(event.getJobFlowFileId());
        if (jobExecution != null) {
            return true;
        } else {
            jobExecution = (JpaBatchJobExecution) findLatestJobForFeed(event.getFeedName());

            if (jobExecution != null) {
                String jobFlowFile = jobExecution.getNifiEventJobExecution().getFlowFileId();
                if (jobFlowFile.equals(event.getJobFlowFileId())) {
                    return true;
                } else {
                    boolean isSkipped = getOtherBatchJobFlowFile(event) != null;
                    Long diff = event.getEventTime() - jobExecution.getStartTime().getMillis();
                    Long threshold = timeBetweenStartingJobs(feed);
                    if (!isSkipped && threshold != -1 && jobExecution != null && diff >= 0 && diff < threshold) {

                        //relate this to that and return
                        BatchRelatedFlowFile related = getOtherBatchJobFlowFile(event);
                        if (related == null) {
                            relateFlowFiles(event.getJobFlowFileId(), jobFlowFile, jobExecution.getJobExecutionId());
                            event.setJobFlowFileId(jobFlowFile);
                            log.debug("Relating {} to {}, {} ", event.getJobFlowFileId(), jobFlowFile, jobExecution.getJobExecutionId());
                        }
                        return false;
                    } else {
                        return !isSkipped;
                    }
                }


            }
        }
        return true;

    }


    private JpaBatchJobExecution getOrCreateBatchJobExecution(ProvenanceEventRecordDTO event, OpsManagerFeed feed) {
        JpaBatchJobExecution jobExecution = null;
        boolean isNew = false;
        try {
            jobExecution = jobExecutionRepository.findByFlowFile(event.getJobFlowFileId());
            if (jobExecution == null) {
                jobExecution = createNewJobExecution(event, feed);
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

            jobExecution = (JpaBatchJobExecution) save(jobExecution);
            if (isNew) {
                log.info("Created new Job Execution with id of {} and starting event {} ", jobExecution.getJobExecutionId(), event);
            }
            if (updatedJobType) {
                //notify operations status
                jobExecutionChangedNotifier.notifyDataConfidenceJob(jobExecution, feed, "Data Confidence Job detected ");
            }

        }
        return jobExecution;
    }

    public void markStreamingFeedAsStopped(String feed) {
        BatchJobExecution jobExecution = findLatestJobForFeed(feed);
        if (jobExecution != null && !jobExecution.getStatus().equals(BatchJobExecution.JobStatus.STOPPED)) {
            log.info("Stopping Streaming feed job {} for Feed {} ", jobExecution.getJobExecutionId(), feed);
            jobExecution.setStatus(BatchJobExecution.JobStatus.STOPPED);
            jobExecution.setExitCode(ExecutionConstants.ExitCode.COMPLETED);
            ((JpaBatchJobExecution)jobExecution).setLastUpdated(DateTimeUtil.getNowUTCTime());
            jobExecution.setEndTime(DateTimeUtil.getNowUTCTime());
            save(jobExecution);
            //update the cache
            latestStreamingJobByFeedName.put(feed, jobExecution);
        }
    }

    public void markStreamingFeedAsStarted(String feed) {
        BatchJobExecution jobExecution = findLatestJobForFeed(feed);
        //ensure its Running
        if (!jobExecution.getStatus().equals(BatchJobExecution.JobStatus.STARTED)) {
            log.info("Starting Streaming feed job {} for Feed {} ", jobExecution.getJobExecutionId(), feed);
            jobExecution.setStatus(BatchJobExecution.JobStatus.STARTED);
            jobExecution.setExitCode(ExecutionConstants.ExitCode.EXECUTING);
            ((JpaBatchJobExecution)jobExecution).setLastUpdated(DateTimeUtil.getNowUTCTime());
            jobExecution.setStartTime(DateTimeUtil.getNowUTCTime());
            save(jobExecution);
            latestStreamingJobByFeedName.put(feed, jobExecution);
        }
    }

    private JpaBatchJobExecution getOrCreateStreamJobExecution(ProvenanceEventRecordDTO event, OpsManagerFeed feed) {
        JpaBatchJobExecution jobExecution = null;
        boolean isNew = false;
        try {
            BatchJobExecution latestJobExecution = latestStreamingJobByFeedName.get(event.getFeedName());
            if (latestJobExecution == null) {
                latestJobExecution = findLatestJobForFeed(event.getFeedName());
            } else {
                if (clusterService.isClustered()) {
                    latestJobExecution = jobExecutionRepository.findOne(latestJobExecution.getJobExecutionId());
                }
            }
            if (latestJobExecution == null || (latestJobExecution != null && !latestJobExecution.isStream())) {
                //If the latest Job is not set to be a Stream and its still running we need to fail it and create the new streaming job.
                if (latestJobExecution != null && !latestJobExecution.isFinished()) {
                    ProvenanceEventRecordDTO tempFailedEvent = new ProvenanceEventRecordDTO();
                    tempFailedEvent.setFeedName(event.getFeedName());
                    tempFailedEvent.setAttributeMap(new HashMap<>());
                    tempFailedEvent.setIsFailure(true);
                    tempFailedEvent.setDetails("Failed Running Batch event as this Feed has now become a Stream");
                    finishJob(tempFailedEvent, (JpaBatchJobExecution) latestJobExecution);
                    latestJobExecution.setExitMessage("Failed Running Batch event as this Feed has now become a Stream");
                    save(latestJobExecution);

                }
                jobExecution = createNewJobExecution(event, feed);
                jobExecution.setStream(true);
                latestStreamingJobByFeedName.put(event.getFeedName(), jobExecution);
                log.info("Created new Streaming Job Execution with id of {} and starting event {} ", jobExecution.getJobExecutionId(), event);
            } else {
                jobExecution = (JpaBatchJobExecution) latestJobExecution;
            }
            if (jobExecution != null) {
                latestStreamingJobByFeedName.put(event.getFeedName(), jobExecution);
            }
        } catch (OptimisticLockException e) {
            //read
            jobExecution = (JpaBatchJobExecution) findLatestJobForFeed(event.getFeedName());
        }

        boolean save = isNew;

        if (!jobExecution.isStream()) {
            jobExecution.setStream(true);
            save = true;
        }

        if (save) {
            save(jobExecution);
        }
        return jobExecution;
    }


    @Override
    public BatchJobExecution save(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {
        if (jobExecution == null) {
            return null;
        }
        batchStepExecutionProvider.createStepExecution(jobExecution, event);
        return jobExecution;
    }

    /**
     * Save the job execution in the database
     *
     * @return the saved job execution
     */
    @Override
    public BatchJobExecution save(ProvenanceEventRecordDTO event, OpsManagerFeed feed) {
        JpaBatchJobExecution jobExecution = getOrCreateJobExecution(event, feed);
        if (jobExecution != null) {
            return save(jobExecution, event);
        }
        return null;
    }


    public BatchJobExecution save(BatchJobExecution jobExecution) {
        return jobExecutionRepository.save((JpaBatchJobExecution) jobExecution);
    }


    @Override
    public BatchJobExecution findByJobExecutionId(Long jobExecutionId) {
        return jobExecutionRepository.findOne(jobExecutionId);
    }


    @Override
    public List<? extends BatchJobExecution> findRunningJobsForFeed(String feedName) {
        List<? extends BatchJobExecution> jobs = jobExecutionRepository.findJobsForFeedMatchingStatus(feedName, BatchJobExecution.JobStatus.STARTED, BatchJobExecution.JobStatus.STARTING);
        return jobs != null ? jobs : Collections.emptyList();
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

    public BatchJobExecution findLatestFinishedJobForFeed(String feedName) {
        List<JpaBatchJobExecution> jobExecutions = jobExecutionRepository.findLatestFinishedJobForFeed(feedName);
        if (jobExecutions != null && !jobExecutions.isEmpty()) {
            return jobExecutions.get(0);
        } else {
            return null;
        }
    }

    @Override
    public BatchJobExecution findLatestJobForFeed(String feedName) {
        List<JpaBatchJobExecution> jobExecutions = null;
        Long latestStartTime = latestStartTimeByFeedName.get(feedName);
        if(latestStartTime != null) {
            jobExecutions = jobExecutionRepository.findLatestJobForFeedWithStartTimeLimit(feedName,latestStartTime);
        }
        if(jobExecutions == null) {
            jobExecutions = jobExecutionRepository.findLatestJobForFeed(feedName);
        }
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
     * Streaming Feeds are given a count of 1 if they are running, regardless of the number of active running flows
     */
    public List<BatchAndStreamingJobStatusCount> getBatchAndStreamingJobCounts(String filter) {

        QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;

        QJpaBatchJobInstance jobInstance = QJpaBatchJobInstance.jpaBatchJobInstance;

        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;

        QJpaNifiFeedStats feedStats = QJpaNifiFeedStats.jpaNifiFeedStats;

        BooleanBuilder whereBuilder = new BooleanBuilder();
        if (StringUtils.isNotBlank(filter)) {
            whereBuilder.and(GenericQueryDslFilter.buildFilter(jobExecution, filter));
        }

        Expression<JpaBatchAndStreamingJobStatusCounts> expr =
            Projections.bean(JpaBatchAndStreamingJobStatusCounts.class,
                             JobStatusDslQueryExpressionBuilder.jobState().as("status"),
                             feed.id.as("opsManagerFeedId"),
                             feed.name.as("feedName"),
                             feed.isStream.as("isStream"),
                             feedStats.runningFeedFlows.as("runningFeedFlows"),
                             jobExecution.jobExecutionId.count().as("count"),
                             feedStats.lastActivityTimestamp.max().as("lastActivityTimestamp"));

        JPAQuery<?> query = factory.select(expr)
            .from(feed)
            .innerJoin(jobInstance).on(jobInstance.feed.id.eq(feed.id))
            .innerJoin(jobExecution).on(jobExecution.jobInstance.jobInstanceId.eq(jobInstance.jobInstanceId))
            .leftJoin(feedStats).on(feed.id.uuid.eq(feedStats.feedId.uuid))
            .where(whereBuilder)
            .groupBy(jobExecution.status, feed.id, feed.name, feed.isStream, feedStats.runningFeedFlows);
        List<BatchAndStreamingJobStatusCount> stats = (List<BatchAndStreamingJobStatusCount>) query.fetch();

        return stats.stream().map(s -> {
            if (s.isStream()
                && (BatchJobExecution.RUNNING_DISPLAY_STATUS.equalsIgnoreCase(s.getStatus())
                    || BatchJobExecution.JobStatus.STARTING.name().equalsIgnoreCase(s.getStatus())
                    || BatchJobExecution.JobStatus.STARTED.name().equalsIgnoreCase(s.getStatus())) && s.getRunningFeedFlows() == 0L) {
                ((JpaBatchAndStreamingJobStatusCounts) s).setStatus(BatchJobExecution.JobStatus.STOPPED.name());
            }
            return s;
        }).collect(Collectors.toList());
        //  return stats;

    }


    /**
     * Get count of Jobs grouped by Status
     * Streaming Feeds are given a count of 1 if they are running, regardless of the number of active running flows
     */
    @Override
    public List<JobStatusCount> getJobStatusCount(String filter) {

        QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;

        QJpaBatchJobInstance jobInstance = QJpaBatchJobInstance.jpaBatchJobInstance;

        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;

        BooleanBuilder whereBuilder = new BooleanBuilder();
        if (StringUtils.isNotBlank(filter)) {
            whereBuilder.and(GenericQueryDslFilter.buildFilter(jobExecution, filter));
        }

        ConstructorExpression<JpaBatchJobExecutionStatusCounts> expr =
            Projections.constructor(JpaBatchJobExecutionStatusCounts.class,
                                    JobStatusDslQueryExpressionBuilder.jobState().as("status"),
                                    jobExecution.jobExecutionId.count().as("count"));

        JPAQuery<?> query = factory.select(expr)
            .from(jobExecution)
            .innerJoin(jobInstance).on(jobExecution.jobInstance.jobInstanceId.eq(jobInstance.jobInstanceId))
            .innerJoin(feed).on(jobInstance.feed.id.eq(feed.id))
            .where(whereBuilder.and(feed.isStream.eq(false))
                       .and(FeedAclIndexQueryAugmentor.generateExistsExpression(feed.id, controller.isEntityAccessControlled())))
            .groupBy(jobExecution.status);
        List<JobStatusCount> stats = (List<JobStatusCount>) query.fetch();

        //merge in streaming feed stats
        List<? extends NifiFeedStats> streamingFeedStats = feedStatisticsProvider.findFeedStats(true);
        if (streamingFeedStats != null) {
            if (stats == null) {
                stats = new ArrayList<>();
            }
            Long runningCount = streamingFeedStats.stream().filter(s -> s.getRunningFeedFlows() > 0L).count();
            if (runningCount > 0) {
                JobStatusCount runningStatusCount = stats.stream().filter(s -> s.getStatus().equalsIgnoreCase(BatchJobExecution.RUNNING_DISPLAY_STATUS)).findFirst().orElse(null);
                if (runningStatusCount != null) {
                    runningCount = runningStatusCount.getCount() + runningCount;
                    runningStatusCount.setCount(runningCount);
                } else {
                    JpaBatchJobExecutionStatusCounts runningStreamingFeedCounts = new JpaBatchJobExecutionStatusCounts();
                    runningStreamingFeedCounts.setCount(runningCount);
                    runningStreamingFeedCounts.setStatus(BatchJobExecution.RUNNING_DISPLAY_STATUS);
                    stats.add(runningStreamingFeedCounts);
                }
            }
        }
        return stats;


    }

    @Override
    public List<JobStatusCount> getJobStatusCountByDate() {

        QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;

        QJpaBatchJobInstance jobInstance = QJpaBatchJobInstance.jpaBatchJobInstance;

        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;

        JPAQuery
            query = factory.select(
            Projections.constructor(JpaBatchJobExecutionStatusCounts.class,
                                    JobStatusDslQueryExpressionBuilder.jobState().as("status"),
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

        BooleanBuilder whereBuilder = new BooleanBuilder();
        whereBuilder.and(jobExecution.startTime.goe(DateTimeUtil.getNowUTCTime().minus(period)));
        if (StringUtils.isNotBlank(filter)) {
            whereBuilder.and(GenericQueryDslFilter.buildFilter(jobExecution, filter));
        }

        JPAQuery
            query = factory.select(
            Projections.constructor(JpaBatchJobExecutionStatusCounts.class,
                                    JobStatusDslQueryExpressionBuilder.jobState().as("status"),
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


    @Override
    public BatchJobExecution abandonJob(Long executionId) {
        BatchJobExecution execution = findByJobExecutionId(executionId);
        if (execution != null && !execution.getStatus().equals(BatchJobExecution.JobStatus.ABANDONED)) {
            if (execution.getStartTime() == null) {
                execution.setStartTime(DateTimeUtil.getNowUTCTime());
            }
            execution.setStatus(BatchJobExecution.JobStatus.ABANDONED);
            if (execution.getEndTime() == null) {
                execution.setEndTime(DateTimeUtil.getNowUTCTime());
            }
            String abandonMessage = "Job manually abandoned @ " + DateTimeUtil.getNowFormattedWithTimeZone();
            String msg = execution.getExitMessage() != null ? execution.getExitMessage() + "\n" : "";
            msg += abandonMessage;
            execution.setExitMessage(msg);
            //also stop any running steps??
            //find the feed associated with the job
            OpsManagerFeed feed = execution.getJobInstance().getFeed();

            save(execution);

            jobExecutionChangedNotifier.notifyAbandoned(execution, feed, null);

            //clear the associated alert
            String alertId = execution.getJobExecutionContextAsMap().get(BatchJobExecutionProvider.KYLO_ALERT_ID_PROPERTY);
            if (StringUtils.isNotBlank(alertId)) {
                provider.respondTo(provider.resolve(alertId), (alert1, response) -> response.handle(abandonMessage));
            }

        }
        return execution;
    }

    public void notifyFailure(BatchJobExecution jobExecution, String feedName, boolean isStream, String status) {
        OpsManagerFeed feed = jobExecution.getJobInstance().getFeed();
        notifyFailure(jobExecution, feed, isStream, status);
    }

    @Override
    public void notifySuccess(BatchJobExecution jobExecution, OpsManagerFeed feed, String status) {
        jobExecutionChangedNotifier.notifySuccess(jobExecution, feed, status);
    }

    @Override
    public void notifyStopped(BatchJobExecution jobExecution, OpsManagerFeed feed, String status) {
        jobExecutionChangedNotifier.notifySuccess(jobExecution, feed, status);
    }

    @Override
    public void notifyFailure(BatchJobExecution jobExecution, OpsManagerFeed feed, boolean isStream, String status) {

        jobExecutionChangedNotifier.notifyOperationStatusEvent(jobExecution, feed, FeedOperation.State.FAILURE, status);

        Alert alert = null;

        //see if the feed has an unhandled alert already.
        String feedId = feed.getId().toString();
        String alertId = jobExecution.getJobExecutionContextAsMap().get(BatchJobExecutionProvider.KYLO_ALERT_ID_PROPERTY);
        String message = "Failed Job " + jobExecution.getJobExecutionId() + " for feed " + feed != null ? feed.getName() : null;
        if (StringUtils.isNotBlank(alertId)) {
            alert = provider.getAlertAsServiceAccount(provider.resolve(alertId)).orElse(null);
        }
        if (alert == null) {
            alert = alertManager.createEntityAlert(OperationalAlerts.JOB_FALURE_ALERT_TYPE,
                                                   Alert.Level.FATAL,
                                                   message, alertManager.createEntityIdentificationAlertContent(feedId,
                                                                                                                SecurityRole.ENTITY_TYPE.FEED, jobExecution.getJobExecutionId()));
            Alert.ID providerAlertId = provider.resolve(alert.getId(), alert.getSource());

            JpaBatchJobExecutionContextValue executionContext = new JpaBatchJobExecutionContextValue(jobExecution, KYLO_ALERT_ID_PROPERTY);
            executionContext.setStringVal(providerAlertId.toString());
            ((JpaBatchJobExecution) jobExecution).addJobExecutionContext(executionContext);
            save(jobExecution);


        } else {
            //if streaming feed with unhandled alerts attempt to update alert content
            DefaultAlertChangeEventContent alertContent = null;

            if (isStream && alert.getState().equals(Alert.State.UNHANDLED)) {
                if (alert.getEvents() != null && alert.getEvents().get(0) != null) {

                    alertContent = alert.getEvents().get(0).getContent();

                    if (alertContent == null) {
                        alertContent = new DefaultAlertChangeEventContent();
                        alertContent.getContent().put("failedCount", 1);
                        alertContent.getContent().put("stream", true);
                    } else {
                        Integer count = (Integer) alertContent.getContent().putIfAbsent("failedCount", 0);
                        count++;
                        alertContent.getContent().put("failedCount", count);
                    }
                    final DefaultAlertChangeEventContent content = alertContent;
                    provider.respondTo(alert.getId(), (alert1, response) -> response.updateAlertChange(message, content));
                } else {
                    if (alertContent == null) {
                        alertContent = new DefaultAlertChangeEventContent();
                        alertContent.getContent().put("failedCount", 1);
                        alertContent.getContent().put("stream", true);
                    }

                    final DefaultAlertChangeEventContent content = alertContent;
                    provider.respondTo(alert.getId(), (alert1, response) -> response.unhandle(message, content));
                }
            } else {
                alertContent = new DefaultAlertChangeEventContent();
                alertContent.getContent().put("failedCount", 1);
                if (isStream) {
                    alertContent.getContent().put("stream", true);
                }
                final DefaultAlertChangeEventContent content = alertContent;
                provider.respondTo(alert.getId(), (alert1, response) -> response.unhandle(message, content));
            }

        }
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


    @Override
    public void notifyBatchToStream(BatchJobExecution jobExecution, OpsManagerFeed feed) {
        FeedOperationBatchStatusChange change = new FeedOperationBatchStatusChange(feed.getId(), feed.getName(), jobExecution.getJobExecutionId(), FeedOperationBatchStatusChange.BatchType.STREAM);
        clusterService.sendMessageToOthers(FeedOperationBatchStatusChange.CLUSTER_MESSAGE_TYPE, change);
    }

    @Override
    public void notifyStreamToBatch(BatchJobExecution jobExecution, OpsManagerFeed feed) {
        FeedOperationBatchStatusChange change = new FeedOperationBatchStatusChange(feed.getId(), feed.getName(), jobExecution.getJobExecutionId(), FeedOperationBatchStatusChange.BatchType.BATCH);
        clusterService.sendMessageToOthers(FeedOperationBatchStatusChange.CLUSTER_MESSAGE_TYPE, change);
    }

    private class BatchStatusChangeReceiver implements ClusterServiceMessageReceiver {

        @Override
        public void onMessageReceived(String from, ClusterMessage message) {
            if (FeedOperationBatchStatusChange.CLUSTER_MESSAGE_TYPE.equalsIgnoreCase(message.getType())) {
                FeedOperationBatchStatusChange change = (FeedOperationBatchStatusChange) message.getMessage();
                latestStreamingJobByFeedName.remove(change.getFeedName());
            }

        }
    }
}
