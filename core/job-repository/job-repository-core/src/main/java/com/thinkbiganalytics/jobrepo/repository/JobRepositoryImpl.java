package com.thinkbiganalytics.jobrepo.repository;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.query.job.AugmentJobExecutionTimingDataQuery;
import com.thinkbiganalytics.jobrepo.query.job.JobProgressQuery;
import com.thinkbiganalytics.jobrepo.query.job.JobQueryConstants;
import com.thinkbiganalytics.jobrepo.query.job.JobStatusCountByDayQuery;
import com.thinkbiganalytics.jobrepo.query.job.JobStepQuery;
import com.thinkbiganalytics.jobrepo.query.job.RelatedJobExecutionsQuery;
import com.thinkbiganalytics.jobrepo.query.job.RunningOrFailedJobCountsQuery;
import com.thinkbiganalytics.jobrepo.query.job.StepExecutionTimingQuery;
import com.thinkbiganalytics.jobrepo.query.model.DailyJobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.DailyJobStatusCountResult;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.JobStepQueryRow;
import com.thinkbiganalytics.jobrepo.query.model.RelatedJobExecution;
import com.thinkbiganalytics.jobrepo.query.model.SearchResult;
import com.thinkbiganalytics.jobrepo.query.model.SearchResultImpl;
import com.thinkbiganalytics.jobrepo.query.model.StepExecutionTiming;
import com.thinkbiganalytics.jobrepo.query.model.TbaJobExecution;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitution;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;
import com.thinkbiganalytics.jobrepo.query.support.QueryColumnFilterSqlString;
import com.thinkbiganalytics.jobrepo.repository.dao.JobDao;
import com.thinkbiganalytics.jobrepo.repository.dao.PipelineDao;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import static com.thinkbiganalytics.jobrepo.repository.RepositoryUtils.convertBatchStatus;
import static com.thinkbiganalytics.jobrepo.repository.RepositoryUtils.convertJobParameters;
import static com.thinkbiganalytics.jobrepo.repository.RepositoryUtils.utcDateTimeCorrection;


/**
 * Simple repository abstraction to spring's job explorer that delivers a flattened json friendly view of current and previous job
 * executions.
 */
@SuppressWarnings("UnusedDeclaration")
@Named
public class JobRepositoryImpl implements JobRepository {

  protected static final Logger LOG = LoggerFactory.getLogger(JobRepositoryImpl.class);

  public static final String DESCRIPTION_PARAM = "description";

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private JobExplorer jobExplorer;


  @Inject
  private JobDao jobDao;

  @Autowired
  private PipelineDao pipelineDao;

  /**
   * Convert a step execution to a JSON friendly ExecutedStep version
   *
   * @param stepExecution The step execution to convert
   * @return An ExecutedStep object
   */
  static private ExecutedStep convertToExecutedStep(final StepExecution stepExecution) {
    Object descr = stepExecution.getExecutionContext().get(DESCRIPTION_PARAM);

    ExecutedStep step = new DefaultExecutedStep();
    step.setStatus(ExecutionStatus.valueOf(stepExecution.getStatus().name()));
    step.setStepName(descr != null ? descr.toString() : stepExecution.getStepName());
    step.setReadCount(stepExecution.getReadCount());
    step.setWriteCount(stepExecution.getWriteCount());
    step.setCommitCount(stepExecution.getCommitCount());
    step.setRollbackCount(stepExecution.getRollbackCount());
    step.setReadSkipCount(stepExecution.getReadSkipCount());
    step.setProcessSkipCount(stepExecution.getProcessSkipCount());
    step.setStartTime(utcDateTimeCorrection(stepExecution.getStartTime()));
    step.setEndTime(utcDateTimeCorrection(stepExecution.getEndTime()));
    step.setLastUpdateTime(utcDateTimeCorrection(stepExecution.getLastUpdated()));
    step.setExecutionContext(new HashMap<String, Object>());
    // Only add serializable items to the execution context
    for (Map.Entry<String, Object> entry : stepExecution.getExecutionContext().entrySet()) {
      if (Serializable.class.isAssignableFrom(entry.getValue().getClass())) {
        step.getExecutionContext().put(entry.getKey(), entry.getValue());
      }
    }
    step.setExitCode(stepExecution.getExitStatus().getExitCode());
    step.setExitDescription(stepExecution.getExitStatus().getExitDescription());
    step.setId(stepExecution.getId());
    step.setVersion(stepExecution.getVersion());
    step.setRunning(stepExecution.getEndTime() == null);

    return step;
  }


