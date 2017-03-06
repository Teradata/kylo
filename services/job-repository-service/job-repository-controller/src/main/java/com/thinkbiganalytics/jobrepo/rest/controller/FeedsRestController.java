package com.thinkbiganalytics.jobrepo.rest.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.joda.time.Period;

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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;
import com.thinkbiganalytics.jobrepo.query.model.FeedStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.transform.FeedModelTransform;
import com.thinkbiganalytics.jobrepo.query.model.transform.JobStatusTransform;
import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.security.AccessController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Operations Manager - Feeds", produces = "application/json")
@Path("/v1/feeds")
public class FeedsRestController {

    @Inject
    OpsManagerFeedProvider opsFeedManagerFeedProvider;

    @Inject
    BatchJobExecutionProvider batchJobExecutionProvider;

    @Inject
    private AccessController accessController;

    @Inject
    private MetadataAccess metadataAccess;

    @GET
    @Path("/{feedName}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the latest execution of the specified feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the latest execution.", response = ExecutedFeed.class)
    )
    public ExecutedFeed findLatestFeedsByName(@PathParam("feedName") String feedName, @Context HttpServletRequest request) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {

            BatchJobExecution latestJob = batchJobExecutionProvider.findLatestCompletedJobForFeed(feedName);
            OpsManagerFeed feed = opsFeedManagerFeedProvider.findByName(feedName);
            return FeedModelTransform.executedFeed(latestJob, feed);
        });
    }

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Provides a detailed health status of every feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the health.", response = FeedStatus.class)
    )
    public FeedStatus getFeedHealth(@Context HttpServletRequest request) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            List<FeedHealth> feedHealth = getFeedHealthCounts(request);
            FeedStatus status = FeedModelTransform.feedStatus(feedHealth);
            return status;
        });
    }

    @GET
    @Path("/health-count")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Provides a summarized health status of every feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the health.", response = FeedHealth.class, responseContainer = "List")
    )
    public List<FeedHealth> getFeedHealthCounts(@Context HttpServletRequest request) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            List<? extends com.thinkbiganalytics.metadata.api.feed.FeedHealth> feedHealthList = opsFeedManagerFeedProvider.getFeedHealth();
            //Transform to list
            return FeedModelTransform.feedHealth(feedHealthList);
        });
    }

    @GET
    @Path("/health-count/{feedName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets a health summary for the specified feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the health.", response = FeedHealth.class)
    )
    public FeedHealth getFeedHealthCounts(@Context HttpServletRequest request, @PathParam("feedName") String feedName) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            com.thinkbiganalytics.metadata.api.feed.FeedHealth feedHealth = opsFeedManagerFeedProvider.getFeedHealth(feedName);
            if (feedHealth != null) {
                return FeedModelTransform.feedHealth(feedHealth);
            } else {
                return null;
            }
        });
    }

    @GET
    @Path("/health/{feedName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the detailed health status of the specified feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the health.", response = FeedStatus.class)
    )
    public FeedStatus getFeedHealthForFeed(@Context HttpServletRequest request, @PathParam("feedName") String feedName) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            FeedHealth feedHealth = getFeedHealthCounts(request, feedName);
            if (feedHealth != null) {
                return FeedModelTransform.feedStatus(Lists.newArrayList(feedHealth));
            } else {
                return null;
            }
        });
    }

    @GET
    @Path("/{feedName}/daily-status-count")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets a daily health summary for the specified feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the health.", response = JobStatusCount.class, responseContainer = "List")
    )
    public List<JobStatusCount> findFeedDailyStatusCount(@PathParam("feedName") String feedName,
                                                         @QueryParam("period") String periodString) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            Period period = DateTimeUtil.period(periodString);
            List<com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount> counts = opsFeedManagerFeedProvider.getJobStatusCountByDateFromNow(feedName, period);
            if (counts != null) {
                List<JobStatusCount> jobStatusCounts = counts.stream().map(c -> JobStatusTransform.jobStatusCount(c)).collect(Collectors.toList());
                JobStatusTransform.ensureDateFromPeriodExists(jobStatusCounts, period);
                return jobStatusCounts;
            } else {
                return Collections.emptyList();
            }
        });
    }

    @GET
    @Path("/names")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the name of every feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the feed names.", response = String.class, responseContainer = "List")
    )
    public List<String> getFeedNames() {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> opsFeedManagerFeedProvider.getFeedNames());
    }
}
