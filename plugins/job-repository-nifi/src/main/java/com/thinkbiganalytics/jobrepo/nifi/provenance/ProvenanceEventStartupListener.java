package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.jobrepo.ApplicationStartupListener;
import com.thinkbiganalytics.jobrepo.JobRepoApplicationStartupListener;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileEvents;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.jobrepo.nifi.support.NifiSpringBatchConstants;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedStep;
import com.thinkbiganalytics.jobrepo.repository.JobRepository;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.rest.JerseyClientException;
import org.apache.nifi.web.api.entity.ProvenanceEventEntity;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.batch.runtime.BatchStatus;
import javax.inject.Inject;
import java.util.*;

/**
 * On Startup this class will check to see if there are any Jobs that were in a STARTED/STARTING state prior to the restart.
 * It will then attach Nifi Provenance Events and associate those events with the correct JOB EXECUTION and STEP EXECUTIONS
 * and merge them in with the JMS queue that is listening for other provenance events.
 *
 * Once it completes the startup it will then notify all listeners that it is complete
 */
public class ProvenanceEventStartupListener implements ApplicationStartupListener {



    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventStartupListener.class);

    @Inject
    JobRepoApplicationStartupListener jobRepoApplicationStartupListener;


    @Inject
    private JobRepository jobRepository;

    @Autowired
    private ProvenanceEventListener provenanceEventListener;

    @Autowired
    NifiRestClient nifiRestClient;

     private Boolean isProcessingStartupJobs = null;

    @PostConstruct
    private void init() {
        jobRepoApplicationStartupListener.subscribe(this);
    }

    private List<ProvenanceEventStartupCompleteListener> completeListeners = new ArrayList<>();

    public void subscribe(ProvenanceEventStartupCompleteListener listener) {
        this.completeListeners.add(listener);
    }




    public boolean isFinishedProcessingStartupJobs() {
        return isProcessingStartupJobs != null && isProcessingStartupJobs == false;
    }
    @Override
    public void onStartup(DateTime startTime) {
        isProcessingStartupJobs = true;
        log.info("*******APPLICATION STARTUP @ {} ****** ",startTime);
        log.info("Looking for Jobs that were running prior to start Time of {} ",startTime);

        List<ExecutedJob> jobs = jobRepository.findJobsCurrentStartedBeforeSpecifiedTime(startTime);
        if (jobs != null) {
            log.info("Found {} Jobs that were running prior to start Time of {} ",jobs.size(),startTime);
            processStartupJobExecutions(jobs);
            //what to do with these jobs??
            //try to build up the FlowFileEvents with these objects

            //if we cant get any FlowFileEvents then mark the jobs as abandoned?
            //jobService.abandonJobExecution();
        }
        else {
            log.info("Found 0 Jobs that were running prior to start Time of {} ",startTime);
        }
        for(ProvenanceEventStartupCompleteListener completeListener: completeListeners){
            completeListener.onEventsInitialized();
        }
        isProcessingStartupJobs = false;
    }
    private void processStartupJobExecutions(List<ExecutedJob> jobs){
        for(ExecutedJob job : jobs){
            processStartupJobExecution(job);
        }
    }


    private void handleNifiRestFailure(ExecutedStep step, Object nifiEventId){

    }

    private void processStartupJobExecution(ExecutedJob job) {

        String flowFileId = "";
        List<String> eventIds = new ArrayList<>();
        List<ExecutedStep> steps = job.getExecutedSteps();
        List<ProvenanceEventRecordDTO> provenanceEventDTOs = new ArrayList<>();

        log.info("Attempting to attach FlowFile and Component for Job: {} with {} steps ",job.getExecutionId(), (steps != null && !steps.isEmpty()) ? steps.size() : 0 );

        Map<Long,ExecutedStep> eventToStepIdMap = new HashMap<>();
        Collections.sort(steps, new Comparator<ExecutedStep>() {
            @Override
            public int compare(ExecutedStep o1, ExecutedStep o2) {
               if(o1 == null && o2 == null){
                   return 0;
               }
                else if(o1 == null) {
                   return -1;
               }
                else if(o2 == null){
                   return 1;
               }
                else {
                   return new Long(o1.getId()).compareTo(new Long(o2.getId()));
               }
            }
        });

        for(ExecutedStep step :steps){
            Map<String,Object> executionContext = step.getExecutionContext();
            Object nifiEventId = executionContext.get(NifiSpringBatchConstants.NIFI_EVENT_ID_STEP_PROPERTY);
            log.info("Attempt to find {} in Execution context for Step {}, {} returned: ",NifiSpringBatchConstants.NIFI_EVENT_ID_STEP_PROPERTY,step.getId(),executionContext,nifiEventId);
            if(nifiEventId != null) {
                try {
                    ProvenanceEventEntity provenanceEventEntity =  nifiRestClient.getProvenanceEvent(nifiEventId.toString());
                    if(provenanceEventEntity != null && provenanceEventEntity.getProvenanceEvent() != null){
                        ProvenanceEventRecordDTO recordDTO = new ProvenanceEventRecordDTO(provenanceEventEntity.getProvenanceEvent().getEventId(),provenanceEventEntity.getProvenanceEvent());
                        provenanceEventDTOs.add(recordDTO);
                        eventToStepIdMap.put(recordDTO.getEventId(),step);
                    }

                } catch (JerseyClientException e) {
                    e.printStackTrace();
                    handleNifiRestFailure(step,nifiEventId);
                }
            }
        }
        String feedName = null;
        if(job.getJobParameters() != null && job.getJobParameters().containsKey(FeedConstants.PARAM__FEED_NAME)) {
            feedName = (String) job.getJobParameters().get(FeedConstants.PARAM__FEED_NAME);
        }
        NifiJobExecution nifiJobExection = new NifiJobExecution(job);

        //now that we have the list of events we processed for this job construct the FlowFileEvents Object
        for(ProvenanceEventRecordDTO event :  provenanceEventDTOs){
            FlowFileEvents events = provenanceEventListener.attachFlowFileAndComponentsToEvent(event);
            if(nifiJobExection.getFlowFile() == null) {
                nifiJobExection.setFlowFile(events.getRoot());
                nifiJobExection.markRunning(job.getStartTime());
            }
            ExecutedStep step = eventToStepIdMap.get(event.getEventId());
            if(step.isRunning()){
                event.getFlowFileComponent().markRunning(step.getStartTime());
            }
            if(step.getEndTime() != null) {
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
        log.info("Finished attaching FlowFile {} for Job {}. ",nifiJobExection.getFlowFile(), job.getExecutionId());


    }


}
