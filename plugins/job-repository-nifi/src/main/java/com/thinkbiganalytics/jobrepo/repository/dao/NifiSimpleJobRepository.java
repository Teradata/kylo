package com.thinkbiganalytics.jobrepo.repository.dao;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.jobrepo.nifi.support.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.nifi.support.NifiSpringBatchTransformer;
import com.thinkbiganalytics.jobrepo.service.ExecutionContextValuesService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 3/2/16.
 */
public class NifiSimpleJobRepository implements NifiJobRepository {

    private static final Logger LOG = LoggerFactory.getLogger(NifiSimpleJobRepository.class);


    private Map<Long, JobExecution> jobExecutionMap = new HashMap<>();

    private JobInstanceDao jobInstanceDao;
    private JobExecutionDao jobExecutionDao;
    private StepExecutionDao stepExecutionDao;
    private ExecutionContextDao ecDao;
    private JobParametersDao jobParametersDao;
    private ExecutionContextValuesService executionContextValuesService;
    private NifiPipelineControllerDao nifiPipelineControllerDao;


    public NifiSimpleJobRepository(JobInstanceDao jobInstanceDao, JobExecutionDao jobExecutionDao, StepExecutionDao stepExecutionDao, ExecutionContextDao ecDao, JobParametersDao jobParametersDao,
                                   NifiPipelineControllerDao nifiPipelineControllerDao, ExecutionContextValuesService executionContextValuesService) {
        this.jobInstanceDao = jobInstanceDao;
        this.jobExecutionDao = jobExecutionDao;
        this.stepExecutionDao = stepExecutionDao;
        this.ecDao = ecDao;
        this.jobParametersDao = jobParametersDao;
        this.nifiPipelineControllerDao = nifiPipelineControllerDao;
        this.executionContextValuesService = executionContextValuesService;
    }


    public ExecutionContextValuesService getExecutionContextValuesService() {
        return executionContextValuesService;
    }

    public void setExecutionContextValuesService(ExecutionContextValuesService executionContextValuesService) {
        this.executionContextValuesService = executionContextValuesService;
    }

    public Long createJobInstance(NifiJobExecution nifiJobExecution) {
        JobInstance jobInstance = getOrCreateJobInstance(nifiJobExecution);
        nifiJobExecution.setJobInstanceId(jobInstance.getInstanceId());
        return jobInstance.getInstanceId();
    }

    private JobInstance getOrCreateJobInstance(NifiJobExecution nifiJobExecution) {
        if (nifiJobExecution.getJobInstanceId() != null) {
            return jobInstanceDao.getJobInstance(nifiJobExecution.getJobInstanceId());
        } else {
            Map<String, JobParameter> params = new HashMap<>();
            JobParameter jobParameter = new JobParameter(nifiJobExecution.getFlowFile().getUuid());
            params.put("flowFileUUID", jobParameter);
            params.put("createDate", new JobParameter(new Date().getTime()));

            JobParameters jobParameters = new JobParameters(params);
            //store job parameters on nifiJobExecution????
            JobInstance jobInstance = jobInstanceDao.createJobInstance(nifiJobExecution.getFeedName(), jobParameters);
            nifiJobExecution.setJobInstanceId(jobInstance.getInstanceId());
            return jobInstance;
        }
    }

    public Long findJobExecutionId(Long eventId, String flowfileUuid) {
        return nifiPipelineControllerDao.findJobExecutionId(eventId, flowfileUuid);
    }


    public Long saveJobExecution(NifiJobExecution nifiJobExecution) {
        JobExecution jobExecution = getOrCreateJobExecution(nifiJobExecution);
        //save the mappings so we can correlate the Nifi Flow to the Spring Batch flow later if needed
        nifiPipelineControllerDao.saveJobExecution(nifiJobExecution);
        return jobExecution.getId();
    }

    private JobExecution getOrCreateJobExecution(NifiJobExecution nifiJobExecution) {
        JobExecution jobExecution = null;
        if (nifiJobExecution.getJobExecutionId() != null) {
            if (jobExecutionMap.containsKey(nifiJobExecution.getJobExecutionId())) {
                jobExecution = jobExecutionMap.get(nifiJobExecution.getJobExecutionId());
            } else {
                JobInstance jobInstance = getOrCreateJobInstance(nifiJobExecution);
                jobExecution = jobExecutionDao.getJobExecution(nifiJobExecution.getJobExecutionId());
                jobExecution.setJobInstance(jobInstance);
                jobExecutionMap.put(jobExecution.getId(), jobExecution);
            }
            if (nifiJobExecution.getFlowFile().getJobExecutionId() == null) {
                nifiJobExecution.getFlowFile().setJobExecutionId(jobExecution.getId());
            }
        } else {
            jobExecution = NifiSpringBatchTransformer.getJobExecution(nifiJobExecution);
            jobExecution.setJobInstance(getOrCreateJobInstance(nifiJobExecution));
            jobExecutionDao.saveJobExecution(jobExecution);
            jobExecutionMap.put(jobExecution.getId(), jobExecution);
            nifiJobExecution.setJobExecutionId(jobExecution.getId());
            nifiJobExecution.getFlowFile().setJobExecutionId(jobExecution.getId());
        }
        nifiJobExecution.setVersion(jobExecution.getVersion());
        return jobExecution;

    }


