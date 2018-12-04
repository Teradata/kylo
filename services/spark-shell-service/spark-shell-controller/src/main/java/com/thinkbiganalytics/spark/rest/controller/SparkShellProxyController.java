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

import com.thinkbiganalytics.discovery.FileParserFactory;
import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
//import com.thinkbiganalytics.discovery.parsers.hadoop.TextBinarySparkFileSchemaParser;
import com.thinkbiganalytics.discovery.rest.controller.SchemaParserAnnotationTransformer;
import com.thinkbiganalytics.discovery.rest.controller.SchemaParserDescriptorUtil;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.kylo.catalog.dataset.DataSetUtil;
import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceUtil;
import com.thinkbiganalytics.kylo.catalog.rest.model.CatalogModelTransform;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DefaultDataSetTemplate;
import com.thinkbiganalytics.kylo.reactive.AsyncResponseSubscriber;
import com.thinkbiganalytics.kylo.reactive.SubscriberFactory;
import com.thinkbiganalytics.kylo.spark.SparkException;
import com.thinkbiganalytics.kylo.spark.file.metadata.FileMetadataScalaScriptGenerator;
import com.thinkbiganalytics.kylo.spark.job.SparkJobContext;
import com.thinkbiganalytics.kylo.spark.job.SparkJobService;
import com.thinkbiganalytics.kylo.spark.job.SparkJobStatus;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobRequest;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResponse;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.catalog.DataSetProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.spark.rest.filemetadata.FileMetadataTransformResponseModifier;
import com.thinkbiganalytics.spark.rest.filemetadata.tasks.FileMetadataCompletionTask;
import com.thinkbiganalytics.spark.rest.filemetadata.tasks.FileMetadataTaskService;
import com.thinkbiganalytics.spark.rest.model.DataSources;
import com.thinkbiganalytics.spark.rest.model.Datasource;
import com.thinkbiganalytics.spark.rest.model.JdbcDatasource;
import com.thinkbiganalytics.spark.rest.model.KyloCatalogReadRequest;
import com.thinkbiganalytics.spark.rest.model.ModifiedTransformResponse;
import com.thinkbiganalytics.spark.rest.model.PageSpec;
import com.thinkbiganalytics.spark.rest.model.PreviewDataSetRequest;
import com.thinkbiganalytics.spark.rest.model.PreviewDataSetTransformResponse;
import com.thinkbiganalytics.spark.rest.model.RegistrationRequest;
import com.thinkbiganalytics.spark.rest.model.SaveRequest;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.ServerStatusResponse;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.rest.model.TransformResultModifier;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;
import com.thinkbiganalytics.spark.shell.SparkShellSaveException;
import com.thinkbiganalytics.spark.shell.SparkShellTransformException;

import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
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
@Path(SparkShellProxyController.BASE)
@SuppressWarnings({"RSReferenceInspection", "SpringJavaAutowiredFieldsWarningInspection"})
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Data Wrangler", description = "data transformations"))
public class SparkShellProxyController {

    private static final Logger log = LoggerFactory.getLogger(SparkShellProxyController.class);

    public static final String BASE = "/v1/spark/shell";
    public static final String TRANSFORM = "/transform";
    public static final String SERVER_STATUS = "/status";
    public static final String FILE_METADATA = "/file-metadata";
    public static final String TRANSFORM_DOWNLOAD = "/transform/{transform}/save/{save}/zip";
    public static final String TRANSFORM_SAVE = "/transform/{transform}/save";
    public static final String TRANSFORM_SAVE_RESULT = "/transform/{transform}/save/{save}";

    /**
     * Pattern for matching exceptions in messages.
     */
    private static final Pattern EXCEPTION = Pattern.compile(
        "^(\\s*[a-zA-Z0-9_$.]+:(?=\\s*[a-zA-Z0-9_$.]+:))*"               // matches all but last "package.Class: package.Class: package.Class:"
        + "\\s*([a-zA-Z_$][a-zA-Z0-9_$.]+($|\\.))?(?=[a-zA-Z0-9_]+:)");  // matches last "package.Class:"

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
     * Provides access to Kylo Catalog data sources
     */
    @Inject
    private DataSourceProvider kyloCatalogDataSourceProvider;

    /**
     * Provides access to Kylo Catalog data sources
     */
    @Inject
    private DataSetProvider kyloCatalogDataSetProvider;


