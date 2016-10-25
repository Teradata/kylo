package com.thinkbiganalytics.metadata.jpa.jobrepo.step;

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.step.FailedStepExecutionListener;
import com.thinkbiganalytics.metadata.jpa.jobrepo.BatchExecutionContextProvider;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiEventStepExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiEventStepExecutionRepository;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 10/24/16.
 */
@Service
public class JpaBatchStepExecutionProvider implements BatchStepExecutionProvider {

    private static final Logger log = LoggerFactory.getLogger(JpaBatchStepExecutionProvider.class);

    private BatchStepExecutionRepository nifiStepExecutionRepository;

    private NifiEventStepExecutionRepository nifiEventStepExecutionRepository;


    /**
     * Used to serialize the ExecutionContext for JOB and STEPs This is needed for usage with the existing Spring Batch Apis; however, will be removed once the UI doesnt reference those tables
     * anymore
     */
    @Autowired
    private BatchExecutionContextProvider batchExecutionContextProvider;

    private List<FailedStepExecutionListener> failedStepExecutionListeners = new ArrayList<>();

    @Autowired
    public JpaBatchStepExecutionProvider(BatchStepExecutionRepository nifiStepExecutionRepository,
                                         NifiEventStepExecutionRepository nifiEventStepExecutionRepository
    ) {

        this.nifiStepExecutionRepository = nifiStepExecutionRepository;
        this.nifiEventStepExecutionRepository = nifiEventStepExecutionRepository;

    }


    @Override
    public void subscribeToFailedSteps(FailedStepExecutionListener listener) {
        if (listener != null) {
            failedStepExecutionListeners.add(listener);
        }
    }

    /**
     * We get Nifi Events after a step has executed. If a flow takes some time we might not initially get the event that the given step has failed when we write the StepExecution record. This should
     * be called when a Job Completes as it will verify all failures and then update the correct step status to reflect the failure if there is one.
     */
    public boolean ensureFailureSteps(BatchJobExecution jobExecution) {

        //find all the Steps for this Job that have records in the Failure table for this job flow file
        List<JpaBatchStepExecution> stepsNeedingToBeFailed = nifiStepExecutionRepository.findStepsInJobThatNeedToBeFailed(jobExecution.getJobExecutionId());
        if (stepsNeedingToBeFailed != null) {
            for (JpaBatchStepExecution se : stepsNeedingToBeFailed) {
                //find the event associated to this step
                NifiEventStepExecution nifiEventStepExecution = nifiEventStepExecutionRepository.findByStepExecution(se.getStepExecutionId());
                String flowFileId = null;
                String componentId = null;
                if (nifiEventStepExecution != null) {
                    flowFileId = nifiEventStepExecution.getJobFlowFileId();
                    componentId = nifiEventStepExecution.getComponentId();
                }
                failStep(jobExecution, se, flowFileId, componentId);
            }
            //save them
            nifiStepExecutionRepository.save(stepsNeedingToBeFailed);
            return true;
        }
        return false;
    }

    public BatchStepExecution update(BatchStepExecution stepExecution) {
        return nifiStepExecutionRepository.save((JpaBatchStepExecution) stepExecution);
    }

    private void failStep(BatchJobExecution jobExecution, BatchStepExecution stepExecution, String flowFileId, String componentId) {
        ((JpaBatchStepExecution) stepExecution).failStep();
        if (failedStepExecutionListeners != null) {
            for (FailedStepExecutionListener listener : failedStepExecutionListeners) {
                listener.failedStep(jobExecution, stepExecution, flowFileId, componentId);
            }
        }

        //   update(stepExecution);

    }


    public BatchStepExecution createStepExecution(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {

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
            log.info("New Step Execution {} on Job: {} using event {} ", stepExecution.getStepName(), jobExecution.getJobExecutionId(), event.getEventId());

            boolean failure = event.isFailure();
            if (failure) {
                //notify failure listeners
                failStep(jobExecution, stepExecution, event.getFlowFileUuid(), event.getComponentId());
                if (StringUtils.isBlank(stepExecution.getExitMessage())) {
                    stepExecution.setExitMessage(event.getDetails());
                }
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
            //saving the StepExecution will cascade and save the nifiEventStep
            stepExecution = nifiStepExecutionRepository.save(stepExecution);
            //also persist to spring batch tables
            //TODO to be removed in next release once Spring batch is completely removed.  Needed since the UI references this table
            batchExecutionContextProvider.saveStepExecutionContext(stepExecution.getStepExecutionId(), event.getUpdatedAttributes());


        } else {
            //update it
            assignStepExecutionContextMap(event, stepExecution);
            //log.info("Update Step Execution {} ({}) on Job: {} using event {} ", stepExecution.getStepName(), stepExecution.getStepExecutionId(), jobExecution, event);
            stepExecution = nifiStepExecutionRepository.save(stepExecution);
            //also persist to spring batch tables
            //TODO to be removed in next release once Spring batch is completely removed.  Needed since the UI references this table
            batchExecutionContextProvider.saveStepExecutionContext(stepExecution.getStepExecutionId(), stepExecution.getStepExecutionContextAsMap());
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
}
