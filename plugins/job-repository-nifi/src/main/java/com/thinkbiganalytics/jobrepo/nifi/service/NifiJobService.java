package com.thinkbiganalytics.jobrepo.nifi.service;


import com.thinkbiganalytics.jobrepo.config.OperationalMetadataAccess;
import com.thinkbiganalytics.jobrepo.jpa.ExecutionConstants;
import com.thinkbiganalytics.jobrepo.jpa.NifiJobExecutionProvider;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiJobExecution;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiStepExecution;
import com.thinkbiganalytics.jobrepo.nifi.support.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.service.AbstractJobService;
import com.thinkbiganalytics.jobrepo.service.JobExecutionException;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;

import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by sr186054 on 4/14/16.
 */
@Named
public class NifiJobService extends AbstractJobService {

    private static final Logger log = LoggerFactory.getLogger(NifiJobService.class);

    @Inject
    private OperationalMetadataAccess operationalMetadataAccess;

    @Autowired
    private NifiJobExecutionProvider nifiJobExecutionProvider;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private NifiRestClient nifiRestClient;

    @Override
    public Long restartJobExecution(Long executionId) throws JobExecutionException {
        log.info("Attempt to Restart Job with Execution id of: {} ", executionId);
        //1 find the NifiProvenance Event associated with this JobExecution
        //1 find all steps that have failed

        return null;

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
        operationalMetadataAccess.commit(() -> {
            NifiJobExecution execution = this.nifiJobExecutionProvider.findByJobExecutionId(executionId);
            if (execution != null) {
                if (execution.getStartTime() == null) {
                    execution.setStartTime(DateTimeUtil.getNowUTCTime());
                }
                execution.setStatus(NifiJobExecution.JobStatus.ABANDONED);
                if (execution.getEndTime() == null) {
                    execution.setEndTime(DateTimeUtil.getNowUTCTime());
                }
                //also stop any running steps??
                this.nifiJobExecutionProvider.save(execution);

            }
            return execution;
        });
    }

    @Override
    public void failJobExecution(Long executionId) {
        operationalMetadataAccess.commit(() -> {

            NifiJobExecution execution = this.nifiJobExecutionProvider.findByJobExecutionId(executionId);
            if (execution != null && !execution.isFailed()) {
                for (NifiStepExecution step : execution.getStepExecutions()) {
                    if (!step.isFinished()) {
                        step.setStatus(NifiStepExecution.StepStatus.FAILED);
                        step.setExitCode(ExecutionConstants.ExitCode.FAILED);
                    }
                }
                if (execution.getStartTime() == null) {
                    execution.setStartTime(DateTimeUtil.getNowUTCTime());
                }
                execution.setStatus(NifiJobExecution.JobStatus.FAILED);
                if (execution.getEndTime() == null) {
                    execution.setEndTime(DateTimeUtil.getNowUTCTime());
                }
                this.nifiJobExecutionProvider.save(execution);
            }
            return execution;
        });
    }

    @Override
    public ExecutedJob createJob(String jobName, Map<String, Object> jobParameters) throws JobExecutionException {
        return null;
    }

}