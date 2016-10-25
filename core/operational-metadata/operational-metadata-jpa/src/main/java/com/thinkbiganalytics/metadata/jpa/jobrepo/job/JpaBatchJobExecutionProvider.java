package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.common.constants.CheckDataStepConstants;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.metadata.api.OperationalMetadataAccess;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobInstance;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecutionProvider;
import com.thinkbiganalytics.metadata.jpa.jobrepo.BatchExecutionContextProvider;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiEventJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiRelatedRootFlowFiles;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiRelatedRootFlowFilesRepository;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;

/**
 * Created by sr186054 on 8/31/16.
 */
@Service
public class JpaBatchJobExecutionProvider implements BatchJobExecutionProvider {

    private static final Logger log = LoggerFactory.getLogger(JpaBatchJobExecutionProvider.class);

    private static String PARAM_TB_JOB_TYPE = "tb.jobType";


    /**
     * Used to serialize the ExecutionContext for JOB and STEPs This is needed for usage with the existing Spring Batch Apis; however, will be removed once the UI doesnt reference those tables
     * anymore
     */
    @Autowired
    private BatchExecutionContextProvider batchExecutionContextProvider;

    @Inject
    private OperationalMetadataAccess operationalMetadataAccess;



    @Autowired
    private JPAQueryFactory factory;

    private BatchJobExecutionRepository jobExecutionRepository;

    private BatchJobInstanceRepository jobInstanceRepository;

    private BatchJobParametersRepository jobParametersRepository;


    private NifiRelatedRootFlowFilesRepository relatedRootFlowFilesRepository;

    @Inject
    private BatchStepExecutionProvider batchStepExecutionProvider;





