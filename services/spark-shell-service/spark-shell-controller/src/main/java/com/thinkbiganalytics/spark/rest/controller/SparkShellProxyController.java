package com.thinkbiganalytics.spark.rest.controller;

/*-
 * #%L
 * Spark Shell Service Controllers
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

import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.spark.rest.model.Datasource;
import com.thinkbiganalytics.spark.rest.model.JdbcDatasource;
import com.thinkbiganalytics.spark.rest.model.RegistrationRequest;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * Provides an endpoint for proxying to the actual Spark Shell service.
 */
@Api(tags = "Feed Manager - Data Wrangler")
@Component
@Path("/v1/spark/shell")
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Data Wrangler", description = "data transformations"))
public class SparkShellProxyController {

    private static final Logger log = LoggerFactory.getLogger(SparkShellProxyController.class);

    /**
     * Resources for error messages
     */
    private static final ResourceBundle STRINGS = ResourceBundle.getBundle("spark-shell");

    /**
     * Ensures the user has the correct permissions
     */
    @Inject
    private AccessController accessController;

    /**
     * Provides access to {@code Datasource} objects
     */
    @Inject
    private DatasourceProvider datasourceProvider;

    /**
     * The {@code Datasource} transformer
     */
    @Inject
    private DatasourceModelTransform datasourceTransform;

    /**
     * Metadata access service
     */
    @Inject
    private MetadataAccess metadata;

    /**
     * Manages Spark Shell processes
     */
    @Inject
    private SparkShellProcessManager processManager;

    /**
     * Communicates with Spark Shell processes
     */
    @Inject
    private SparkShellRestClient restClient;

    /**
     * Requests the status of a transformation.
     *
     * @param id the destination table name
     * @return the transformation status
     */
    @GET
    @Path("/transform/{table}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Fetches the status of a transformation.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the transformation.", response = TransformResponse.class),
                      @ApiResponse(code = 404, message = "The transformation does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response getTable(@Nonnull @PathParam("table") final String id) {
        // Forward to the Spark Shell process
        final SparkShellProcess process = getSparkShellProcess();
        final Optional<TransformResponse> response;

        try {
            response = restClient.getTable(process, id);
        } catch (final Exception e) {
            throw error(Response.Status.INTERNAL_SERVER_ERROR, "transform.error", e);
        }

        // Return response
        if (response.isPresent()) {
            return Response.ok(response.get()).build();
        } else {
            throw error(Response.Status.NOT_FOUND, "getTable.unknownTable", null);
        }
    }

