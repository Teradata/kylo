package com.thinkbiganalytics.jobrepo.rest.controller;

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.RelatedJobExecution;
import com.thinkbiganalytics.jobrepo.query.model.SearchResult;
import com.thinkbiganalytics.jobrepo.repository.rest.model.JobAction;
import com.thinkbiganalytics.jobrepo.query.model.transform.JobModelTransform;
import com.thinkbiganalytics.jobrepo.query.model.transform.JobStatusTransform;
import com.thinkbiganalytics.jobrepo.query.model.transform.ModelUtils;
import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.jobrepo.service.JobExecutionException;
import com.thinkbiganalytics.jobrepo.service.JobService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecutionProvider;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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

import io.swagger.annotations.Api;

/**
 * Provides rest endpoints for control and monitoring of the pipeline
 */
@Api(value = "jobs", produces = "application/json")
@Path("/v1/jobs")
public class JobsRestController {

    private static final Logger LOG = LoggerFactory.getLogger(JobsRestController.class);


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


    @GET
    @Path("/{executionId}")
    @Produces({MediaType.APPLICATION_JSON})
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

    @GET
    @Path("/{executionId}/related")
    @Produces({MediaType.APPLICATION_JSON})
    public List<RelatedJobExecution> getRelatedJobExecutions(@PathParam("executionId") String executionId) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        //TODO IMPLEMENT

