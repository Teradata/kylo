package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementGroup;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementMetricTransformerHelper;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.rest.model.beanvalidation.UUID;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

@Api(value = "service-level-agreement", produces = "application/json")
@Path("/v1/feedmgr/sla")
@Component
public class ServiceLevelAgreementRestController {

    @Inject
    ServiceLevelAgreementService serviceLevelAgreementService;

    @GET
    @Path("/available-metrics")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAvailableSLAMetrics() {
        return Response.ok(serviceLevelAgreementService.discoverSlaMetrics()).build();
    }

    @GET
    @Path("/available-responders")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAvailableSLAResponders() {
        return Response.ok(serviceLevelAgreementService.discoverActionConfigurations()).build();
    }

    /**
     * Save the General SLA not attached to a feed
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    public Response saveSla(ServiceLevelAgreementGroup sla) {
        ServiceLevelAgreement serviceLevelAgreement = serviceLevelAgreementService.saveSla(sla);
        ServiceLevelAgreementMetricTransformerHelper helper = new ServiceLevelAgreementMetricTransformerHelper();
        ServiceLevelAgreementGroup serviceLevelAgreementGroup = helper.toServiceLevelAgreementGroup(serviceLevelAgreement);
        return Response.ok(serviceLevelAgreementGroup).build();
    }

    /**
     * Save an SLA and attach the ref to the Feed
     */
    @POST
    @Path("/feed/{feedId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response saveSla(@UUID @PathParam("feedId") String feedId, ServiceLevelAgreementGroup sla) {
        ServiceLevelAgreement serviceLevelAgreement = serviceLevelAgreementService.saveFeedSla(sla, feedId);
        ServiceLevelAgreementMetricTransformerHelper helper = new ServiceLevelAgreementMetricTransformerHelper();
        ServiceLevelAgreementGroup serviceLevelAgreementGroup = helper.toServiceLevelAgreementGroup(serviceLevelAgreement);
        return Response.ok(serviceLevelAgreementGroup).build();
    }


    /**
     * Delete an SLA
     */
    @DELETE
    @Path("/{slaId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteCategory(@UUID @PathParam("slaId") String slaId) throws InvalidOperationException {
        serviceLevelAgreementService.removeAgreement(slaId);
        return Response.ok().build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllSlas() {
        List<ServiceLevelAgreement> agreementList = serviceLevelAgreementService.getServiceLevelAgreements();
        if (agreementList == null) {
            agreementList = new ArrayList<>();
        }
        return Response.ok(agreementList).build();
    }

    @GET
    @Path("/{slaId}/form-object")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSlaAsForm(@UUID @PathParam("slaId") String slaId) {
        ServiceLevelAgreementGroup agreement = serviceLevelAgreementService.getServiceLevelAgreementAsFormObject(slaId);

        return Response.ok(agreement).build();
    }



}
