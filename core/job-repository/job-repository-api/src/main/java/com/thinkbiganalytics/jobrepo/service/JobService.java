package com.thinkbiganalytics.jobrepo.service;


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
  boolean stopJobExecution(final Long executionId) throws
                                                          JobExecutionException;

  /**
   * Take the given job execution and abandon the job.
   *
   * @param executionId The job execution to start again
   */
  void abandonJobExecution(final Long executionId) throws JobExecutionException;

  /**
   * Take the given job execution and fail the job.
   *
   * @param executionId The job execution to start again
   */
  void failJobExecution(final Long executionId);


}
