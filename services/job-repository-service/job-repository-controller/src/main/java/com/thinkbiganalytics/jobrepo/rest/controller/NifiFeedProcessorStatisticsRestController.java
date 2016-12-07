package com.thinkbiganalytics.jobrepo.rest.controller;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats;
import com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStatsTransform;
import com.thinkbiganalytics.rest.model.LabelValue;
import com.thinkbiganalytics.security.AccessController;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 8/23/16.
 */
@Api(value = "provenance-stats", produces = "application/json")
@Path("/v1/provenance-stats")
public class NifiFeedProcessorStatisticsRestController {


    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private AccessController accessController;

    @Autowired
    private NifiFeedProcessorStatisticsProvider statsProvider;

    private static final String DEFAULT_TIMEFRAME = NifiFeedProcessorStatisticsProvider.TimeFrame.HOUR.name();


    @GET
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findStats() {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
        List<? extends NifiFeedProcessorStats> list = statsProvider.findWithinTimeWindow(DateTime.now().minusDays(1), DateTime.now());
        List<com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStats> model = NifiFeedProcessorStatsTransform.toModel(list);
        return Response.ok(model).build();
        });
    }

    @GET
    @Path("/{feedName}/processor-duration/{timeframe}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findStats(@PathParam("feedName") String feedName, @PathParam("timeframe") @DefaultValue("HOUR") NifiFeedProcessorStatisticsProvider.TimeFrame timeframe) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            List<? extends NifiFeedProcessorStats> list = statsProvider.findForFeedProcessorStatistics(feedName, timeframe);
            List<com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStats> model = NifiFeedProcessorStatsTransform.toModel(list);
            return Response.ok(model).build();
        });
    }

    @GET
    @Path("/{feedName}/{timeframe}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findFeedStats(@PathParam("feedName") String feedName, @PathParam("timeframe") @DefaultValue("HOUR") NifiFeedProcessorStatisticsProvider.TimeFrame timeframe) {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        return metadataAccess.read(() -> {
            List<? extends NifiFeedProcessorStats> list = statsProvider.findForFeedStatisticsGroupedByTime(feedName, timeframe);
            List<com.thinkbiganalytics.metadata.rest.jobrepo.nifi.NifiFeedProcessorStats> model = NifiFeedProcessorStatsTransform.toModel(list);
            return Response.ok(model).build();
        });
    }

    @GET
    @Path("/time-frame-options")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTimeFrameOptions() {
        List<LabelValue>
            vals =
            Lists.newArrayList(NifiFeedProcessorStatisticsProvider.TimeFrame.values()).stream().map(timeFrame -> new LabelValue(timeFrame.getDisplayName(), timeFrame.name())).collect(
                Collectors.toList());
        return Response.ok(vals).build();
    }

}
