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

import com.thinkbiganalytics.spark.rest.model.SaveRequest;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
public class SparkShellTransformController extends AbstractTransformController {

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
        } catch (final ScriptException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * Saves the results of a Spark script.
     */
    @POST
    @Path("{table}/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Saves the results of a transformation.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the save.", response = SaveResponse.class),
                      @ApiResponse(code = 404, message = "The transformation does not exist.", response = SaveResponse.class)
                  })
    @Nonnull
    public Response save(@Nonnull @PathParam("table") final String id,
                         @ApiParam(value = "The request indicates the destination for saving the transformation. The format is required.", required = true) @Nullable final SaveRequest request) {
        // Validate request
        if (request == null || (request.getJdbc() == null && request.getFormat() == null)) {
            return error(Response.Status.BAD_REQUEST, "save.missingFormat");
        }

        // Execute request
        try {
            final SaveResponse response = transformService.saveShell(id, request);
            return Response.ok(response).build();
        } catch (final IllegalArgumentException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "save.notFound");
        } catch (final Exception e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }
}
