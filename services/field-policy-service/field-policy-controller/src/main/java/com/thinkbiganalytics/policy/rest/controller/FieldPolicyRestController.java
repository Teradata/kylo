package com.thinkbiganalytics.policy.rest.controller;


import com.thinkbiganalytics.policy.AvailablePolicies;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;


import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Returns a list of all properties that can be assigned durning Feed Registation process
 * this is the list of @MetadataField annotations on the FeedMetadata object
 */
@Api(value = "field -policies", produces = "application/json")
@Path("/v1/field-policies")
public class FieldPolicyRestController {

    public FieldPolicyRestController() {
    }

    @GET
    @Path("/standardization")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getStandardizationPolicies(){
       List<FieldStandardizationRule>standardizationRules = AvailablePolicies.discoverStandardizationRules();
       return Response.ok(standardizationRules).build();
    }

    @GET
    @Path("/validation")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getValidationPolicies(){
        List<FieldValidationRule>standardizationRules = AvailablePolicies.discoverValidationRules();
        return Response.ok(standardizationRules).build();
    }

}
