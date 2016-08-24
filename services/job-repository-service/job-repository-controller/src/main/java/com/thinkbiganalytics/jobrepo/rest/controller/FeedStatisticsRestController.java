package com.thinkbiganalytics.jobrepo.rest.controller;

import com.thinkbiganalytics.jobrepo.model.ProvenanceEventSummaryStats;
import com.thinkbiganalytics.jobrepo.service.ProvenanceEventSummaryStatsProvider;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 8/23/16.
 */
@Api(value = "stats", produces = "application/json")
@Path("/v1/stats")
public class FeedStatisticsRestController {


    @Autowired
    private ProvenanceEventSummaryStatsProvider statsProvider;


    @GET
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findStats() {

        //TODO convert to Rest Model
        List<? extends ProvenanceEventSummaryStats> list = statsProvider.findWithinTimeWindow(DateTime.now().minusDays(1), DateTime.now());
        return Response.ok(list).build();

    }


}
