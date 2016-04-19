package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.feedmgr.nifi.NifiConfigurationPropertiesService;

import org.springframework.stereotype.Component;

import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by sr186054 on 1/13/16.
 */
@Path("/v1/nifi/configuration")
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
