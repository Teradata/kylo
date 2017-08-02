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
 * Endpoint for accessing Kylo users.
 */
@Api(tags = "Security - Users")
@Component
@Path("/v1/security/users")
@SwaggerDefinition(tags = @Tag(name = "Security - Users", description = "manages users"))
public class UsersController {

    /**
     * Service for accessing Kylo users
     */
    @Inject
    UserService userService;

    /**
     * Adds or updates a Kylo user.
     *
     * @param user the user
     * @return the result
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Adds or updates a Kylo user.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The user was added or updated."),
                      @ApiResponse(code = 500, message = "There was a problem adding or updating the user.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response addUser(@Nonnull final User user) {
        userService.updateUser(user);
        return Response.noContent().build();
    }

    /**
     * Deletes the specified user.
     *
     * @param userId the system name of the user
     * @return the result
     * @throws NotFoundException if the user does not exist
     */
    @DELETE
    @Path("{userId}")
    @ApiOperation("Deletes the specified user.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The user was deleted."),
                      @ApiResponse(code = 404, message = "The user was not found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem deleting the user.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response deleteUser(@Nonnull @PathParam("userId") final String userId) {
        if (userService.deleteUser(decodeUserId(userId))) {
            return Response.noContent().build();
        } else {
            throw new NotFoundException();
        }
    }

    /**
     * Returns the specified user.
     *
     * @param userId the system name of the user
     * @return the user
     * @throws NotFoundException if the user does not exist
     */
    @GET
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns the specified user.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The requested user.", response = User.class),
                      @ApiResponse(code = 404, message = "The user was not found.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the user.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response getUser(@Nonnull @PathParam("userId") final String userId) {
        final User user = userService.getUser(decodeUserId(userId)).orElseThrow(NotFoundException::new);
        return Response.ok(user).build();
    }

    /**
     * Returns a list of all users.
     *
     * @return all users
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns a list of all users.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The list of users.", response = User.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "There was a problem accessing the users.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response getUsers() {
        final List<User> users = userService.getUsers();
        return Response.ok(users).build();
    }

    /**
     * Decodes the specified user name. This should only be used on path parameters.
     *
     * @param userId the path parameter
     * @return the system name of the user
     */
    @Nonnull
    private String decodeUserId(@Nonnull final String userId) {
        try {
            return URLDecoder.decode(userId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BadRequestException("Only UTF-8 encoding is supported.");
        }
    }
}
