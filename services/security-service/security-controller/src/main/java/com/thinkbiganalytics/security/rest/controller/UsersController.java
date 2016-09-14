package com.thinkbiganalytics.security.rest.controller;

import com.thinkbiganalytics.security.rest.model.UserPrincipal;
import com.thinkbiganalytics.security.service.user.UserService;

import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
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

/**
 * Endpoint for accessing Kylo users.
 */
@Api(value = "security")
@Component
@Path("/v1/security/users")
public class UsersController {

    /** Service for accessing Kylo users */
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
        @ApiResponse(code = 500, message = "There was a problem adding or updating the user.")
    })
    @Nonnull
    public Response addUser(@Nonnull final UserPrincipal user) {
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
        @ApiResponse(code = 404, message = "The user was not found."),
        @ApiResponse(code = 500, message = "There was a problem deleting the user.")
    })
    @Nonnull
    public Response deleteUser(@Nonnull @PathParam("userId") final String userId) {
        if (userService.deleteUser(userId)) {
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
        @ApiResponse(code = 200, message = "The requested user.", response = UserPrincipal.class),
        @ApiResponse(code = 404, message = "The user was not found."),
        @ApiResponse(code = 500, message = "There was a problem accessing the user.")
    })
    @Nonnull
    public Response getUser(@Nonnull @PathParam("userId") final String userId) {
        final UserPrincipal user = userService.getUser(userId).orElseThrow(NotFoundException::new);
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
        @ApiResponse(code = 200, message = "The list of users.", response = UserPrincipal.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "There was a problem accessing the users.")
    })
    @Nonnull
    public Response getUsers() {
        final List<UserPrincipal> users = userService.getUsers();
        return Response.ok(users).build();
    }
}
