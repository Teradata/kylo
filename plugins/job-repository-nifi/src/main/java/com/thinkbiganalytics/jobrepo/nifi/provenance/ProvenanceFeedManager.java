package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.activemq.ObjectMapperSerializer;
import com.thinkbiganalytics.jobrepo.common.constants.CheckDataStepConstants;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.repository.JobRepository;
import com.thinkbiganalytics.jobrepo.repository.dao.NifiJobRepository;
import com.thinkbiganalytics.nifi.rest.client.NifiConnectionException;

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

import javax.inject.Inject;

/**
 * Created by sr186054 on 2/26/16.
 */
@Component
public class ProvenanceFeedManager {

    private static final Logger LOG = LoggerFactory.getLogger(ProvenanceFeedManager.class);
    public static final String NIFI_JOB_TYPE_PROPERTY = "tb.jobType";
    public static final String NIFI_FEED_PROPERTY = "feed";
    public static final String NIFI_CATEGORY_PROPERTY = "category";
    public static final String AUTO_TERMINATED_FAILURE_RELATIONSHIP = "auto-terminated by failure relationship";

    private ObjectMapperSerializer objectMapperSerializer;
    @Autowired
    private NifiJobRepository jobRepository;

    @Inject
    private JobRepository executedJobsRepository;

    @Autowired
    private NifiComponentFlowData nifiComponentFlowData;

    @Autowired
    private ProvenanceEventExecutedJob provenanceEventExecutedJob;


    public ProvenanceFeedManager() {
        objectMapperSerializer = new ObjectMapperSerializer();
    }


    public ProcessorDTO getProcessor(String id) {
        return nifiComponentFlowData.getProcessor(id);
    }

    public String getFeedNameForComponentId(ProvenanceEventRecordDTO event) {
        return nifiComponentFlowData.getFeedNameForComponentId(event);
    }

    public ProcessorDTO getFeedProcessor(ProvenanceEventRecordDTO event) {
        return nifiComponentFlowData.getFeedProcessor(event);
    }

    public ProcessGroupDTO getFeedProcessGroup(ProvenanceEventRecordDTO event) {
        return nifiComponentFlowData.getFeedProcessGroup(event);
    }

    public boolean isFailureProcessor(ProvenanceEventRecordDTO event) {
        return nifiComponentFlowData.isFailureProcessor(event);
    }

    public Integer getEndingProcessorCount(ProvenanceEventRecordDTO event) {
        return nifiComponentFlowData.getEndingProcessorCount(event);
    }

    public Set<String> getEndingProcessorIds(ProvenanceEventRecordDTO event) {
        return nifiComponentFlowData.getEndingProcessorIds(event);
    }


    public void populateGroupIdForEvent(ProvenanceEventRecordDTO event) {
        nifiComponentFlowData.populateGroupIdForEvent(event);
    }


    public List<BulletinDTO> getBulletins(ProvenanceEventRecordDTO event) {
        return nifiComponentFlowData.getBulletinsNotYetProcessed(event);
    }


    public void updateJobType(ProvenanceEventRecordDTO event) {
        NifiJobExecution jobExecution = event.getFlowFileComponent().getJobExecution();
        if (event.getUpdatedAttributes().containsKey(NIFI_JOB_TYPE_PROPERTY)) {
            String jobType = (String) event.getUpdatedAttributes().get(NIFI_JOB_TYPE_PROPERTY);
            String nifiCategory = (String) event.getAttributeMap().get(NIFI_CATEGORY_PROPERTY);
            String nifiFeedName = (String) event.getAttributeMap().get(NIFI_FEED_PROPERTY);
            String feedName = nifiCategory + "." + nifiFeedName;
            LOG.info("UPDATING JOB TYPE TO BE " + jobType + " for Feed " + feedName);
            if (FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK.equalsIgnoreCase(jobType)) {
                jobExecution.setJobType(FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK);
                jobRepository.setAsCheckDataJob(jobExecution.getJobExecutionId(), feedName);
            }
        }
    }


