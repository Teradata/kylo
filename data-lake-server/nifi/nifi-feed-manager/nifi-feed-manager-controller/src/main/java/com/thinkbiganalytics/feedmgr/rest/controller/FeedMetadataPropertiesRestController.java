package com.thinkbiganalytics.feedmgr.rest.controller;


import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.support.AnnotatedFieldProperty;

import org.springframework.stereotype.Component;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by sr186054 on 1/13/16.
 */
@Path("/v1/nifi/metadata")
@Component
public class FeedMetadataPropertiesRestController {

    public FeedMetadataPropertiesRestController() {
    }

    @GET
    @Path("/property-names")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getProperties(){
        List<AnnotatedFieldProperty> properties = PropertyExpressionResolver.getMetadataProperties();
       return Response.ok(properties).build();
    }


}
