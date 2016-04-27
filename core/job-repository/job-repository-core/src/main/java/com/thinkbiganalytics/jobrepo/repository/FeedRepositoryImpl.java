package com.thinkbiganalytics.jobrepo.repository;

import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.query.feed.FeedHealthCheckDataQuery;
import com.thinkbiganalytics.jobrepo.query.feed.FeedHealthQuery;
import com.thinkbiganalytics.jobrepo.query.feed.FeedQueryConstants;
import com.thinkbiganalytics.jobrepo.query.feed.FeedStatusCountByDayQuery;
import com.thinkbiganalytics.jobrepo.query.feed.LatestCompletedFeedQuery;
import com.thinkbiganalytics.jobrepo.query.feed.LatestOperationalFeedQuery;
import com.thinkbiganalytics.jobrepo.query.job.JobQueryConstants;
import com.thinkbiganalytics.jobrepo.query.model.DailyJobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.DailyJobStatusCountResult;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.DefaultFeedStatus;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealthQueryResult;
import com.thinkbiganalytics.jobrepo.query.model.FeedStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.SearchResult;
import com.thinkbiganalytics.jobrepo.query.model.SearchResultImpl;
import com.thinkbiganalytics.jobrepo.query.model.TbaJobExecution;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitution;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.FeedHealthUtil;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;
import com.thinkbiganalytics.jobrepo.query.support.OrderByClause;
import com.thinkbiganalytics.jobrepo.query.support.QueryColumnFilterSqlString;
import com.thinkbiganalytics.jobrepo.repository.dao.FeedDao;
import com.thinkbiganalytics.jobrepo.repository.dao.PipelineDao;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
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
import static com.thinkbiganalytics.jobrepo.repository.RepositoryUtils.utcDateTimeCorrection;

@SuppressWarnings("UnusedDeclaration")
@Named
public class FeedRepositoryImpl implements FeedRepository {

  private static final Logger LOG = LoggerFactory.getLogger(FeedRepositoryImpl.class);
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private JobExplorer jobExplorer;


  @Autowired
  private PipelineDao pipelineDao;

  @Inject
  FeedDao feedDao;

  public static ExecutedFeed convertToExecutedFeed(final JobExecution jobExecution) {
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


    if (jobExecution instanceof TbaJobExecution) {
     runTime = ((TbaJobExecution) jobExecution).getRunTime();
     timeSinceEndTime = ((TbaJobExecution) jobExecution).getTimeSinceEndTime();
    }

    JobInstance jobInstance = jobExecution.getJobInstance();

    ExecutedFeed executedFeed = new DefaultExecutedFeed();
    executedFeed.setFeedInstanceId(jobInstance.getInstanceId());
    executedFeed.setFeedExecutionId(jobExecution.getId());
    executedFeed.setName(jobExecution.getJobParameters().getString("feed"));
    executedFeed.setExceptions(jobExecution.getAllFailureExceptions());
    Map<String, Object> executionContext = new HashMap<String, Object>();
    // Only add serializable items
    for (Map.Entry<String, Object> entry : jobExecution.getExecutionContext().entrySet()) {
      if (Serializable.class.isAssignableFrom(entry.getValue().getClass())) {
        executionContext.put(entry.getKey(), entry.getValue());
      }
    }
    executedFeed.setExitCode(jobExecution.getExitStatus().getExitCode());
    executedFeed.setExitStatus(jobExecution.getExitStatus().getExitDescription());
    executedFeed.setStatus(convertBatchStatus(jobExecution.getStatus()));

    executedFeed.setStartTime(startTime);
    executedFeed.setEndTime(endTime);
    executedFeed.setRunTime(runTime);
    executedFeed.setTimeSinceEndTime(timeSinceEndTime);
    if (jobExecution instanceof TbaJobExecution) {
      executedFeed.setIsLatest(((TbaJobExecution) jobExecution).isLatest());
    }
    executedFeed.setExecutedJobs(new ArrayList<ExecutedJob>());

    executedFeed.getExecutedJobs().add(JobRepositoryImpl.convertToExecutedJob(jobInstance, jobExecution));
    return executedFeed;
  }