    /**
     * If a processor throws a runtime exception a ProvenanceEvent may not be recorded. The Processor will log the error to the BulletinBoard. This method will check the BulletinBoard for any errors
     * that have not been processed, create the missing Event and Component and mark it as failed with the correct log message as the Execution details.
     */
    public boolean processBulletinsAndFailComponents(ProvenanceEventRecordDTO event) {
        boolean failedStep = false;
        NifiJobExecution jobExecution = event.getFlowFileComponent().getJobExecution();
        Map<String, Set<BulletinDTO>> otherErrors = new HashMap<>();

        Set<BulletinDTO> warnings = new HashSet<>();
        //Get any bulletins that have not been processed and dont match the current component
        //if this happens it is because the processor failed and didnt get registered in the Provenance event list.
        //we need to create a new  Component and Event with the bulletin id and add it in the correct place in the event history

        List<BulletinDTO> bulletins = nifiComponentFlowData.getBulletinsNotYetProcessedForOtherComponents(event);
        if (bulletins != null && !bulletins.isEmpty()) {
            LOG.debug("Checking for Bulletins... found {} for NIFI Processor ID: {}", bulletins.size(), event.getComponentId());
            for (BulletinDTO dto : bulletins) {
                if (dto != null && dto.getSourceId() != null) {
                    if (!otherErrors.containsKey(dto.getSourceId())) {
                        otherErrors.put(dto.getSourceId(), new HashSet<BulletinDTO>());
                    }
                    otherErrors.get(dto.getSourceId()).add(dto);
                }
                if ("WARNING".equalsIgnoreCase(dto.getLevel())) {
                    warnings.add(dto);
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
                if (!warnings.containsAll(otherBulletins)) {
                    if (failedEventAndComponent.getFlowFileComponent().isRunning()
                        || failedEventAndComponent.getFlowFileComponent().getEndTime() == null) {
                        failedEventAndComponent.markFailed();
                        failedEventAndComponent.getFlowFileComponent().markFailed();
                    }
                    LOG.info("Fail Step due to other bulletins {}", failedEventAndComponent.getFlowFileComponent());
                    jobRepository.failStep(failedEventAndComponent.getFlowFileComponent());
                    jobExecution.addFailedComponent(failedEventAndComponent.getFlowFileComponent());
                    failedStep = true;
                } else {
                    // this is just a warning, complete the step
                    failedEventAndComponent.markCompleted();
                    failedEventAndComponent.getFlowFileComponent().markCompleted();
                    jobRepository.completeStep(failedEventAndComponent.getFlowFileComponent());
                }
                jobExecution.addBulletinErrors(otherBulletins);

            } else {
                FlowFileComponent component = jobExecution.getComponent(componentId);
                if (component.getStepExecutionId() != null && !component.isStepFinished()) {
                    String details = nifiComponentFlowData.getBulletinDetails(otherBulletins);
                    component.getLastEvent().setDetails(details);
                    if (!warnings.containsAll(otherBulletins)) {
                        if (component.isRunning() || component.getEndTime() == null) {
                            component.markFailed();
                        }
                        LOG.info("About to fail step for  for other component that was already in flow " + component);
                        jobRepository.failStep(component);
                        failedStep = true;
                        jobExecution.addBulletinErrors(otherBulletins);
                        jobExecution.addFailedComponent(component);
                    }

                } else {
                    //We should never get to this block but if we do
                    //add them to the job execution context
                    LOG.error("Additional Bulletin Exceptions found " + componentId + ", " + otherBulletins);
                    if (event.hasJobExecution()) {
                        Map<String, Object> details = new HashMap<>();
                        details.put("Additional Bulletin Messages",
                                    "Bulletin messages were found for components in the flow that were already completed.");
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


    public NifiJobExecution feedStart(ProvenanceEventRecordDTO event) throws NifiConnectionException {
        //get the Feed Name associated with this events component
//        registerFlowFileWithFeed(event.getFlowFile().getUuid(), event.getComponentId());

        String feedName = getFeedNameForComponentId(event);
        ProcessGroupDTO feedGroup = getFeedProcessGroup(event);
        if (feedGroup == null) {
            LOG.info("unable to start feed for event: {}, feedName: {} and Process Group: {} ", event, feedName, feedGroup);
            //remove the event from the flow file
            event.getFlowFile().removeEvent(event);
            throw new UnsupportedOperationException("Unable to Start feed for incoming Provenance Event of " + event
                                                    + ".  Unable to find the correct Nifi Feed Process Group related to this event.  For More information query Nifi for this event at: http://localhost:8079/nifi-api/controller/provenance/events/"
                                                    + event.getEventId());
        }

        //TODO figure out how to deal with Restarts reusing the same Job Instance
        NifiJobExecution jobExecution = new NifiJobExecution(feedName, event);
        jobExecution.setEndingProcessorCount(getEndingProcessorCount(event));
        jobExecution.setEndingProcessorComponentIds(getEndingProcessorIds(event));
        jobExecution.markStarted();
        //add the new Job to the map for lookup later
        // flowFileJobExecutions.put(event.getFlowFileUuid(), jobExecution);
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
        ProcessorDTO feedProcessor = getFeedProcessor(event);
        executionContext.put("Feed Process Group Id", feedGroup.getId());
        executionContext.put("Feed Name", feedName);
        jobRepository.saveJobExecutionContext(jobExecution, executionContext);

        return jobExecution;

    }

    private void saveStepExecutionContext(ProvenanceEventRecordDTO event) {
        Map<String, Object> attrs = new HashMap<>();
        ProcessorDTO processorDTO = getProcessor(event.getComponentId());
        if (processorDTO != null) {
            attrs.put("Group Id", processorDTO.getParentGroupId());
        }
        attrs.put("Component Id", event.getFlowFileComponent().getComponentId());
        attrs.put("Event Id", event.getEventId().toString());
        attrs.put("Flow File Id", event.getFlowFileUuid());
        attrs.put("Component Type", event.getComponentType());
        jobRepository.saveStepExecutionContext(event.getFlowFileComponent(), attrs);
    }

    public void setComponentName(ProvenanceEventRecordDTO event) {
        if (event.getFlowFileComponent().getComponetName() == null) {
            ProcessorDTO dto = getFeedProcessor(event);
            if (dto != null) {
                event.getFlowFileComponent().setComponetName(dto.getName());
            } else {
                //add a unique name
                event.getFlowFileComponent().setComponetName(event.getComponentType());
            }
        }
    }


    public void componentStarted(ProvenanceEventRecordDTO event) {
        if (hasJobExecution(event)) {
            //set the componentName
            setComponentName(event);
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

    private boolean hasDetailsIndicatingFailure(ProvenanceEventRecordDTO event) {
        return event != null && event.getDetails() != null && event.getDetails().equalsIgnoreCase(AUTO_TERMINATED_FAILURE_RELATIONSHIP);
    }

    public void componentCompleted(ProvenanceEventRecordDTO event) {
        if (hasJobExecution(event)) {

            List<BulletinDTO> bulletins = nifiComponentFlowData.getBulletinsNotYetProcessedForComponent(event);
            if (bulletins == null || bulletins.isEmpty()) {
                bulletins = nifiComponentFlowData.getProcessorBulletinsForComponentInFlowFile(event.getFlowFile().getRoot(), event.getComponentId());
            }

            boolean failComponent = hasDetailsIndicatingFailure(event);

            if (bulletins != null && !bulletins.isEmpty() && event.getFlowFileComponent().getStepExecutionId() != null) {
                LOG.info("componentCompleted.  Checking for Bulletins... found {} for {}", bulletins.size(), event.getComponentName());
                String details = nifiComponentFlowData.getBulletinDetails(bulletins);

                event.setDetails(details);

                //fail if Bulletin is not Warning
                boolean justWarnings = Iterables.all(bulletins, new Predicate<BulletinDTO>() {
                    @Override
                    public boolean apply(BulletinDTO bulletinDTO) {
                        return "WARNING".equalsIgnoreCase(bulletinDTO.getLevel());
                    }
                });

                if (!justWarnings || failComponent) {
                    if (event.getFlowFileComponent().isRunning() || event.getFlowFileComponent().getEndTime() == null) {
                        event.getFlowFileComponent().markFailed();
                    }
                    LOG.info("Fail Step due to bulletin errors that were not all WARNING {}, {}, {}",
                             event.getFlowFileComponent(), bulletins.size(), bulletins);
                    jobRepository.failStep(event.getFlowFileComponent());
                    event.getFlowFileComponent().getJobExecution().addFailedComponent(event.getFlowFileComponent());
                } else {
                    LOG.info("Completing Component {} ({}) with WARNINGS", event.getFlowFileComponent().getComponetName(),
                             event.getId());
                    jobRepository.completeStep(event.getFlowFileComponent());

                }
                event.getFlowFileComponent().getJobExecution().addBulletinErrors(bulletins);
            } else {
                //check to see if the event autoterminated by a failure relationship.  if so fail the flow

                if (failComponent) {
                    if (event.getFlowFileComponent().isRunning() || event.getFlowFileComponent().getEndTime() == null) {
                        event.getFlowFileComponent().markFailed();
                    }
                    LOG.info("FAILING EVENT {} with COMPONENT {} with STEP id of {} ", event, event.getFlowFileComponent(), event.getFlowFileComponent().getStepExecutionId());
                    jobRepository.failStep(event.getFlowFileComponent());
                    event.getFlowFileComponent().getJobExecution().addFailedComponent(event.getFlowFileComponent());
                } else {
                    jobRepository.completeStep(event.getFlowFileComponent());
                }

            }
            event.getFlowFileComponent().getJobExecution().componentComplete(event.getComponentId());
        } else {
            LOG.error(" Not Completing Component." + event.getFlowFileComponent() + "  NifiJobExecution is null");
        }
    }

    public void componentFailed(ProvenanceEventRecordDTO event) {
        if (hasJobExecution(event)) {
            LOG.info("Fail Step due to componentFailed {}", event.getFlowFileComponent());
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
                LOG.info("Failing Job " + jobExecution.getJobExecutionId() + ", " + jobExecution.getFailedComponents().size() + " Failed Components exist");
            } else {

                //if it is a Check Data Job ensure the Validation string is true or otherwise fail the job
                if (event.getFlowFileComponent().getJobExecution().isCheckDataJob()) {
                    String value = event.getAttributeMap().get(CheckDataStepConstants.VALIDATION_KEY);
                    String message = event.getAttributeMap().get(CheckDataStepConstants.VALIDATION_MESSAGE_KEY);
                    Map<String, Object> jobExecutionAttrs = new HashMap<>();
                    jobExecutionAttrs.put(CheckDataStepConstants.VALIDATION_KEY, value);
                    jobExecutionAttrs.put(CheckDataStepConstants.VALIDATION_MESSAGE_KEY, message);
                    jobRepository.saveJobExecutionContext(event.getFlowFileComponent().getJobExecution(), jobExecutionAttrs);
                    if (!"true".equalsIgnoreCase(value)) {
                        jobRepository.failJobExecution(jobExecution);
                        event.getFlowFileComponent().getJobExecution().addFailedComponent(event.getFlowFileComponent());
                        LOG.info("Failing Check DAta  Job " + jobExecution.getJobExecutionId() + ", " + jobExecution.getFailedComponents().size() + ".");
                    } else {
                        jobRepository.completeJobExecution(jobExecution);
                        LOG.info("Completing Job " + jobExecution.getJobExecutionId() + ", " + jobExecution.getFailedComponents().size() + "." + " jobExecutionAttrs: " + jobExecutionAttrs);
                    }
                } else {
                    jobRepository.completeJobExecution(jobExecution);
                    LOG.info("Completing Job " + jobExecution.getJobExecutionId() + ", " + jobExecution.getFailedComponents().size() + ".");
                }

            }
        } else {
            String feedName = getFeedNameForComponentId(event);
            LOG.error(" Not Completing Feed. " + feedName + "  NifiJobExecution is null");
        }

    }

    public void feedEvent(ProvenanceEventRecordDTO event) {
        if (event.hasUpdatedAttributes() && event.getFlowFileComponent() != null) {
            //write to the step execution context
            jobRepository.saveStepExecutionContext(event.getFlowFileComponent(), event.getUpdatedAttributes());
        }


    }

    /**
     * Get a NifiJobExecution for an event and flowfile id
     */
    public NifiJobExecution getNifiJobExection(Long nifiEventId, String flowfileUuid) {
        Long jobExecutionId = jobRepository.findJobExecutionId(nifiEventId, flowfileUuid);
        if (jobExecutionId != null) {
            ExecutedJob job = executedJobsRepository.findByExecutionId(jobExecutionId.toString());
            if (job != null) {
                return provenanceEventExecutedJob.processExecutedJob(job);
            }
        }
        return null;
    }

    public boolean isConnectedToNifi() {
        return this.nifiComponentFlowData.isConnectedToNifi();
    }

    public void setJobRepository(NifiJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public void setNifiComponentFlowData(NifiComponentFlowData nifiComponentFlowData) {
        this.nifiComponentFlowData = nifiComponentFlowData;
    }
}
