package com.thinkbiganalytics.jobrepo.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.jpa.model.BatchJobExecutionContextValues;
import com.thinkbiganalytics.jobrepo.jpa.model.BatchStepExecutionContextValues;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiEvent;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiEventJobExecution;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiEventStepExecution;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiJobExecution;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiJobExecutionParameters;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiJobInstance;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiRelatedRootFlowFiles;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiStepExecution;
import com.thinkbiganalytics.jobrepo.nifi.support.DateTimeUtil;
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
public class NifiJobExecutionProvider {

    private static final Logger log = LoggerFactory.getLogger(NifiJobExecutionProvider.class);

    public static final String NIFI_JOB_TYPE_PROPERTY = "tb.jobType";
    public static final String NIFI_FEED_PROPERTY = "feed";
    public static final String NIFI_CATEGORY_PROPERTY = "category";


    /**
     * Used to serialize the ExecutionContext for JOB and STEPs This is needed for usage with the existing Spring Batch Apis; however, will be removed once the UI doesnt reference those tables
     * anymore
     */
    @Autowired
    private BatchExecutionContextProvider batchExecutionContextProvider;


    @Autowired
    private JPAQueryFactory factory;

    private NifiJobExecutionRepository jobExecutionRepository;

    private NifiJobInstanceRepository jobInstanceRepository;


    private NifiStepExecutionRepository nifiStepExecutionRepository;

    private NifiRelatedRootFlowFilesRepository relatedRootFlowFilesRepository;


    @Autowired
    public NifiJobExecutionProvider(NifiJobExecutionRepository jobExecutionRepository, NifiJobInstanceRepository jobInstanceRepository,
                                    NifiStepExecutionRepository nifiStepExecutionRepository,
                                    NifiRelatedRootFlowFilesRepository relatedRootFlowFilesRepository
    ) {

        this.jobExecutionRepository = jobExecutionRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.nifiStepExecutionRepository = nifiStepExecutionRepository;
        this.relatedRootFlowFilesRepository = relatedRootFlowFilesRepository;

    }

    public NifiJobInstance createJobInstance(ProvenanceEventRecordDTO event) {

        NifiJobInstance jobInstance = new NifiJobInstance();
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


    private NifiJobExecution createJobExecution(NifiJobInstance jobInstance, ProvenanceEventRecordDTO event) {

        NifiJobExecution jobExecution = new NifiJobExecution();
        jobExecution.setJobInstance(jobInstance);
        //add in the parameters from the attributes
        jobExecution.setCreateTime(DateTimeUtil.convertToUTC(DateTime.now()));
        jobExecution.setStartTime(DateTimeUtil.convertToUTC(event.getEventTime()));
        jobExecution.setStatus(NifiJobExecution.JobStatus.STARTED);
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
        Set<NifiJobExecutionParameters> jobExecutionParametersList = new HashSet<>();
        for (Map.Entry<String, Object> entry : jobParameters.entrySet()) {
            NifiJobExecutionParameters jobExecutionParameters = jobExecution.addParameter(entry.getKey(), entry.getValue());
            jobExecutionParametersList.add(jobExecutionParameters);
        }
        jobExecution.setJobParameters(jobExecutionParametersList);
        NifiEventJobExecution eventJobExecution = new NifiEventJobExecution(jobExecution, event.getEventId(), event.getJobFlowFileId());
        jobExecution.setNifiEventJobExecution(eventJobExecution);
        return this.jobExecutionRepository.save(jobExecution);
    }

    private NifiJobExecution createNewJobExecution(ProvenanceEventRecordDTO event) {
        NifiJobInstance jobInstance = createJobInstance(event);
        NifiJobExecution jobExecution = createJobExecution(jobInstance, event);

        return jobExecution;
    }

    /**
     * if a event is a Merge (JOIN event) that merges other Root flow files (other JobExecutions) it will contain this relationship. These files need to be related together to determine when the final
     * job is complete.
     */
    private void checkAndRelateJobs(ProvenanceEventRecordDTO event, NifiEvent nifiEvent) {
        if (event.getRelatedRootFlowFiles() != null && !event.getRelatedRootFlowFiles().isEmpty()) {
            //relate the files together
            List<NifiRelatedRootFlowFiles> relatedRootFlowFiles = new ArrayList<>();
            String relationId = UUID.randomUUID().toString();
            for (String flowFile : event.getRelatedRootFlowFiles()) {
                NifiRelatedRootFlowFiles nifiRelatedRootFlowFile = new NifiRelatedRootFlowFiles(nifiEvent, flowFile, relationId);
                relatedRootFlowFiles.add(nifiRelatedRootFlowFile);
            }
            relatedRootFlowFilesRepository.save(relatedRootFlowFiles);
        }
    }

    /**
     * When the job is complete determine its status, write out exection context, and determine if all related jobs are complete
     * @param event
     * @param jobExecution
     */
    private void finishJob(ProvenanceEventRecordDTO event, NifiJobExecution jobExecution) {



        if(jobExecution.getJobExecutionId() == null) {
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
                BatchJobExecutionContextValues executionContext = new BatchJobExecutionContextValues(jobExecution, entry.getKey());
                executionContext.setStringVal(entry.getValue());
                jobExecution.addJobExecutionContext(executionContext);
            }
            //also persist to spring batch tables
            batchExecutionContextProvider.saveJobExecutionContext(jobExecution.getJobExecutionId(), allAttrs);
        }

        //If you want to know when a feed is truely finished along with any of its related feeds you can use this method below.
        //commented out since the ProvenanceEventReceiver already handles it
        //checkIfJobAndRelatedJobsAreFinished

    }


