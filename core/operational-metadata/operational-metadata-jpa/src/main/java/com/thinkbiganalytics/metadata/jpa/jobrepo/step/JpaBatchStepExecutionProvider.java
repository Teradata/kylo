package com.thinkbiganalytics.metadata.jpa.jobrepo.step;

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.common.constants.KyloProcessorFlowType;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.step.FailedStepExecutionListener;
import com.thinkbiganalytics.metadata.jpa.jobrepo.BatchExecutionContextProvider;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiEventStepExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiEventStepExecutionRepository;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 10/24/16.
 */
@Service
public class JpaBatchStepExecutionProvider implements BatchStepExecutionProvider {

    private static final Logger log = LoggerFactory.getLogger(JpaBatchStepExecutionProvider.class);

    private BatchStepExecutionRepository batchStepExecutionRepository;

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

        this.batchStepExecutionRepository = nifiStepExecutionRepository;
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
        List<JpaBatchStepExecution> stepsNeedingToBeFailed = batchStepExecutionRepository.findStepsInJobThatNeedToBeFailed(jobExecution.getJobExecutionId());
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
            batchStepExecutionRepository.save(stepsNeedingToBeFailed);
            return true;
        }
        return false;
    }

    public BatchStepExecution update(BatchStepExecution stepExecution) {
        return batchStepExecutionRepository.save((JpaBatchStepExecution) stepExecution);
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
        JpaBatchStepExecution stepExecution = batchStepExecutionRepository.findByProcessorAndJobFlowFile(event.getComponentId(), event.getJobFlowFileId());
        if (stepExecution == null) {

            stepExecution = new JpaBatchStepExecution();
            stepExecution.setJobExecution(jobExecution);
            stepExecution.setStartTime(event.getStartTime() != null ? DateTimeUtil.convertToUTC(event.getStartTime()) :
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
            Set<BatchStepExecution> steps = jobExecution.getStepExecutions();
            if(steps == null){
                ((JpaBatchJobExecution)jobExecution).setStepExecutions(new HashSet<>());
            }
            jobExecution.getStepExecutions().add(stepExecution);
            //saving the StepExecution will cascade and save the nifiEventStep
            stepExecution = batchStepExecutionRepository.save(stepExecution);

        } else {
            //update it
            assignStepExecutionContextMap(event, stepExecution);
            //log.info("Update Step Execution {} ({}) on Job: {} using event {} ", stepExecution.getStepName(), stepExecution.getStepExecutionId(), jobExecution, event);
            stepExecution = batchStepExecutionRepository.save(stepExecution);
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
        //add in the flow type if its there
        if (event.getProcessorType() != null && !KyloProcessorFlowType.NORMAL_FLOW.equals(event.getProcessorType())) {
            KyloProcessorFlowType processorFlowType = event.getProcessorType();

            JpaBatchStepExecutionContextValue stepExecutionContext = new JpaBatchStepExecutionContextValue(stepExecution, "Kylo Flow Processor Type");
            stepExecutionContext.setStringVal(processorFlowType != null ? processorFlowType.getDisplayName() : "NULL");
            stepExecution.addStepExecutionContext(stepExecutionContext);
            if (KyloProcessorFlowType.WARNING.equals(event.getProcessorType())) {
                stepExecution.setExitCode(ExecutionConstants.ExitCode.WARNING);
                ((JpaBatchJobExecution) (stepExecution.getJobExecution())).setExitCode(ExecutionConstants.ExitCode.WARNING);
            } else if (KyloProcessorFlowType.FAILURE.equals(event.getProcessorType())) {
                stepExecution.setExitCode(ExecutionConstants.ExitCode.FAILED);
            }

        }
    }


    public List<? extends BatchStepExecution> getSteps(Long jobExecutionId){
        return batchStepExecutionRepository.findSteps(jobExecutionId);
    }




}
