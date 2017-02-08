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

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.beanvalidation.NewFeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.beanvalidation.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * REST API for managing categories within the Feed Manager.
 */
@Api(tags = "Feed Manager - Categories", produces = "application/json")
@Path("/v1/feedmgr/categories")
@Component
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Categories", description = "manages categories"))
public class FeedCategoryRestController {

    @Autowired
    MetadataService metadataService;

    private MetadataService getMetadataService() {
        return metadataService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of categories.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the list of categories.", response = FeedCategory.class, responseContainer = "List")
    )
    public Response getCategories() {
        Collection<FeedCategory> categories = getMetadataService().getCategories();
        return Response.ok(categories).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Creates or updates a category.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The category was saved successfully.", response = FeedCategory.class),
                      @ApiResponse(code = 400, message = "The category name is invalid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The category could not be saved.", response = RestResponseStatus.class)
                  })
    public Response saveCategory(@NewFeedCategory FeedCategory feedCategory) {
        getMetadataService().saveCategory(feedCategory);
        return Response.ok(feedCategory).build();
    }

    @DELETE
    @Path("/{categoryId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Deletes the specified category.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The category was deleted."),
                      @ApiResponse(code = 400, message = "The categoryId is invalid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The category could not be deleted.", response = RestResponseStatus.class)
                  })
    public Response deleteCategory(@UUID @PathParam("categoryId") String categoryId) throws InvalidOperationException {
        getMetadataService().deleteCategory(categoryId);
        return Response.ok().build();
    }

    @GET
    @Path("/{categoryId}/feeds")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the feeds for the specified category.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The list of related feeds."),
                      @ApiResponse(code = 400, message = "The categoryId is invalid.", response = RestResponseStatus.class)
                  })
    public Response getCategory(@UUID @PathParam("categoryId") String categoryId) {
        List<FeedSummary> summaryList = getMetadataService().getFeedSummaryForCategory(categoryId);
        return Response.ok(summaryList).build();
    }

    /**
     * Returns the user fields for categories.
     *
     * @return the user fields
     */
    @GET
    @Path("user-fields")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns the user fields for categories.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the user fields.", response = UserProperty.class, responseContainer = "List")
    )
    @Nonnull
    public Response getCategoryUserFields() {
        final Set<UserProperty> userFields = getMetadataService().getCategoryUserFields();
        return Response.ok(userFields).build();
    }

    /**
     * Returns the user fields for feeds within the specified category.
     *
     * @param categoryId the category id
     * @return the user fields
     * @throws NotFoundException if the category does not exist
     */
    @GET
    @Path("{categoryId}/user-fields")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns the user fields for feeds within the specified category.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the user fields.", response = UserProperty.class, responseContainer = "List"),
                      @ApiResponse(code = 400, message = "The categoryId is invalid.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response getFeedUserFields(@Nonnull @PathParam("categoryId") @UUID final String categoryId) {
        final Set<UserProperty> userFields = getMetadataService().getFeedUserFields(categoryId).orElseThrow(NotFoundException::new);
        return Response.ok(userFields).build();
    }
}
