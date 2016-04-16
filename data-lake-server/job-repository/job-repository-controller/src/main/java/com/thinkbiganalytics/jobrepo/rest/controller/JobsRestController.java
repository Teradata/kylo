package com.thinkbiganalytics.jobrepo.rest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.jobrepo.query.job.JobQueryConstants;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.JobParameterType;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.RelatedJobExecution;
import com.thinkbiganalytics.jobrepo.query.model.SearchResult;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitution;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;
import com.thinkbiganalytics.jobrepo.query.support.QueryColumnFilterSqlString;
import com.thinkbiganalytics.jobrepo.repository.JobRepository;
import com.thinkbiganalytics.jobrepo.repository.rest.model.JobAction;
import com.thinkbiganalytics.jobrepo.rest.support.DataTableColumnFactory;
import com.thinkbiganalytics.jobrepo.rest.support.RestUtil;
import com.thinkbiganalytics.jobrepo.rest.support.WebColumnFilterUtil;
import com.thinkbiganalytics.jobrepo.service.JobExecutionException;
import com.thinkbiganalytics.jobrepo.service.JobService;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Provides rest endpoints for control and monitoring of the pipeline
 */
@Api(basePath = "/api/api-version/jobs", value = "Jobs", description = "Job Details", produces = "application/json")
@Path("/v1/jobs")
public class JobsRestController {

  private static final Logger LOG = LoggerFactory.getLogger(JobsRestController.class);

  @Inject
  private JobRepository jobRepository;

  @Inject
  private JobService jobService;


  @GET
  @Path("/{executionId}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response getJob(@PathParam("executionId") String executionId,
                         @QueryParam(value = "includeSteps") @DefaultValue("false") boolean includeSteps) {
    ExecutedJob executedJob = jobRepository.findByExecutionId(executionId);
    if (includeSteps) {
      List<ExecutedStep> steps = jobRepository.getJobProgress(executionId);
      executedJob.setExecutedSteps(steps);
    }
    return Response.ok(executedJob).build();
  }

  @GET
  @Path("/{executionId}/related")
  @Produces({MediaType.APPLICATION_JSON})
  public List<RelatedJobExecution> getRelatedJobExecutions(@PathParam("executionId") String executionId) {
    if (executionId != null) {
      return jobRepository.findRelatedJobExecutions(new Long(executionId));
    } else {
      return null;
    }
  }


  /**
   * Get the progress of each of the steps of the job execution for the given job instance id
   *
   * @return A list of each step and its progress, or an HTTP error code on failure
   */
  @GET
  @Path("/{executionId}/steps")
  @Produces({MediaType.APPLICATION_JSON})
  public List<ExecutedStep> getJobSteps(@PathParam("executionId") String executionId) {
    return jobRepository.getJobProgress(executionId);
  }

  /**
   * Restart the job associated with the given instance id
   *
   * @return A status message and the apropriate http status code
   */
  @POST
  @Path("/{executionId}/restart")
  @Produces({MediaType.APPLICATION_JSON})
  public ExecutedJob restartJob(@PathParam("executionId") Long executionId) throws JobExecutionException {
    ExecutedJob job = null;

    // Verify the job instance exists
    Long foundId = this.jobRepository.findJobInstanceIdForJobExecutionId(executionId);
    if (foundId != null) {
      Long newJobExecutionId = this.jobService.restartJobExecution(executionId);
      Long newJobInstanceId = null;
      if (newJobExecutionId != null) {
        job = this.jobRepository.findByExecutionId(newJobExecutionId + "");
      }
    }
    if (job == null) {
      throw new JobExecutionException("Could not find the job with execution Id of " + executionId);
    }
    return job;
  }