  /**
   * Convert an JobExecution instance to a JSON friendly ExecutedJob instance
   *
   * @param jobInstance  The Job instance information
   * @param jobExecution The Job Execution information
   * @return An ExecutedJob instance representing both the JobInstance and its execution
   */
  public ExecutedFeed convertToExecutedFeed(final JobInstance jobInstance, final JobExecution jobExecution) {

    ExecutedFeed executedFeed = FeedRepositoryImpl.convertToExecutedFeed(jobExecution);
    List<ExecutedJob> childJobs = findChildJobs(new Long(jobInstance.getInstanceId()).toString(), 50);
    for (ExecutedJob job : childJobs) {
      executedFeed.getExecutedJobs().add(job);
      if (job.getEndTime().isAfter(jobExecution.getEndTime().getTime())) {
        executedFeed.setEndTime(utcDateTimeCorrection(job.getEndTime()));
      }
      if (!job.getStatus().equals(jobExecution.getStatus())) {
        executedFeed.setStatus(job.getStatus());
      }
      if (!job.getExitCode().equals(jobExecution.getExitStatus().getExitCode())) {
        executedFeed.setExitCode(job.getExitCode());
      }
    }
    return executedFeed;
  }

  public List<ExecutedFeed> findLastCompletedFeeds(List<ColumnFilter> filters) {
    List<ExecutedFeed> executedFeeds = new ArrayList<ExecutedFeed>();
    LatestCompletedFeedQuery query = feedDao.getQuery(LatestCompletedFeedQuery.class);
    query.setColumnFilterList(filters);
    List<OrderBy> orderBy = new ArrayList<>();
    orderBy.add(new OrderByClause(FeedQueryConstants.QUERY_FEED_NAME_COLUMN, "asc"));
    query.setOrderByList(orderBy);
    List<JobExecution> jobExecutions = feedDao.findList(query, 0, -1);
    if (jobExecutions != null && !jobExecutions.isEmpty()) {
      for (JobExecution jobExecution : jobExecutions) {
        executedFeeds.add(convertToExecutedFeed(jobExecution));
      }
    }
    return executedFeeds;
  }

  public ExecutedFeed findLastCompletedFeed(String feedName) {
    ExecutedFeed feed = null;
    List<ColumnFilter> filter = new ArrayList<ColumnFilter>();
    filter.add(new QueryColumnFilterSqlString(FeedQueryConstants.QUERY_FEED_NAME_COLUMN, feedName));
    LatestCompletedFeedQuery query = feedDao.getQuery(LatestCompletedFeedQuery.class);
    query.setColumnFilterList(filter);
    List<JobExecution> jobExecutions = feedDao.findList(query, 0, -1);
    if (jobExecutions != null && !jobExecutions.isEmpty()) {
      feed = convertToExecutedFeed(jobExecutions.get(0));
    }
    return feed;
  }


  /**
   * @param filters
   * @param limit
   * @param orderBy
   * @return
   */
  public List<ExecutedFeed> findFeeds(List<ColumnFilter> filters, List<OrderBy> orderBy, final Integer start,
                                      final Integer limit) {
    List<ExecutedFeed> executedFeeds = new ArrayList<ExecutedFeed>();
    executedFeeds = feedDao.findExecutedFeeds(filters, orderBy, start, limit);
    return executedFeeds;
  }

  public List<Object> selectDistinctColumnValues(List<ColumnFilter> filters, String columnName) {
    List<Object> columnValues = new ArrayList<Object>();
    columnValues = feedDao.selectDistinctColumnValues(filters, columnName);
    return columnValues;
  }

  public Long selectCount(List<ColumnFilter> filters) {
    return feedDao.selectCount(filters);
  }

  public SearchResult getDataTablesSearchResult(List<ColumnFilter> conditions, List<ColumnFilter> defaultFilters,
                                                List<OrderBy> order, Integer start, Integer limit) {
    boolean noConditions = false;
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

    List<ExecutedFeed> feeds = feedDao.findExecutedFeeds(conditions, order, start, limit);
    Long allCount = feedDao.selectCount(defaultFilters);
    Long filterCount = null;
    //No need to do the filter count query since there are no filter conditions present.
    if (noConditions) {
      filterCount = allCount;
    } else {
      filterCount = feedDao.selectCount(conditions);
    }

    SearchResult searchResult = new SearchResultImpl();
    searchResult.setData(feeds);
    searchResult.setRecordsFiltered(filterCount);
    searchResult.setRecordsTotal(allCount);
    return searchResult;
  }