    public void completeJobExecution(NifiJobExecution nifiJobExecution) {
        JobExecution jobExecution = getOrCreateJobExecution(nifiJobExecution);
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        jobExecution.setLastUpdated(DateTimeUtil.getUTCTime());
        jobExecution.setEndTime(nifiJobExecution.getEndTime() != null ? nifiJobExecution.getUTCEndTime() : DateTimeUtil.getUTCTime());
        executionContextValuesService.saveJobExecutionContextValues(jobExecution);
        try {
            jobExecutionDao.updateJobExecution(jobExecution);
        } catch (OptimisticLockingFailureException e) {
            //increment the version and try to save again
            jobExecution.incrementVersion();
            jobExecutionDao.updateJobExecution(jobExecution);
        }
        nifiJobExecution.setVersion(jobExecution.getVersion());

        //clear from maps
        jobExecutionMap.remove(jobExecution.getId());
    }


    public void failJobExecution(NifiJobExecution nifiJobExecution) {
        JobExecution jobExecution = getOrCreateJobExecution(nifiJobExecution);
        jobExecution.setStatus(BatchStatus.FAILED);
        jobExecution.setExitStatus(ExitStatus.FAILED);
        jobExecution.setLastUpdated(DateTimeUtil.getUTCTime());
        jobExecution.setEndTime(nifiJobExecution.getEndTime() != null ? nifiJobExecution.getUTCEndTime() : DateTimeUtil.getUTCTime());
        StringBuffer sb = new StringBuffer();
        Set<FlowFileComponent> failedComponents = nifiJobExecution.getFailedComponents();
        List<FlowFileComponent> allComponents = nifiJobExecution.getComponentOrder();
        //order the components to get the step number

        if (failedComponents != null && !failedComponents.isEmpty()) {

            sb.append(failedComponents.size() + " steps failed in the flow. The failed Components are:");

            for (FlowFileComponent failedComponent : failedComponents) {
                sb.append("\n");
                int index = allComponents.indexOf(failedComponent);
                String msg = "";
                if (index >= 0) {
                    msg += " Failed Step #" + (index + 1) + ": ";
                } else {
                    msg += " Failed Step: ";
                }
                sb.append(msg + failedComponent.getComponetName());
            }
        }
        ExitStatus exitStatus = jobExecution.getExitStatus().addExitDescription(sb.toString());
        jobExecution.setExitStatus(exitStatus);
        executionContextValuesService.saveJobExecutionContextValues(jobExecution);
        try {
            jobExecutionDao.updateJobExecution(jobExecution);
        } catch (OptimisticLockingFailureException e) {
            //increment the version and try to save again
            jobExecution.incrementVersion();
            jobExecutionDao.updateJobExecution(jobExecution);
        }
        nifiJobExecution.setVersion(jobExecution.getVersion());
        //clear from maps
        jobExecutionMap.remove(jobExecution.getId());
    }

    public Long saveStepExecution(FlowFileComponent flowFileComponent) {
        JobExecution jobExecution = getOrCreateJobExecution(flowFileComponent.getJobExecution());
        Assert.notNull(jobExecution, "A JobExecution must be set on this component. " + flowFileComponent.getComponetName());

        StepExecution stepExecution = jobExecution.createStepExecution(flowFileComponent.getComponetName());
        NifiSpringBatchTransformer.populateStepExecution(stepExecution, flowFileComponent);
        Long stepExecId = stepExecution.getId();
        stepExecutionDao.saveStepExecution(stepExecution);
        flowFileComponent.setStepExecutionId(stepExecution.getId());
        //save the mappings so we can correlate the Nifi Flow to the Spring Batch flow later if needed
        try {
            nifiPipelineControllerDao.saveStepExecution(flowFileComponent);
        } catch (Exception e) {
            LOG.error("Unable to save Mapping between NIFI Event to Spring Batch Step to the BATCH_NIFI_STEP table because of the following error, {}, {} ", e.getMessage(), e.getStackTrace());
        }
        flowFileComponent.setVersion(stepExecution.getVersion());
        return stepExecution.getId();
    }


    private StepExecution getStepExecution(FlowFileComponent flowFileComponent) {
        JobExecution jobExecution = getOrCreateJobExecution(flowFileComponent.getJobExecution());
        Assert.notNull(jobExecution, "A JobExecution must be set on this component. " + flowFileComponent.getComponetName());

        final Long stepExecutionId = flowFileComponent.getStepExecutionId();
        return Iterables.tryFind(jobExecution.getStepExecutions(), new Predicate<StepExecution>() {
            @Override
            public boolean apply(StepExecution stepExecution) {
                return stepExecution.getId().equals(stepExecutionId);
            }
        }).orNull();

    }


