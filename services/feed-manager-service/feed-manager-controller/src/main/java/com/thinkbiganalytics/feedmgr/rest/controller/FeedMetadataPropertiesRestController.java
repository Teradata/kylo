package com.thinkbiganalytics.feedmgr.rest.controller;

/*-
 * #%L
 * thinkbig-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;

import org.springframework.beans.factory.annotation.Autowired;
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
@Api(tags = "Feed Manager: Feeds", produces = "application/json")
@Path("/v1/feedmgr/metadata-properties")
@Component
public class FeedMetadataPropertiesRestController {

    @Autowired
    PropertyExpressionResolver propertyExpressionResolver;

    public FeedMetadataPropertiesRestController() {
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getProperties(){
        List<AnnotatedFieldProperty> properties = propertyExpressionResolver.getMetadataProperties();
       return Response.ok(properties).build();
    }


}