    @Autowired
    public JpaBatchJobExecutionProvider(BatchJobExecutionRepository jobExecutionRepository, BatchJobInstanceRepository jobInstanceRepository,
                                        NifiRelatedRootFlowFilesRepository relatedRootFlowFilesRepository,
                                        BatchJobParametersRepository jobParametersRepository
    ) {

        this.jobExecutionRepository = jobExecutionRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.relatedRootFlowFilesRepository = relatedRootFlowFilesRepository;
        this.jobParametersRepository = jobParametersRepository;

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
        jobExecution.setCreateTime(DateTimeUtil.getNowUTCTime());
        jobExecution.setStartTime(DateTimeUtil.convertToUTC(event.getEventTime()));
        jobExecution.setStatus(BatchJobExecution.JobStatus.STARTED);
        jobExecution.setExitCode(ExecutionConstants.ExitCode.EXECUTING);
        jobExecution.setLastUpdated(DateTimeUtil.getNowUTCTime());

        //create the job params
        Map<String, Object> jobParameters = new HashMap<>();
        if(event.isStartOfJob() && event.getAttributeMap() != null) {
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
        jobParameters.put(FeedConstants.PARAM__FEED_IS_PARENT, "true");
        Set<JpaBatchJobExecutionParameter> jpaJobParameters = addJobParameters(jobExecution, jobParameters);
        this.jobParametersRepository.save(jpaJobParameters);
        return jobExecution;
    }

    private Set<JpaBatchJobExecutionParameter> addJobParameters(JpaBatchJobExecution jobExecution, Map<String, Object> jobParameters) {
        Set<JpaBatchJobExecutionParameter> jobExecutionParametersList = new HashSet<>();
        for (Map.Entry<String, Object> entry : jobParameters.entrySet()) {
            JpaBatchJobExecutionParameter jobExecutionParameters = jobExecution.addParameter(entry.getKey(), entry.getValue());
            jobExecutionParametersList.add(jobExecutionParameters);
        }
        return jobExecutionParametersList;
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
     * Check to see if the NifiEvent has the attributes indicating it is a Check Data Job
     */
    private boolean isCheckDataJob(ProvenanceEventRecordDTO event) {
        if (event.getAttributeMap() != null) {
            String jobType = event.getAttributeMap().get(PARAM_TB_JOB_TYPE);
            return StringUtils.isNotBlank(jobType) && FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK.equalsIgnoreCase(jobType);
        }
        return false;
    }

    /**
     * When the job is complete determine its status, write out exection context, and determine if all related jobs are complete
     */
    private void finishJob(ProvenanceEventRecordDTO event, JpaBatchJobExecution jobExecution) {

        if (jobExecution.getJobExecutionId() == null) {
            log.error("Warning execution id is null for ending event {} ", event);
        }
        if(event.isHasFailedEvents()){
            jobExecution.failJob();
        }
        else {
            jobExecution.completeJob();
        }

        ///END OF THE JOB... fail or complete the job?
        //  ensureFailureSteps(jobExecution);
        //jobExecution.completeOrFailJob();

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
        //ensureRelatedJobsAreFinished(event,jobExecution);

    }


    /**
     * Get or Create the JobExecution for a given ProvenanceEvent
     * @param event
     * @return
     */
   public synchronized  JpaBatchJobExecution getOrCreateJobExecution(ProvenanceEventRecordDTO event){
           JpaBatchJobExecution jobExecution = null;
       boolean isNew = false;
        try {
            jobExecution = jobExecutionRepository.findByFlowFile(event.getJobFlowFileId());
            if (jobExecution == null) {
              jobExecution =  createNewJobExecution(event);
                isNew = true;
            }
        }catch(OptimisticLockException e){
            //read
            jobExecution = jobExecutionRepository.findByFlowFile(event.getJobFlowFileId());
        }

       //if the attrs coming in change the type to a CHECK job then update the entity
      boolean updatedJobType = updateJobType(jobExecution, event);
       boolean save = isNew || updatedJobType;
       if (event.isEndOfJob()) {
           finishJob(event, jobExecution);
           save = true;
       }

       //if the event is the start of the Job, but the job execution was created from another downstream event, ensure the start time and event are related correctly
       if(event.isStartOfJob() && !isNew ){
           jobExecution.getNifiEventJobExecution().setEventId(event.getEventId());
           jobExecution.setStartTime(DateTimeUtil.convertToUTC(event.getEventTime()));
           //create the job params
           Map<String, Object> jobParameters = new HashMap<>();
           if(event.isStartOfJob() && event.getAttributeMap() != null) {
               jobParameters = new HashMap<>(event.getAttributeMap());
           } else {
               jobParameters = new HashMap<>();
           }

           this.jobParametersRepository.save(addJobParameters(jobExecution, jobParameters));
           save = true;
       }
       if(save) {
           jobExecutionRepository.save(jobExecution);
       }
    return jobExecution;
    }

    public BatchJobExecution save(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event, NifiEvent nifiEvent) {
        if(jobExecution == null){
            //log
            return null;
        }
            JpaBatchJobExecution jpaBatchJobExecution = (JpaBatchJobExecution)jobExecution;
            checkAndRelateJobs(event, nifiEvent);
           BatchStepExecution stepExecution = batchStepExecutionProvider.createStepExecution(jobExecution, event);

          /*  if (event.isEndOfJob()) {
                finishJob(event, jpaBatchJobExecution);
                jobExecution =    this.jobExecutionRepository.save(jpaBatchJobExecution);
            }
       */

        if (jobExecution.isFinished()) {
            //ensure failures
            boolean addedFailures = batchStepExecutionProvider.ensureFailureSteps(jpaBatchJobExecution);

        }
        return jobExecution;
    }

    @Override
    public BatchJobExecution save(ProvenanceEventRecordDTO event, NifiEvent nifiEvent) {
        JpaBatchJobExecution jobExecution = getOrCreateJobExecution(event);
        return save(jobExecution,event,nifiEvent);

    }








    /**
     * Sets the Job Execution params to either Check Data or Feed Jobs
     */
    private boolean updateJobType(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {

        if (event.getUpdatedAttributes() != null && event.getUpdatedAttributes().containsKey(NIFI_JOB_TYPE_PROPERTY)) {
            String jobType = event.getUpdatedAttributes().get(NIFI_JOB_TYPE_PROPERTY);
            String nifiCategory = event.getAttributeMap().get(NIFI_CATEGORY_PROPERTY);
            String nifiFeedName = event.getAttributeMap().get(NIFI_FEED_PROPERTY);
            String feedName = FeedNameUtil.fullName(nifiCategory, nifiFeedName);
            if (FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK.equalsIgnoreCase(jobType)) {
                Set<JpaBatchJobExecutionParameter> updatedParams = ((JpaBatchJobExecution) jobExecution).setAsCheckDataJob(feedName);
                jobParametersRepository.save(updatedParams);
                return true;
            }
        }
        return false;
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
                    log.debug("FINISHED AND FAILED JOB with relation {} ", jobExecution.getJobExecutionId());
                } else {
                    log.debug("FINISHED JOB with relation {} ", jobExecution.getJobExecutionId());
                }
            }
        } else {
            if (jobExecution.isFailed()) {
                log.debug("Failed JobExecution");
            } else if (jobExecution.isSuccess()) {
                log.debug("Completed Job Execution");
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
                    log.debug("Finishing related running job {} for event ", job.getJobExecutionId(), event);
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


/*
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
    */


    @Override
    public BatchJobExecution findByJobExecutionId(Long jobExecutionId) {
        return jobExecutionRepository.findOne(jobExecutionId);
    }

    @Override
    public BatchJobExecution save(BatchJobExecution jobExecution) {
        return jobExecutionRepository.save((JpaBatchJobExecution) jobExecution);
    }

}