  /**
   * Stop the job associated with the given instance id
   *
   * @param executionId The job instance id
   * @return A status message and the apropriate http status code
   */
  @POST
  @Path("/{executionId}/stop")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
  @Produces({MediaType.APPLICATION_JSON})
  public ExecutedJob stopJob(@PathParam("executionId") Long executionId, JobAction jobAction) throws JobExecutionException {
    boolean stopped = this.jobService.stopJobExecution(executionId);
    ExecutedJob job = this.jobRepository.findByExecutionId(executionId + "");
    if (jobAction.isIncludeSteps()) {
      List<ExecutedStep> steps = jobRepository.getJobProgress(executionId + "");
      job.setExecutedSteps(steps);
    }
    return job;
  }

  /**
   * Abandon the job associated with the given instance id
   *
   * @param executionId The job instance id
   * @return A status message and the apropriate http status code
   */
  @POST
  @Path("/{executionId}/abandon")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
  @Produces({MediaType.APPLICATION_JSON})
  public ExecutedJob abandonJob(@PathParam("executionId") Long executionId, JobAction jobAction) throws JobExecutionException {

    this.jobService.abandonJobExecution(executionId);
    //abandon any previous runs as well.
    List<RelatedJobExecution> previousJobs = this.jobRepository.findPreviousJobExecutions(executionId);
    if (previousJobs != null && !previousJobs.isEmpty()) {
      for (RelatedJobExecution previousJob : previousJobs) {
        this.jobService.abandonJobExecution(previousJob.getJobExecutionId());
      }
    }

    ExecutedJob job = this.jobRepository.findByExecutionId(executionId + "");
    if (jobAction.isIncludeSteps()) {
      List<ExecutedStep> steps = jobRepository.getJobProgress(executionId + "");
      job.setExecutedSteps(steps);
    }
    return job;
  }

  /**
   * Fail the job associated with the given instance id
   *
   * @param executionId The job instance id
   * @return A status message and the apropriate http status code
   */
  @POST
  @Path("/{executionId}/fail")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
  @Produces({MediaType.APPLICATION_JSON})
  public ExecutedJob failJob(@PathParam("executionId") Long executionId, JobAction jobAction) {
    this.jobService.failJobExecution(executionId);
    //requery for status
    ExecutedJob job = this.jobRepository.findByExecutionId(executionId + "");
    if (jobAction.isIncludeSteps()) {
      List<ExecutedStep> steps = jobRepository.getJobProgress(executionId + "");
      job.setExecutedSteps(steps);
    }
    return job;
  }


  @GET
  @Path("/")
  @Produces({MediaType.APPLICATION_JSON})
  public SearchResult findJobs(@QueryParam("sort") @DefaultValue("") String sort,
                               @QueryParam("limit") @DefaultValue("10") Integer limit,
                               @QueryParam("start") @DefaultValue("1") Integer start, @Context HttpServletRequest request) {

    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    SearchResult searchResult = jobRepository.getDataTablesSearchResult(filters, null, orderByList, start, limit);
    return searchResult;

  }

  @GET
  @Path("/list")
  public List<ExecutedJob> findJobsList(@QueryParam("sort") @DefaultValue("") String sort,
                                        @QueryParam("limit") @DefaultValue("10") Integer limit,
                                        @QueryParam("start") @DefaultValue("1") Integer start,
                                        @Context HttpServletRequest request) {
    List<ExecutedJob> jobs = new ArrayList<>();
    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    jobs = jobRepository.findLatestJobs(filters, orderByList, start, limit);
    return jobs;
  }


