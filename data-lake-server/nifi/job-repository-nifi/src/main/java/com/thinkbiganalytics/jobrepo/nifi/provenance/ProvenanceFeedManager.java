package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.activemq.ObjectMapperSerializer;
import com.thinkbiganalytics.jobrepo.common.constants.CheckDataStepConstants;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.jobrepo.repository.dao.NifiJobRepository;

import org.apache.nifi.web.api.dto.BulletinDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 2/26/16.
 */
@Component
public class ProvenanceFeedManager {

    private static final Logger LOG = LoggerFactory.getLogger(ProvenanceFeedManager.class);
    public static final String NIFI_JOB_TYPE_PROPERTY = "tb.jobType";

    private ObjectMapperSerializer objectMapperSerializer;
    @Autowired
    private NifiJobRepository jobRepository;


    @Autowired
    private NifiComponentFlowData nifiComponentFlowData;

    private Map<String, NifiJobExecution> flowFileJobExecutions = new HashMap<>();

    public ProvenanceFeedManager() {
        objectMapperSerializer = new ObjectMapperSerializer();
    }

    public String getFeedNameForComponentId(String componentId) {
        return nifiComponentFlowData.getFeedNameForComponentId(componentId);
    }

    public ProcessorDTO getFeedProcessor(String componentId) {
        return nifiComponentFlowData.getFeedProcessor(componentId);
    }

    public ProcessGroupDTO getFeedProcessGroup(String componentId) {
        return nifiComponentFlowData.getFeedProcessGroup(componentId);
    }

    public boolean isFailureProcessor(String componentId) {
        return nifiComponentFlowData.isFailureProcessor(componentId);
    }

    public Integer getEndingProcessorCount(String componentId) {
        return nifiComponentFlowData.getEndingProcessorCount(componentId);
    }

    public Set<String> getEndingProcessorIds(String componentId) {
        return nifiComponentFlowData.getEndingProcessorIds(componentId);
    }

    public List<BulletinDTO> getBulletins(ProvenanceEventRecordDTO event) {
        return nifiComponentFlowData.getBulletinsNotYetProcessed(event);
    }

    public List<BulletinDTO> getBulletinForEvent(ProvenanceEventRecordDTO event) {
        return nifiComponentFlowData.getBulletinsNotYetProcessed(event);
    }

    public List<BulletinDTO> getBulletinForOtherEvents(ProvenanceEventRecordDTO event) {
        return nifiComponentFlowData.getBulletinsNotYetProcessed(event);
    }

    public void updateJobType(ProvenanceEventRecordDTO event)
    {
        NifiJobExecution jobExecution = event.getFlowFileComponent().getJobExecution();
        if(event.getUpdatedAttributes().containsKey(NIFI_JOB_TYPE_PROPERTY)) {
            String jobType = (String) event.getUpdatedAttributes().get(NIFI_JOB_TYPE_PROPERTY);
            String feedName = (String) event.getAttributeMap().get(FeedConstants.PARAM__FEED_NAME);
            LOG.info("UPDATING JOB TYPE TO BE " + jobType+" for Feed "+feedName);
            if(FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK.equalsIgnoreCase(jobType)) {
                jobExecution.setJobType(FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK);
                jobRepository.setAsCheckDataJob(jobExecution.getJobExecutionId(), feedName);
            }
        }
    }

