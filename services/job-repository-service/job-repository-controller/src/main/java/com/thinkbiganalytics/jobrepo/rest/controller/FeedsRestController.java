package com.thinkbiganalytics.jobrepo.rest.controller;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.alerts.rest.AlertsModel;
import com.thinkbiganalytics.alerts.rest.model.AlertSummaryGrouped;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;
import com.thinkbiganalytics.jobrepo.query.model.FeedStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.transform.FeedModelTransform;
import com.thinkbiganalytics.jobrepo.query.model.transform.JobStatusTransform;
import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.metadata.alerts.KyloEntityAwareAlertManager;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.api.sla.ServiceLevelAgreementDescriptionProvider;
import com.thinkbiganalytics.metadata.jpa.feed.OpsFeedManagerFeedProvider;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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

    @Inject
    private ServiceLevelAgreementDescriptionProvider serviceLevelAgreementDescriptionProvider;


    @Inject
    private AlertProvider alertProvider;

    @Inject
    private KyloEntityAwareAlertManager kyloEntityAwareAlertService;

    @Inject
    private AlertsModel alertsModel;

    @GET
    @Path("/{feedName}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the latest execution of the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the latest execution.", response = ExecutedFeed.class),
                      @ApiResponse(code = 400, message = "The feed does not exist or has no jobs.", response = RestResponseStatus.class)
                  })
    public ExecutedFeed findLatestFeedsByName(@PathParam("feedName") String feedName, @Context HttpServletRequest request) {
        accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            final BatchJobExecution latestJob = batchJobExecutionProvider.findLatestCompletedJobForFeed(feedName);
            if (latestJob != null) {
                final OpsManagerFeed feed = opsFeedManagerFeedProvider.findByName(feedName);
                return FeedModelTransform.executedFeed(latestJob, feed);
            } else {
                throw new NotFoundException();
            }
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
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the health.", response = FeedHealth.class),
                      @ApiResponse(code = 404, message = "The feed does not exist.", response = RestResponseStatus.class)
                  })
    public FeedHealth getFeedHealthCounts(@Context HttpServletRequest request, @PathParam("feedName") String feedName) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            final com.thinkbiganalytics.metadata.api.feed.FeedHealth feedHealth = opsFeedManagerFeedProvider.getFeedHealth(feedName);
            if (feedHealth != null) {
                return FeedModelTransform.feedHealth(feedHealth);
            }

            final OpsManagerFeed feed = opsFeedManagerFeedProvider.findByName(feedName);
            if (feed != null) {
                return FeedModelTransform.feedHealth(feed);
            } else {
                throw new NotFoundException();
            }
        });
    }

    @GET
    @Path("/health/{feedName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the detailed health status of the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the health.", response = FeedStatus.class),
                      @ApiResponse(code = 404, message = "The feed does not exist.", response = RestResponseStatus.class)
                  })
    public FeedStatus getFeedHealthForFeed(@Context HttpServletRequest request, @PathParam("feedName") String feedName) {

        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            final FeedHealth feedHealth = getFeedHealthCounts(request, feedName);
            if (feedHealth != null) {
                return FeedModelTransform.feedStatus(Lists.newArrayList(feedHealth));
            }

            final OpsManagerFeed feed = opsFeedManagerFeedProvider.findByName(feedName);
            if (feed != null) {
                return FeedModelTransform.feedStatus(feed);
            } else {
                throw new NotFoundException();
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


    @GET
    @Path("/query/{feedId}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Gets the name of the feed matching the feedId.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the feed name.", response = String.class)
    )
    public String getFeedName(@PathParam("feedId") String feedId) {
        return metadataAccess.read(() -> {
            String filter="id.uuid=="+feedId;
            List<OpsManagerFeed> feeds = ((OpsFeedManagerFeedProvider)opsFeedManagerFeedProvider).findFeedsWithFilter(filter);
            if(feeds != null){
                return feeds.stream().map(f->f.getName()).collect(Collectors.joining(","));
            }
            return "NOT FOUND";
        });
    }



    /**
     * Get alerts associated to the feed
     * Combine both SLA and Feed alerts
     * @param feedId
     * @return
     */
    @GET
    @Path("/{feedName}/alerts")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the name of every feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the feed names.", response = String.class, responseContainer = "List")
    )
    public Collection<AlertSummaryGrouped> getFeedAlerts(@PathParam("feedName") String feedName,@QueryParam("feedId") String feedId){
        return getAlerts(feedName,feedId);
    }

    private Collection<AlertSummaryGrouped> getAlerts(final String feedName, final String feedId){
       return  metadataAccess.read(() -> {

           String derivedFeedId = feedId;
            //get necessary feed info
            if (StringUtils.isBlank(feedId) && StringUtils.isNotBlank(feedName)) {
                //get the feedId for this feed name
                OpsManagerFeed feed = opsFeedManagerFeedProvider.findByName(feedName);
                if (feed != null) {
                    derivedFeedId = feed.getId().toString();
                }
            }

            if (StringUtils.isBlank(derivedFeedId)) {
                return Collections.emptyList();
            }

            List<? extends ServiceLevelAgreementDescription> slas = serviceLevelAgreementDescriptionProvider.findForFeed(opsFeedManagerFeedProvider.resolveId(derivedFeedId));
            List<String> slaIds = new ArrayList<>();
            if (slas != null && !slas.isEmpty()) {
                slaIds = slas.stream().map(sla -> sla.getSlaId().toString()).collect(Collectors.toList());
            }
            List<String> ids = new ArrayList<>();
            ids.addAll(slaIds);
            ids.add(derivedFeedId);
            String filter = ids.stream().collect(Collectors.joining("||"));

            List<AlertSummary> alerts = new ArrayList<>();
            AlertCriteria criteria = alertProvider.criteria().state(Alert.State.UNHANDLED).orFilter(filter);
            alertProvider.getAlertsSummary(criteria).forEachRemaining(alerts::add);

            return alertsModel.groupAlertSummaries(alerts);
        });
    }
}
