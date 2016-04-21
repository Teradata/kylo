package com.thinkbiganalytics.jobrepo.service;


import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobRepository;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by sr186054 on 4/14/16.
 */
@Named
public class NifiJobService extends AbstractJobService {

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

  @Override
  public Long restartJobExecution(Long executionId) throws JobExecutionException {
    return null;
  }

  @Override
  public boolean stopJobExecution(Long executionId) throws JobExecutionException {
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

  @Override
  public void abandonJobExecution(Long executionId) throws JobExecutionException {
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
      execution.setStartTime(new Date());
    }
    execution.setStatus(BatchStatus.FAILED);
    execution.setEndTime(new Date());
    this.jobRepository.update(execution);
  }

  @Override
  public ExecutedJob createJob(String jobName, Map<String, Object> jobParameters) throws JobExecutionException {
    return null;
  }

}
