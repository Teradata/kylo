package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileEvents;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.jobrepo.nifi.support.NifiSpringBatchConstants;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedStep;
import com.thinkbiganalytics.nifi.rest.client.NifiConnectionException;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;

import org.apache.nifi.web.api.entity.ProvenanceEventEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.batch.runtime.BatchStatus;

/**
 * Created by sr186054 on 6/20/16.
 */
@Component
public class ProvenanceEventExecutedJob {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventExecutedJob.class);

    @Autowired
    private NifiRestClient nifiRestClient;

    @Autowired
    private ProvenanceEventListener provenanceEventListener;

    public NifiJobExecution processExecutedJob(ExecutedJob job) throws NifiConnectionException {

        String flowFileId = "";
        List<String> eventIds = new ArrayList<>();
        List<ExecutedStep> steps = job.getExecutedSteps();
        List<ProvenanceEventRecordDTO> provenanceEventDTOs = new ArrayList<>();

        log.info("Attempting to attach FlowFile and Component for Job: {} with {} steps ", job.getExecutionId(), (steps != null && !steps.isEmpty()) ? steps.size() : 0);

        Map<Long, ExecutedStep> eventToStepIdMap = new HashMap<>();
        Collections.sort(steps, new Comparator<ExecutedStep>() {
            @Override
            public int compare(ExecutedStep o1, ExecutedStep o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                } else if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                } else {
                    return new Long(o1.getId()).compareTo(new Long(o2.getId()));
                }
            }
        });

        for (ExecutedStep step : steps) {
            Map<String, Object> executionContext = step.getExecutionContext();
            Object nifiEventId = executionContext.get(NifiSpringBatchConstants.NIFI_EVENT_ID_STEP_PROPERTY);
            log.info("Attempt to find {} in Execution context for Step {} returned: {} ", NifiSpringBatchConstants.NIFI_EVENT_ID_STEP_PROPERTY, step.getId(), nifiEventId);
            if (nifiEventId != null) {
                try {
                    ProvenanceEventEntity provenanceEventEntity = nifiRestClient.getProvenanceEvent(nifiEventId.toString());
                    if (provenanceEventEntity != null && provenanceEventEntity.getProvenanceEvent() != null) {
                        ProvenanceEventRecordDTO recordDTO = new ProvenanceEventRecordDTO(provenanceEventEntity.getProvenanceEvent().getEventId(), provenanceEventEntity.getProvenanceEvent());
                        provenanceEventDTOs.add(recordDTO);
                        eventToStepIdMap.put(recordDTO.getEventId(), step);
                    }

                } catch (Exception e) {
                    if (e instanceof NifiConnectionException) {
                        log.error(
                            "Cannot get connection to NIFI to match NIFI Provenance Events to existing Job/Step Executions.  Attempt to notify the JMS Listener to hold all events until Nifi is back up");
                        eventToStepIdMap.clear();
                        provenanceEventDTOs.clear();
                        throw e;
                    }

                }
            }
        }
        String feedName = null;
        if (job.getJobParameters() != null && job.getJobParameters().containsKey(FeedConstants.PARAM__FEED_NAME)) {
            feedName = (String) job.getJobParameters().get(FeedConstants.PARAM__FEED_NAME);
        }
        NifiJobExecution nifiJobExection = new NifiJobExecution(job);

        //now that we have the list of events we processed for this job construct the FlowFileEvents Object
        for (ProvenanceEventRecordDTO event : provenanceEventDTOs) {
            FlowFileEvents events = provenanceEventListener.attachFlowFileAndComponentsToEvent(event);
            if (nifiJobExection.getFlowFile() == null) {
                nifiJobExection.setFlowFile(events.getRoot());
                nifiJobExection.markRunning(job.getStartTime());
            }
            ExecutedStep step = eventToStepIdMap.get(event.getEventId());
            if (step.isRunning()) {
                event.getFlowFileComponent().markRunning(step.getStartTime());
            }
            if (step.getEndTime() != null) {
                if (BatchStatus.FAILED.equals(step.getStatus())) {
                    event.getFlowFileComponent().markFailed(step.getEndTime());
                } else if (BatchStatus.COMPLETED.equals(step.getStatus())) {
                    event.getFlowFileComponent().markCompleted(step.getEndTime());
                } else {
                    event.getFlowFileComponent().markCompleted(step.getEndTime());
                }
            }
            //attach the step and job execution ids correctly to the objects
            event.getFlowFile().setNifiJobExecution(nifiJobExection);
            event.getFlowFile().setJobExecutionId(job.getExecutionId());
            event.getFlowFile().setInsertedIntoDatabase(true);
            event.getFlowFileComponent().setJobExecution(nifiJobExection);
            event.getFlowFileComponent().updateJobExecution();

            event.getFlowFileComponent().setStepExecutionId(step.getId());
            event.getFlowFileComponent().setVersion(step.getVersion());
            event.getFlowFileComponent().setExecutionContextMap(step.getExecutionContext());
            event.getFlowFileComponent().setExecutionContextSet(true);

            log.info("created/updated FlowFile for event {} - {}, {}, as {}", event.getEventId(), event.getComponentId(), event.getComponentName(), events);

        }
        log.info("Finished attaching FlowFile {} for Job {}. ", nifiJobExection.getFlowFile(), job.getExecutionId());
        return nifiJobExection;


    }

}
