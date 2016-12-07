package com.thinkbiganalytics.jobrepo.service;


import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobParameterType;

import java.util.List;
import java.util.Map;

/**
 * High level services associated with jobs
 */
public interface JobService {

  /**
   * @param executionId The job execution to start again
   * @return The new Job Execution Id
   */
  Long restartJobExecution(final Long executionId) throws JobExecutionException;

  /**
   * Take the given job execution and stop the job.
   *
   * @param executionId The job execution to start again
   * @return true/false
   */
  public boolean stopJobExecution(final Long executionId) throws
                                                          JobExecutionException;

  /**
   * Take the given job execution and abandon the job.
   *
   * @param executionId The job execution to start again
   */
  public void abandonJobExecution(final Long executionId) throws JobExecutionException;

  /**
   * Take the given job execution and fail the job.
   *
   * @param executionId The job execution to start again
   */
  public void failJobExecution(final Long executionId);


}
