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

import com.thinkbiganalytics.alerts.rest.model.AlertRange;
import com.thinkbiganalytics.alerts.rest.model.AlertSummaryGrouped;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.DataConfidenceSummary;
import com.thinkbiganalytics.jobrepo.query.model.FeedStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.metadata.cache.CacheService;
import com.thinkbiganalytics.metadata.cache.CategoryFeedService;
import com.thinkbiganalytics.metadata.cache.Dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Api(tags = "Operations Manager - Dashboard", produces = "application/json")
@Path("/v1/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Inject
    private CacheService cacheService;

    @Inject
    private CategoryFeedService categoryFeedService;


    @GET
    @Path("/data-confidence/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the data confidence metrics.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the data confidence metrics.", response = DataConfidenceSummary.class)
    )
    public DataConfidenceSummary getDashbardDataConfidenceSummary() {
        List<CheckDataJob> checkDataJobs = cacheService.getUserDataConfidenceJobs();
        return new DataConfidenceSummary(checkDataJobs, 60);
    }


    @GET
    @Path("/feeds/feed-name/{feedName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Provides a detailed health status of every feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the health.", response = FeedStatus.class)
    )
    public FeedStatus getFeedHealth(@PathParam("feedName") String feedName) {
        return cacheService.getUserFeedHealth(feedName);
    }


    @GET
    @Path("/feeds")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Provides a detailed health status of every feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the health.", response = FeedStatus.class)
    )
    public FeedStatus getFeedHealth(@Context HttpServletRequest request) {
        return cacheService.getUserFeedHealth();
    }


    @GET
    @Path("/running-jobs")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the daily statistics.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the stats.", response = JobStatusCount.class, responseContainer = "List")
    )
    public List<JobStatusCount> getRunningJobCounts() {
        return cacheService.getUserRunningJobs();
    }

    //TODO get Service status


    @GET
    @Path("/alerts")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists alerts")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns summary of the alerts grouped.", response = AlertRange.class)
    )
    public Collection<AlertSummaryGrouped> getAlertSummaryUnhandled() {
        return cacheService.getUserAlertSummary();
    }

    @GET
    @Path("/alerts/feed-id/{feedId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists alerts")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns summary of the alerts grouped.", response = AlertRange.class)
    )
    public Collection<AlertSummaryGrouped> getUserAlertSummaryForFeedId(@PathParam("feedId") String feedId) {
        return cacheService.getUserAlertSummaryForFeedId(feedId);
    }

    @GET
    @Path("/alerts/feed-name/{feedName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists alerts")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns summary of the alerts grouped.", response = AlertRange.class)
    )
    public Collection<AlertSummaryGrouped> getUserAlertSummaryForFeedName(@PathParam("feedName") String feedName) {
        return cacheService.getUserAlertSummaryForFeedName(feedName);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("get dashboard")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns summary of the alerts grouped.", response = AlertRange.class)
    )
    public Dashboard getDashboard() {
        return cacheService.getDashboard();
    }


}
