package com.thinkbiganalytics.security.rest.controller;

/*-
 * #%L
 * thinkbig-security-controller
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

import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.rest.model.UserGroup;
import com.thinkbiganalytics.security.rest.model.User;
import com.thinkbiganalytics.security.service.user.UserService;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
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
 * Endpoint for accessing Kylo groups.
 */
@Api(tags = "Security - Groups")
@Component
@Path("/v1/security/groups")
@SwaggerDefinition(tags = @Tag(name = "Security - Groups", description = "manages groups"))
public class GroupsController {

    /**
     * Service for accessing Kylo groups
     */
    @Inject
    UserService userService;

    /**
     * Adds for updates a Kylo group.
     *
     * @param group the group
     * @return the result
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Adds or updates a Kylo group.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The group was added or updated."),
                      @ApiResponse(code = 500, message = "There was a problem adding or updating the group.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response addGroup(@Nonnull final UserGroup group) {
        userService.updateGroup(group);
        return Response.noContent().build();
    }

    /**
     * Deletes the specified group.
     *
     * @param groupId the system name of the group
     * @return the result
     * @throws NotFoundException if the group does not exist
     */
    @DELETE
    @Path("{groupId}")
    @ApiOperation("Deletes the specified group.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The group was deleted."),
                      @ApiResponse(code = 404, message = "The group was not found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem deleting the group.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response deleteGroup(@Nonnull @PathParam("groupId") final String groupId) {
        if (userService.deleteGroup(decodeGroupId(groupId))) {
            return Response.noContent().build();
        } else {
            throw new NotFoundException();
        }
    }

    /**
     * Returns the specified group.
     *
     * @param groupId the system name of the group
     * @return the group
     * @throws NotFoundException if the group does not exist
     */
    @GET
    @Path("{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns the specified group.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The requested group.", response = UserGroup.class),
                      @ApiResponse(code = 404, message = "The group was not found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the group.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response getGroup(@Nonnull @PathParam("groupId") final String groupId) {
        final UserGroup group = userService.getGroup(decodeGroupId(groupId)).orElseThrow(NotFoundException::new);
        return Response.ok(group).build();
    }

    /**
     * Returns a list of all groups.
     *
     * @return all groups
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns a list of all groups.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The list of groups.", response = UserGroup.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "There was a problem accessing the groups.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response getGroups() {
        final List<UserGroup> groups = userService.getGroups();
        return Response.ok(groups).build();
    }

    /**
     * Returns a list of all users in the specified group.
     *
     * @param groupId the system name of the group
     * @return the list of users
     * @throws NotFoundException if the group does not exist
     */
    @GET
    @Path("{groupId}/users")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns a list of all users in the specified group.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The list of users.", response = User.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "The group was not found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the group.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response getUsers(@Nonnull @PathParam("groupId") final String groupId) {
        final List<User> users = userService.getUsersByGroup(decodeGroupId(groupId)).orElseThrow(NotFoundException::new);
        return Response.ok(users).build();
    }

    /**
     * Decodes the specified group name. This should only be used on path parameters.
     *
     * @param groupId the path parameter
     * @return the system name of the group
     */
    @Nonnull
    private String decodeGroupId(@Nonnull final String groupId) {
        try {
            return URLDecoder.decode(groupId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BadRequestException("Only UTF-8 encoding is supported.");
        }
    }
}
