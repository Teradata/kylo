package com.thinkbiganalytics.jobrepo.repository;


import com.thinkbiganalytics.jobrepo.query.model.*;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitution;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;

import java.util.List;
import java.util.Map;

/**
 * A simple repository like interface into spring batch jobs
 */
public interface JobRepository {
    /**
     * Find the executed job with the provided instance id
     *
     * @param instanceId The instance id
     * @return The job executions matching the instance id, or empty list if none found
     */
    public List<ExecutedJob> findByInstanceId(final String instanceId);

    public ExecutedJob findByExecutionId(final String executionId);


    /**
     * Return a list of all jobs that have been executed
     *
     * @param limit The maximum number of results to return
     * @return A list of executed jobs
     */
    public List<ExecutedJob> findJobs(final int start, final int limit);

    public List<ExecutedJob> findJobs(List<ColumnFilter> conditions, List<OrderBy> order, Integer start, Integer limit);

    public List<ExecutedJob> findLatestJobs(List<ColumnFilter> conditions, List<OrderBy> order, Integer start, Integer limit);

    public SearchResult getDataTablesSearchResult(List<ColumnFilter> conditions, List<ColumnFilter> defaultFilters, List<OrderBy> order, Integer start, Integer limit);

    public List<Object> selectDistinctColumnValues(List<ColumnFilter> filters, String columnName);

    public Long selectCount(List<ColumnFilter> filters);

    public List<ExecutedJob> findParentAndChildJobs(String parentJobExecutionId);


    /**
     * Return a list of all unique job names that have been executed at least once
     *
     * @return A list of job names
     */
    public List<String> uniqueJobNames();

    /**
     * Return a list of all unique job parameter names we have seen in past executions
     *
     * @return A list of job parameter names, or an empty list if none are found
     */
    public List<String> uniqueJobParameterNames();

    public Map<ExecutionStatus, Long> getCountOfJobsByStatus();

    public Map<ExecutionStatus, Long> getCountOfLatestJobsByStatus();

    /**
     * Get a list of executed steps and their progression towards completion.  Steps that are not running will not
     * have a percent complete assigned to them
     *
     * @param executionId The job execution id to get the progress for
     * @return A list of all executed steps and their progress for the given job
     */
    public List<ExecutedStep> getJobProgress(final String executionId);

    public List<RelatedJobExecution> findRelatedJobExecutions(Long executionId);

    public List<RelatedJobExecution> findPreviousJobExecutions(Long executionId);

    public Long findJobInstanceIdForJobExecutionId(Long jobExecutionId);

    public List<JobStatusCount> getRunningOrFailedJobCounts();

    public List<JobStatusCount> getJobStatusCountByDay(DatabaseQuerySubstitution.DATE_PART datePart, Integer interval);


}