    /**
     * If a processor throws a runtime exception a ProvenanceEvent may not be recorded.
     * The Processor will log the error to the BulletinBoard.
     * This method will check the BulletinBoard for any errors that have not been processed, create the missing Event and Component
     * and mark it as failed with the correct log message as the Execution details.
     *
     * @param event
     * @return
     */
    public boolean processBulletinsAndFailComponents(ProvenanceEventRecordDTO event) {
        boolean failedStep = false;
        NifiJobExecution jobExecution = event.getFlowFileComponent().getJobExecution();
        Map<String, Set<BulletinDTO>> otherErrors = new HashMap<>();
        //Get any bulletins that have not been processed and dont match the current component
        //if this happens it is because the processor failed and didnt get registered in the PRovenance event list.
        //we need to create a new  Component and Event with the bulletin id and add it in the correct place in the event history

        List<BulletinDTO> bulletins = nifiComponentFlowData.getBulletinsNotYetProcessedForOtherComponents(event);
        if (bulletins != null && !bulletins.isEmpty()) {
            for (BulletinDTO dto : bulletins) {
                if (dto != null && dto.getSourceId() != null) {
                    if (!otherErrors.containsKey(dto.getSourceId())) {
                        otherErrors.put(dto.getSourceId(), new HashSet<BulletinDTO>());
                    }
                    otherErrors.get(dto.getSourceId()).add(dto);
                }
            }
        }
        //if the otherErrors exist then we need to add in the component that does not exist in the current flow
        for (Map.Entry<String, Set<BulletinDTO>> entry : otherErrors.entrySet()) {
            String componentId = entry.getKey();
            Set<BulletinDTO> otherBulletins = entry.getValue();
            if (!jobExecution.containsComponent(componentId)) {
                //need to add it in
                //create a new PovenanceEventRecordDTO and associated componentId and then add it in prior to this current event?
                ProvenanceEventRecordDTO failedEventAndComponent = nifiComponentFlowData.createFailedComponentPriorToEvent(event, componentId, otherBulletins);
                //TODO add timing to start event at previous event endtime...
                //Save this now as a failed step
                //create the step and then fail it
                jobRepository.saveStepExecution(failedEventAndComponent.getFlowFileComponent());
                saveStepExecutionContext(failedEventAndComponent);
                jobExecution.addComponentToOrder(failedEventAndComponent.getFlowFileComponent());
                if (failedEventAndComponent.getFlowFileComponent().isRunning() || failedEventAndComponent.getFlowFileComponent().getEndTime() == null) {
                    failedEventAndComponent.markFailed();
                    failedEventAndComponent.getFlowFileComponent().markFailed();
                }
                jobRepository.failStep(failedEventAndComponent.getFlowFileComponent());
                jobExecution.addFailedComponent(failedEventAndComponent.getFlowFileComponent());
                failedStep = true;

            } else {
                FlowFileComponent component = jobExecution.getComponent(componentId);
                if (component.getStepExecutionId() != null && !component.isStepFinished()) {
                    String details = nifiComponentFlowData.getBulletinDetails(otherBulletins);
                    component.getLastEvent().setDetails(details);
                    if (component.isRunning() || component.getEndTime() == null) {
                        component.markFailed();
                    }
                    LOG.info("About to fail step for  for other component that was already in flow " + component);
                    jobRepository.failStep(component);
                    failedStep = true;
                    jobExecution.addBulletinErrors(otherBulletins);
                    jobExecution.addFailedComponent(component);

                } else {
                    //We should never get to this block but if we do
                    //add them to the job execution context
                    LOG.error("Additional Bulletin Exceptions found " + componentId + ", " + otherBulletins);
                    if (event.hasJobExecution()) {
                        Map<String, Object> details = new HashMap<>();
                        details.put("Additional Bulletin Messages", "Bulletin messages were found for components in the flow that were already completed.");
                        for (BulletinDTO bulletinDTO : otherBulletins) {
                            String json = objectMapperSerializer.serialize(bulletinDTO);
                            details.put("Bulletin Id:" + bulletinDTO.getId(), json);
                        }
                        jobRepository.saveJobExecutionContext(event.getFlowFileComponent().getJobExecution(), details);

                    }
                }
            }
        }

        return failedStep;

    }


    public NifiJobExecution feedStart(ProvenanceEventRecordDTO event) {
        //get the Feed Name associated with this events component
        String feedName = getFeedNameForComponentId(event.getComponentId());

        //TODO figure out how to deal with Restarts reusing the same Job Instance
        NifiJobExecution jobExecution = new NifiJobExecution(feedName, event);
        jobExecution.setEndingProcessorCount(getEndingProcessorCount(event.getComponentId()));
        jobExecution.setEndingProcessorComponentIds(getEndingProcessorIds(event.getComponentId()));
        jobExecution.markStarted();
        //add the new Job to the map for lookup later
        flowFileJobExecutions.put(event.getFlowFileUuid(), jobExecution);
        //mark it as started/running
        jobExecution.markRunning();
        Long jobInstanceId = jobRepository.createJobInstance(jobExecution);
        jobExecution.setJobInstanceId(jobInstanceId);
        //create the jobExecution
        Long jobExecutionId = jobRepository.saveJobExecution(jobExecution);
        jobExecution.setJobExecutionId(jobExecutionId);
        event.getFlowFileComponent().setJobExecution(jobExecution);
        //write some attrs to the job execution context

        Map<String, Object> executionContext = new HashMap<>();
        ProcessorDTO feedProcessor = getFeedProcessor(event.getComponentId());
        ProcessGroupDTO feedGroup = getFeedProcessGroup(event.getComponentId());
        executionContext.put("Feed Process Group Id", feedGroup.getId());
        executionContext.put("Feed Name", feedName);
        jobRepository.saveJobExecutionContext(jobExecution,executionContext);

        return jobExecution;

    }

    private void saveStepExecutionContext(ProvenanceEventRecordDTO event){
        Map<String,Object> attrs = new HashMap<>();
        attrs.put("Group Id", getFeedProcessor(event.getComponentId()).getParentGroupId());
        attrs.put("Component Id", event.getFlowFileComponent().getComponentId());
        attrs.put("Event Id", event.getEventId().toString());
        attrs.put("Flow File Id", event.getFlowFileUuid());
        attrs.put("Component Type", event.getComponentType());
        jobRepository.saveStepExecutionContext(event.getFlowFileComponent(),attrs);
    }