    public void completeStep(FlowFileComponent flowFileComponent) {
        StepExecution stepExecution = getStepExecution(flowFileComponent);
        NifiSpringBatchTransformer.populateStepExecution(stepExecution, flowFileComponent);
        stepExecution.setExitStatus(ExitStatus.COMPLETED);
        stepExecution.setStatus(BatchStatus.COMPLETED);
        ProvenanceEventRecordDTO lastEvent = flowFileComponent.getLastEvent();
        if (lastEvent != null && lastEvent.getDetails() != null) {
            ExitStatus exitStatus = ExitStatus.COMPLETED.addExitDescription(lastEvent.getDetails());
            stepExecution.setExitStatus(exitStatus);
        }

        stepExecutionDao.updateStepExecution(stepExecution);
        flowFileComponent.setVersion(stepExecution.getVersion());
        flowFileComponent.setStepFinished(true);

    }

    public void failStep(FlowFileComponent flowFileComponent) {
        LOG.info("FAILING STEP " + flowFileComponent);
        StepExecution stepExecution = getStepExecution(flowFileComponent);
        NifiSpringBatchTransformer.populateStepExecution(stepExecution, flowFileComponent);
        stepExecution.setExitStatus(ExitStatus.FAILED);
        stepExecution.setStatus(BatchStatus.FAILED);
        ProvenanceEventRecordDTO lastEvent = flowFileComponent.getLastEvent();
        if (lastEvent != null && lastEvent.getDetails() != null) {
            ExitStatus exitStatus = ExitStatus.FAILED.addExitDescription(lastEvent.getDetails());
            stepExecution.setExitStatus(exitStatus);
            LOG.info("FAILING STEP " + flowFileComponent + " with details " + lastEvent.getDetails());
        }

        stepExecutionDao.updateStepExecution(stepExecution);
        flowFileComponent.setVersion(stepExecution.getVersion());
        flowFileComponent.setStepFinished(true);
    }


    public void saveStepExecutionContext(FlowFileComponent flowFileComponent, Map<String, Object> attrs) {
        Map<String, Object> allAttrs = new HashMap<>();
        allAttrs.putAll(flowFileComponent.getExecutionContextMap());
        allAttrs.putAll(attrs);
        StepExecution stepExecution = getStepExecution(flowFileComponent);
        ExecutionContext executionContext = new ExecutionContext(allAttrs);
        if (stepExecution != null) {
            stepExecution.setExecutionContext(executionContext);
            if (flowFileComponent.isExecutionContextSet()) {
                ecDao.updateExecutionContext(stepExecution);
            } else {
                try {
                    ecDao.saveExecutionContext(stepExecution);
                } catch (DuplicateKeyException e) {
                    ecDao.updateExecutionContext(stepExecution);
                }
                flowFileComponent.setExecutionContextSet(true);
            }
            flowFileComponent.setExecutionContextMap(allAttrs);

        } else {
            Long jobExecutionId = flowFileComponent.getJobExecution() != null ? flowFileComponent.getJobExecution().getJobExecutionId() : null;
            LOG.error("Unable to assign step execution context data to step.  StepExecution for JobExecutionId of {} is null for Component {}.  Execution Map is {} ", jobExecutionId,
                      flowFileComponent, allAttrs);
        }
    }

    public void saveJobExecutionContext(NifiJobExecution nifiJobExecution, Map<String, String> attrs) {

        Map<String, Object> allAttrs = new HashMap<>();
        JobExecution jobExecution = getOrCreateJobExecution(nifiJobExecution);
        allAttrs.putAll(nifiJobExecution.getJobExecutionContextMap());
        allAttrs.putAll(attrs);
        ExecutionContext executionContext = new ExecutionContext(allAttrs);
        jobExecution.setExecutionContext(executionContext);
        if (nifiJobExecution.isJobExecutionContextSet()) {
            ecDao.updateExecutionContext(jobExecution);
        } else {
            try {
                ecDao.saveExecutionContext(jobExecution);
            } catch (DuplicateKeyException e) {
                ecDao.updateExecutionContext(jobExecution);
            }
            nifiJobExecution.setJobExecutionContextSet(true);
        }
        nifiJobExecution.setJobExecutionContextMap(allAttrs);
    }


    public void setAsCheckDataJob(Long jobExecutionId, String feedName) {
        jobParametersDao.updateJobParameter(jobExecutionId, FeedConstants.PARAM__FEED_NAME, feedName, JobParameter.ParameterType.STRING);
        jobParametersDao.updateJobParameter(jobExecutionId, FeedConstants.PARAM__JOB_TYPE, FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK, JobParameter.ParameterType.STRING);
    }


    public Long getLastEventIdProcessedByPipelineController() {
        return nifiPipelineControllerDao.getMaxEventId();
    }


}
