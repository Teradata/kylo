package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionParameter;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobInstance;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.BatchExecutionContextProvider;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiEventJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiEventStepExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiRelatedRootFlowFiles;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiRelatedRootFlowFilesRepository;
import com.thinkbiganalytics.metadata.jpa.jobrepo.step.BatchStepExecutionRepository;
import com.thinkbiganalytics.metadata.jpa.jobrepo.step.JpaBatchStepExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.step.JpaBatchStepExecutionContextValue;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Created by sr186054 on 8/31/16.
 */
@Service
public class JpaBatchJobExecutionProvider implements BatchJobExecutionProvider {

    private static final Logger log = LoggerFactory.getLogger(JpaBatchJobExecutionProvider.class);


    /**
     * Used to serialize the ExecutionContext for JOB and STEPs This is needed for usage with the existing Spring Batch Apis; however, will be removed once the UI doesnt reference those tables
     * anymore
     */
    @Autowired
    private BatchExecutionContextProvider batchExecutionContextProvider;


    @Autowired
    private JPAQueryFactory factory;

    private BatchJobExecutionRepository jobExecutionRepository;

    private BatchJobInstanceRepository jobInstanceRepository;


    private BatchStepExecutionRepository nifiStepExecutionRepository;

    private NifiRelatedRootFlowFilesRepository relatedRootFlowFilesRepository;


    @Autowired
    public JpaBatchJobExecutionProvider(BatchJobExecutionRepository jobExecutionRepository, BatchJobInstanceRepository jobInstanceRepository,
                                        BatchStepExecutionRepository nifiStepExecutionRepository,
                                        NifiRelatedRootFlowFilesRepository relatedRootFlowFilesRepository
    ) {

        this.jobExecutionRepository = jobExecutionRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.nifiStepExecutionRepository = nifiStepExecutionRepository;
        this.relatedRootFlowFilesRepository = relatedRootFlowFilesRepository;

    }

    @Override
    public BatchJobInstance createJobInstance(ProvenanceEventRecordDTO event) {

        JpaBatchJobInstance jobInstance = new JpaBatchJobInstance();
        jobInstance.setJobKey(jobKeyGenerator(event));
        jobInstance.setJobName(event.getFeedName());
        return this.jobInstanceRepository.save(jobInstance);
    }

