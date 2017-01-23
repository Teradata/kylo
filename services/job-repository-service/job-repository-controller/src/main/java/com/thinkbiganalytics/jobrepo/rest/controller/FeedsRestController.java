package com.thinkbiganalytics.jobrepo.rest.controller;

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

import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import io.swagger.annotations.Api;

@Api(tags = "Operations Manager: Jobs", produces = "application/json")
@Path("/v1/feeds")
public class FeedsRestController {

    private static final Logger LOG = LoggerFactory.getLogger(FeedsRestController.class);

    // @Inject
    // private FeedRepository feedRepository;

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
    @Produces({MediaType.APPLICATION_JSON})
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
    @Produces({MediaType.APPLICATION_JSON})
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
    @Produces({MediaType.APPLICATION_JSON})
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
    @Produces({MediaType.APPLICATION_JSON})
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
    @Produces({MediaType.APPLICATION_JSON})
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
    @Produces({MediaType.APPLICATION_JSON})
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
    @Produces({MediaType.APPLICATION_JSON})
    public List<String> getFeedNames() {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            return opsFeedManagerFeedProvider.getFeedNames();
        });
    }

}
