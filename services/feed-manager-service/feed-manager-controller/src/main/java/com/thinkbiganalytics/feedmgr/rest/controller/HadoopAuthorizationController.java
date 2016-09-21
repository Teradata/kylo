package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.datalake.authorization.HadoopAuthorizationService;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;

import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by Jeremy Merrifield on 9/12/16.
 */
@Api(value = "hadoop-authorization", produces = "application/json")
@Path("/v1/feedmgr/hadoop-authorization")
public class HadoopAuthorizationController {

    @Inject
    @Qualifier("hadoopAuthorizationService")
    private HadoopAuthorizationService hadoopAuthorizationService;

    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getGroups() {
        try {

            List<HadoopAuthorizationGroup> groups = hadoopAuthorizationService.getAllGroups();

            return Response.ok(groups).build();
        }catch (Exception e){
            throw new RuntimeException("Unable to get the external security groups "+e.getMessage());
        }
    }

}