    public NifiStepExecution save(ProvenanceEventRecordDTO event, NifiEvent nifiEvent) {
        //find the JobExecution for the event if it exists, otherwise create one
        NifiJobExecution jobExecution = jobExecutionRepository.findByEventAndFlowFile(event.getJobEventId(), event.getJobFlowFileId());
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
        }
        else if (jobExecution != null && jobExecution.isFinished()) {
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
    private boolean ensureFailureSteps(NifiJobExecution jobExecution) {

        //find all the Steps for this Job that have records in the Failure table for this job flow file
        List<NifiStepExecution> stepsNeedingToBeFailed = nifiStepExecutionRepository.findStepsInJobThatNeedToBeFailed(jobExecution.getJobExecutionId());
        if (stepsNeedingToBeFailed != null) {
            for (NifiStepExecution se : stepsNeedingToBeFailed) {
                se.failStep();
            }
            //save them
            nifiStepExecutionRepository.save(stepsNeedingToBeFailed);
            return true;
        }
        return false;
    }


    private NifiStepExecution createStepExecution(NifiJobExecution jobExecution, ProvenanceEventRecordDTO event) {

        //only create the step if it doesnt exist yet for this event
        NifiStepExecution stepExecution = nifiStepExecutionRepository.findByProcessorAndJobFlowFile(event.getComponentId(), event.getJobFlowFileId());
        if (stepExecution == null) {

            stepExecution = new NifiStepExecution();
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
            NifiEventStepExecution eventStepExecution = new NifiEventStepExecution(jobExecution, stepExecution, event.getEventId(), event.getJobFlowFileId());
            eventStepExecution.setComponentId(event.getComponentId());
            eventStepExecution.setJobFlowFileId(event.getJobFlowFileId());
            stepExecution.setNifiEventStepExecution(eventStepExecution);

            jobExecution.getStepExecutions().add(stepExecution);
            stepExecution =  nifiStepExecutionRepository.save(stepExecution);
            //also persist to spring batch tables
            //TODO to be removed in next release once Spring batch is completely removed.  Needed since the UI references this table
            batchExecutionContextProvider.saveStepExecutionContext(stepExecution.getStepExecutionId(), event.getUpdatedAttributes());


        } else {
            //update it
            assignStepExecutionContextMap(event, stepExecution);

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

    private void assignStepExecutionContextMap(ProvenanceEventRecordDTO event, NifiStepExecution stepExecution) {
        Map<String, String> updatedAttrs = event.getUpdatedAttributes();
        if (updatedAttrs != null && !updatedAttrs.isEmpty()) {
            for (Map.Entry<String, String> entry : updatedAttrs.entrySet()) {
                BatchStepExecutionContextValues stepExecutionContext = new BatchStepExecutionContextValues(stepExecution, entry.getKey());
                stepExecutionContext.setStringVal(entry.getValue());
                stepExecution.addStepExecutionContext(stepExecutionContext);
            }
        }
    }


    /**
     * Sets the Job Execution params to either Check Data or Feed Jobs
     *
     * @param jobExecution
     * @param event
     */
    private void updateJobType(NifiJobExecution jobExecution, ProvenanceEventRecordDTO event) {

        if (event.getUpdatedAttributes() != null && event.getUpdatedAttributes().containsKey(NIFI_JOB_TYPE_PROPERTY)) {
            String jobType = event.getUpdatedAttributes().get(NIFI_JOB_TYPE_PROPERTY);
            String nifiCategory = event.getAttributeMap().get(NIFI_CATEGORY_PROPERTY);
            String nifiFeedName = event.getAttributeMap().get(NIFI_FEED_PROPERTY);
            String feedName = nifiCategory + "." + nifiFeedName;
            if (FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK.equalsIgnoreCase(jobType)) {
                jobExecution.setAsCheckDataJob(feedName);
            }
        }
    }


    /**
     * When a Job Finishes this will check if it has any relatedJobExecutions and allow you to get notified when the set of Jobs are complete
     * Currently this is not needed as the ProvenanceEventReceiver already handles this event for both Batch and Streaming Jobs
     * @see com.thinkbiganalytics.jobrepo.nifi.provenance.ProvenanceEventReceiver#failedJob(ProvenanceEventRecordDTO)
     * @see com.thinkbiganalytics.jobrepo.nifi.provenance.ProvenanceEventReceiver#successfulJob(ProvenanceEventRecordDTO)
     * @param jobExecution
     */
    private void checkIfJobAndRelatedJobsAreFinished(NifiJobExecution jobExecution) {
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


    /**
     * Find the Job Execution by the Nifi EventId and JobFlowFileId
     * @param eventId
     * @param flowfileid
     * @return
     */
    public NifiJobExecution findByEventAndFlowFile(Long eventId, String flowfileid) {
        return jobExecutionRepository.findByEventAndFlowFile(eventId, flowfileid);
    }


    public NifiJobExecution failStepsInJobThatNeedToBeFailed(NifiJobExecution jobExecution) {
        List<NifiStepExecution> steps = nifiStepExecutionRepository.findStepsInJobThatNeedToBeFailed(jobExecution.getJobExecutionId());
        if (steps != null && !steps.isEmpty()) {
            log.info(" Second Fail Attempt for {}. {} steps ", jobExecution.getJobExecutionId(), steps.size());
            for (NifiStepExecution step : steps) {
                step.failStep();
            }
            jobExecution.completeOrFailJob();
            jobExecutionRepository.save(jobExecution);
        }

        return jobExecution;
    }


    public NifiJobExecution findByJobExecutionId(Long jobExecutionId) {
        return jobExecutionRepository.findOne(jobExecutionId);
    }

    public NifiJobExecution save(NifiJobExecution jobExecution) {
        return jobExecutionRepository.save(jobExecution);
    }

}