  @GET
  @Path("/running")
  @Produces({MediaType.APPLICATION_JSON})
  public SearchResult findRunningJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                      @QueryParam("limit") @DefaultValue("10") Integer limit,
                                      @QueryParam("start") @DefaultValue("1") Integer start,
                                      @Context HttpServletRequest request) {

    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    ColumnFilter runningFilter = new QueryColumnFilterSqlString();
    runningFilter.setSqlString(" AND END_TIME IS NULL");
    filters.add(runningFilter);
    SearchResult searchResult = jobRepository.getDataTablesSearchResult(filters, null, orderByList, start, limit);
    return searchResult;

  }


  @GET
  @Path("/failed")
  @Produces({MediaType.APPLICATION_JSON})
  public SearchResult findFailedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                     @QueryParam("limit") @DefaultValue("10") Integer limit,
                                     @QueryParam("start") @DefaultValue("1") Integer start, @Context HttpServletRequest request) {

    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    ColumnFilter failedFilter = new QueryColumnFilterSqlString();
    failedFilter.setSqlString(" AND (STATUS <> 'ABANDONED' AND ( STATUS IN('FAILED','UNKNOWN') or EXIT_CODE = 'FAILED')) ");
    filters.add(failedFilter);
    SearchResult searchResult = jobRepository.getDataTablesSearchResult(filters, null, orderByList, start, limit);
    return searchResult;

  }


  @GET
  @Path("/stopped")
  @Produces({MediaType.APPLICATION_JSON})
  public SearchResult findStoppedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                      @QueryParam("limit") @DefaultValue("10") Integer limit,
                                      @QueryParam("start") @DefaultValue("1") Integer start,
                                      @Context HttpServletRequest request) {

    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    ColumnFilter failedFilter = new QueryColumnFilterSqlString();
    failedFilter.setSqlString(" AND STATUS IN('STOPPED','STOPPING') ");
    filters.add(failedFilter);
    SearchResult searchResult = jobRepository.getDataTablesSearchResult(filters, null, orderByList, start, limit);
    return searchResult;

  }

  @GET
  @Path("/completed")
  @Produces({MediaType.APPLICATION_JSON})
  public SearchResult findCompletedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                        @QueryParam("limit") @DefaultValue("10") Integer limit,
                                        @QueryParam("start") @DefaultValue("1") Integer start,
                                        @Context HttpServletRequest request) {

    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    ColumnFilter failedFilter = new QueryColumnFilterSqlString();
    failedFilter.setSqlString(" AND STATUS = 'COMPLETED' AND EXIT_CODE = 'COMPLETED' ");
    filters.add(failedFilter);
    SearchResult searchResult = jobRepository.getDataTablesSearchResult(filters, null, orderByList, start, limit);
    return searchResult;

  }


  @GET
  @Path("/abandoned")
  @Produces({MediaType.APPLICATION_JSON})
  public SearchResult findAbandonedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                        @QueryParam("limit") @DefaultValue("10") Integer limit,
                                        @QueryParam("start") @DefaultValue("1") Integer start,
                                        @Context HttpServletRequest request) {

    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    ColumnFilter runningFilter = new QueryColumnFilterSqlString();
    runningFilter.setSqlString(" AND STATUS = 'ABANDONED'");
    filters.add(runningFilter);
    SearchResult searchResult = jobRepository.getDataTablesSearchResult(filters, null, orderByList, start, limit);
    return searchResult;

  }


  @GET
  @Path("/since/{timeframe}")
  @Produces({MediaType.APPLICATION_JSON})
  public SearchResult findJobActivity(@PathParam("timeframe") String timeframe, @QueryParam("sort") @DefaultValue("") String sort,
                                      @QueryParam("limit") @DefaultValue("10") Integer limit,
                                      @QueryParam("start") @DefaultValue("1") Integer start,
                                      @Context HttpServletRequest request) {

    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    if (org.apache.commons.lang3.StringUtils.isNotBlank(timeframe)) {
      DatabaseQuerySubstitution.DATE_PART datePart = DatabaseQuerySubstitution.DATE_PART.valueOf(timeframe);
      if (datePart != null) {
        ColumnFilter filter = new QueryColumnFilterSqlString();
        String filterName = JobQueryConstants.DAY_DIFF_FROM_NOW;
        switch (datePart) {
          case DAY:
            filterName = JobQueryConstants.DAY_DIFF_FROM_NOW;
          case WEEK:
            filterName = JobQueryConstants.WEEK_DIFF_FROM_NOW;
          case MONTH:
            filterName = JobQueryConstants.MONTH_DIFF_FROM_NOW;
          case YEAR:
            filterName = JobQueryConstants.YEAR_DIFF_FROM_NOW;

        }
        filter.setName(filterName);


      }
    }
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.JOB);
    SearchResult searchResult = jobRepository.getDataTablesSearchResult(filters, null, orderByList, start, limit);
    return searchResult;
  }


  @GET
  @Path("/daily-status-count/{timeframe}")
  @Produces({MediaType.APPLICATION_JSON})
  public List<JobStatusCount> findDailyStatusCount(@PathParam("timeframe") String timeframe) {
    return findDailyStatusCount(timeframe, 1);
  }


  @GET
  @Path("/daily-status-count/{timeframe}/{amount}")
  @Produces({MediaType.APPLICATION_JSON})
  public List<JobStatusCount> findDailyStatusCount(@PathParam("timeframe") String timeframe,
                                                   @PathParam("amount") Integer amount) {
    DatabaseQuerySubstitution.DATE_PART datePart = DatabaseQuerySubstitution.DATE_PART.DAY;
    if (org.apache.commons.lang3.StringUtils.isNotBlank(timeframe)) {
      try {
        datePart = DatabaseQuerySubstitution.DATE_PART.valueOf(timeframe.toUpperCase());
      } catch (IllegalArgumentException e) {

      }
    }
    List<JobStatusCount> list = jobRepository.getJobStatusCountByDay(datePart, amount);
    return list;
  }


  @GET
  @Path("/running-failed-counts")
  @Produces({MediaType.APPLICATION_JSON})
  public List<JobStatusCount> getRunningOrFailedJobCounts() {
    return jobRepository.getRunningOrFailedJobCounts();
  }


  private Map<String, Object> convertJsonToJobParametersMap(final JsonNode json) {
    Map<String, Object> params = new HashMap<String, Object>();

    for (JsonNode parameter : json) {
      String key = parameter.path("name").asText();
      if (parameter.path("type").textValue().equalsIgnoreCase("string") ||
          (parameter.path("type").textValue().equalsIgnoreCase("uuid"))) {
        params.put(key, parameter.path("value").asText());
      } else if (parameter.path("type").textValue().equalsIgnoreCase("number")) {
        params.put(key, Long.valueOf(parameter.path("value").asText()));
      } else if (parameter.path("type").textValue().equalsIgnoreCase("date")) {
        params.put(key, new DateTime(parameter.path("value").asText()).toDate());
      }
    }
    return params;
  }


  @POST
  @Path("/")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
  @Produces({MediaType.APPLICATION_JSON})
  public ExecutedJob createJob(String job) throws Exception {
    ExecutedJob executedJob = null;

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(job);
    if (json.path("jobName").isMissingNode() || json.path("jobParameters").isMissingNode()) {
      throw new IllegalArgumentException("jobName and jobParameters are required");
    } else {
      String jobName = json.path("jobName").asText();
      Map<String, Object> jobParameters = convertJsonToJobParametersMap(json.path("jobParameters"));
      executedJob = this.jobService.createJob(jobName, jobParameters);
    }
    return executedJob;
  }


  @GET
  @Path("/{executionId}/parameters")
  @Produces({MediaType.APPLICATION_JSON})
  public List<JobParameterType> jobParameters(@PathParam("executionId") Long executionId) {

    List<JobParameterType> jobParameters = jobService.getJobParametersForJob(executionId);
    return jobParameters;
  }

  /**
   * Get all of the unique job names of any previously run job
   *
   * @return A list of unique job names, or an HTTP error code on failure
   */
  @ApiOperation(value = "/api/v1/jobs/names", notes = "Returns a list of unique Job Names for any previously run job.")
  @GET
  @Path("/names")
  @Produces({MediaType.APPLICATION_JSON})
  public List<String> jobNameValues() {
    return jobRepository.uniqueJobNames();
  }

}