  /**
   * Return a list of all jobs with the specified parentJobExecutionId
   *
   * @param parentJobExecutionId The parentJobExecutionId
   * @param limit                The maximum number of results to return
   * @return A list of executed feeds
   */
  public List<ExecutedJob> findChildJobs(final String parentJobExecutionId, final int limit) {
    List<ExecutedJob> executedJobs = new ArrayList<ExecutedJob>();

    String sql = "SELECT JOB_INSTANCE_ID from BATCH_JOB_EXECUTION_PARAMS p " +
                 "inner join BATCH_JOB_EXECUTION e on p.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
                 "WHERE p.KEY_NAME = 'parentJobExecutionId' AND p.STRING_VAL = " + parentJobExecutionId + " " +
                 "ORDER BY e.CREATE_TIME desc " +
                 "LIMIT " + limit;
    // Query q = this.entityManager.createNativeQuery(sql);
    // List<BigInteger> queryResult = q.getResultList();
    List<BigInteger> queryResult = pipelineDao.queryForList(sql, BigInteger.class);

    for (BigInteger jobInstanceId : queryResult) {
      JobInstance jobInstance = this.jobExplorer.getJobInstance(jobInstanceId.longValue());
      List<JobExecution> executions = this.jobExplorer.getJobExecutions(jobInstance);

      for (JobExecution jobExecution : executions) {
        executedJobs.add(JobRepositoryImpl.convertToExecutedJob(jobInstance, jobExecution));
      }
    }
    return executedJobs;
  }

  /**
   * Return a list containing the feed with the specified feedInstanceId
   *
   * @param feedInstanceId The feed value
   * @return A list of executed jobs
   */
  public List<ExecutedFeed> findFeedByInstanceId(String feedInstanceId) {
    List<ExecutedFeed> executedFeeds = new ArrayList<ExecutedFeed>();

    JobInstance jobInstance = this.jobExplorer.getJobInstance(Long.valueOf(feedInstanceId));
    List<JobExecution> executions = this.jobExplorer.getJobExecutions(jobInstance);
    for (JobExecution jobExecution : executions) {
      executedFeeds.add(convertToExecutedFeed(jobInstance, jobExecution));
    }
    return executedFeeds;
  }

  public List<JobStatusCount> getFeedStatusCountByDay(String feedName, DatabaseQuerySubstitution.DATE_PART datePart,
                                                      Integer interval) {

    List<ColumnFilter> filters = new ArrayList<>();
    filters.add(new QueryColumnFilterSqlString(FeedQueryConstants.QUERY_FEED_NAME_COLUMN, feedName));

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

    FeedStatusCountByDayQuery query = feedDao.getQuery(FeedStatusCountByDayQuery.class);
    query.setColumnFilterList(filters);
    List<JobStatusCount> statusCountList = feedDao.findList(query);
    DailyJobStatusCount statusCount = new DailyJobStatusCountResult(datePart, interval, statusCountList);
    statusCount.checkAndAddStartDate();
    return statusCount.getJobStatusCounts();


  }

  public List<FeedHealth> getFeedHealthCounts() {
    return getFeedHealthCounts(null);
  }

  public List<FeedHealth> getFeedHealthCounts(String feedName) {

    FeedHealthQuery feedHealthQuery = feedDao.getQuery(FeedHealthQuery.class);
    if (StringUtils.isNotBlank(feedName)) {
      List<ColumnFilter> filters = new ArrayList<>();
      filters.add(new QueryColumnFilterSqlString(FeedQueryConstants.QUERY_FEED_NAME_COLUMN, feedName));
    }
    List<FeedHealthQueryResult> feedHealthQueryResults = feedDao.findList(feedHealthQuery);
    List<FeedHealth> feedHealthList = FeedHealthUtil.parseToList(null, null, feedHealthQueryResults);

    FeedHealthCheckDataQuery feedHealthCheckDataQuery = feedDao.getQuery(FeedHealthCheckDataQuery.class);
    List<FeedHealthQueryResult> feedHealthCheckDataQueryResults = feedDao.findList(feedHealthCheckDataQuery);
    if (feedHealthCheckDataQueryResults != null && !feedHealthCheckDataQueryResults.isEmpty()) {
      mergeCheckDataHealthWithFeedHealth(feedHealthList, feedHealthCheckDataQueryResults);
    }

    return feedHealthList;

  }


