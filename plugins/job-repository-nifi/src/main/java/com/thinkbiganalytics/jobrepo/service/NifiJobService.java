package com.thinkbiganalytics.jobrepo.service;


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.jobrepo.nifi.support.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.nifi.support.NifiSpringBatchConstants;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.repository.dao.NifiJobRepository;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;

import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.apache.nifi.web.api.entity.ProvenanceEventEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by sr186054 on 4/14/16.
 */
@Named
public class NifiJobService extends AbstractJobService {

    private static final Logger log = LoggerFactory.getLogger(NifiJobService.class);

    ///// INITIALL DELEGATE TO SPRING BATCH JobOperator and JobExplorer to do the work of start/stop/restart/etc
    /// LATER MOVE SPRING BATCH OUT OF this dependency

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private JobOperator jobOperator;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private JobExplorer jobExplorer;


    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private JobRepository jobRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private NifiJobRepository nifiJobRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private NifiRestClient nifiRestClient;

    private Comparator sortStepExecution() {
        return new Comparator<StepExecution>() {
            @Override
            public int compare(StepExecution o1, StepExecution o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                } else if (o1 == null) {
                    return 1;
                } else if (o2 == null) {
                    return -1;
                } else {
                    return o1.getId().compareTo(o2.getId());
                }
            }
        };
    }

    @Override
    public Long restartJobExecution(Long executionId) throws JobExecutionException {
        log.info("Attempt to Restart Job with Execution id of: {} ", executionId);
        //1 find the NifiProvenance Event associated with this JobExecution
        //1 find all steps that have failed
        JobExecution execution = this.jobExplorer.getJobExecution(executionId);
        Collection<StepExecution> steps = execution.getStepExecutions();
        if (steps != null && !steps.isEmpty()) {
            List<StepExecution> stepsToReplay = Lists.newArrayList(Iterables.filter(execution.getStepExecutions(), new Predicate<StepExecution>() {
                @Override
                public boolean apply(StepExecution stepExecution) {
                    return BatchStatus.FAILED.equals(stepExecution.getStatus());
                }
            }));

            if (stepsToReplay == null || stepsToReplay.isEmpty()) {
                //find the first step that has a replay ability
                stepsToReplay = new ArrayList<>(execution.getStepExecutions());
                log.info("Restart Job Status... No Failed steps exist for this Job {}.  Attempting to find First Step that can be replayed.  Total Steps found: {}", executionId, stepsToReplay.size());
            } else {
                log.info("Restart Job Status... Found {} FAILED Steps for  Job Execution {} ", stepsToReplay.size(), executionId);
            }
            //try to replay the first one
            Collections.sort(stepsToReplay, sortStepExecution());
            boolean attemptToReplay = true;
            while (attemptToReplay == true) {
                //lookup if we can replay this step
                for (StepExecution stepExecution : stepsToReplay) {
                    List<ProvenanceEventDTO> events = getEventsForStepExecution(stepExecution);
                    if (events != null) {
                        log.info("Restart Job Status... Found {} ProvenanceEvents for  Step Execution {} ", events.size(), stepExecution.getId());
                        for (ProvenanceEventDTO event : events) {
                            if (event != null) {
                                if (canReplay(event)) {
                                    //attempt to replay it
                                    try {
                                        log.info("Restart Job Status... Attempt to Replay Nifi Event:  {}, Component: {} ({}), Flow File: {}, for  Step Execution {} ", event.getId(),
                                                 event.getComponentName(), event.getComponentId(), event.getFlowFileUuid(), stepExecution.getId());
                                        ProvenanceEventEntity entity = nifiRestClient.replayProvenanceEvent(event.getEventId());

                                        if (entity != null) {
                                            log.info("Successfully Restarted Job Status... for Nifi Event:  {}, Component: {}, Flow File: {}, for  Step Execution {}.  NEW data is:  {}, {}, {} ",
                                                     event.getId(), event.getComponentName(), event.getFlowFileUuid(), stepExecution.getId(), entity.getProvenanceEvent().getEventId(),
                                                     entity.getProvenanceEvent().getFlowFileUuid(), entity.getProvenanceEvent().getComponentId());
                                        }
                                        attemptToReplay = false;
                                        break;

                                    } catch (Exception e) {
                                        log.error("Error Restarting Provenance Event:  {}, Component: {}, Flow File: {}, for  Step Execution {} ", event.getId(), event.getComponentName(),
                                                 event.getFlowFileUuid(), stepExecution.getId());
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    log.info("Unable to replay event {}, {}, reason: {}", event.getEventId(), event.getComponentName(), event.getReplayExplanation());
                                }
                            }
                        }
                    }
                    attemptToReplay = false;
                }

            }
        }

        return null;

    }

    public List<ProvenanceEventDTO> getEventsForStepExecution(StepExecution stepExecution) {
        List<ProvenanceEventDTO> provenanceEventDTOs = new ArrayList<>();
        Object nifiEventId = stepExecution.getExecutionContext().get(NifiSpringBatchConstants.NIFI_EVENT_ID_STEP_PROPERTY);
        if (nifiEventId != null) {
            try {
                ProvenanceEventEntity provenanceEventEntity = nifiRestClient.getProvenanceEvent(nifiEventId.toString());
                if (provenanceEventEntity != null && provenanceEventEntity.getProvenanceEvent() != null) {
                    provenanceEventDTOs.add(provenanceEventEntity.getProvenanceEvent());
                }

            } catch (Exception e) {
                //TODO Change

            }

        }
        return provenanceEventDTOs;
    }

    public boolean canReplay(ProvenanceEventDTO event) {
        return event.getReplayAvailable() != null ? event.getReplayAvailable().booleanValue() : false;
    }

    @Override
    public boolean stopJobExecution(Long executionId) throws JobExecutionException {
        throw new UnsupportedOperationException("Unable to stop Nifi Job Execution at this time.  Please mark the job as Failed and Abandoned, if necessary.");
    }

    @Override
    public void abandonJobExecution(Long executionId) throws JobExecutionException {
        JobExecution execution = this.jobExplorer.getJobExecution(executionId);
        if (execution != null) {
            if (execution.getStartTime() == null) {
                execution.setStartTime(DateTimeUtil.getUTCTime());
            }
            execution.setStatus(BatchStatus.ABANDONED);
            execution.setEndTime(DateTimeUtil.getUTCTime());
            this.jobRepository.update(execution);

        }
    }

    @Override
    public void failJobExecution(Long executionId) {
        JobExecution execution = this.jobExplorer.getJobExecution(executionId);
        for (StepExecution step : execution.getStepExecutions()) {
            if (step.getStatus().equals(BatchStatus.STARTED)) {
                step.setStatus(BatchStatus.FAILED);
                step.setExitStatus(ExitStatus.FAILED);

                this.jobRepository.update(step);
            }
        }
        if (execution.getStartTime() == null) {
            execution.setStartTime(DateTimeUtil.getUTCTime());
        }
        execution.setStatus(BatchStatus.FAILED);
        execution.setEndTime(DateTimeUtil.getUTCTime());
        this.jobRepository.update(execution);
    }

    @Override
    public ExecutedJob createJob(String jobName, Map<String, Object> jobParameters) throws JobExecutionException {
        return null;
    }

}