  /**
   * Convert an JobExecution instance to a JSON friendly ExecutedJob instance
   *
   * @param jobInstance  The Job instance information
   * @param jobExecution The Job Execution information
   * @return An ExecutedJob instance representing both the JobInstance and its execution
   */
  static public ExecutedJob convertToExecutedJob(final JobInstance jobInstance, final JobExecution jobExecution) {
    DateTime startTime = utcDateTimeCorrection(jobExecution.getStartTime());
    DateTime endTime = utcDateTimeCorrection(jobExecution.getEndTime());
    if (jobExecution.getStartTime() == null) {
      startTime = null;
    }
    Long runTime = null;
    Long timeSinceEndTime = null;
    ;
    if (startTime != null) {
      runTime = endTime.getMillis() - startTime.getMillis();
      timeSinceEndTime = utcDateTimeCorrection((DateTime) null).getMillis() - endTime.getMillis();
    }

    ExecutedJob executedJob = new DefaultExecutedJob();
    executedJob.setRunTime(runTime);
    executedJob.setTimeSinceEndTime(timeSinceEndTime);
    if (jobExecution instanceof TbaJobExecution) {
      executedJob.setJobType(((TbaJobExecution) jobExecution).getJobType());
      executedJob.setIsLatest(((TbaJobExecution) jobExecution).isLatest());
      executedJob.setFeedName(((TbaJobExecution) jobExecution).getFeedName());
      executedJob.setRunTime(((TbaJobExecution) jobExecution).getRunTime());
      executedJob.setTimeSinceEndTime(((TbaJobExecution) jobExecution).getTimeSinceEndTime());
    }
    executedJob.setInstanceId(jobInstance.getInstanceId());
    executedJob.setJobName(jobInstance.getJobName());
    executedJob.setExceptions(jobExecution.getAllFailureExceptions());
    executedJob.setCreateTime(utcDateTimeCorrection(jobExecution.getCreateTime()));
    executedJob.setEndTime(endTime);
    Map<String, Object> executionContext = new HashMap<String, Object>();
    // Only add serializable items
    for (Map.Entry<String, Object> entry : jobExecution.getExecutionContext().entrySet()) {
      if (Serializable.class.isAssignableFrom(entry.getValue().getClass())) {
        executionContext.put(entry.getKey(), entry.getValue());
      }
    }
    executedJob.setExecutionContext(executionContext);
    executedJob.setExitCode(jobExecution.getExitStatus().getExitCode());
    executedJob.setExitStatus(jobExecution.getExitStatus().getExitDescription());
    executedJob.setJobConfigurationName(jobExecution.getJobConfigurationName());
    executedJob.setJobId(jobExecution.getJobId());
    executedJob.setJobParameters(convertJobParameters(jobExecution.getJobParameters()));
    executedJob.setLastUpdated(utcDateTimeCorrection(jobExecution.getLastUpdated()));
    executedJob.setStartTime(startTime);
    executedJob.setStatus(convertBatchStatus(jobExecution.getStatus()));
    executedJob.setJobId(jobExecution.getJobId());
    executedJob.setExecutionId(jobExecution.getId());

    executedJob.setExecutedSteps(new ArrayList<ExecutedStep>());
    for (StepExecution step : jobExecution.getStepExecutions()) {
      ExecutedStep executedStep = convertToExecutedStep(step);
      executedJob.getExecutedSteps().add(executedStep);
    }

    return executedJob;
  }


  /**
   * Return a list of all jobs that have been executed ordered by job instance id desc
   *
   * @param limit The maximum number of results to return
   * @return A list of executed jobs
   */
  @Override
  public List<ExecutedJob> findJobs(final int start, final int limit) {
    List<ExecutedJob> executedJobs = new ArrayList<ExecutedJob>();
    return jobDao.findAllExecutedJobs(null, null, start, limit);
  }


  public List<JobStatusCount> getRunningOrFailedJobCounts() {
    return jobDao.findList(jobDao.getQuery(RunningOrFailedJobCountsQuery.class));
  }

