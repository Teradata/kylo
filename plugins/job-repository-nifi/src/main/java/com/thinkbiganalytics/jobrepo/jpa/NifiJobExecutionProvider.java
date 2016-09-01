package com.thinkbiganalytics.jobrepo.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/31/16.
 */
@Service
public class NifiJobExecutionProvider {

    @Autowired
    private JPAQueryFactory factory;

    private NifiJobExecutionRepository jobExecutionRepository;

    private NifiJobInstanceRepository jobInstanceRepository;

    private NifiJobParametersRepository nifiJobParametersRepository;

    private NifiFailedEventRepository nifiFailedEventRepository;

    private NifiEventJobExecutionRepository nifiEventJobExecutionRepository;

    private NifiStepExecutionRepository nifiStepExecutionRepository;

    @Autowired
    public NifiJobExecutionProvider(NifiJobExecutionRepository jobExecutionRepository, NifiJobInstanceRepository jobInstanceRepository, NifiFailedEventRepository nifiFailedEventRepository,
                                    NifiJobParametersRepository nifiJobParametersRepository, NifiEventJobExecutionRepository nifiEventJobExecutionRepository,
                                    NifiStepExecutionRepository nifiStepExecutionRepository) {

        this.jobExecutionRepository = jobExecutionRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.nifiFailedEventRepository = nifiFailedEventRepository;
        this.nifiJobParametersRepository = nifiJobParametersRepository;
        this.nifiEventJobExecutionRepository = nifiEventJobExecutionRepository;
        this.nifiStepExecutionRepository = nifiStepExecutionRepository;
    }

    public NifiJobInstance createJobInstance(ProvenanceEventRecordDTO event) {

        NifiJobInstance jobInstance = new NifiJobInstance();
        jobInstance.setJobKey(jobKeyGenerator(event));
        jobInstance.setJobName(event.getFeedName());
        return this.jobInstanceRepository.save(jobInstance);
    }

    private String jobKeyGenerator(ProvenanceEventRecordDTO t) {

        StringBuffer stringBuffer = new StringBuffer(t.getFlowFileUuid()); //.append(t.getEventTime().getMillis());
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


    public NifiJobExecution createJobExecution(NifiJobInstance jobInstance, ProvenanceEventRecordDTO event) {

        NifiJobExecution jobExecution = new NifiJobExecution();
        jobExecution.setJobInstance(jobInstance);
        //add in the parameters from the attributes
        jobExecution.setCreateTime(DateTime.now());
        jobExecution.setStartTime(event.getEventTime());
        jobExecution.setStatus(NifiJobExecution.JobStatus.STARTED);
        jobExecution.setExitCode(NifiJobExecution.ExitCode.EXECUTING);
        jobExecution.setLastUpdated(DateTime.now());

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
        List<NifiJobExecutionParameters> jobExecutionParametersList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : jobParameters.entrySet()) {
            NifiJobExecutionParameters jobExecutionParameters = new NifiJobExecutionParameters();
            jobExecutionParameters.setJobExecutionParametersPK(new NifiJobExecutionParameters.NifiJobExecutionParametersPK(jobExecution.getJobExecutionId(), entry.getKey()));
            jobExecutionParameters.setJobExecution(jobExecution);
            jobExecutionParameters.setStringVal(entry.getValue().toString());
            jobExecutionParametersList.add(jobExecutionParameters);
        }
        jobExecution.setJobParameters(jobExecutionParametersList);
        NifiEventJobExecution eventJobExecution = new NifiEventJobExecution(jobExecution, event.getEventId(), event.getJobFlowFileId());
        jobExecution.setNifiEventJobExecution(eventJobExecution);
        return this.jobExecutionRepository.save(jobExecution);
    }

    public NifiJobExecution createNewJobExecution(ProvenanceEventRecordDTO event) {
        NifiJobInstance jobInstance = createJobInstance(event);
        NifiJobExecution jobExecution = createJobExecution(jobInstance, event);

        return jobExecution;
    }