  private void mergeCheckDataHealthWithFeedHealth(List<FeedHealth> feedHealth, List<FeedHealthQueryResult> checkDataHealth) {
    if (checkDataHealth != null && !checkDataHealth.isEmpty() && feedHealth != null && !feedHealth.isEmpty()) {
      //merge in the check data results
      Map<String, FeedHealth> map = new HashMap<String, FeedHealth>();
      for (FeedHealth feedHealthResult : feedHealth) {
        map.put(feedHealthResult.getFeed(), feedHealthResult);
      }

      for (FeedHealthQueryResult checkDataResult : checkDataHealth) {
        if (checkDataResult.getHealth().equalsIgnoreCase("UNHEALTHY") && checkDataResult.getCount() > 0L) {
          FeedHealth feedResult = map.get(checkDataResult.getFeed());
          if (feedResult != null && (feedResult.getUnhealthyCount() == null || feedResult.getUnhealthyCount() == 0L)) {
            feedResult.setUnhealthyCount(1L); //mark it unhealthy as the check data job for the feed failed
          } else if (feedResult != null) {
            //it is already unhealthy, add it to the error count
            Long unhealthCount = feedResult.getUnhealthyCount();
            if (unhealthCount == null) {
              unhealthCount = 0L;
            }
            unhealthCount += 1;
            feedResult.setUnhealthyCount(unhealthCount);
          }
          if (feedResult != null) {
            if (feedResult.getLastUnhealthyTime() == null) {
              feedResult.setLastUnhealthyTime(checkDataResult.getEndTime());
            } else if (feedResult.getLastUnhealthyTime().before(checkDataResult.getEndTime())) {
              feedResult.setLastUnhealthyTime(checkDataResult.getEndTime());
            }
          }
        }
      }


    }
  }

  public FeedStatus getFeedStatusAndSummary(List<ColumnFilter> filters) {
    //Get a list of the Latest Feeds without a NOOP exit code
    LatestOperationalFeedQuery latestOperationalFeedQuery = feedDao.getQuery(LatestOperationalFeedQuery.class);
    latestOperationalFeedQuery.setColumnFilterList(filters);
    List<ExecutedFeed> latestOpFeeds = feedDao.findExecutedFeeds(latestOperationalFeedQuery);
    Map<String, Long> avgRunTimes = null;//feedDao.findAverageRunTimes();

    FeedHealthQuery feedHealthQuery = feedDao.getQuery(FeedHealthQuery.class);
    feedHealthQuery.setColumnFilterList(filters);
    List<FeedHealthQueryResult> feedHealthQueryResults = feedDao.findList(feedHealthQuery);

    List<FeedHealth> feedHealthList = FeedHealthUtil.parseToList(latestOpFeeds, avgRunTimes, feedHealthQueryResults);

    FeedHealthCheckDataQuery feedHealthCheckDataQuery = feedDao.getQuery(FeedHealthCheckDataQuery.class);
    feedHealthCheckDataQuery.setColumnFilterList(filters);
    List<FeedHealthQueryResult> feedHealthCheckDataQueryResults = feedDao.findList(feedHealthCheckDataQuery);
    if (feedHealthCheckDataQueryResults != null && !feedHealthCheckDataQueryResults.isEmpty()) {
      mergeCheckDataHealthWithFeedHealth(feedHealthList, feedHealthCheckDataQueryResults);
    }
    return new DefaultFeedStatus(feedHealthList);
  }

  public List<String> getFeedNames() {
    return feedDao.getFeedNames();
  }


  /**
   * Return a list of all jobs with the specified feed and exit code
   *
   * @param feed  The feed value
   * @param limit The maximum number of results to return
   * @return A list of executed jobs
   */
  @Override
  public List<ExecutedJob> findJobsByFeed(final String feed, final int limit) {
    List<ExecutedJob> executedJobs = new ArrayList<ExecutedJob>();

    String sql = "SELECT DISTINCT JOB_INSTANCE_ID from BATCH_JOB_EXECUTION e " +
                 "inner join BATCH_JOB_EXECUTION_PARAMS p on p.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
                 "WHERE p.STRING_VAL = '" + feed + "' " +
                 "ORDER BY e.CREATE_TIME desc " +
                 "LIMIT " + limit;

    List<BigInteger> queryResult = pipelineDao.queryForList(sql, BigInteger.class);

    for (BigInteger jobInstanceId : queryResult) {
      JobInstance jobInstance = this.jobExplorer.getJobInstance(jobInstanceId.longValue());
      List<JobExecution> executions = this.jobExplorer.getJobExecutions(jobInstance);

      for (JobExecution jobExecution : executions) {
        executedJobs.add(JobRepositoryImpl.convertToExecutedJob(jobInstance, jobExecution));
      }
    }
    return executedJobs;
  }

  /**
   * Return a list of all unique job names that have been executed at least once
   *
   * @return A list of job names, or empty list if there are none
   */
  @Override
  public List<String> uniqueFeedNames() {
    List<String> jobNames = new ArrayList<String>();
    String
        sql =
        "SELECT DISTINCT STRING_CAL FROM BATCH_JOB_EXECUTION_PARAMS WHERE KEY_NAME = '" + FeedConstants.PARAM__FEED_NAME + "'";

    List<String> queryResult = pipelineDao.queryForList(sql, String.class);
    // Protection against null list
    if (queryResult != null) {
      jobNames.addAll(queryResult);
    }

    return jobNames;
  }

}