  /**
   * return a list of JobStatusCounts based upon looking back for a given Date Part and interval If the minimum Date for the given
   * interval is not included in the returned list then it will add a Date matching the minimum date with a count of 0.
   */
  public List<JobStatusCount> getJobStatusCountByDay(DatabaseQuerySubstitution.DATE_PART datePart, Integer interval) {

    List<ColumnFilter> filters = new ArrayList<>();
    if (datePart != null) {
      ColumnFilter filter = new QueryColumnFilterSqlString();
      String filterName = JobQueryConstants.DAY_DIFF_FROM_NOW;
      switch (datePart) {
        case DAY:
          filterName = JobQueryConstants.DAY_DIFF_FROM_NOW;
          break;
        case WEEK:
          filterName = JobQueryConstants.WEEK_DIFF_FROM_NOW;
          break;
        case MONTH:
          filterName = JobQueryConstants.MONTH_DIFF_FROM_NOW;
          break;
        case YEAR:
          filterName = JobQueryConstants.YEAR_DIFF_FROM_NOW;
          break;

      }
      filter.setName(filterName);
      filter.setValue(interval);
      filters.add(filter);
    }

    JobStatusCountByDayQuery query = jobDao.getQuery(JobStatusCountByDayQuery.class);
    query.setColumnFilterList(filters);
    List<JobStatusCount> statusCountList = jobDao.findList(query);

    DailyJobStatusCount statusCount = new DailyJobStatusCountResult(datePart, interval, statusCountList);
    statusCount.checkAndAddStartDate();
    return statusCount.getJobStatusCounts();

  }


  public Long findJobInstanceIdForJobExecutionId(Long jobExecutionId) {
    return jobDao.findJobInstanceIdForJobExecutionId(jobExecutionId);
  }


  public List<ExecutedJob> findJobs(List<ColumnFilter> conditions, List<OrderBy> order, Integer start, Integer limit) {
    //find distinct columns
    return jobDao.findAllExecutedJobs(conditions, order, start, limit);
  }

  public List<ExecutedJob> findLatestJobs(List<ColumnFilter> conditions, List<OrderBy> order, Integer start, Integer limit) {
    return jobDao.findLatestExecutedJobs(conditions, order, start, limit);
  }

  public List<ExecutedJob> findParentAndChildJobs(String parentJobExecutionId) {
    return jobDao.findChildJobs(parentJobExecutionId, true, 0, null);
  }

  public SearchResult getDataTablesSearchResult(List<ColumnFilter> conditions, List<ColumnFilter> defaultFilters,
                                                List<OrderBy> order, Integer start, Integer limit) {
    boolean noConditions = false;
    //find distinct columns
    if (defaultFilters != null && !defaultFilters.isEmpty()) {
      if (conditions == null) {
        conditions = new ArrayList<ColumnFilter>();
        noConditions = true;
      }
      conditions.addAll(defaultFilters);
    }
    if (!noConditions && (conditions == null || conditions.isEmpty())) {
      noConditions = true;
    }
    Long filterCount = null;
    Long allCount = null;

    List<ExecutedJob> jobs = jobDao.findLatestExecutedJobs(conditions, order, start, limit);
    if (noConditions) {
      allCount = jobDao.selectLatestExecutedJobsCount(defaultFilters);

      //No need to do the filter count query since there are no filter conditions present.

      filterCount = allCount;
    } else {
      filterCount = jobDao.selectLatestExecutedJobsCount(conditions);
      allCount = filterCount;
    }
    SearchResult searchResult = new SearchResultImpl();
    searchResult.setData(jobs);
    searchResult.setRecordsFiltered(filterCount);
    searchResult.setRecordsTotal(allCount);
    return searchResult;
  }

  public List<Object> selectDistinctColumnValues(List<ColumnFilter> filters, String columnName) {
    List<Object> columnValues = new ArrayList<Object>();
    columnValues = jobDao.selectDistinctColumnValues(filters, columnName);
    return columnValues;
  }

  public Long selectCount(List<ColumnFilter> filters) {
    return jobDao.selectCount(filters);
  }


  /**
   * Find the executed jobs with the provided instance id
   *
   * @param instanceId The instance id
   * @return The job executions matching the instance id, or empty list if none found
   */
  @Override
  public List<ExecutedJob> findByInstanceId(String instanceId) {
    List<ExecutedJob> executedJobs = new ArrayList<ExecutedJob>();

    JobInstance jobInstance = this.jobExplorer.getJobInstance(Long.valueOf(instanceId));
    List<JobExecution> executions = this.jobExplorer.getJobExecutions(jobInstance);
    for (JobExecution jobExecution : executions) {
      List<TbaJobExecution> list =  jobDao.findList(new AugmentJobExecutionTimingDataQuery(jobDao.getDatabaseType(),jobExecution));
      if(list != null){
        jobExecution = list.get(0);
      }
      executedJobs.add(convertToExecutedJob(jobInstance, jobExecution));
    }
    return executedJobs;
  }

