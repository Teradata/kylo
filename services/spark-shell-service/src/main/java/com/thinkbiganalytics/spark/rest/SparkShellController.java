package com.thinkbiganalytics.spark.rest;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.script.ScriptException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.spark.metadata.TransformRequest;
import com.thinkbiganalytics.spark.metadata.TransformResponse;
import com.thinkbiganalytics.spark.service.TransformService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;

/**
 * Endpoint for executing Spark scripts on the server.
 */
@Api(value = "spark-shell")
@Path("/api/v1/spark/shell")
public class SparkShellController {

    /** Name of the status field for responses */
    private static final String STATUS = "status";

    /** Resources for error messages */
    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("spark-shell");

    /** Service for evaluating transform scripts */
    @Context
    public TransformService transformService;

    /**
     * Executes a Spark script that performs transformations using a {@code DataFrame}.
     *
     * @param request the transformation request
     * @return the result of the script
     */
    @POST
    @Path("/transform")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Queries a Hive table and applies a series of transformations on the rows.")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Returns the status from executing the script.",
                                                response = TransformResponse.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "The request could not be parsed.",
                                                response = TransformResponse.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "There was a problem processing the data.",
                                                response = TransformResponse.class) })
    @Nonnull
    public Response transform(@ApiParam(value = "The request indicates the transformations to apply to the source table and how "
                                                + "the user wishes the results to be displayed. Exactly one parent or source "
                                                + "must be specified." , required=true)
                                  @Nonnull final TransformRequest request) {
        // Validate request
        if (request.getScript() == null) {
            return error(Response.Status.BAD_REQUEST, "transform.missingScript");
        }
        if (request.getParent() != null) {
            if (request.getParent().getScript() == null) {
                return error(Response.Status.BAD_REQUEST, "transform.missingParentScript");
            }
            if (request.getParent().getTable() == null) {
                return error(Response.Status.BAD_REQUEST, "transform.missingParentTable");
            }
        }

        // Execute request
        try {
            TransformResponse response = this.transformService.execute(request);
            return Response.ok(response).build();
        } catch (ScriptException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Generates an error response for the specified message.
     *
     * @param key the resource key or the error message
     * @return the error response
     */
    @Nonnull
    private Response error(@Nonnull final Response.Status status, @Nonnull final String key) {
        // Determine the error message
        String message = key;

        try {
            message = STRINGS.getString(key);
        }
        catch (MissingResourceException e) {
            message = key;
        }

        // Generate the response
        TransformResponse entity = new TransformResponse();
        entity.setMessage(message);
        entity.setStatus(TransformResponse.Status.ERROR);
        return Response.status(status).entity(entity).build();
    }
}
