package com.thinkbiganalytics.metadata.jpa.jobrepo.step;

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

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.step.FailedStepExecutionListener;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiEventStepExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiEventStepExecutionRepository;
import com.thinkbiganalytics.nifi.provenance.KyloProcessorFlowType;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
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
 * Provider for accessing {@link JpaBatchStepExecution}
 */
@Service
public class JpaBatchStepExecutionProvider implements BatchStepExecutionProvider {

    private static final Logger log = LoggerFactory.getLogger(JpaBatchStepExecutionProvider.class);

    private BatchStepExecutionRepository batchStepExecutionRepository;

    private NifiEventStepExecutionRepository nifiEventStepExecutionRepository;


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
     *
     * @param jobExecution the job execution
     * @return {@code true} if the steps were evaluated, {@code false} if no work was needed to be done
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
    }


    public BatchStepExecution createStepExecution(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event) {

        //only create the step if it doesnt exist yet for this event
        JpaBatchStepExecution stepExecution = batchStepExecutionRepository.findByProcessorAndJobFlowFile(event.getComponentId(), event.getJobFlowFileId());
        if (stepExecution == null) {
            if(!"KYLO".equalsIgnoreCase(event.getEventType())) {
                stepExecution = new JpaBatchStepExecution();
                stepExecution.setJobExecution(jobExecution);
                stepExecution.setStartTime(event.getStartTime() != null ? DateTimeUtil.convertToUTC(event.getStartTime()) :
                                            DateTimeUtil.convertToUTC(event.getEventTime()).minus(event.getEventDuration()));
                stepExecution.setEndTime(DateTimeUtil.convertToUTC(event.getEventTime()));
                stepExecution.setStepName(event.getComponentName());
                if (StringUtils.isBlank(stepExecution.getStepName())) {
                    stepExecution.setStepName("Unknown Step ");
                }
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
                if (steps == null) {
                    ((JpaBatchJobExecution) jobExecution).setStepExecutions(new HashSet<>());
                }
                jobExecution.getStepExecutions().add(stepExecution);
                //saving the StepExecution will cascade and save the nifiEventStep
                stepExecution = batchStepExecutionRepository.save(stepExecution);
            }

        } else {
            //update it
            assignStepExecutionContextMap(event, stepExecution);
            //update the timing info
            Long originatingNiFiEventId = stepExecution.getNifiEventStepExecution().getEventId();
            //only update the end time if the eventid is > than the first one
            if(event.getEventId()> originatingNiFiEventId) {
                DateTime newEndTime = DateTimeUtil.convertToUTC(event.getEventTime());
                if (newEndTime.isAfter(stepExecution.getEndTime())) {
                    stepExecution.setEndTime(newEndTime);
                }
            }
            else {
                DateTime newStartTime = DateTimeUtil.convertToUTC(event.getStartTime());
                if (newStartTime.isBefore(stepExecution.getStartTime())) {
                    stepExecution.setStartTime(newStartTime);
                }
            }
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
        //add the event id for debugging

        JpaBatchStepExecutionContextValue eventIdContextValue = new JpaBatchStepExecutionContextValue(stepExecution, "NiFi Event Id");
        eventIdContextValue.setStringVal(event.getEventId().toString());
        stepExecution.addStepExecutionContext(eventIdContextValue);

    }
}