  public ExecutedJob findByExecutionId(final String executionId) {
    return findByExecutionId(executionId, false);
  }

  public ExecutedJob findByExecutionId(final String executionId, boolean fetchLatest) {
    ExecutedJob executedJob = null;
    JobExecution jobExecution = null;
    try {
      jobExecution = this.jobExplorer.getJobExecution(Long.valueOf(executionId));
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (fetchLatest) {
      JobInstance jobInstance = jobExecution.getJobInstance();
      jobExecution = this.jobExplorer.getJobExecutions(jobInstance).get(0);
    }
    if (jobExecution != null) {
      List<TbaJobExecution> list =  jobDao.findList(new AugmentJobExecutionTimingDataQuery(jobDao.getDatabaseType(),jobExecution));
      if(list != null){
        jobExecution = list.get(0);
      }
      executedJob = convertToExecutedJob(jobExecution.getJobInstance(), jobExecution);

      // if any steps are running we need to get the correct step timing info
      if(executedJob.getExecutedSteps() != null) {
        List<ExecutedStep> steps = Lists.newArrayList(Iterables.filter(executedJob.getExecutedSteps(), new Predicate<ExecutedStep>() {
          @Override
          public boolean apply(ExecutedStep executedStep) {
            return executedStep.isRunning();

          }
        }));

        if(steps != null){
          for(ExecutedStep step: steps){
            addStepTimingData(step);
          }
        }

      }

      JobParameters jobParameters = jobExecution.getJobParameters();
      if (jobParameters != null) {

        for (Map.Entry<String, JobParameter> entry : jobParameters.getParameters().entrySet()) {
          if (FeedConstants.PARAM__FEED_NAME.equalsIgnoreCase(entry.getKey())) {
            Object feedName = entry.getValue().getValue();
            if (feedName != null) {
              executedJob.setFeedName(feedName.toString());
            }
          }
          if (FeedConstants.PARAM__JOB_TYPE.equalsIgnoreCase(entry.getKey())) {
            Object jobType = entry.getValue().getValue();
            if (jobType != null) {
              executedJob.setJobType(jobType.toString());
            }
          }
        }
      }
    }
    return executedJob;


  }


  /**
   * Return a list of all unique job names that have been executed at least once
   *
   * @return A list of job names, or empty list if there are none
   */
  @Override
  public List<String> uniqueJobNames() {
    List<String> jobNames = new ArrayList<String>();
    String sql = "SELECT DISTINCT JOB_NAME FROM BATCH_JOB_INSTANCE";
    List<String> queryResult = pipelineDao.queryForList(sql, String.class);

    // Protection against null list
    if (queryResult != null) {
      jobNames.addAll(queryResult);
    }

    return jobNames;
  }

  /**
   * Return a list of all unique job parameter names we have seen in past executions
   *
   * @return A list of job parameter names, or an empty list if none are found
   */
  @Override
  public List<String> uniqueJobParameterNames() {
    String sql = "SELECT DISTINCT KEY_NAME FROM BATCH_EXECUTION_PARAMS";
    List<String> parameterNames = new ArrayList<String>();

    List<String> queryResult = pipelineDao.queryForList(sql, String.class);

    // Protection against null list
    if (queryResult != null) {
      parameterNames.addAll(queryResult);
    }

    return parameterNames;
  }


  public Map<ExecutionStatus, Long> getCountOfJobsByStatus() {
    return jobDao.getCountOfJobsByStatus();
  }

  public Map<ExecutionStatus, Long> getCountOfLatestJobsByStatus() {
    return jobDao.getCountOfLatestJobsByStatus();
  }


  public List<ExecutedStep> getJobProgress(String executionId) {

    List<ExecutedStep> executedSteps = new ArrayList<>();
    JobStepQuery jobStepQuery = jobDao.getQuery(JobStepQuery.class);
    jobStepQuery.setJobExecutionId(new Long(executionId));
    List<JobStepQueryRow> steps = jobDao.findList(jobStepQuery);

    Map<Long, JobExecution> fetchedJobs = new HashMap<>();

    List<Long> stepExecutionIds = new ArrayList<>();
    for (JobStepQueryRow row : steps) {
      stepExecutionIds.add(row.getStepExecutionId());
    }
    for (JobStepQueryRow row : steps) {
      Long jobExecutionId = row.getJobExecutionId();
      if (!fetchedJobs.containsKey(jobExecutionId)) {
        JobExecution execution = this.jobExplorer.getJobExecution(jobExecutionId.longValue());
        if (execution != null) {
          for (StepExecution step : execution.getStepExecutions()) {
            if (stepExecutionIds.contains(step.getId())) {
              ExecutedStep executedStep = convertToExecutedStep(step);
             addStepTimingData(executedStep);
              executedSteps.add(executedStep);
            }
          }
          fetchedJobs.put(jobExecutionId, execution);
        }
      }
    }
    return executedSteps;
  }

  public  void addStepTimingData(ExecutedStep executedStep) {
    List<StepExecutionTiming> list = jobDao.findList(
        new StepExecutionTimingQuery(jobDao.getDatabaseType(), executedStep.getId()));
    if(list != null) {
      executedStep.setRunTime(list.get(0).getRunTime());
      executedStep.setTimeSinceEndTime(list.get(0).getTimeSinceEndTime());
    }

  }


  // get a list of all execution Ids and their respective Dates for a give Job Instance
  public List<RelatedJobExecution> findRelatedJobExecutions(Long executionId) {
    RelatedJobExecutionsQuery relatedJobExecutionsQuery = jobDao.getQuery(RelatedJobExecutionsQuery.class);
    relatedJobExecutionsQuery.setJobExecutionId(new Long(executionId));
    return jobDao.findList(relatedJobExecutionsQuery);

  }

  public List<RelatedJobExecution> findPreviousJobExecutions(Long executionId) {
    RelatedJobExecutionsQuery relatedJobExecutionsQuery = jobDao.getQuery(RelatedJobExecutionsQuery.class);
    List<ColumnFilter> filters = new ArrayList<>();
    ColumnFilter filter = new QueryColumnFilterSqlString();
    filter.setSqlString(" JOB_EXECUTION_ID < " + executionId);
    filters.add(filter);
    relatedJobExecutionsQuery.setColumnFilterList(filters);
    relatedJobExecutionsQuery.setJobExecutionId(new Long(executionId));
    return jobDao.findList(relatedJobExecutionsQuery);

  }


  /**
   * Get a list of executed steps and their progression towards completion.  Steps that are not running will not have a percent
   * complete assigned to them
   *
   * @param executionId The job instance id to get the progress for
   * @return A list of all executed steps and their progress for the given job
   */

  public List<ExecutedStep> getJobProgressxx(String executionId) {
    List<ExecutedStep> steps = new ArrayList<ExecutedStep>();

    JobProgressQuery jobProgressQuery = jobDao.getQuery(JobProgressQuery.class);

    jobProgressQuery.setJobExecutionId(executionId);
    List<Object[]> queryResult = jobDao.findList(jobProgressQuery);

    JobExecution execution = null;
    for (Object[] singleResult : queryResult) {
      String stepName = (String) singleResult[0];
      String status = null;
      if (singleResult.length > 1) {
        status = (String) singleResult[1];
      }
      BigInteger jobExecutionId = null;
      if (singleResult.length > 2) {
        jobExecutionId = (BigInteger) singleResult[2];
      }

      if (jobExecutionId != null && execution == null) {
        execution = this.jobExplorer.getJobExecution(jobExecutionId.longValue());
      }

      StepExecution matchingStep = null;
      if (execution != null) {
        for (StepExecution step : execution.getStepExecutions()) {
          if (step.getStepName().equals(stepName)) {
            matchingStep = step;
            break;
          }
        }
      }

      if (matchingStep == null) {
        // Create a place holder step with zero percent complete as we haven't run this yet
        ExecutedStep placeHolder = new DefaultExecutedStep();
        placeHolder.setStepName(stepName);
        placeHolder.setStatus(ExecutionStatus.UNKNOWN);
        placeHolder.setExecutionContext(new HashMap<String, Object>());
        placeHolder.getExecutionContext().put("percentComplete", "0");
        steps.add(placeHolder);
      } else {
        // Look for steps that didn't implement percent complete
        if (!matchingStep.getExecutionContext().containsKey("percentComplete") &&
            matchingStep.getStatus().equals(BatchStatus.COMPLETED)) {
          matchingStep.getExecutionContext().putString("percentComplete", "100");
        }
        steps.add(convertToExecutedStep(matchingStep));
      }
    }

    return steps;
  }
}
