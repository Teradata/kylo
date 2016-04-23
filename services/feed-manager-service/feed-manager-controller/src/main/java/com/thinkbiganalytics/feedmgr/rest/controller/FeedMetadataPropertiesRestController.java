package com.thinkbiganalytics.feedmgr.rest.controller;


import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;

import org.springframework.stereotype.Component;

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
@Api(value = "feed-manager-feed-properties", produces = "application/json")
@Path("/v1/feedmgr/metadata-properties")
@Component
public class FeedMetadataPropertiesRestController {

    public FeedMetadataPropertiesRestController() {
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getProperties(){
        List<AnnotatedFieldProperty> properties = PropertyExpressionResolver.getMetadataProperties();
       return Response.ok(properties).build();
    }


}
