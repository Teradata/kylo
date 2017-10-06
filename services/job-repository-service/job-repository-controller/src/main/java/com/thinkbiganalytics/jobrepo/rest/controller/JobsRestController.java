package com.thinkbiganalytics.jobrepo.rest.controller;

/*-
 * #%L
 * thinkbig-job-repository-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.transform.JobModelTransform;
import com.thinkbiganalytics.jobrepo.query.model.transform.JobStatusTransform;
import com.thinkbiganalytics.jobrepo.query.model.transform.ModelUtils;
import com.thinkbiganalytics.jobrepo.repository.rest.model.JobAction;
import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.jobrepo.service.JobExecutionException;
import com.thinkbiganalytics.jobrepo.service.JobService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecutionProvider;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.security.AccessController;

import org.joda.time.Period;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Provides rest endpoints for control and monitoring of the pipeline
 */
@Api(tags = "Operations Manager - Jobs", produces = "application/json")
@Path(JobsRestController.BASE)
public class JobsRestController {

    public static final String BASE = "/v1/jobs";

    @Inject
    OpsManagerFeedProvider opsFeedManagerFeedProvider;

    @Inject
    BatchJobExecutionProvider jobExecutionProvider;

    @Inject
    BatchStepExecutionProvider stepExecutionProvider;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private JobService jobService;

    @Inject
    private AccessController accessController;

    @Inject
    private NifiFeedStatisticsProvider nifiFeedStatisticsProvider;

