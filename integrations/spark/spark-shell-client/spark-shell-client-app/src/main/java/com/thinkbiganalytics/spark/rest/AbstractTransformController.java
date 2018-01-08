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

import com.thinkbiganalytics.spark.io.ZipStreamingOutput;
import com.thinkbiganalytics.spark.metadata.SaveJob;
import com.thinkbiganalytics.spark.metadata.TransformJob;
import com.thinkbiganalytics.spark.model.SaveResult;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.service.TransformService;

import org.apache.hadoop.fs.FileSystem;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public abstract class AbstractTransformController {

    /**
     * Resources for error messages
     */
    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("spark-shell");

    /**
     * Default file system
     */
    @Context
    public FileSystem fileSystem;

    /**
     * Service for evaluating transform scripts
     */
    @Context
    public TransformService transformService;

    /**
     * Request info
     */
    @Context
    public UriInfo uriInfo;

    /**
     * Downloads the saved results as a ZIP file.
     *
     * @param id the save id
     * @return the zip file response
     */
    @GET
    @Path("{table}/save/{save}/zip")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation("Downloads the saved results in a ZIP file")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the saved file."),
                      @ApiResponse(code = 404, message = "The save does not exist."),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.")
                  })
    @Nonnull
    public Response download(@Nonnull @PathParam("save") final String id) {
        // Find job
        SaveJob job;
        try {
            job = transformService.getSaveJob(id, true);
        } catch (final IllegalArgumentException e) {
            job = null;
        }

        // Get result
        final SaveResult result;
        if (job != null && job.isDone()) {
            try {
                result = job.get();
            } catch (final Exception e) {
                return error(Response.Status.INTERNAL_SERVER_ERROR, e);
            }
        } else {
            result = null;
        }

        // Return response
        if (result != null && result.getPath() != null) {
            return Response.ok(new ZipStreamingOutput(result.getPath(), fileSystem))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".zip\"")
                .build();
        } else {
            return error(Response.Status.NOT_FOUND, "download.notFound");
        }
    }

    /**
     * Requests the status of a save.
     *
     * @param id the save id
     * @return the save status
     */
    @GET
    @Path("{table}/save/{save}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Fetches the status of a save")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the save.", response = SaveResponse.class),
                      @ApiResponse(code = 404, message = "The transformation or save does not exist.", response = SaveResponse.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.", response = SaveResponse.class)
                  })
    @Nonnull
    public Response getSave(@Nonnull @PathParam("save") final String id) {
        try {
            final SaveJob job = transformService.getSaveJob(id, false);
            final SaveResponse response = new SaveResponse();

            if (job.isDone()) {
                response.setId(job.getGroupId());
                response.setStatus(SaveResponse.Status.SUCCESS);

                final SaveResult result = job.get();
                if (result.getPath() != null) {
                    response.setLocation("./zip");
                } else {
                    transformService.getSaveJob(id, true);
                }
            } else {
                response.setId(job.getGroupId());
                response.setProgress(job.progress());
                response.setStatus(SaveResponse.Status.PENDING);
            }

            return Response.ok(response).build();
        } catch (final IllegalArgumentException e) {
            return error(Response.Status.NOT_FOUND, "getSave.notFound");
        } catch (final Exception e) {
            final SaveResponse save = new SaveResponse();
            save.setId(id);
            save.setMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            save.setStatus(SaveResponse.Status.ERROR);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(save).build();
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
        try {
            TransformJob job = transformService.getTransformJob(id);

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
            return error(Response.Status.NOT_FOUND, "getTable.notFound");
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
