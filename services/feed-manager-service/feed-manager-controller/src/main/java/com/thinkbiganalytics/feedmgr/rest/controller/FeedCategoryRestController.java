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
import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.beanvalidation.UUID;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.RoleMembership;
import com.thinkbiganalytics.security.rest.model.RoleMembershipChange;
import com.thinkbiganalytics.security.rest.model.PermissionsChange.ChangeType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
@Path(FeedCategoryRestController.BASE)
@Component
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Categories", description = "manages categories"))
public class FeedCategoryRestController {
    
    private static final Logger log = LoggerFactory.getLogger(FeedCategoryRestController.class);

    public static final String BASE = "/v1/feedmgr/categories";

    @Autowired
    MetadataService metadataService;
    
    @Inject
    private SecurityService securityService;

    @Inject
    private SecurityModelTransform securityTransform;

    private MetadataService getMetadataService() {
        return metadataService;
    }

    /* Get category details by system name */
    @GET
    @Path("/by-name/{categorySystemName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets category details by system name")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the category details.", response = FeedCategory.class),
                      @ApiResponse(code = 400, message = "The category system name is invalid.", response = RestResponseStatus.class)
                  })
    public Response getCategoryByName(@PathParam("categorySystemName") String categorySystemName) {
        FeedCategory category = getMetadataService().getCategoryBySystemName(categorySystemName);

        if (category != null) {
            return Response.ok(category).build();
        }
        else {
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    /* Get category details by id */
    @GET
    @Path("/by-id/{categoryId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets category details by id")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the category details.", response = FeedCategory.class),
                      @ApiResponse(code = 400, message = "The category id is invalid.", response = RestResponseStatus.class)
                  })
    public Response getCategoryById(@UUID @PathParam("categoryId") String categoryId) {
        FeedCategory category = getMetadataService().getCategoryById(categoryId);

        if (category != null) {
            return Response.ok(category).build();
        }
        else {
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of categories.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the list of categories.", response = FeedCategory.class, responseContainer = "List")
    )
    public Response getCategories(@QueryParam("includeFeedDetails") @DefaultValue("false") boolean includeFeedDetails) {
        Collection<FeedCategory> categories = getMetadataService().getCategories(includeFeedDetails);
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
        //requery it to get the allowed actions
        FeedCategory savedCategory = getMetadataService().getCategoryBySystemName(feedCategory.getSystemName());
        return Response.ok(savedCategory).build();
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
        boolean successful = getMetadataService().deleteCategory(categoryId);
        if(!successful) {
            throw new WebApplicationException("Unable to delete the category ");
        }
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
    
    @GET
    @Path("{categoryId}/actions/available")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of available actions that may be permitted or revoked on a category.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A category with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getAvailableActions(@PathParam("categoryId") String categoryIdStr) {
        log.debug("Get available actions for category: {}", categoryIdStr);

        return this.securityService.getAvailableCategoryActions(categoryIdStr)
                        .map(g -> Response.ok(g).build())
                        .orElseThrow(() -> new WebApplicationException("A category with the given ID does not exist: " + categoryIdStr, Response.Status.NOT_FOUND));
    }
    
    @GET
    @Path("{categoryId}/actions/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of actions permitted for the given username and/or groups.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A category with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getAllowedActions(@PathParam("categoryId") String categoryIdStr,
                                         @QueryParam("user") Set<String> userNames,
                                         @QueryParam("group") Set<String> groupNames) {
        log.debug("Get allowed actions for category: {}", categoryIdStr);
        
        Set<? extends Principal> users = Arrays.stream(this.securityTransform.asUserPrincipals(userNames)).collect(Collectors.toSet());
        Set<? extends Principal> groups = Arrays.stream(this.securityTransform.asGroupPrincipals(groupNames)).collect(Collectors.toSet());

        return this.securityService.getAllowedCategoryActions(categoryIdStr, Stream.concat(users.stream(), groups.stream()).collect(Collectors.toSet()))
                        .map(g -> Response.ok(g).build())
                        .orElseThrow(() -> new WebApplicationException("A category with the given ID does not exist: " + categoryIdStr, Status.NOT_FOUND));
    }
    
    @POST
    @Path("{categoryId}/actions/allowed")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the permissions for a category using the supplied permission change request.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The permissions were changed successfully.", response = ActionGroup.class),
                      @ApiResponse(code = 400, message = "The type is not valid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "No category exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public Response postPermissionsChange(@PathParam("categoryId") String categoryIdStr,
                                             PermissionsChange changes) {

        return this.securityService.changeCategoryPermissions(categoryIdStr, changes)
                        .map(g -> Response.ok(g).build())
                        .orElseThrow(() -> new WebApplicationException("A category with the given ID does not exist: " + categoryIdStr, Response.Status.NOT_FOUND));
    }

    @GET
    @Path("{categoryId}/actions/change")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Constructs and returns a permission change request for a set of users/groups containing the actions that the requester may permit or revoke.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the change request that may be modified by the client and re-posted.", response = PermissionsChange.class),
                      @ApiResponse(code = 400, message = "The type is not valid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "No category exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public Response getAllowedPermissionsChange(@PathParam("categoryId") String categoryIdStr,
                                                         @QueryParam("type") String changeType,
                                                         @QueryParam("user") Set<String> userNames,
                                                         @QueryParam("group") Set<String> groupNames) {
        if (StringUtils.isBlank(changeType)) {
            throw new WebApplicationException("The query parameter \"type\" is required", Status.BAD_REQUEST);
        }

        Set<? extends Principal> users = Arrays.stream(this.securityTransform.asUserPrincipals(userNames)).collect(Collectors.toSet());
        Set<? extends Principal> groups = Arrays.stream(this.securityTransform.asGroupPrincipals(groupNames)).collect(Collectors.toSet());

        return this.securityService.createCategoryPermissionChange(categoryIdStr, 
                                                               ChangeType.valueOf(changeType.toUpperCase()), 
                                                               Stream.concat(users.stream(), groups.stream()).collect(Collectors.toSet()))
                        .map(p -> Response.ok(p).build())
                        .orElseThrow(() -> new WebApplicationException("A category with the given ID does not exist: " + categoryIdStr, Status.NOT_FOUND));
    }
    
    @GET
    @Path("{categoryId}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of assigned members the category's roles")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Returns the role memberships.", response = ActionGroup.class),
        @ApiResponse(code = 404, message = "A category with the given ID does not exist.", response = RestResponseStatus.class)
    })
    public Response getRoleMemberships(@PathParam("categoryId") String categoryIdStr,
                                       @QueryParam("verbose") @DefaultValue("false") boolean verbose) {
        
        return this.securityService.getCategoryRoleMemberships(categoryIdStr)
                        .map(m -> Response.ok(m).build())
                        .orElseThrow(() -> new WebApplicationException("A category with the given ID does not exist: " + categoryIdStr, Status.NOT_FOUND));
    }
    

    @POST
    @Path("{categoryId}/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the members of one of a category's roles.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The permissions were changed successfully.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "No category exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public Response postRoleMembershipChange(@PathParam("categoryId") String categoryIdStr,
                                             RoleMembershipChange changes) {
        return this.securityService.changeCategoryRoleMemberships(categoryIdStr, changes)
                        .map(m -> Response.ok(m).build())
                        .orElseThrow(() -> new WebApplicationException("Either a category with the ID \"" + categoryIdStr
                                                                       + "\" does not exist or it does not have a role the named \"" 
                                                                       + changes.getRoleName() + "\"", Status.NOT_FOUND));
    }
    
    
    @GET
    @Path("{categoryId}/roles/change")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoleMemberships(@PathParam("categoryId") String categoryIdStr) {
        RoleMembershipChange change = this.securityService.getCategoryRoleMemberships(categoryIdStr)
                        .map(membships -> membships.getAssigned().values().stream().findAny()
                                        .map(membership -> new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, membership))
                                        .orElse(new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, "read-only")))
                        .orElseThrow(() -> new WebApplicationException("A category with the given ID does not exist: " + categoryIdStr, Status.NOT_FOUND));
        
        return Response.ok(change).build();
    }

    @GET
    @Path("{categoryId}/feed-roles")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of assigned members the category's roles")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Returns the role memberships.", response = ActionGroup.class),
        @ApiResponse(code = 404, message = "A category with the given ID does not exist.", response = RestResponseStatus.class)
    })
    public Response getFeedRoleMemberships(@PathParam("categoryId") String categoryIdStr,
                                           @QueryParam("verbose") @DefaultValue("false") boolean verbose) {

        return this.securityService.getCategoryFeedRoleMemberships(categoryIdStr)
                        .map(m -> Response.ok(m).build())
                        .orElseThrow(() -> new WebApplicationException("A category with the given ID does not exist: " + categoryIdStr, Status.NOT_FOUND));
    }
    
    
    @POST
    @Path("{categoryId}/feed-roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the members of one of a category's roles.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "The permissions were changed successfully.", response = ActionGroup.class),
        @ApiResponse(code = 404, message = "No category exists with the specified ID.", response = RestResponseStatus.class)
    })
    public Response postFeedRoleMembershipChange(@PathParam("categoryId") String categoryIdStr,
                                                 RoleMembershipChange changes) {
        return this.securityService.changeCategoryFeedRoleMemberships(categoryIdStr, changes)
                        .map(m -> Response.ok(m).build())
                        .orElseThrow(() -> new WebApplicationException("Either a category with the ID \"" + categoryIdStr
                                                                       + "\" does not exist or it does not have a role the named \"" 
                                                                       + changes.getRoleName() + "\"", Status.NOT_FOUND));
    }

}
