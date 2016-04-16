package com.thinkbiganalytics.jobrepo.service;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.util.List;

/**
 * Created by sr186054 on 8/27/15.
 */
public interface ExecutionContextValuesService {

  public void saveStepExecutionContextValues(StepExecution stepExecution);

  public void saveStepExecutionContextValues(StepExecution stepExecution, List<String> stepExecutionKeys);

  public void saveJobExecutionContextValues(JobExecution jobExecution);

  public void saveJobExecutionContextValues(JobExecution jobExecution, List<String> jobExecutionKeys);
}