    /**
     * Generate a Unique key for the Job Instance table This code is similar to what was used by Spring Batch
     */
    private String jobKeyGenerator(ProvenanceEventRecordDTO event) {

        StringBuffer stringBuffer = new StringBuffer(event.getEventTime().getMillis() + "").append(event.getFlowFileUuid());
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


    private JpaBatchJobExecution createJobExecution(BatchJobInstance jobInstance, ProvenanceEventRecordDTO event) {

        JpaBatchJobExecution jobExecution = new JpaBatchJobExecution();
        jobExecution.setJobInstance(jobInstance);
        //add in the parameters from the attributes
        jobExecution.setCreateTime(DateTimeUtil.convertToUTC(DateTime.now()));
        jobExecution.setStartTime(DateTimeUtil.convertToUTC(event.getEventTime()));
        jobExecution.setStatus(BatchJobExecution.JobStatus.STARTED);
        jobExecution.setExitCode(ExecutionConstants.ExitCode.EXECUTING);
        jobExecution.setLastUpdated(DateTimeUtil.convertToUTC(DateTime.now()));

        //create the job params
        Map<String, Object> jobParameters;
        if (event.getAttributeMap() != null) {
            jobParameters = new HashMap<>(event.getAttributeMap());
        } else {
            jobParameters = new HashMap<>();
        }
        //bootstrap the feed parameters
        jobParameters.put(FeedConstants.PARAM__FEED_NAME, event.getFeedName());
        jobParameters.put(FeedConstants.PARAM__JOB_TYPE, FeedConstants.PARAM_VALUE__JOB_TYPE_FEED);
        jobParameters.put(FeedConstants.PARAM__FEED_IS_PARENT, "true");

        //save the params
        Set<BatchJobExecutionParameter> jobExecutionParametersList = new HashSet<>();
        for (Map.Entry<String, Object> entry : jobParameters.entrySet()) {
            BatchJobExecutionParameter jobExecutionParameters = jobExecution.addParameter(entry.getKey(), entry.getValue());
            jobExecutionParametersList.add(jobExecutionParameters);
        }
        jobExecution.setJobParameters(jobExecutionParametersList);
        JpaNifiEventJobExecution eventJobExecution = new JpaNifiEventJobExecution(jobExecution, event.getEventId(), event.getJobFlowFileId());
        jobExecution.setNifiEventJobExecution(eventJobExecution);
        return this.jobExecutionRepository.save(jobExecution);
    }

    private JpaBatchJobExecution createNewJobExecution(ProvenanceEventRecordDTO event) {
        BatchJobInstance jobInstance = createJobInstance(event);
        JpaBatchJobExecution jobExecution = createJobExecution(jobInstance, event);

        return jobExecution;
    }

    /**
     * if a event is a Merge (JOIN event) that merges other Root flow files (other JobExecutions) it will contain this relationship. These files need to be related together to determine when the final
     * job is complete.
     */
    private void checkAndRelateJobs(ProvenanceEventRecordDTO event, NifiEvent nifiEvent) {
        if (event.getRelatedRootFlowFiles() != null && !event.getRelatedRootFlowFiles().isEmpty()) {
            //relate the files together
            List<JpaNifiRelatedRootFlowFiles> relatedRootFlowFiles = new ArrayList<>();
            String relationId = UUID.randomUUID().toString();
            for (String flowFile : event.getRelatedRootFlowFiles()) {
                JpaNifiRelatedRootFlowFiles nifiRelatedRootFlowFile = new JpaNifiRelatedRootFlowFiles(nifiEvent, flowFile, relationId);
                relatedRootFlowFiles.add(nifiRelatedRootFlowFile);
            }
            relatedRootFlowFilesRepository.save(relatedRootFlowFiles);
        }
    }

    /**
     * When the job is complete determine its status, write out exection context, and determine if all related jobs are complete
     */
    private void finishJob(ProvenanceEventRecordDTO event, JpaBatchJobExecution jobExecution) {

        if (jobExecution.getJobExecutionId() == null) {
            log.error("Warning execution id is null for ending event {} ", event);
        }

        ///END OF THE JOB... fail or complete the job?
        ensureFailureSteps(jobExecution);
        jobExecution.completeOrFailJob();
        jobExecution.setEndTime(DateTimeUtil.convertToUTC(event.getEventTime()));
        log.info("Finishing Job: {} with a status of: {}", jobExecution.getJobExecutionId(), jobExecution.getStatus());
        //add in execution contexts
        Map<String, String> allAttrs = event.getAttributeMap();
        if (allAttrs != null && !allAttrs.isEmpty()) {
            for (Map.Entry<String, String> entry : allAttrs.entrySet()) {
                JpaBatchJobExecutionContextValue executionContext = new JpaBatchJobExecutionContextValue(jobExecution, entry.getKey());
                executionContext.setStringVal(entry.getValue());
                jobExecution.addJobExecutionContext(executionContext);
            }
            //also persist to spring batch tables
            batchExecutionContextProvider.saveJobExecutionContext(jobExecution.getJobExecutionId(), allAttrs);
        }

        //If you want to know when a feed is truely finished along with any of its related feeds you can use this method below.
        //commented out since the ProvenanceEventReceiver already handles it
        ensureRelatedJobsAreFinished(event,jobExecution);

    }


    @Override
    public BatchStepExecution save(ProvenanceEventRecordDTO event, NifiEvent nifiEvent) {
        //find the JobExecution for the event if it exists, otherwise create one
        JpaBatchJobExecution jobExecution = jobExecutionRepository.findByEventAndFlowFile(event.getJobEventId(), event.getJobFlowFileId());
        if (jobExecution == null && event.isStartOfJob()) {
            jobExecution = createNewJobExecution(event);
        }
        //add in the stepExecutions
        if (jobExecution != null && !jobExecution.isFinished()) {
            checkAndRelateJobs(event, nifiEvent);
            createStepExecution(jobExecution, event);
            if (event.isEndOfJob()) {
                finishJob(event, jobExecution);
            }
            this.jobExecutionRepository.save(jobExecution);
        } else if (jobExecution != null && jobExecution.isFinished()) {
            //ensure failures
            boolean addedFailures = ensureFailureSteps(jobExecution);
            if (addedFailures) {
                jobExecution.completeOrFailJob();
                this.jobExecutionRepository.save(jobExecution);
            }
        }
        return null;
    }


    /**
     * We get Nifi Events after a step has executed. If a flow takes some time we might not initially get the event that the given step has failed when we write the StepExecution record. This should
     * be called when a Job Completes as it will verify all failures and then update the correct step status to reflect the failure if there is one.
     */
    private boolean ensureFailureSteps(JpaBatchJobExecution jobExecution) {

        //find all the Steps for this Job that have records in the Failure table for this job flow file
        List<JpaBatchStepExecution> stepsNeedingToBeFailed = nifiStepExecutionRepository.findStepsInJobThatNeedToBeFailed(jobExecution.getJobExecutionId());
        if (stepsNeedingToBeFailed != null) {
            for (JpaBatchStepExecution se : stepsNeedingToBeFailed) {
                se.failStep();
            }
            //save them
            nifiStepExecutionRepository.save(stepsNeedingToBeFailed);
            return true;
        }
        return false;
    }


    private BatchStepExecution createStepExecution(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {

        //only create the step if it doesnt exist yet for this event
        JpaBatchStepExecution stepExecution = nifiStepExecutionRepository.findByProcessorAndJobFlowFile(event.getComponentId(), event.getJobFlowFileId());
        if (stepExecution == null) {

            stepExecution = new JpaBatchStepExecution();
            stepExecution.setJobExecution(jobExecution);
            stepExecution.setStartTime(
                event.getPreviousEventTime() != null ? DateTimeUtil.convertToUTC(event.getPreviousEventTime())
                                                     : DateTimeUtil.convertToUTC((event.getEventTime().minus(event.getEventDuration()))));
            stepExecution.setEndTime(DateTimeUtil.convertToUTC(event.getEventTime()));
            stepExecution.setStepName(event.getComponentName());
            log.info("New Step Execution {} on Job: {} using event {} ", stepExecution.getStepName(), jobExecution, event);

            boolean failure = event.isFailure();
            if (failure) {
                stepExecution.failStep();
            } else {
                stepExecution.completeStep();
            }
            //add in execution contexts
            assignStepExecutionContextMap(event, stepExecution);

            //Attach the NifiEvent object to this StepExecution
            JpaNifiEventStepExecution eventStepExecution = new JpaNifiEventStepExecution(jobExecution, stepExecution, event.getEventId(), event.getJobFlowFileId());
            eventStepExecution.setComponentId(event.getComponentId());
            eventStepExecution.setJobFlowFileId(event.getJobFlowFileId());
            stepExecution.setNifiEventStepExecution(eventStepExecution);

            jobExecution.getStepExecutions().add(stepExecution);
            stepExecution = nifiStepExecutionRepository.save(stepExecution);
            //also persist to spring batch tables
            //TODO to be removed in next release once Spring batch is completely removed.  Needed since the UI references this table
            batchExecutionContextProvider.saveStepExecutionContext(stepExecution.getStepExecutionId(), event.getUpdatedAttributes());


        } else {
            //update it
            assignStepExecutionContextMap(event, stepExecution);
            log.info("Update Step Execution {} on Job: {} using event {} ", stepExecution.getStepName(), jobExecution, event);
            stepExecution = nifiStepExecutionRepository.save(stepExecution);
            //also persist to spring batch tables
            //TODO to be removed in next release once Spring batch is completely removed.  Needed since the UI references this table
            batchExecutionContextProvider.saveStepExecutionContext(stepExecution.getStepExecutionId(), stepExecution.getStepExecutionContextAsMap());
        }

        if (stepExecution != null) {
            //if the attrs coming in change the type to a CHECK job then update the entity
            updateJobType(jobExecution, event);
        }
        return stepExecution;

    }

    private void assignStepExecutionContextMap(ProvenanceEventRecordDTO event, JpaBatchStepExecution stepExecution) {
        Map<String, String> updatedAttrs = event.getUpdatedAttributes();
        if (updatedAttrs != null && !updatedAttrs.isEmpty()) {
            for (Map.Entry<String, String> entry : updatedAttrs.entrySet()) {
                JpaBatchStepExecutionContextValue stepExecutionContext = new JpaBatchStepExecutionContextValue(stepExecution, entry.getKey());
                stepExecutionContext.setStringVal(entry.getValue());
                stepExecution.addStepExecutionContext(stepExecutionContext);
            }
        }
    }


    /**
     * Sets the Job Execution params to either Check Data or Feed Jobs
     */
    private void updateJobType(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {

        if (event.getUpdatedAttributes() != null && event.getUpdatedAttributes().containsKey(NIFI_JOB_TYPE_PROPERTY)) {
            String jobType = event.getUpdatedAttributes().get(NIFI_JOB_TYPE_PROPERTY);
            String nifiCategory = event.getAttributeMap().get(NIFI_CATEGORY_PROPERTY);
            String nifiFeedName = event.getAttributeMap().get(NIFI_FEED_PROPERTY);
            String feedName = nifiCategory + "." + nifiFeedName;
            if (FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK.equalsIgnoreCase(jobType)) {
                ((JpaBatchJobExecution) jobExecution).setAsCheckDataJob(feedName);
            }
        }
    }


    /**
     * When a Job Finishes this will check if it has any relatedJobExecutions and allow you to get notified when the set of Jobs are complete Currently this is not needed as the
     * ProvenanceEventReceiver already handles this event for both Batch and Streaming Jobs com.thinkbiganalytics.jobrepo.nifi.provenance.ProvenanceEventReceiver#failedJob(ProvenanceEventRecordDTO)
     * com.thinkbiganalytics.jobrepo.nifi.provenance.ProvenanceEventReceiver#successfulJob(ProvenanceEventRecordDTO)
     */
    private void checkIfJobAndRelatedJobsAreFinished(BatchJobExecution jobExecution) {
        //Check related jobs
        if (jobExecutionRepository.hasRelatedJobs(jobExecution.getJobExecutionId())) {
            boolean isComplete = !jobExecutionRepository.hasRunningRelatedJobs(jobExecution.getJobExecutionId());

            if (isComplete) {
                boolean hasFailures = jobExecutionRepository.hasRelatedJobFailures(jobExecution.getJobExecutionId());
                if (jobExecution.isFailed() || hasFailures) {
                    log.info("FINISHED AND FAILED JOB with relation {} ", jobExecution.getJobExecutionId());
                } else {
                    log.info("FINISHED JOB with relation {} ", jobExecution.getJobExecutionId());
                }
            }
        } else {
            if (jobExecution.isFailed()) {
                log.info("Failed JobExecution");
            } else if (jobExecution.isSuccess()) {
                log.info("Completed Job Execution");
            }
        }
    }

    private void ensureRelatedJobsAreFinished(ProvenanceEventRecordDTO event,BatchJobExecution jobExecution) {
        //Check related jobs
        if (event.isFinalJobEvent() && jobExecutionRepository.hasRelatedJobs(jobExecution.getJobExecutionId())) {

            List<JpaBatchJobExecution> runningJobs = jobExecutionRepository.findRunningRelatedJobExecutions(jobExecution.getJobExecutionId());
            if (runningJobs != null && !runningJobs.isEmpty()) {
                for (JpaBatchJobExecution job : runningJobs) {
                    job.completeOrFailJob();
                    log.info("Finishing related running job {} for event ",job.getJobExecutionId(),event);
                }
                jobExecutionRepository.save(runningJobs);
            }
        }
    }


    /**
     * Find the Job Execution by the Nifi EventId and JobFlowFileId
     */
    @Override
    public BatchJobExecution findByEventAndFlowFile(Long eventId, String flowfileid) {
        return jobExecutionRepository.findByEventAndFlowFile(eventId, flowfileid);
    }


    @Override
    public BatchJobExecution failStepsInJobThatNeedToBeFailed(BatchJobExecution jobExecution) {
        List<JpaBatchStepExecution> steps = nifiStepExecutionRepository.findStepsInJobThatNeedToBeFailed(jobExecution.getJobExecutionId());
        if (steps != null && !steps.isEmpty()) {
            log.info(" Second Fail Attempt for {}. {} steps ", jobExecution.getJobExecutionId(), steps.size());
            for (JpaBatchStepExecution step : steps) {
                step.failStep();
            }
            ((JpaBatchJobExecution) jobExecution).completeOrFailJob();
            jobExecutionRepository.save((JpaBatchJobExecution) jobExecution);
        }

        return jobExecution;
    }


    @Override
    public BatchJobExecution findByJobExecutionId(Long jobExecutionId) {
        return jobExecutionRepository.findOne(jobExecutionId);
    }

    @Override
    public BatchJobExecution save(BatchJobExecution jobExecution) {
        return jobExecutionRepository.save((JpaBatchJobExecution) jobExecution);
    }

}