        return null;
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
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            List<? extends BatchStepExecution> steps = stepExecutionProvider.getSteps(Long.parseLong(executionId));
            return JobModelTransform.executedSteps(steps);
        });
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
     * @return A status message and the apropriate http status code
     */
    @POST
    @Path("/{executionId}/stop")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
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
     * @return A status message and the apropriate http status code
     */
    @POST
    @Path("/{executionId}/abandon")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public ExecutedJob abandonJob(@PathParam("executionId") Long executionId, JobAction jobAction) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        metadataAccess.commit(() -> {
            this.jobService.abandonJobExecution(executionId);
            return null;
        });
            return getJob(executionId.toString(), jobAction.isIncludeSteps());
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
    @Produces({MediaType.APPLICATION_JSON})
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
    @Produces({MediaType.APPLICATION_JSON})
    public SearchResult findJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                 @QueryParam("limit") @DefaultValue("10") Integer limit,
                                 @QueryParam("start") @DefaultValue("1") Integer start,
                                 @QueryParam("filter") String filter,
                                 @Context HttpServletRequest request) {
        return metadataAccess.read(() -> {
            Page<ExecutedJob> page = jobExecutionProvider.findAll(filter, pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });


    }

    @GET
    @Path("/list")
    public List<ExecutedJob> findJobsList(@QueryParam("sort") @DefaultValue("") String sort,
                                          @QueryParam("limit") @DefaultValue("10") Integer limit,
                                          @QueryParam("start") @DefaultValue("1") Integer start,
                                          @QueryParam("filter") String filter,
                                          @Context HttpServletRequest request) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            Page<ExecutedJob> page = jobExecutionProvider.findAll(filter, pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return page != null ? page.getContent() : Collections.emptyList();
        });
    }


    @GET
    @Path("/running")
    @Produces({MediaType.APPLICATION_JSON})
    public SearchResult findRunningJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                        @QueryParam("limit") @DefaultValue("10") Integer limit,
                                        @QueryParam("start") @DefaultValue("1") Integer start,
                                        @QueryParam("filter") String filter,
                                        @Context HttpServletRequest request) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            String defaultFilter = ensureDefaultFilter(filter, jobExecutionProvider.RUNNING_FILTER);
            Page<ExecutedJob> page = jobExecutionProvider.findAll(defaultFilter, pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });

    }


    @GET
    @Path("/failed")
    @Produces({MediaType.APPLICATION_JSON})
    public SearchResult findFailedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                       @QueryParam("limit") @DefaultValue("10") Integer limit,
                                       @QueryParam("start") @DefaultValue("1") Integer start,
                                       @QueryParam("filter") String filter,
                                       @Context HttpServletRequest request) {

        return metadataAccess.read(() -> {
            String defaultFilter = ensureDefaultFilter(filter, jobExecutionProvider.FAILED_FILTER);
            Page<ExecutedJob> page = jobExecutionProvider.findAll(defaultFilter, pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });
    }


    @GET
    @Path("/stopped")
    @Produces({MediaType.APPLICATION_JSON})
    public SearchResult findStoppedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                        @QueryParam("limit") @DefaultValue("10") Integer limit,
                                        @QueryParam("start") @DefaultValue("1") Integer start,
                                        @QueryParam("filter") String filter,
                                        @Context HttpServletRequest request) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            String defaultFilter = ensureDefaultFilter(filter, jobExecutionProvider.STOPPED_FILTER);
            Page<ExecutedJob> page = jobExecutionProvider.findAll(defaultFilter, pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });

    }

    @GET
    @Path("/completed")
    @Produces({MediaType.APPLICATION_JSON})
    public SearchResult findCompletedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                          @QueryParam("limit") @DefaultValue("10") Integer limit,
                                          @QueryParam("start") @DefaultValue("1") Integer start,
                                          @QueryParam("filter") String filter,
                                          @Context HttpServletRequest request) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            String defaultFilter = ensureDefaultFilter(filter, jobExecutionProvider.COMPLETED_FILTER);
            Page<ExecutedJob> page = jobExecutionProvider.findAll(defaultFilter, pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });

    }


    @GET
    @Path("/abandoned")
    @Produces({MediaType.APPLICATION_JSON})
    public SearchResult findAbandonedJobs(@QueryParam("sort") @DefaultValue("") String sort,
                                          @QueryParam("limit") @DefaultValue("10") Integer limit,
                                          @QueryParam("start") @DefaultValue("1") Integer start,
                                          @QueryParam("filter") String filter,
                                          @Context HttpServletRequest request) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        return metadataAccess.read(() -> {
            String defaultFilter = ensureDefaultFilter(filter, jobExecutionProvider.ABANDONED_FILTER);
            Page<ExecutedJob> page = jobExecutionProvider.findAll(defaultFilter, pageRequest(start, limit, sort)).map(jobExecution -> JobModelTransform.executedJobSimple(jobExecution));
            return ModelUtils.toSearchResult(page);
        });
    }


    @GET
    @Path("/daily-status-count")
    @Produces({MediaType.APPLICATION_JSON})
    public List<JobStatusCount> findDailyStatusCount(@QueryParam("period") String periodString) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);

        Period period = DateTimeUtil.period(periodString);
        return metadataAccess.read(() -> {
            List<com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount> counts = jobExecutionProvider.getJobStatusCountByDateFromNow(period, null);
            if (counts != null) {
                return counts.stream().map(c -> JobStatusTransform.jobStatusCount(c)).collect(Collectors.toList());
            }
            return Collections.emptyList();
        });
    }


    @GET
    @Path("/running-failed-counts")
    @Produces({MediaType.APPLICATION_JSON})
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

    /**
     * This will evaluate the {@code incomingFilter} and append/set the value including the {@code defaultFilter} and return a new String with the updated filter
     */
    private String ensureDefaultFilter(String incomingFilter, String defaultFilter) {
        String filter = incomingFilter;
        if (StringUtils.isBlank(filter) || !StringUtils.containsIgnoreCase(filter, defaultFilter)) {
            if (StringUtils.isNotBlank(filter)) {
                if (StringUtils.endsWith(filter, ",")) {
                    filter += defaultFilter;
                } else {
                    filter += "," + defaultFilter;
                }
            } else {
                filter = defaultFilter;
            }
        }
        return filter;
    }

    private PageRequest pageRequest(Integer start, Integer limit, String sort) {
        if (sort != null) {
            Sort.Direction dir = Sort.Direction.ASC;
            if (sort.startsWith("-")) {
                dir = Sort.Direction.DESC;
                sort = sort.substring(1);
            }
            return new PageRequest((start / limit), limit, dir, sort);
        } else {
            return new PageRequest((start / limit), limit);
        }
    }

}