    public NifiStepExecution save(ProvenanceEventRecordDTO event) {
        //if its root make the JobExecution
        NifiJobExecution jobExecution = null;
        if (event.isStartOfJob()) {
            jobExecution = createNewJobExecution(event);
        } else {
            //find one
            NifiEventJobExecution nifiEventJobExecution = nifiEventJobExecutionRepository.findOne(new NifiEventJobExecution.NifiEventJobExecutionPK(event.getJobEventId(), event.getJobFlowFileId()));
            if (nifiEventJobExecution != null) {
                jobExecution = nifiEventJobExecution.getJobExecution();
            }
        }

        if (jobExecution == null) {
            //ERROR!!!!!
        } else {
            NifiStepExecution stepExecution = createStepExecution(jobExecution, event);
            if (event.isEndingFlowFileEvent() && event.getFlowFileUuid().equalsIgnoreCase(event.getJobFlowFileId())) {
                ///END OF THE JOB... fail or complete the job?

                StringBuffer stringBuffer = null;
                boolean failedJob = false;
                for (NifiStepExecution se : jobExecution.getStepExecutions()) {
                    if (NifiStepExecution.StepStatus.FAILED.equals(se.getStatus())) {
                        failedJob = true;
                        if (stringBuffer == null) {
                            stringBuffer = new StringBuffer();
                        } else {
                            stringBuffer.append(",");
                        }
                        stringBuffer.append("Failed Step " + se.getStepName());
                    }
                }
                if (failedJob) {
                    jobExecution.setExitMessage(stringBuffer != null ? stringBuffer.toString() : "");
                    jobExecution.setStatus(NifiJobExecution.JobStatus.FAILED);
                    jobExecution.setExitCode(NifiJobExecution.ExitCode.FAILED);
                } else {
                    jobExecution.setStatus(NifiJobExecution.JobStatus.COMPLETED);
                    jobExecution.setExitCode(NifiJobExecution.ExitCode.COMPLETED);
                }
            }
            this.jobExecutionRepository.save(jobExecution);
        }
        return null;
    }


    public NifiStepExecution createStepExecution(NifiJobExecution jobExecution, ProvenanceEventRecordDTO event) {
        NifiStepExecution stepExecution = new NifiStepExecution();
        stepExecution.setJobExecution(jobExecution);
        stepExecution.setStartTime(event.getPreviousEvent() != null ? event.getPreviousEvent().getEventTime() : (event.getEventTime().minus(event.getEventDuration())));
        stepExecution.setEndTime(event.getEventTime());
        //is this a failued event?
        NifiFailedEvent failedEvent = nifiFailedEventRepository.findOne(new NifiFailedEvent.NiFiFailedEventPK(event.getEventId(), event.getFlowFileUuid()));
        stepExecution.setStatus(failedEvent != null ? NifiStepExecution.StepStatus.FAILED : NifiStepExecution.StepStatus.COMPLETED);
        stepExecution.setStepName(event.getProcessorName());

        ///TODO: update NifiEvent mapping so we know the stepid to flowfile?

        //add in execution contexts
        List<NifiStepExecutionContext> stepExecutionContextList = new ArrayList<>();
        Map<String, String> updatedAttrs = event.getUpdatedAttributes();
        if (updatedAttrs != null && !updatedAttrs.isEmpty()) {
            for (Map.Entry<String, String> entry : updatedAttrs.entrySet()) {
                NifiStepExecutionContext stepExecutionContext = new NifiStepExecutionContext(stepExecution, entry.getKey());
                stepExecutionContext.setStringVal(entry.getValue());
                stepExecutionContextList.add(stepExecutionContext);
            }
        }
        stepExecution.setStepExecutionContext(stepExecutionContextList);
        stepExecution = nifiStepExecutionRepository.save(stepExecution);
        jobExecution.getStepExecutions().add(stepExecution);

        return stepExecution;
    }

}