    @GET
    @Path("/{executionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the specified job.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the job.", response = ExecutedJob.class),
                      @ApiResponse(code = 400, message = "The executionId is not a valid integer.", response = RestResponseStatus.class)
                  })
    public ExecutedJob getJob(@PathParam("executionId") String executionId,
                              @QueryParam(value = "includeSteps") @DefaultValue("false") boolean includeSteps) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            ExecutedJob executedJob = null;
            BatchJobExecution jobExecution = jobExecutionProvider.findByJobExecutionId(Long.parseLong(executionId));
            if (jobExecution != null) {
                if (includeSteps) {
                    executedJob = JobModelTransform.executedJob(jobExecution);
                } else {
                    executedJob = JobModelTransform.executedJobSimple(jobExecution);
                }
            }
            return executedJob;
        });

    }

    /**
     * Restart the job associated with the given instance id
     *
     * @return A status message and the appropriate http status code
     */
    @POST
    @Path("/{executionId}/restart")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Restarts the specified job.", hidden = true)
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the job.", response = ExecutedJob.class),
                      @ApiResponse(code = 404, message = "The executionId is not a valid integer.", response = RestResponseStatus.class)
                  })
    public ExecutedJob restartJob(@PathParam("executionId") Long executionId) throws JobExecutionException {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        ExecutedJob job = metadataAccess.commit(() -> {
            Long newJobExecutionId = this.jobService.restartJobExecution(executionId);
            if (newJobExecutionId != null) {
                BatchJobExecution jobExecution = jobExecutionProvider.findByJobExecutionId(newJobExecutionId);
                if (jobExecution != null) {
                    return JobModelTransform.executedJob(jobExecution);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        });
        if (job == null) {
            throw new JobExecutionException("Could not restart the job with execution Id of " + executionId);
        }
        return job;
    }

    /**
     * Stop the job associated with the given instance id
     *
     * @param executionId The job instance id
     * @return A status message and the appropriate http status code
     */
    @POST
    @Path("/{executionId}/stop")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Stops the specified job.", hidden = true)
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the job.", response = ExecutedJob.class),
                      @ApiResponse(code = 404, message = "The executionId is not a valid integer.", response = RestResponseStatus.class)
                  })
    public ExecutedJob stopJob(@PathParam("executionId") Long executionId, JobAction jobAction) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        metadataAccess.commit(() -> {
            boolean stopped = this.jobService.stopJobExecution(executionId);
            return stopped;
        });
        return getJob(executionId.toString(), jobAction.isIncludeSteps());
    }

    /**
     * Abandon the job associated with the given instance id
     *
     * @param executionId The job instance id
     * @return A status message and the appropriate http status code
     */
    @POST
    @Path("/{executionId}/abandon")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Abandons the specified job.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the abandoned job.", response = ExecutedJob.class),
                      @ApiResponse(code = 204, message = "The job could not be found."),
                      @ApiResponse(code = 404, message = "The executionId is not a valid integer.", response = RestResponseStatus.class)
                  })
    public ExecutedJob abandonJob(@PathParam("executionId") Long executionId, JobAction jobAction) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        metadataAccess.commit(() -> {
            this.jobService.abandonJobExecution(executionId);
            return null;
        });
        return getJob(executionId.toString(), jobAction.isIncludeSteps());
    }

    /**
     * Abandon the job associated with the given instance id
     *
     * @param feedName Full feed name (including category) for which all jobs are to be abandoned
     * @return Feed Health status
     */
    @POST
    @Path("/abandon-all/{feedName}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Abandons all jobs for the specified feed.")
    @ApiResponses(
        @ApiResponse(code = 204, message = "Return no content", response = Void.class)
    )
    public Response abandonAllJobs(@Context HttpServletRequest request, @PathParam("feedName") String feedName) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);

        metadataAccess.commit(() -> {
            opsFeedManagerFeedProvider.abandonFeedJobs(feedName);
        });

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * Fail the job associated with the given instance id
     *
     * @param executionId The job instance id
     * @return A status message and the appropriate http status code
     */
    @POST
    @Path("/{executionId}/fail")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Fails the specified job.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the job.", response = ExecutedJob.class),
                      @ApiResponse(code = 404, message = "The executionId is not a valid integer.", response = RestResponseStatus.class)
                  })
    public ExecutedJob failJob(@PathParam("executionId") Long executionId, JobAction jobAction) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        metadataAccess.commit(() -> {
            this.jobService.failJobExecution(executionId);
            return null;
        });
        return getJob(executionId.toString(), jobAction.isIncludeSteps());
    }


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists all jobs.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the jobs.", response = SearchResult.class),
                      @ApiResponse(code = 400, message = "The sort cannot be empty.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The start or limit is not a valid integer.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The sort contains an invalid value.", response = RestResponseStatus.class)
                  })
    public SearchResult findJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                 @QueryParam("limit") @DefaultValue("10") Integer limit,
                                 @QueryParam("start") @DefaultValue("1") Integer start,
                                 @QueryParam("filter") String filter,
                                 @Context HttpServletRequest request) {
        return metadataAccess.read(() -> {
            Page<ExecutedJob> page = jobExecutionProvider.findAll(filter, QueryUtils.pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });


    }

    @GET
    @Path("/list")
    @ApiOperation("Lists all jobs.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the jobs.", response = SearchResult.class),
                      @ApiResponse(code = 400, message = "The sort cannot be empty.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The start or limit is not a valid integer.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The sort contains an invalid value.", response = RestResponseStatus.class)
                  })
    public List<ExecutedJob> findJobsList(@QueryParam("sort") @DefaultValue("") String sort,
                                          @QueryParam("limit") @DefaultValue("10") Integer limit,
                                          @QueryParam("start") @DefaultValue("1") Integer start,
                                          @QueryParam("filter") String filter,
                                          @Context HttpServletRequest request) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            Page<ExecutedJob> page = jobExecutionProvider.findAll(filter, QueryUtils.pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return page != null ? page.getContent() : Collections.emptyList();
        });
    }


    @GET
    @Path("/running")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of running jobs.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the jobs.", response = SearchResult.class),
                      @ApiResponse(code = 400, message = "The sort cannot be empty.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The start or limit is not a valid integer.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The sort contains an invalid value.", response = RestResponseStatus.class)
                  })
    public SearchResult findRunningJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                        @QueryParam("limit") @DefaultValue("10") Integer limit,
                                        @QueryParam("start") @DefaultValue("1") Integer start,
                                        @QueryParam("filter") String filter,
                                        @Context HttpServletRequest request) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            String defaultFilter = QueryUtils.ensureDefaultFilter(filter, jobExecutionProvider.RUNNING_FILTER);
            Page<ExecutedJob> page = jobExecutionProvider.findAll(defaultFilter, QueryUtils.pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });

    }


    @GET
    @Path("/failed")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of failed jobs.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the jobs.", response = SearchResult.class),
                      @ApiResponse(code = 400, message = "The sort cannot be empty.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The start or limit is not a valid integer.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The sort contains an invalid value.", response = RestResponseStatus.class)
                  })
    public SearchResult findFailedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                       @QueryParam("limit") @DefaultValue("10") Integer limit,
                                       @QueryParam("start") @DefaultValue("1") Integer start,
                                       @QueryParam("filter") String filter,
                                       @Context HttpServletRequest request) {

        return metadataAccess.read(() -> {
            String defaultFilter = QueryUtils.ensureDefaultFilter(filter, jobExecutionProvider.FAILED_FILTER);
            Page<ExecutedJob> page = jobExecutionProvider.findAll(defaultFilter, QueryUtils.pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });
    }


    @GET
    @Path("/stopped")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of failed jobs.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the jobs.", response = SearchResult.class),
                      @ApiResponse(code = 400, message = "The sort cannot be empty.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The start or limit is not a valid integer.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The sort contains an invalid value.", response = RestResponseStatus.class)
                  })
    public SearchResult findStoppedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                        @QueryParam("limit") @DefaultValue("10") Integer limit,
                                        @QueryParam("start") @DefaultValue("1") Integer start,
                                        @QueryParam("filter") String filter,
                                        @Context HttpServletRequest request) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            String defaultFilter = QueryUtils.ensureDefaultFilter(filter, jobExecutionProvider.STOPPED_FILTER);
            Page<ExecutedJob> page = jobExecutionProvider.findAll(defaultFilter, QueryUtils.pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });

    }

    @GET
    @Path("/completed")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of completed jobs.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the jobs.", response = SearchResult.class),
                      @ApiResponse(code = 400, message = "The sort cannot be empty.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The start or limit is not a valid integer.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The sort contains an invalid value.", response = RestResponseStatus.class)
                  })
    public SearchResult findCompletedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                          @QueryParam("limit") @DefaultValue("10") Integer limit,
                                          @QueryParam("start") @DefaultValue("1") Integer start,
                                          @QueryParam("filter") String filter,
                                          @Context HttpServletRequest request) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            String defaultFilter = QueryUtils.ensureDefaultFilter(filter, jobExecutionProvider.COMPLETED_FILTER);
            Page<ExecutedJob> page = jobExecutionProvider.findAll(defaultFilter, QueryUtils.pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });

    }


    @GET
    @Path("/abandoned")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of abandoned jobs.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the jobs.", response = SearchResult.class),
                      @ApiResponse(code = 400, message = "The sort cannot be empty.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The start or limit is not a valid integer.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The sort contains an invalid value.", response = RestResponseStatus.class)
                  })
    public SearchResult findAbandonedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                          @QueryParam("limit") @DefaultValue("10") Integer limit,
                                          @QueryParam("start") @DefaultValue("1") Integer start,
                                          @QueryParam("filter") String filter,
                                          @Context HttpServletRequest request) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            String defaultFilter = QueryUtils.ensureDefaultFilter(filter, jobExecutionProvider.ABANDONED_FILTER);
            Page<ExecutedJob> page = jobExecutionProvider.findAll(defaultFilter, QueryUtils.pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });
    }


    @GET
    @Path("/daily-status-count")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the daily statistics.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the daily stats.", response = JobStatusCount.class, responseContainer = "List")
    )
    public List<JobStatusCount> findDailyStatusCount(@QueryParam("period") String periodString) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        Period period = DateTimeUtil.period(periodString);
        return metadataAccess.read(() -> {
            List<com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount> counts = jobExecutionProvider.getJobStatusCountByDateFromNow(period, null);
            if (counts != null) {
                List<JobStatusCount> jobStatusCounts = counts.stream().map(c -> JobStatusTransform.jobStatusCount(c)).collect(Collectors.toList());
                JobStatusTransform.ensureDateFromPeriodExists(jobStatusCounts, period);
                return jobStatusCounts;
            }
            return Collections.emptyList();
        });
    }


    @GET
    @Path("/running-failed-counts")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the daily statistics.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the stats.", response = JobStatusCount.class, responseContainer = "List")
    )
    public List<JobStatusCount> getRunningOrFailedJobCounts() {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            List<com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount> counts = jobExecutionProvider.getJobStatusCount(jobExecutionProvider.RUNNING_OR_FAILED_FILTER);
            if (counts != null) {
                return counts.stream().map(c -> JobStatusTransform.jobStatusCount(c)).collect(Collectors.toList());
            }
            return Collections.emptyList();
        });

    }


}
