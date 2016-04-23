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

  /**
   * Return a map of Batch Stati and their associated counts
   *
   * @return A map of each batch status, and the associated count of number of jobs in that status
   */
  public Map<ExecutionStatus, Long> getJobStatusCount();

  public Map<ExecutionStatus, Long> getLatestJobStatusCount();

  /**
   * Get a map of the average run times by job name of only completed tasks
   * @return The map of run times
   */
  //public Map<String, BigDecimal> getAverageRunTimes();

  /**
   * Returns generic statistics about current and historically run jobs;
   *
   * @return A map of name value pairs containing the individual statistical values
   */
  //public Map<String, Object> getJobStatistics();

  /**
   * Create a job instance using the given name and parameters
   *
   * @param jobName       The name of the job
   * @param jobParameters the parameters for the job
   * @throws JobExecutionException if a job could not be found with the provided name
   */
  public ExecutedJob createJob(final String jobName, final Map<String, Object> jobParameters) throws JobExecutionException;

  public List<JobParameterType> getJobParametersForJob(String jobName);

  public List<JobParameterType> getJobParametersForJob(Long executionId);
}
