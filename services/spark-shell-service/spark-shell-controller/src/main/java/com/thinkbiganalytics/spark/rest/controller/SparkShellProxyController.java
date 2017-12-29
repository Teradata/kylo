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
import com.thinkbiganalytics.spark.rest.model.SaveRequest;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;
import com.thinkbiganalytics.spark.shell.SparkShellSaveException;
import com.thinkbiganalytics.spark.shell.SparkShellTransformException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.regex.Pattern;
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
import javax.ws.rs.core.HttpHeaders;
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
     * Pattern for matching exceptions in messages.
     */
    private static final Pattern EXCEPTION = Pattern.compile("^(\\s*[a-zA-Z0-9.]+:(?=\\s*[a-zA-Z0-9.]+:))*\\s*[a-zA-Z0-9.]+?(?=[a-zA-Z0-9]+:)");

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
     * Downloads the saved results of a query.
     *
     * @param queryId query identifier
     * @param saveId  save identifier
     * @return the download response
     */
    @GET
    @Path("/query/{query}/save/{save}/zip")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation("Downloads the saved results in a ZIP file")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the saved file."),
                      @ApiResponse(code = 404, message = "The save does not exist."),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.")
                  })
    @Nonnull
    public Response downloadQuery(@Nonnull @PathParam("query") final String queryId, @Nonnull @PathParam("save") final String saveId) {
        final SparkShellProcess process = getSparkShellProcess();
        return getDownloadResponse(() -> restClient.downloadQuery(process, queryId, saveId));
    }

    /**
     * Downloads the saved results of a transformation.
     *
     * @param transformId transform identifier
     * @param saveId      save identifier
     * @return the download response
     */
    @GET
    @Path("/transform/{transform}/save/{save}/zip")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation("Downloads the saved results in a ZIP file")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the saved file."),
                      @ApiResponse(code = 404, message = "The save does not exist."),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.")
                  })
    @Nonnull
    public Response downloadTransform(@Nonnull @PathParam("transform") final String transformId, @Nonnull @PathParam("save") final String saveId) {
        final SparkShellProcess process = getSparkShellProcess();
        return getDownloadResponse(() -> restClient.downloadTransform(process, transformId, saveId));
    }

    /**
     * Returns the data sources available to Spark.
     */
    @GET
    @Path("data-sources")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Finds Spark data sources")
    @ApiResponse(code = 200, message = "List of Spark data sources.", response = String.class, responseContainer = "List")
    public Response getDataSources() {
        final SparkShellProcess process = getSparkShellProcess();
        return Response.ok(restClient.getDataSources(process)).build();
    }

    /**
     * Requests the status of a query.
     *
     * @param id the destination table name
     * @return the query status
     */
    @GET
    @Path("/query/{table}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Fetches the status of a query.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the query.", response = TransformResponse.class),
                      @ApiResponse(code = 404, message = "The query does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response getQueryResult(@Nonnull @PathParam("table") final String id) {
        final SparkShellProcess process = getSparkShellProcess();
        return getResultResponse(() -> restClient.getQueryResult(process, id));
    }

    /**
     * Requests the status of a save.
     *
     * @param queryId query identifier
     * @param saveId  save identifier
     * @return the save status
     */
    @GET
    @Path("/query/{query}/save/{save}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Fetches the status of a save")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the save.", response = SaveResponse.class),
                      @ApiResponse(code = 404, message = "The transformation or save does not exist.", response = SaveResponse.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.", response = SaveResponse.class)
                  })
    @Nonnull
    public Response getQuerySave(@Nonnull @PathParam("query") final String queryId, @Nonnull @PathParam("save") final String saveId) {
        final SparkShellProcess process = getSparkShellProcess();
        return getSaveResponse(() -> restClient.getQuerySave(process, queryId, saveId));
    }

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
    public Response getTransformResult(@Nonnull @PathParam("table") final String id) {
        final SparkShellProcess process = getSparkShellProcess();
        return getResultResponse(() -> restClient.getTransformResult(process, id));
    }

    /**
     * Requests the status of a save.
     *
     * @param transformId transform identifier
     * @param saveId      save identifier
     * @return the save status
     */
    @GET
    @Path("/transform/{transform}/save/{save}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Fetches the status of a save")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the save.", response = SaveResponse.class),
                      @ApiResponse(code = 404, message = "The transformation or save does not exist.", response = SaveResponse.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.", response = SaveResponse.class)
                  })
    @Nonnull
    public Response getTransformSave(@Nonnull @PathParam("transform") final String transformId, @Nonnull @PathParam("save") final String saveId) {
        final SparkShellProcess process = getSparkShellProcess();
        return getSaveResponse(() -> restClient.getTransformSave(process, transformId, saveId));
    }

    /**
     * Executes a SQL query.
     *
     * @param request the query request
     * @return the query status
     */
    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Queries a data source table.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the query.", response = TransformResponse.class),
                      @ApiResponse(code = 400, message = "The requested data source does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem processing the data.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response query(@ApiParam(value = "The request indicates the query to execute. Exactly one source must be specified.", required = true) @Nullable final TransformRequest request) {
        // Validate request
        if (request == null || request.getScript() == null) {
            throw transformError(Response.Status.BAD_REQUEST, "query.missingScript", null);
        }

        // Add data source details
        addDatasourceDetails(request);

        // Execute request
        final SparkShellProcess process = getSparkShellProcess();
        return getTransformResponse(() -> restClient.query(process, request));
    }

    /**
     * Saves the results of a Spark script.
     */
    @POST
    @Path("/query/{query}/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Saves the results of a transformation.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the save.", response = SaveResponse.class),
                      @ApiResponse(code = 404, message = "The transformation does not exist.", response = SaveResponse.class)
                  })
    @Nonnull
    public Response saveQuery(@Nonnull @PathParam("query") final String queryId,
                              @ApiParam(value = "The request indicates the destination for saving the transformation. The format is required.", required = true) @Nullable final SaveRequest request) {
        // Validate request
        if (request == null || (request.getJdbc() == null && request.getFormat() == null)) {
            throw transformError(Response.Status.BAD_REQUEST, SparkShellProxyResources.SAVE_MISSING_FORMAT, null);
        }

        // Add data source details
        addDatasourceDetails(request);

        // Execute request
        final SparkShellProcess process = getSparkShellProcess();
        return getSaveResponse(() -> Optional.of(restClient.saveQuery(process, queryId, request)));
    }

    /**
     * Saves the results of a Spark script.
     */
    @POST
    @Path("/transform/{transform}/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Saves the results of a transformation.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the save.", response = SaveResponse.class),
                      @ApiResponse(code = 404, message = "The transformation does not exist.", response = SaveResponse.class)
                  })
    @Nonnull
    public Response saveTransform(@Nonnull @PathParam("transform") final String transformId,
                                  @ApiParam(value = "The request indicates the destination for saving the transformation. The format is required.", required = true) @Nullable
                                  final SaveRequest request) {
        // Validate request
        if (request == null || (request.getJdbc() == null && request.getFormat() == null)) {
            throw transformError(Response.Status.BAD_REQUEST, SparkShellProxyResources.SAVE_MISSING_FORMAT, null);
        }

        // Add data source details
        addDatasourceDetails(request);

        // Execute request
        final SparkShellProcess process = getSparkShellProcess();
        return getSaveResponse(() -> Optional.of(restClient.saveTransform(process, transformId, request)));
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
            throw transformError(Response.Status.INTERNAL_SERVER_ERROR, "start.error", e);
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
            throw transformError(Response.Status.FORBIDDEN, "register.forbidden", null);
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
            throw transformError(Response.Status.BAD_REQUEST, "transform.missingScript", null);
        }
        if (request.getParent() != null) {
            if (request.getParent().getScript() == null) {
                throw transformError(Response.Status.BAD_REQUEST, "transform.missingParentScript", null);
            }
            if (request.getParent().getTable() == null) {
                throw transformError(Response.Status.BAD_REQUEST, "transform.missingParentTable", null);
            }
        }

        // Add data source details
        addDatasourceDetails(request);

        // Execute request
        final SparkShellProcess process = getSparkShellProcess();
        return getTransformResponse(() -> restClient.transform(process, request));
    }

    /**
     * Adds the data source details to the specified request.
     */
    private void addDatasourceDetails(@Nonnull final SaveRequest request) {
        // Skip empty data source
        if (request.getJdbc() == null) {
            return;
        }

        // Resolve datasource details
        final Datasource datasource = resolveDatasources(Collections.singletonList(request.getJdbc())).get(0);
        if (datasource instanceof JdbcDatasource) {
            request.setJdbc((JdbcDatasource) datasource);
        } else {
            throw new BadRequestException("Not a supported datasource: " + datasource.getClass().getSimpleName());
        }
    }

    /**
     * Adds the data source details to the specified request.
     */
    private void addDatasourceDetails(@Nonnull final TransformRequest request) {
        // Skip empty data sources
        if (request.getDatasources() == null || request.getDatasources().isEmpty()) {
            return;
        }

        // Resolve datasource details
        final List<Datasource> datasources = resolveDatasources(request.getDatasources());
        request.setDatasources(datasources);
    }

    /**
     * Generates an error response for a failed save.
     *
     * <p>Example:
     * <code>
     * throw saveError(Response.Status.BAD_REQUEST, "save.error", e);
     * </code></p>
     *
     * @param status HTTP response status
     * @param key    resource key or error message
     * @param cause  the cause
     * @return the error response
     */
    @Nonnull
    private WebApplicationException saveError(@Nonnull final Response.Status status, @Nonnull final String key, @Nullable final Throwable cause) {
        // Create entity
        final SaveResponse entity = new SaveResponse();
        entity.setId(cause instanceof SparkShellSaveException ? ((SparkShellSaveException) cause).getId() : null);
        entity.setStatus(SaveResponse.Status.ERROR);

        try {
            entity.setMessage(STRINGS.getString(key));
        } catch (final MissingResourceException e) {
            log.warn("Missing resource message: {}", key, e);
            entity.setMessage(key);
        }

        // Generate the response
        final Response response = Response.status(status).entity(entity).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        if (cause != null) {
            return new WebApplicationException(cause, response);
        } else {
            return new WebApplicationException(response);
        }
    }

    /**
     * Generates an error response for a failed transform.
     *
     * <p>Example:
     * <code>
     * throw transformError(Response.Status.BAD_REQUEST, "transform.error", e);
     * </code></p>
     *
     * @param status HTTP response status
     * @param key    resource key or error message
     * @param cause  the cause
     * @return the error response
     */
    @Nonnull
    private WebApplicationException transformError(@Nonnull final Response.Status status, @Nonnull final String key, @Nullable final Throwable cause) {
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
        final Response response = Response.status(status).entity(entity).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        if (cause != null) {
            return new WebApplicationException(cause, response);
        } else {
            return new WebApplicationException(response);
        }
    }

    /**
     * Gets the download response from the specified supplier.
     */
    @Nonnull
    private Response getDownloadResponse(@Nonnull final Supplier<Optional<Response>> supplier) {
        // Get the result
        final Optional<Response> response;
        try {
            response = supplier.get();
        } catch (final Exception e) {
            throw transformError(Response.Status.INTERNAL_SERVER_ERROR, SparkShellProxyResources.DOWNLOAD_ERROR, e);
        }

        // Return response
        return response.orElseThrow(() -> transformError(Response.Status.NOT_FOUND, SparkShellProxyResources.DOWNLOAD_NOT_FOUND, null));
    }

    /**
     * Gets the transform response from the specified supplier.
     */
    @Nonnull
    private Response getResultResponse(@Nonnull final Supplier<Optional<TransformResponse>> supplier) {
        // Get the result
        final Optional<TransformResponse> response;
        try {
            response = supplier.get();
        } catch (final Exception e) {
            throw transformError(Response.Status.INTERNAL_SERVER_ERROR, SparkShellProxyResources.TRANSFORM_ERROR, e);
        }

        // Return response
        try {
            final TransformResponse transformResponse = response.orElseThrow(() -> transformError(Response.Status.NOT_FOUND, "getTable.unknownTable", null));
            return Response.ok(transformResponse).build();
        } catch (final SparkShellTransformException e) {
            final String message = (e.getMessage() != null) ? EXCEPTION.matcher(e.getMessage()).replaceAll("") : SparkShellProxyResources.TRANSFORM_ERROR;
            throw transformError(Response.Status.INTERNAL_SERVER_ERROR, message, e);
        } catch (final Exception e) {
            throw transformError(Response.Status.INTERNAL_SERVER_ERROR, SparkShellProxyResources.TRANSFORM_ERROR, e);
        }
    }

    /**
     * Gets the save response from the specified supplier.
     */
    @Nonnull
    private Response getSaveResponse(@Nonnull final Supplier<Optional<SaveResponse>> supplier) {
        // Get the result
        final Optional<SaveResponse> response;
        try {
            response = supplier.get();
        } catch (final SparkShellSaveException e) {
            final String message = (e.getMessage() != null) ? EXCEPTION.matcher(e.getMessage()).replaceAll("") : SparkShellProxyResources.SAVE_ERROR;
            throw saveError(Response.Status.INTERNAL_SERVER_ERROR, message, e);
        } catch (final Exception e) {
            throw saveError(Response.Status.INTERNAL_SERVER_ERROR, SparkShellProxyResources.SAVE_ERROR, e);
        }

        // Return response
        final SaveResponse saveResponse = response.orElseThrow(() -> transformError(Response.Status.NOT_FOUND, SparkShellProxyResources.SAVE_NOT_FOUND, null));
        return Response.ok(saveResponse).build();
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
            throw transformError(Response.Status.INTERNAL_SERVER_ERROR, "start.error", e);
        }
    }

    /**
     * Gets the transform response from the specified supplier.
     */
    @Nonnull
    private Response getTransformResponse(@Nonnull final Supplier<TransformResponse> supplier) {
        try {
            final TransformResponse response = supplier.get();
            return Response.ok(response).build();
        } catch (final SparkShellTransformException e) {
            final String message = (e.getMessage() != null) ? EXCEPTION.matcher(e.getMessage()).replaceAll("") : SparkShellProxyResources.TRANSFORM_ERROR;
            throw transformError(Response.Status.INTERNAL_SERVER_ERROR, message, e);
        } catch (final Exception e) {
            throw transformError(Response.Status.INTERNAL_SERVER_ERROR, SparkShellProxyResources.TRANSFORM_ERROR, e);
        }
    }

    /**
     * Retrieves all details of the specified data sources.
     */
    @Nonnull
    private List<Datasource> resolveDatasources(@Nonnull final List<Datasource> sources) {
        // Verify access to data sources
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_DATASOURCES);

        final List<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> datasourceIds = metadata.read(
            () -> sources.stream()
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

        // Retrieve admin-level details
        return metadata.read(
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
    }
}