    @Inject
    private CatalogModelTransform catalogModelTransform;

    @Inject
    private DataSetProvider dataSetProvider;

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

    @Inject
    private FileMetadataTaskService fileMetadataTrackerService;

    @Autowired
    @Qualifier("sparkShellMessages")
    MessageSource messages;

    @Inject
    HttpServletRequest request;

    /**
     * Executes Spark jobs
     */
    @Inject
    SparkJobService sparkJobService;


    @POST
    @Path("/job")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Submits a Spark job to be executed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the job.", response = SparkJobResponse.class),
                      @ApiResponse(code = 400, message = "The requested job does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem processing the job.", response = SparkJobResponse.class)
                  })
    public void createJob(final SparkJobRequest request, @Suspended final AsyncResponse response) {
        // Validate request
        if (request == null || request.getScript() == null) {
            throw transformError(Response.Status.BAD_REQUEST, "job.missing-script", null);
        }

        // Execute request
        final SparkJobContext context = sparkJobService.create(request);
        context.subscribe(jobSubscriber(context, response));
    }

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
    @Path(TRANSFORM_DOWNLOAD)
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
    @ApiResponse(code = 200, message = "List of Spark data sources.", response = DataSources.class)
    public Response getDataSources() {
        final SparkShellProcess process = getSparkShellProcess();
        return Response.ok(restClient.getDataSources(process)).build();
    }

    @GET
    @Path("/job/{job}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Fetches the status of a job")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the job.", response = SparkJobResponse.class),
                      @ApiResponse(code = 404, message = "The job does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.", response = SparkJobResponse.class)
                  })
    public void getJobResult(@Nonnull @PathParam("job") final String jobId, @Suspended final AsyncResponse response) {
        final SparkJobContext context = sparkJobService.findById(jobId).orElseThrow(NotFoundException::new);
        context.subscribe(jobSubscriber(context, response));
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
    @Path(TRANSFORM_SAVE_RESULT)
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

        //Add Catalog details
        addCatalogDataSets(request);

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
        if (request == null || (request.getJdbc() == null && request.getCatalogDatasource() == null && request.getFormat() == null)) {
            throw transformError(Response.Status.BAD_REQUEST, SparkShellProxyResources.SAVE_MISSING_FORMAT, null);
        }

        // Add data source details
        addDatasourceDetails(request);

        //Add Catalog details
        addCatalogDataSource(request);

        // Execute request
        final SparkShellProcess process = getSparkShellProcess();
        return getSaveResponse(() -> Optional.of(restClient.saveQuery(process, queryId, request)));
    }

    /**
     * Saves the results of a Spark script.
     */
    @POST
    @Path(TRANSFORM_SAVE)
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
        if (request == null || (request.getJdbc() == null && request.getCatalogDatasource() == null && request.getFormat() == null)) {
            throw transformError(Response.Status.BAD_REQUEST, SparkShellProxyResources.SAVE_MISSING_FORMAT, null);
        }

        // Add data source details
        addDatasourceDetails(request);



        addCatalogDataSource(request);

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
            log.error("Could not start spark shell", e);
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
    @Path(TRANSFORM)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Queries a Hive table and applies a series of transformations on the rows.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the transformation.", response = TransformResponse.class),
                      @ApiResponse(code = 400, message = "The requested data source does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem processing the data.", response = RestResponseStatus.class)
                  })
    @Nonnull
    @SuppressWarnings("squid:S1845")
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

        PageSpec ps = request.getPageSpec();
        if (ps != null) {
            if (Stream.of(ps.getFirstCol(), ps.getNumCols(), ps.getFirstRow(), ps.getNumRows())
                .allMatch(Objects::isNull)) {
                throw transformError(Response.Status.BAD_REQUEST, "transform.badPageSpec", null);
            }
        }

        // Add data source details
        addDatasourceDetails(request);

        //Add Catalog details
        addCatalogDataSets(request);

        addCatalogDataSources(request);

        // Execute request
        final SparkShellProcess process = getSparkShellProcess();
        return getTransformResponse(() -> restClient.transform(process, request));
    }


    @POST
    @Path(FILE_METADATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("returns filemetadata based upon the list of file paths in the dataset.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the file-metadata job.", response = TransformResponse.class),
                      @ApiResponse(code = 400, message = "The requested data source does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem processing the data.", response = RestResponseStatus.class)
                  })
    public Response fileMetadata(com.thinkbiganalytics.kylo.catalog.rest.model.DataSet dataSet) {
        TransformRequest request = new TransformRequest();
        DataSet decrypted = catalogModelTransform.decryptOptions(dataSet);
        request.setScript(FileMetadataScalaScriptGenerator.getScript(DataSetUtil.getPaths(decrypted).orElseGet(Collections::emptyList), DataSetUtil.mergeTemplates(decrypted).getOptions()));

        final SparkShellProcess process = getSparkShellProcess();
        return getModifiedTransformResponse(() -> Optional.of(restClient.transform(process, request)), new FileMetadataTransformResponseModifier(fileMetadataTrackerService));
    }


    @GET
    @Path(FILE_METADATA + "/{table}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Fetches the status of a transformation.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the transformation.", response = TransformResponse.class),
                      @ApiResponse(code = 404, message = "The transformation does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response getFileMetadataTransformResult(@Nonnull @PathParam("table") final String id) {
        //first look at the cache to see if its there
        FileMetadataCompletionTask result = fileMetadataTrackerService.get(id);
        if (result != null) {
            if (result.getModifiedTransformResponse().getStatus() != TransformResponse.Status.PENDING) {
                fileMetadataTrackerService.removeFromCache(id);
            }
            return Response.ok(result.getModifiedTransformResponse()).build();
        } else {
            final SparkShellProcess process = getSparkShellProcess();
            return getModifiedTransformResponse(() -> restClient.getTransformResult(process, id), new FileMetadataTransformResponseModifier(fileMetadataTrackerService));
        }

    }

    @GET
    @Path(SERVER_STATUS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Fetches the status of a transformation.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the spark server and session for current user if applicable.", response = ServerStatusResponse.class),
                      @ApiResponse(code = 500, message = "There was a problem checking the spark server.", response = RestResponseStatus.class)
                  })
    @Nonnull
    public Response getServerStatus() {
        try {
            final SparkShellProcess process = getSparkShellProcess();
            final ServerStatusResponse serverStatusResponse = restClient.serverStatus(process);
            return Response.ok(serverStatusResponse).build();
        } catch (Exception e) {
            throw new WebApplicationException("Unhandled exception attempting to get server status", e);
        }
    }


    @POST
    @Path("/preview")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns the dataset preview")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the file-metadata job.", response = PreviewDataSetTransformResponse.class),
                      @ApiResponse(code = 400, message = "The requested data source does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem processing the data.", response = RestResponseStatus.class)
                  })
    public Response preview(PreviewDataSetRequest previewRequest) {

        DataSource catalogDataSource = fetchCatalogDataSource(previewRequest.getDataSource().getId());
        previewRequest.setDataSource(catalogDataSource);
        if(previewRequest.isFilePreview() && previewRequest.getSchemaParser() == null){
            // set it to a text preview
            previewRequest.setSchemaParser(getTextSchemaParserDescriptor());
        }
        KyloCatalogReadRequest request = KyloCatalogReaderUtil.toKyloCatalogRequest(previewRequest);


        final SparkShellProcess process = getSparkShellProcess();
        return getTransformResponse(() -> {
            PreviewDataSetTransformResponse response = null;
            boolean fallbackToTextParser = previewRequest.isFallbackToTextOnError();
            try {
                TransformResponse transformResponse = restClient.kyloCatalogTransform(process, request);
                response = new PreviewDataSetTransformResponse(transformResponse,previewRequest.getSchemaParser());
            }
            catch (Exception e) {
                //should we attempt to re preview the data as plain text
                if(fallbackToTextParser && previewRequest.getSchemaParser() != null && !"text".equalsIgnoreCase(previewRequest.getSchemaParser().getSparkFormat())){
                    previewRequest.setSchemaParser(getTextSchemaParserDescriptor());
                    KyloCatalogReadRequest  request2 = KyloCatalogReaderUtil.toKyloCatalogRequest(previewRequest);
                    TransformResponse transformResponse = restClient.kyloCatalogTransform(process, request2);
                    response = new PreviewDataSetTransformResponse(transformResponse,previewRequest.getSchemaParser());
                }
                else {
                    throw  e;
                }

           throw e;
            }
            return response;
        });


    }


    /**
     * Get the text schema parser, first looking for the cached one if it exists
     * @return
     */

    private SchemaParserDescriptor getTextSchemaParserDescriptor(){
            SchemaParserDescriptor textParser = new SchemaParserDescriptor();
            textParser.setName("Text");
            textParser.setDisplayName("Text");
            textParser.setSparkFormat("text");
            textParser.setUsesSpark(true);
            textParser.setMimeTypes(new String[] {"text"});
            textParser.setPrimary(true);
            textParser.setAllowSkipHeader(true);
            return textParser;
    }



    private <T> Response getModifiedTransformResponse(Supplier<Optional<TransformResponse>> supplier, TransformResultModifier<T> modifier) {

        final Optional<TransformResponse> response;
        try {
            response = supplier.get();
        } catch (final Exception e) {
            final String message = (e.getMessage() != null) ? EXCEPTION.matcher(e.getMessage()).replaceAll("") : SparkShellProxyResources.TRANSFORM_ERROR;
            throw transformError(Response.Status.INTERNAL_SERVER_ERROR, message, e);
        }
        ModifiedTransformResponse modifiedResponse = modifier.modify(response.get());
        return Response.ok(modifiedResponse).build();
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


    private void addCatalogDataSources(@Nonnull final TransformRequest request) {
        // Skip empty data source
        if (request.getCatalogDataSources() == null) {
            return;
        }

        List<DataSource> dataSources = metadata.read(() -> {
           return  request.getCatalogDataSources().stream()
                .map(DataSource::getId)
                .map(kyloCatalogDataSourceProvider::resolveId)
                .map(id -> {
                    Optional<com.thinkbiganalytics.metadata.api.catalog.DataSource> ds = kyloCatalogDataSourceProvider.find(id);
                    if(ds.isPresent()){
                         return catalogModelTransform.dataSourceToRestModel(true,false).apply(ds.get());
                    }else {
                        throw new BadRequestException("No catgalog datasource exists with the given ID: " + id);
                    }
                })
                .collect(Collectors.toList());
        });
        if(dataSources != null){
            request.setCatalogDataSources(dataSources);
        }
        else {
            throw new BadRequestException("Unable to find catalog datasources");
        }
    }

    private void addCatalogDataSource(@Nonnull final SaveRequest request) {
        // Skip empty data source
        if (request.getCatalogDatasource() == null) {
            return;
        }

       DataSource catalogDataSource = metadata.read(() -> {
                         DataSource ds = kyloCatalogDataSourceProvider.find(kyloCatalogDataSourceProvider.resolveId(request.getCatalogDatasource().getId())).map(dataSource -> {
                             //merge in connection info??
                             return catalogModelTransform.dataSourceToRestModel(true,false).apply(dataSource);
                         }).orElse(null);
                         return ds;
                      });
        if(catalogDataSource != null){
            request.setCatalogDatasource(catalogDataSource);
        }
        else {
            throw new BadRequestException("Unable to find catalog datasource for "+request.getCatalogDatasource().getTitle());
        }
    }




    private void addCatalogDataSets(@Nonnull final TransformRequest request) {
        if (request.getCatalogDatasets() == null || request.getCatalogDatasets().isEmpty()) {
            return;
        }
            List<DataSet> updatedDataSets = new ArrayList<>();
            request.getCatalogDatasets().forEach((dataSet) -> {
                // DataSets will now be added when a user adds them to the wrangler canvas and then associated with an ID
                // In the unlikely event the DataSet coming in doesn't have an ID, ensure it's built.
                if (StringUtils.isBlank(dataSet.getId())) {
                    com.thinkbiganalytics.metadata.api.catalog.DataSource.ID dataSourceId = kyloCatalogDataSourceProvider.resolveId(dataSet.getDataSource().getId());
                    com.thinkbiganalytics.metadata.api.catalog.DataSet domainDs = catalogModelTransform.buildDataSet(dataSet, dataSetProvider.build(dataSourceId));
                    updatedDataSets.add(addDataSourceInformation(catalogModelTransform.dataSetToRestModel().apply(domainDs)));
                } else {
                    updatedDataSets.add(addDataSourceInformation(dataSet));
                }
            });
            request.setCatalogDatasets(updatedDataSets);
    }

    private DataSet addDataSourceInformation(@Nonnull DataSet dataSet){

        DataSet fetchedDataSet = fetchDataSet(dataSet.getId());
        DataSetTemplate template = DataSetUtil.mergeTemplates(fetchedDataSet);
        fetchedDataSet.getDataSource().setTemplate(template);
        return new DataSet(fetchedDataSet);
    }

    private DataSource fetchCatalogDataSource(@Nonnull String datasourceId){
        return metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.catalog.DataSource.ID dsId = kyloCatalogDataSourceProvider.resolveId(datasourceId);
            
            return kyloCatalogDataSourceProvider.find(dsId)
                .map(catalogModelTransform.dataSourceToRestModel(true,false))
                .orElseThrow(() -> new BadRequestException("No Catalog datasource exists with the given ID: " + datasourceId));
        });

    }

    private DataSet fetchDataSet(@Nonnull String dataSetId){
        return metadata.read(() -> {
            com.thinkbiganalytics.metadata.api.catalog.DataSet.ID dsId = kyloCatalogDataSetProvider.resolveId(dataSetId);

            return kyloCatalogDataSetProvider.find(dsId)
                .map(catalogModelTransform.dataSetToRestModel(false))
                .orElseThrow(() -> new BadRequestException("No Catalog dataset exists with the given ID: " + dataSetId));
        });

    }

    /**
     * Gets the locale of the request.
     */
    @Nonnull
    private Locale getLocale() {
        return RequestContextUtils.getLocale(request);
    }

    /**
     * Gets the message for the specified code using the locale of the request.
     */
    @Nonnull
    private String getMessage(@Nonnull final String code) {
        return getMessage(code, getLocale());
    }

    /**
     * Gets the message for the specified code and locale.
     */
    @Nonnull
    private String getMessage(@Nonnull final String code, @Nonnull final Locale locale) {
        try {
            return messages.getMessage(code, null, locale);
        } catch (final NoSuchMessageException e) {
            log.debug("Missing message for code: {}", code, e);
            return code;
        }
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
        entity.setMessage(getMessage(key));
        entity.setStatus(SaveResponse.Status.ERROR);

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
        entity.setMessage(getMessage(key));
        entity.setStatus(TransformResponse.Status.ERROR);

        // Generate the response
        final Response response = Response.status(status).entity(entity).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        if (cause != null) {
            log.debug("{}: {}", entity.getMessage(), cause);
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

    @Nonnull
    private Subscriber<SparkJobStatus> jobSubscriber(@Nonnull final SparkJobContext context, @Nonnull final AsyncResponse asyncResponse) {
        final Locale locale = getLocale();
        return SubscriberFactory.asyncResponseWithTimeout(asyncResponse, 1, TimeUnit.MINUTES, new AsyncResponseSubscriber<SparkJobStatus>() {
            @Override
            public void onError(@Nonnull final Throwable error, @Nonnull final AsyncResponse errorResponse) {
                final SparkJobResponse entity = new SparkJobResponse();
                Response.Status httpStatus = Response.Status.INTERNAL_SERVER_ERROR;
                entity.setId(context.getId());

                if (error instanceof TimeoutException) {
                    httpStatus = Response.Status.OK;
                    entity.setStatus(SparkJobResponse.Status.PENDING);
                } else {
                    entity.setStatus(SparkJobResponse.Status.ERROR);
                    if (error instanceof SparkException && error.getMessage() != null) {
                        entity.setMessage(EXCEPTION.matcher(error.getMessage()).replaceAll(""));
                    } else {
                        log.warn("Spark job failed with error: ", error);
                        entity.setMessage(getMessage(SparkShellProxyResources.JOB_ERROR, locale));
                    }
                }

                errorResponse.resume(Response.status(httpStatus).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).entity(entity).build());
            }

            @Override
            public void onSuccess(@Nonnull final SparkJobStatus status, @Nonnull final AsyncResponse successResponse) {
                final SparkJobResponse entity = new SparkJobResponse();
                entity.setId(context.getId());
                entity.setResult(status.getResult());
                entity.setStatus(SparkJobResponse.Status.SUCCESS);
                successResponse.resume(Response.ok(entity).build());
            }
        });
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
    protected SparkShellProcess getSparkShellProcess() {
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
