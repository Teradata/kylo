package com.thinkbiganalytics.spark.rest;

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.spark.metadata.TransformRequest;
import com.thinkbiganalytics.spark.metadata.TransformResponse;
import com.thinkbiganalytics.spark.service.TransformService;

import java.util.Map;
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

/**
 * Endpoint for executing Spark scripts on the server.
 */
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
    @Nonnull
    public Response transform(@Nonnull final TransformRequest request) {
        // Validate request
        if (request.getScript() == null) {
            return error(Response.Status.BAD_REQUEST, "transform.missingScript");
        }
        if (request.getParent() != null) {
            if (request.getParent().getScript() == null) {
                return error(Response.Status.BAD_REQUEST, "transform.missingParentScript");
            }
            if (request.getParent().getTable() == null) {
                return error(Response.Status.BAD_REQUEST, "transform.missingTableTable");
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

//        try {
//            message = STRINGS.getString(key);
//        }
//        catch (NullPointerException e) {
//            message = key;
//        }

        // Generate the response
        Map<String, String> entity = ImmutableMap.of(STATUS, "error", "message", message);
        return Response.status(status).entity(entity).build();
    }
}
