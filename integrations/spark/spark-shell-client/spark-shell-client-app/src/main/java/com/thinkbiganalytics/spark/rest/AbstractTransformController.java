package com.thinkbiganalytics.spark.rest;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.thinkbiganalytics.spark.metadata.TransformJob;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.service.TransformService;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public abstract class AbstractTransformController {

    /**
     * Resources for error messages
     */
    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("spark-shell");

    /**
     * Service for evaluating transform scripts
     */
    @Context
    public TransformService transformService;

    /**
     * Requests the status of a transformation.
     *
     * @param id the destination table name
     * @return the transformation status
     */
    @GET
    @Path("{table}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Fetches the status of a transformation.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the transformation.", response = TransformResponse.class),
                      @ApiResponse(code = 404, message = "The transformation does not exist.", response = TransformResponse.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.", response = TransformResponse.class)
                  })
    @Nonnull
    public Response getTable(@Nonnull @PathParam("table") final String id) {
        try {
            TransformJob job = transformService.getJob(id);

            if (job.isDone()) {
                return Response.ok(job.get()).build();
            } else {
                TransformResponse response = new TransformResponse();
                response.setProgress(job.progress());
                response.setStatus(TransformResponse.Status.PENDING);
                response.setTable(job.getGroupId());
                return Response.ok(response).build();
            }
        } catch (final IllegalArgumentException e) {
            return error(Response.Status.NOT_FOUND, "getTable.unknownTable");
        } catch (final Exception e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * Generates an error response for the specified exception.
     *
     * @param status the response status
     * @param e      the exception
     * @return the error response
     */
    protected Response error(@Nonnull final Response.Status status, @Nonnull final Exception e) {
        final String message = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
        return createErrorResponse(status, message);
    }

    /**
     * Generates an error response for the specified message.
     *
     * @param status the response status
     * @param key    the resource key or the error message
     * @return the error response
     */
    @Nonnull
    protected Response error(@Nonnull final Response.Status status, @Nonnull final String key) {
        // Determine the error message
        String message;

        try {
            message = STRINGS.getString(key);
        } catch (MissingResourceException e) {
            message = key;
        }

        // Generate the response
        return createErrorResponse(status, message);
    }

    /**
     * Creates a response for the specified message.
     *
     * @param status  the response status
     * @param message the message text
     * @return the response
     */
    private Response createErrorResponse(@Nonnull final Response.Status status, @Nonnull final String message) {
        final TransformResponse entity = new TransformResponse();
        entity.setMessage(message);
        entity.setStatus(TransformResponse.Status.ERROR);
        return Response.status(status).entity(entity).build();
    }
}
