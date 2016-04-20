package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.feedmgr.nifi.NifiConfigurationPropertiesService;

import org.springframework.stereotype.Component;

import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 1/13/16.
 */
@Api(value = "feed-manager-nifi-configuration", produces = "application/json")
@Path("/v1/feedmgr/nifi/configuration")
@Component
public class NifiConfigurationPropertiesRestController {

    public NifiConfigurationPropertiesRestController() {
    }

    @GET
    @Path("/properties")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getFeeds(){
       Properties properties = NifiConfigurationPropertiesService.getInstance().getPropertiesWithConfigPrefix();
        return Response.ok(properties).build();
    }

}
