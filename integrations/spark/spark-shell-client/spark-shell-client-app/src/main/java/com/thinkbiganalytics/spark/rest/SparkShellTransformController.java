package com.thinkbiganalytics.spark.rest;

/*-
 * #%L
 * thinkbig-spark-shell-client-app
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
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.service.IdleMonitorService;
import com.thinkbiganalytics.spark.service.TransformService;

import org.springframework.stereotype.Component;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Endpoint for executing Spark scripts on the server.
 */
@Api(tags = "spark")
@Component
@Path("/api/v1/spark/shell/transform")
public class SparkShellTransformController {

    /**
     * Resources for error messages
     */
    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("spark-shell");

    /**
     * Service for detecting when this app is idle
     */
    @Context
    public IdleMonitorService idleMonitorService;

    /**
     * Service for evaluating transform scripts
     */
    @Context
    public TransformService transformService;

    /**
     * Executes a Spark script that performs transformations using a {@code DataFrame}.
     *
     * @param request the transformation request
     * @return the transformation status
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Queries a Hive table and applies a series of transformations on the rows.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the transformation.", response = TransformResponse.class),
                      @ApiResponse(code = 400, message = "The request could not be parsed.", response = TransformResponse.class),
                      @ApiResponse(code = 500, message = "There was a problem processing the data.", response = TransformResponse.class)
                  })
    @Nonnull
    public Response create(@ApiParam(value = "The request indicates the transformations to apply to the source table and how the user wishes the results to be displayed. Exactly one parent or source"
                                             + " must be specified.", required = true)
                           @Nullable final TransformRequest request) {
        idleMonitorService.reset();

        // Validate request
        if (request == null || request.getScript() == null) {
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
        } finally {
            idleMonitorService.reset();
        }
    }

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
        idleMonitorService.reset();

        try {
            TransformJob job = transformService.getJob(id);

            if (job.isDone()) {
                return Response.ok(job.get()).build();
            } else {
                TransformResponse response = new TransformResponse();
                response.setProgress(job.progress());
                response.setStatus(TransformResponse.Status.PENDING);
                response.setTable(job.groupId());
                return Response.ok(response).build();
            }
        } catch (IllegalArgumentException e) {
            return error(Response.Status.NOT_FOUND, "transform.unknownTable");
        } catch (Exception e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            idleMonitorService.reset();
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
        String message;

        try {
            message = STRINGS.getString(key);
        } catch (MissingResourceException e) {
            message = key;
        }

        // Generate the response
        TransformResponse entity = new TransformResponse();
        entity.setMessage(message);
        entity.setStatus(TransformResponse.Status.ERROR);
        return Response.status(status).entity(entity).build();
    }
}