    /**
     * Ensures a Spark Shell process has been started for the current user.
     *
     * @return 202 Accepted
     */
    @POST
    @Path("/start")
    @ApiOperation("Starts a new Spark Shell process for the current user if one is not already running.")
    @ApiResponses({
                      @ApiResponse(code = 202, message = "The Spark Shell process will be started."),
                      @ApiResponse(code = 500, message = "The Spark Shell process could not be started.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response start() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            processManager.start(auth.getName());
            return Response.accepted().build();
        } catch (final Exception e) {
            throw error(Response.Status.INTERNAL_SERVER_ERROR, "start.error", e);
        }
    }

    /**
     * Registers a Spark Shell process.
     *
     * @param registration the process information
     * @return 204 No Content
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Registers a new Spark Shell process with Kylo.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The Spark Shell process has been successfully registered with this server."),
                      @ApiResponse(code = 401, message = "The provided credentials are invalid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 403, message = "The Spark Shell process does not have permission to register with this server.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The Spark Shell process could not be registered with this server.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response register(@Nonnull final RegistrationRequest registration) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            processManager.register(auth.getPrincipal().toString(), registration);
            return Response.noContent().build();
        } catch (final IllegalArgumentException e) {
            throw error(Response.Status.FORBIDDEN, "register.forbidden", null);
        }
    }

    /**
     * Executes a Spark script that performs transformations using a {@code DataFrame}.
     *
     * @param request the transformation request
     * @return the transformation status
     */
    @POST
    @Path("/transform")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Queries a Hive table and applies a series of transformations on the rows.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the transformation.", response = TransformResponse.class),
                      @ApiResponse(code = 400, message = "The requested data source does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem processing the data.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response transform(@ApiParam(value = "The request indicates the transformations to apply to the source table and how the user wishes the results to be displayed. Exactly one parent or"
                                                + " source must be specified.", required = true)
                              @Nullable final TransformRequest request) {
        // Validate request
        if (request == null || request.getScript() == null) {
            throw error(Response.Status.BAD_REQUEST, "transform.missingScript", null);
        }
        if (request.getParent() != null) {
            if (request.getParent().getScript() == null) {
                throw error(Response.Status.BAD_REQUEST, "transform.missingParentScript", null);
            }
            if (request.getParent().getTable() == null) {
                throw error(Response.Status.BAD_REQUEST, "transform.missingParentTable", null);
            }
        }

        // Add data source details
        if (request.getDatasources() != null && !request.getDatasources().isEmpty()) {
            // Verify access to data sources
            accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_DATASOURCES);

            final List<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> datasourceIds = metadata.read(
                () -> request.getDatasources().stream()
                    .map(com.thinkbiganalytics.metadata.datasource.Datasource::getId)
                    .map(datasourceProvider::resolve)
                    .map(id -> {
                        final com.thinkbiganalytics.metadata.api.datasource.Datasource datasource = datasourceProvider.getDatasource(id);
                        if (datasource != null) {
                            return datasource.getId();
                        } else {
                            throw new BadRequestException("No datasource exists with the given ID: " + id);
                        }
                    })
                    .collect(Collectors.toList())
            );

            // Retrieve table names using system user
            final List<Datasource> datasources = metadata.read(
                () -> datasourceIds.stream()
                    .map(datasourceProvider::getDatasource)
                    .map(datasource -> {
                        if (datasource instanceof com.thinkbiganalytics.metadata.api.datasource.UserDatasource) {
                            return (com.thinkbiganalytics.metadata.datasource.Datasource) datasourceTransform.toDatasource(datasource, DatasourceModelTransform.Level.ADMIN);
                        } else {
                            throw new BadRequestException("Not a supported datasource: " + datasource.getClass().getSimpleName() + " " + datasource.getId());
                        }
                    })
                    .map(datasource -> {
                        if (datasource instanceof com.thinkbiganalytics.metadata.datasource.JdbcDatasource) {
                            return new JdbcDatasource((com.thinkbiganalytics.metadata.datasource.JdbcDatasource) datasource);
                        } else {
                            throw new BadRequestException("Not a supported datasource: " + datasource.getClass().getSimpleName());
                        }
                    })
                    .collect(Collectors.toList()),
                MetadataAccess.SERVICE);
            request.setDatasources(datasources);
        }

        // Execute request
        final SparkShellProcess process = getSparkShellProcess();

        try {
            final TransformResponse response = restClient.transform(process, request);
            return Response.ok(response).build();
        } catch (final Exception e) {
            throw error(Response.Status.INTERNAL_SERVER_ERROR, "transform.error", e);
        }
    }

    /**
     * Generates an error response for the specified message.
     *
     * @param key the resource key or the error message
     * @return the error response
     */
    @Nonnull
    private WebApplicationException error(@Nonnull final Response.Status status, @Nonnull final String key, @Nullable final Throwable cause) {
        // Create entity
        final TransformResponse entity = new TransformResponse();
        entity.setStatus(TransformResponse.Status.ERROR);

        try {
            entity.setMessage(STRINGS.getString(key));
        } catch (final MissingResourceException e) {
            log.warn("Missing resource message: {}", key, e);
            entity.setMessage(key);
        }

        // Generate the response
        final Response response = Response.status(status).entity(entity).build();
        if (cause != null) {
            return new WebApplicationException(cause, response);
        } else {
            return new WebApplicationException(response);
        }
    }

    /**
     * Retrieves the Spark Shell process for the current user.
     *
     * @return the Spark Shell process
     */
    @Nonnull
    private SparkShellProcess getSparkShellProcess() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String username = (auth.getPrincipal() instanceof User) ? ((User) auth.getPrincipal()).getUsername() : auth.getPrincipal().toString();
        try {
            return processManager.getProcessForUser(username);
        } catch (final Exception e) {
            throw error(Response.Status.INTERNAL_SERVER_ERROR, "start.error", e);
        }
    }
}
