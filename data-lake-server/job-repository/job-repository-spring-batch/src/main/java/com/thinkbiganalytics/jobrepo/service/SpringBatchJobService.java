package com.thinkbiganalytics.jobrepo.service;

import com.thinkbiganalytics.jobrepo.batch.config.CreateJobLauncherConfig;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.repository.JobRepositoryImpl;
import com.thinkbiganalytics.jobrepo.repository.dao.PipelineDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring batch based implementation of the job service
 */
@Named  // REFACTOR
public class SpringBatchJobService extends AbstractJobService {
    private static final Logger LOG = LoggerFactory.getLogger(SpringBatchJobService.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private JobOperator jobOperator;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private JobRepository jobRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private JobExplorer jobExplorer;

    @Autowired
    private PipelineDao pipelineDao;


    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private JobLocator jobLocator;

    @Autowired
    private JobBuilderFactory jobs;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    @Qualifier(CreateJobLauncherConfig.PIPELINE_CONTROLLER_JOB_LAUNCHER)
    private JobLauncher pipelineControllerJobLauncher;

    /**
     * Take the given job execution and restart the job.
     *
     * @param executionId The job execution to start again
     */
    @Override
    public Long restartJobExecution(final Long executionId)
            throws JobExecutionException {
        try {
            return this.jobOperator.restart(executionId);
        } catch (JobInstanceAlreadyCompleteException | NoSuchJobExecutionException | NoSuchJobException | JobRestartException | JobParametersInvalidException e) {
            throw new JobExecutionException(e);
        }

    }

    /**
     * Take the given job execution and stop the job.
     *
     * @param executionId The job execution to start again
     */
    @Override
    public boolean stopJobExecution(final Long executionId) throws JobExecutionException {
        JobExecution execution = this.jobExplorer.getJobExecution(executionId);
        if (execution.getStartTime() == null) {
            execution.setStartTime(new Date());
        }
        execution.setEndTime(new Date());
        try {
            return this.jobOperator.stop(executionId);
        } catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
            throw new JobExecutionException(e);
        }
    }

    /**
     * Take the given job execution and abandon the job.
     *
     * @param executionId The job execution to start again
     */
    @Override
    public void abandonJobExecution(final Long executionId) throws JobExecutionException {
        JobExecution execution = this.jobExplorer.getJobExecution(executionId);
        if (execution.getStartTime() == null) {
            execution.setStartTime(new Date());
        }
        execution.setEndTime(new Date());
        try {
            this.jobOperator.abandon(executionId);
        } catch (NoSuchJobExecutionException | JobExecutionAlreadyRunningException e) {
            throw new JobExecutionException(e);
        }
    }

    /**
     * Take the given job execution and abandon the job.  To do this in spring batch, there is no native interface.  So
     * We find the job and manually mark the executing step as failed, so it get re-run, and then mark the overall
     * execution as failed.  This will allow it to be restarted.
     *
     * @param executionId The job execution to start again
     */
    @Override
    public void failJobExecution(final Long executionId) {
        JobExecution execution = this.jobExplorer.getJobExecution(executionId);
        for (StepExecution step : execution.getStepExecutions()) {
            if (step.getStatus().equals(BatchStatus.STARTED)) {
                step.setStatus(BatchStatus.FAILED);
                step.setExitStatus(ExitStatus.FAILED);
                this.jobRepository.update(step);
            }
        }
        if (execution.getStartTime() == null) {
            execution.setStartTime(new Date());
        }
        execution.setStatus(BatchStatus.FAILED);
        execution.setEndTime(new Date());
        this.jobRepository.update(execution);
    }


    /**
     * Get a map of the average run times by job name of only completed tasks
     *
     * @return The map of run times
     */

    public Map<String, BigDecimal> getAverageRunTimes() {
        Map<String, BigDecimal> averageRunTimes = new HashMap<String, BigDecimal>();

        String sql = "SELECT bji.JOB_NAME AVG(bje.END_TIME - bje.START_TIME) " +
                "FROM BATCH_JOB_EXECUTION bje INNER JOIN BATCH_JOB_INSTANCE bji on bje.JOB_INSTANCE_ID = bji.JOB_INSTANCE_ID " +
                "WHERE bje.STATUS = 'COMPLETED' " +
                "GROUP BY bji.JOB_NAME";

        List<Object[]> queryResult = pipelineDao.queryForObjectArray(sql);
        for (Object[] result : queryResult) {
            // Result is not reliably returned as BigDecimal
            if (result[1] instanceof BigDecimal) {
                averageRunTimes.put((String) result[0], (BigDecimal) result[1]);
            } else if (result[1] instanceof Double) {
                averageRunTimes.put((String) result[0], BigDecimal.valueOf((Double) result[1]));
            }
        }

        return averageRunTimes;
    }


    /**
     * Returns generic statistics about current and historically run jobs;
     *
     * @return A map of name value pairs containing the individual statistical values
     */
    public Map<String, Object> getJobStatistics() {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("averageRunTimes", getAverageRunTimes());

        return result;
    }

    /**
     * Create a job instance using the given name and parameters
     *
     * @param jobName       The name of the job
     * @param jobParameters the parameters for the job
     */
    @Override
    public ExecutedJob createJob(String jobName, Map<String, Object> jobParameters) throws JobExecutionException {
        Job job = null;
        try {
            job = this.jobLocator.getJob(jobName);
            JobExecution jobExecution = null;
            //convert Map to JobParameters

            Map<String, JobParameter> params = new HashMap<>();
            for (Map.Entry<String, Object> entry : jobParameters.entrySet()) {
                JobParameter jobParameter = null;
                Object val = entry.getValue();
                if (val != null) {
                    if (val instanceof Long) {
                        jobParameter = new JobParameter((Long) val);
                    } else if (val instanceof Double) {
                        jobParameter = new JobParameter((Double) val);
                    } else if (val instanceof Date) {
                        jobParameter = new JobParameter((Date) val);
                    } else {
                        jobParameter = new JobParameter(val.toString());
                    }
                    params.put(entry.getKey(), jobParameter);
                }
            }
            JobParameters springBatchJobParameters = new JobParameters(params);


            jobExecution = this.pipelineControllerJobLauncher.run(job, springBatchJobParameters);
            return JobRepositoryImpl.convertToExecutedJob(jobExecution.getJobInstance(), jobExecution);
        } catch (NoSuchJobException | JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            throw new JobExecutionException(e);
        }
    }

    protected void setJobOperator(JobOperator jobOperator) {
        this.jobOperator = jobOperator;
    }

    protected void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    protected void setJobExplorer(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    protected void setPipelineDao(PipelineDao pipelineDao) {
        this.pipelineDao = pipelineDao;
    }


    protected void setJobLocator(JobLocator jobLocator) {
        this.jobLocator = jobLocator;
    }

    protected void setJobs(JobBuilderFactory jobs) {
        this.jobs = jobs;
    }

    protected void setPipelineControllerJobLauncher(JobLauncher pipelineControllerJobLauncher) {
        this.pipelineControllerJobLauncher = pipelineControllerJobLauncher;
    }
}