    public void componentStarted(ProvenanceEventRecordDTO event) {
        if (hasJobExecution(event)) {
            //set the componentName
            ProcessorDTO dto = getFeedProcessor(event.getComponentId());
            if (dto != null) {
                event.getFlowFileComponent().setComponetName(dto.getName());
            } else {
                //add a unique name
                event.getFlowFileComponent().setComponetName(event.getComponentType());
            }
            Long stepId = jobRepository.saveStepExecution(event.getFlowFileComponent());
            saveStepExecutionContext(event);

            event.getFlowFileComponent().getJobExecution().addComponentToOrder(event.getFlowFileComponent());
        } else {
            LOG.error(" Not Starting Component." + event.getFlowFileComponent() + "  NifiJobExecution is null");
        }

    }

    private boolean hasJobExecution(ProvenanceEventRecordDTO event) {
        return event.getFlowFileComponent().getJobExecution() != null;
    }

    public void componentCompleted(ProvenanceEventRecordDTO event) {
        if (hasJobExecution(event)) {

            List<BulletinDTO> bulletins = nifiComponentFlowData.getBulletinsNotYetProcessedForComponent(event);
            if (bulletins != null && !bulletins.isEmpty() && event.getFlowFileComponent().getStepExecutionId() != null) {
                String details = nifiComponentFlowData.getBulletinDetails(bulletins);
                event.setDetails(details);
                if (event.getFlowFileComponent().isRunning() || event.getFlowFileComponent().getEndTime() == null) {
                    event.getFlowFileComponent().markFailed();
                }
                jobRepository.failStep(event.getFlowFileComponent());
                event.getFlowFileComponent().getJobExecution().addFailedComponent(event.getFlowFileComponent());
                event.getFlowFileComponent().getJobExecution().addBulletinErrors(bulletins);
            } else {

                    jobRepository.completeStep(event.getFlowFileComponent());

            }
            event.getFlowFileComponent().getJobExecution().componentComplete(event.getComponentId());
        } else {
            LOG.error(" Not Completing Component." + event.getFlowFileComponent() + "  NifiJobExecution is null");
        }
    }

    public void componentFailed(ProvenanceEventRecordDTO event) {
        if (hasJobExecution(event)) {
            jobRepository.failStep(event.getFlowFileComponent());
            event.getFlowFileComponent().getJobExecution().addFailedComponent(event.getFlowFileComponent());
        } else {
            LOG.error(" Not Failing Component." + event.getFlowFileComponent() + "  NifiJobExecution is null");
        }
    }

    public void feedCompleted(ProvenanceEventRecordDTO event) {
        if (hasJobExecution(event)) {
            event.getFlowFileComponent().getJobExecution().markCompleted();
            NifiJobExecution jobExecution = event.getFlowFileComponent().getJobExecution();
            if (jobExecution.hasFailedComponents()) {
                jobRepository.failJobExecution(jobExecution);
                LOG.info("Failing Job "+jobExecution.getJobExecutionId()+", "+jobExecution.getFailedComponents().size()+" Failed Components exist");
            } else {



                //if it is a Check Data Job ensure the Validation string is true or otherwise fail the job
                if(event.getFlowFileComponent().getJobExecution().isCheckDataJob()) {
                    String value = event.getAttributeMap().get(CheckDataStepConstants.VALIDATION_KEY);
                    String message = event.getAttributeMap().get(CheckDataStepConstants.VALIDATION_MESSAGE_KEY);
                    Map<String,Object> jobExecutionAttrs = new HashMap<>();
                    jobExecutionAttrs.put(CheckDataStepConstants.VALIDATION_KEY,value);
                    jobExecutionAttrs.put(CheckDataStepConstants.VALIDATION_MESSAGE_KEY, message);
                    jobRepository.saveJobExecutionContext(event.getFlowFileComponent().getJobExecution(),jobExecutionAttrs);
                    if(!"true".equalsIgnoreCase(value)) {
                        jobRepository.failJobExecution(jobExecution);
                        event.getFlowFileComponent().getJobExecution().addFailedComponent(event.getFlowFileComponent());
                        LOG.info("Failing Check DAta  Job " + jobExecution.getJobExecutionId() + ", " + jobExecution.getFailedComponents().size() + ".");
                    }
                   else {
                        jobRepository.completeJobExecution(jobExecution);
                        LOG.info("Completing Job " + jobExecution.getJobExecutionId() + ", " + jobExecution.getFailedComponents().size() + "."+" jobExecutionAttrs: "+jobExecutionAttrs);
                    }
                }
                else {
                    jobRepository.completeJobExecution(jobExecution);
                    LOG.info("Completing Job " + jobExecution.getJobExecutionId() + ", " + jobExecution.getFailedComponents().size() + ".");
                }

            }
        } else {
            String feedName = getFeedNameForComponentId(event.getComponentId());
            LOG.error(" Not Completing Feed. " + feedName + "  NifiJobExecution is null");
        }

    }

    public void feedEvent(ProvenanceEventRecordDTO event) {
        if (event.hasUpdatedAttributes() && event.getFlowFileComponent() != null) {
            //write to the step execution context
            jobRepository.saveStepExecutionContext(event.getFlowFileComponent(), event.getUpdatedAttributes());
        }






    }


}
