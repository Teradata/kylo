package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

@Api(value = "service-level-agreement", produces = "application/json")
@Path("/v1/feedmgr/sla")
@Component
public class ServiceLevelAgreementRestController {

    @Inject
    ServiceLevelAgreementService serviceLevelAgreementMetrics;

    @GET
    @Path("/available-metrics")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAvailableSLAMetrics() {
        return Response.ok(serviceLevelAgreementMetrics.discoverSlaMetrics()).build();
    }

    @GET
    @Path("/available-responders")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAvailableSLAResponders() {
        return Response.ok(serviceLevelAgreementMetrics.discoverActionConfigurations()).build();
    }


}
