package com.thinkbiganalytics.kylo.catalog.rest.controller;

/*-
 * #%L
 * kylo-catalog-controller
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.discovery.model.CatalogTableSchema;
import com.thinkbiganalytics.exceptions.ExceptionTransformer;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.hive.exceptions.ThriftConnectionException;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.kylo.catalog.CatalogException;
import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;
import com.thinkbiganalytics.kylo.catalog.credential.api.DataSourceCredentialManager;
import com.thinkbiganalytics.kylo.catalog.dataset.DataSetUtil;
import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceUtil;
import com.thinkbiganalytics.kylo.catalog.datasource.PotentialControllerServiceConflictException;
import com.thinkbiganalytics.kylo.catalog.file.CatalogFileManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.CatalogModelTransform;
import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorTab;
import com.thinkbiganalytics.kylo.catalog.rest.model.CreateDataSourceEntity;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTable;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetWithTableSchema;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSourceCredentials;
import com.thinkbiganalytics.kylo.catalog.rest.model.DefaultDataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin;
import com.thinkbiganalytics.kylo.catalog.table.CatalogTableManager;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceAlreadyExistsException;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.context.SecurityContextUtil;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.RoleMembershipChange;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(tags = "Feed Manager - Catalog", produces = "application/json")
@Path(DataSourceController.BASE)
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("StringConcatenationArgumentToLogCall")  // SLF4J-334: Exceptions not logged with XLogger
public class DataSourceController extends AbstractCatalogController {

    private static final XLogger log = XLoggerFactory.getXLogger(DataSourceController.class);

    public static final String BASE = "/v1/catalog/datasource";

    public static final String PLUGIN_ID = "/v1/catalog/datasource/plugin-id";

    public enum CredentialMode {NONE, EMBED, ATTACH}

    static final ExceptionTransformer<ThriftConnectionException> exceptionTransformer =
        new ExceptionTransformer<>(
            ThriftConnectionException.class, SQLException.class, TTransportException.class);

    @Inject
    private AccessController accessController;

    @Inject
    private DataSourceProvider dataSourceProvider;

    @Inject
    private SecurityService securityService;

    @Inject
    private SecurityModelTransform securityTransform;

    @Inject
    private CatalogModelTransform modelTransform;

    @Inject
    private CatalogFileManager fileManager;

    @Inject
    private MetadataAccess metadataService;

    @Inject
    private CatalogTableManager tableManager;

    @Inject
    private DataSourceCredentialManager credentialManager;

    @Inject
    private ConnectorPluginController pluginController;

    @Inject
    private ConnectorPluginManager pluginManager;

    @Inject
    private DataSetController dataSetController;

    @Inject
    private com.thinkbiganalytics.kylo.catalog.dataset.DataSetProvider dataSetService;

    @Inject
    private com.thinkbiganalytics.kylo.catalog.datasource.DataSourceProvider dataSourceService;


    @POST
    @ApiOperation("Create a new data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Data source created", response = DataSource.class),
                      @ApiResponse(code = 400, message = "Invalid connector", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createDataSource(@Nonnull final CreateDataSourceEntity source) {
        log.entry(source);
        final DataSource dataSource;
        try {
            // TODO: Remove this check for the ID and force updates to use the PUT to updateDataSource() for a more typical REST API
            if (StringUtils.isNotEmpty(source.getId())) {
                return updateDataSource(source);
            } else {
                try {
                    dataSource = dataSourceService.createDataSource(source, source.isDetectSimilarNiFiControllerServices());
                } catch (final CatalogException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Cannot create data source from request: " + source, e);
                    }
                    throw new BadRequestException(getMessage(e));
                }
            }
        }catch(PotentialControllerServiceConflictException e){
            throw new WebApplicationException(Response.status(Status.CONFLICT).entity(e.getControllerServiceConflictEntity()).build());
        }catch(DataSourceAlreadyExistsException e){
            throw new BadRequestException(e.getMessage());
        }

        return Response.ok(log.exit(dataSource)).build();
    }

    @POST
    @ApiOperation("Tests datasource")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "Data source test successful", response = DataSource.class),
                      @ApiResponse(code = 400, message = "Invalid datasource", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/test")
    public Response testDataSource(@Nonnull final DataSource dataSource) {
        log.entry(dataSource);

        String pluginId = dataSource.getConnector().getPluginId();
        Optional<ConnectorPlugin> plugin = this.pluginManager.getPlugin(pluginId);
        if (!plugin.isPresent()) {
            throw new BadRequestException(getMessage("catalog.datasource.testDataSource.connectorPluginNotFound", pluginId));
        }
        List<ConnectorTab> tabs = plugin.get().getDescriptor().getTabs();
        if (tabs == null) {
            throw new BadRequestException(getMessage("catalog.datasource.testDataSource.testNotAvailableForPlugin", pluginId));
        }
        ConnectorTab connectorTab = tabs.get(0);
        String sref = connectorTab.getSref();
        DataSource decrypted = modelTransform.decryptOptions(dataSource);

        if (".browse".equals(sref)) {
            doListFiles(DataSourceUtil.getPaths(decrypted).orElseThrow(IllegalStateException::new).get(0), decrypted);
        } else if (".connection".equals(sref)) {
            doListTables(null, null, decrypted);
        } else {
            throw new BadRequestException(getMessage("catalog.datasource.testDataSource.testNotAvailableForTab", sref));
        }

        log.exit();
        return Response.noContent().build();
    }

    @GET
    @ApiOperation("Gets the specified data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data source", response = DataSource.class),
                      @ApiResponse(code = 404, message = "Data source was not found", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("{id}")
    public Response getDataSource(@PathParam("id") final String dataSourceId,
                                  @QueryParam("credentials") @DefaultValue("embed") final String credentialMode) {  // TODO Change default to be "none"
        log.entry(dataSourceId);
        CredentialMode mode;
        final boolean encryptCredentials = true;

        try {
            mode = CredentialMode.valueOf(credentialMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(log.exit(Status.BAD_REQUEST)).entity(getMessage("catalog.datasource.credential.arg.invalid", credentialMode)).build();
        }

        final Set<Principal> principals = SecurityContextUtil.getCurrentPrincipals();
        DataSource dataSource = findDataSource(dataSourceId, encryptCredentials);  // TODO remove encrypt flag when real credential manager is in place

        switch (mode) {
            case NONE:
                dataSource = this.credentialManager.applyPlaceholders(dataSource, principals);
                break;
            case EMBED:
                dataSource = this.credentialManager.applyCredentials(dataSource, principals);
                break;
            case ATTACH:
                Map<String, String> credProps = this.credentialManager.getCredentials(dataSource, encryptCredentials, principals);
                DataSourceCredentials creds = new DataSourceCredentials(credProps, encryptCredentials);
                dataSource = this.credentialManager.applyPlaceholders(dataSource, principals);
                dataSource.setCredentials(creds);
        }

        return Response.ok(log.exit(dataSource)).build();
    }

    @PUT
    @ApiOperation("Updates an existing data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Data source updated", response = DataSource.class),
                      @ApiResponse(code = 400, message = "Invalid connector", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDataSource(@Nonnull final DataSource source) {
        log.entry(source);

        final DataSource dataSource;
        try {
            dataSource = dataSourceService.updateDataSource(source);
        } catch (final CatalogException e) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot create data source from request: " + source, e);
            }
            throw new BadRequestException(getMessage(e));
        }

        return Response.ok(log.exit(dataSource)).build();
    }

    @DELETE
    @ApiOperation("Deletes the specified data source")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "Data source deleted successfully", response = DataSource.class),
                      @ApiResponse(code = 404, message = "Data source was not found", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("{id}")
    public Response deleteDataSource(@PathParam("id") final String dataSourceId) {
        log.entry(dataSourceId);

        metadataService.commit(() -> {
            final com.thinkbiganalytics.metadata.api.catalog.DataSource.ID domainId = dataSourceProvider.resolveId(dataSourceId);
            final com.thinkbiganalytics.metadata.api.catalog.DataSource dataSource = dataSourceProvider.find(domainId).orElse(null);
            if (dataSource != null) {
                if (dataSource.getDataSets().stream().anyMatch(dataSet -> !dataSet.getFeedSources().isEmpty() || !dataSet.getFeedTargets().isEmpty())) {
                    throw new WebApplicationException(Response.status(Status.CONFLICT).build());
                } else {
                    dataSourceProvider.deleteById(domainId);
                }
            }
        });

        log.exit();
        return Response.noContent().build();
    }

    @GET
    @ApiOperation("Lists all data sources")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data sources", response = SearchResult.class),
                      @ApiResponse(code = 400, message = "", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    public Response getDataSources(@QueryParam("connector") final String connectorId,
                                   @QueryParam("filter") final String filter,
                                   @QueryParam("limit") final Integer limit,
                                   @QueryParam("start") final Integer start) {
        log.entry(connectorId, filter, limit, start);

        final boolean encryptCredentials = true;

        // Validate parameters
        if (start != null && start < 0) {
            throw new BadRequestException(getMessage("catalog.datasource.getDataSources.invalidStart"));
        }
        if (limit != null && limit < 1) {
            throw new BadRequestException(getMessage("catalog.datasource.getDataSources.invalidLimit"));
        }

        // Fetch page
        final PageRequest pageRequest = new PageRequest((start != null) ? start : 0, (limit != null) ? limit : Integer.MAX_VALUE);

        return metadataService.read(() -> {
            // Require admin permission if the results should include unencrypted credentials.
            accessController.checkPermission(AccessController.SERVICES, encryptCredentials ? FeedServicesAccessControl.ACCESS_DATASOURCES : FeedServicesAccessControl.ADMIN_DATASOURCES);

            Page<com.thinkbiganalytics.metadata.api.catalog.DataSource> domainPage = dataSourceProvider.findPage(pageRequest, filter);
            final Page<DataSource> page = domainPage.map(modelTransform.convertDataSourceToRestModel(true, encryptCredentials));

            // Return results
            final SearchResult<DataSource> searchResult = new SearchResultImpl<>();
            searchResult.setData(page.getContent());
            searchResult.setRecordsTotal(page.getTotalElements());
            return Response.ok(searchResult).build();
        });
    }


    @GET
    @ApiOperation("Lists all datasources matching the plugin id")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data sources", response = DataSource.class),
                      @ApiResponse(code = 400, message = "", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("/plugin-id")
    public Response getDataSources(@QueryParam("pluginIds") final String pluginIds) {
        log.entry(pluginIds);
        final boolean encryptCredentials = true;
        List<String> pluginList = Arrays.asList(pluginIds.split(",")).stream().map(id -> id.trim()).collect(Collectors.toList());
        final Set<DataSource> datasources = metadataService.read(() -> {
            // Require admin permission if the results should include unencrypted credentials.
            accessController.checkPermission(AccessController.SERVICES, encryptCredentials ? FeedServicesAccessControl.ACCESS_DATASOURCES : FeedServicesAccessControl.ADMIN_DATASOURCES);

            return dataSourceProvider.findAll().stream().filter(ds -> pluginList.contains(ds.getConnector().getPluginId()))
                .map(modelTransform.dataSourceToRestModel(true, encryptCredentials))
                .collect(Collectors.toSet());
        });
        return Response.ok(datasources).build();
    }


    @GET
    @Path("{id}/files")
    @ApiOperation("List files of a data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "List of files in path", response = DataSetFile.class, responseContainer = "List"),
                      @ApiResponse(code = 400, message = "A path is not valid", response = RestResponseStatus.class),
                      @ApiResponse(code = 403, message = "Access to the path is restricted", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "Data source does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to list files", response = RestResponseStatus.class)
                  })
    public Response listFiles(@PathParam("id") final String dataSourceId, @QueryParam("path") final String path) {
        log.entry(dataSourceId, path);

        return metadataService.read(() -> {
            // List files at path
            final DataSource dataSource = findDataSource(dataSourceId, false);
            return Response.ok(log.exit(doListFiles(path, dataSource))).build();
        });
    }

    private List<DataSetFile> doListFiles(@QueryParam("path") String path, DataSource dataSource) {
        final List<DataSetFile> files;

        try {
            log.debug("Listing files at path: {}", path);
            files = fileManager.listFiles(path, dataSource);
        } catch (final AccessDeniedException e) {
            log.debug("Access denied accessing path: {}: {}", path, e, e);
            throw new ForbiddenException(getMessage("catalog.datasource.listFiles.forbidden", path));
        } catch (final CatalogException e) {
            log.debug("Catalog exception when accessing path: {}: {}", path, e, e);
            throw new BadRequestException(getMessage(e));
        } catch (final Exception e) {
            if (exceptionTransformer.causesInChain(e)) {
                throw new ThriftConnectionException(e);
            }

            if (log.isErrorEnabled()) {
                log.error("Failed to list data source files at path " + path + ": " + e, e);
            }
            final RestResponseStatus status = new RestResponseStatus.ResponseStatusBuilder()
                .message(getMessage("catalog.datasource.listFiles.error", path))
                .url(request.getRequestURI())
                .setDeveloperMessage(e)
                .buildError();
            throw new InternalServerErrorException(Response.serverError().entity(status).build());
        }
        return files;
    }

    @GET
    @Path("{id}/tables")
    @ApiOperation("List tables in a data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "List of tables", response = DataSetTable.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "Data source does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to list tables", response = RestResponseStatus.class)
                  })
    public Response listTables(@PathParam("id") final String dataSourceId,
                               @QueryParam("catalog") final String catalogName,
                               @QueryParam("schema") final String schemaName) {
        log.entry(dataSourceId, catalogName, schemaName);
        //read as the user to see if they can access the datasource
        boolean hasAccess = metadataService.read(() -> {
            return findDataSource(dataSourceId, true) != null;
        });

        if (hasAccess) {
            return metadataService.read(() -> {
                // List tables
                final DataSource dataSource = findDataSource(dataSourceId, false);
                final List<DataSetTable> tables = doListTables(catalogName, schemaName, dataSource);

                return Response.ok(log.exit(tables)).build();
            }, MetadataAccess.SERVICE);
        } else {
            log.debug("Access denied accessing datasource : {}", dataSourceId);
            throw new ForbiddenException(getMessage("catalog.datasource.forbidden"));
        }
    }

    private List<DataSetTable> doListTables(@QueryParam("catalog") String catalogName, @QueryParam("schema") String schemaName, DataSource dataSource) {
        final List<DataSetTable> tables;

        try {
            log.debug("List tables for catalog:{} schema:{}", catalogName, schemaName);
            tables = tableManager.listCatalogsOrTables(dataSource, catalogName, schemaName);
        } catch (final Exception e) {
            if (exceptionTransformer.causesInChain(e)) {
                throw new ThriftConnectionException(e);
            }

            if (log.isErrorEnabled()) {
                log.error("Failed to list tables for catalog [" + catalogName + "] schema [" + schemaName + "]: " + e, e);
            }
            final RestResponseStatus status = new RestResponseStatus.ResponseStatusBuilder()
                .message(getMessage("catalog.datasource.listTables.error", catalogName, schemaName))
                .url(request.getRequestURI())
                .setDeveloperMessage(e)
                .buildError();
            throw new InternalServerErrorException(Response.serverError().entity(status).build());
        }
        return tables;
    }


    @GET
    @Path("{id}/tables/filter")
    @ApiOperation("List all tables in a data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "List of tables", response = DataSetTable.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "Data source does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to list tables", response = RestResponseStatus.class)
                  })
    public Response listTables(@PathParam("id") final String dataSourceId, @QueryParam("filter") String catalogOrSchemaName) {

        List<DataSetTable> tableList = new ArrayList<>();
        //read as the user to see if they can access the datasource
        boolean hasAccess = metadataService.read(() -> {
            return findDataSource(dataSourceId, true) != null;
        });

        if (hasAccess) {

            tableList = metadataService.read(() -> {
                // List tables
                final DataSource dataSource = findDataSource(dataSourceId, false);

                final List<DataSetTable> tables;
                try {
                    log.debug("List tables for catalogOrSchema:{}", catalogOrSchemaName);
                    tables = tableManager.listTables(dataSource, catalogOrSchemaName);
                } catch (final Exception e) {
                    if (exceptionTransformer.causesInChain(e)) {
                        throw new ThriftConnectionException(e);
                    }

                    if (log.isErrorEnabled()) {
                        log.error("Failed to list tables for [" + catalogOrSchemaName + "] : " + e, e);
                    }
                    final RestResponseStatus status = new RestResponseStatus.ResponseStatusBuilder()
                        .message(getMessage("catalog.datasource.listTablesFilter.error", catalogOrSchemaName))
                        .url(request.getRequestURI())
                        .setDeveloperMessage(e)
                        .buildError();
                    throw new InternalServerErrorException(Response.serverError().entity(status).build());
                }
                return tables;


            }, MetadataAccess.SERVICE);
        }

        return Response.ok(log.exit(tableList)).build();
    }


    /**
     * Gets the schema of the specified table using the specified data source.
     *
     * @param dataSourceId the data source id
     * @param tableName    the table name
     * @param schema       the schema name, or {@code null} to search all schemas
     * @return the table and field details
     */
    @POST
    @Path("{id}/tables/{tableName}/dataset")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the schema of the specified table.", notes = "Connects to the database specified by the data source.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the table schema.", response = DataSetWithTableSchema.class),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "A JDBC data source with that id does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "NiFi or the database are unavailable.", response = RestResponseStatus.class)
                  })
    public Response createJdbcTableDataSet(@PathParam("id") final String dataSourceId,
                                           @PathParam("tableName") final String tableName,
                                           @QueryParam("schema") final String schema) {
        // TODO Verify user has access to data source
        // Require admin permission if the results should include unencrypted credentials.
        final boolean encryptCredentials = true;

        accessController.checkPermission(AccessController.SERVICES, encryptCredentials ? FeedServicesAccessControl.ACCESS_DATASOURCES : FeedServicesAccessControl.ADMIN_DATASOURCES);
        DataSetWithTableSchema dataSetWithTableSchema = null;
        boolean hasAccess = false;
        try {
            //ensure the user can read teh datasource

            DataSource encryptedSource = metadataService.read(() -> {
                return findDataSource(dataSourceId, true);
            });
            hasAccess = encryptedSource != null;

            if (hasAccess) {
                //fetch the datasource as a service account to get the creds.
                DataSource dataSource = metadataService.read(() -> {
                    return findDataSource(dataSourceId, false);
                }, MetadataAccess.SERVICE);

                //describe the datasource and table

                CatalogTableSchema tableSchema = tableManager.describeTable(dataSource, schema, tableName);

                //create the dataset as a service account.  if we get here the user has access to view the data source

                // dataSetWithTableSchema = metadataService.commit(() -> {
                // List tables

                if (tableSchema != null) {
                    DataSet dataSet = new DataSet();
                    //assign the datasource to this dataset with encrypted credentials
                    dataSet.setDataSource(encryptedSource);
                    String fullTableName = (tableSchema.getTable() != null)
                                           ? tableSchema.getTable().getQualifiedIdentifier()
                                           : HiveUtils.quoteIdentifier(tableSchema.getSchemaName(), tableSchema.getName());
                    dataSet.setTitle(tableSchema.getSchemaName() + "." + tableSchema.getName());

                    DefaultDataSetTemplate defaultDataSetTemplate = DataSetUtil.mergeTemplates(dataSet);
                    List<String> paths = defaultDataSetTemplate.getPaths();
                    String format = defaultDataSetTemplate.getFormat();

                    Map<String, String> options = defaultDataSetTemplate.getOptions();
                    if (options == null) {
                        options = new HashMap<>();
                    }
                    if ("hive".equalsIgnoreCase(format.toLowerCase())) {
                        if (paths == null) {
                            paths = new ArrayList<>();
                        }
                        paths.add(fullTableName);
                    }
                    options.put("dbtable", fullTableName);

                    dataSet.setFormat(format);
                    dataSet.setPaths(paths);
                    dataSet.setOptions(options);

                    DataSet dataSet1 = dataSetService.findOrCreateDataSet(dataSet, encryptCredentials);
                    dataSetWithTableSchema = new DataSetWithTableSchema(dataSet1, tableSchema);
                } else {
                    if (log.isErrorEnabled()) {
                        log.error("Failed to describe tables for schema [" + schema + "], table [" + tableName + "], dataSource [" + dataSourceId + "] ");
                    }
                    final RestResponseStatus status = new RestResponseStatus.ResponseStatusBuilder()
                        .message(getMessage("catalog.datasource.describeTable.error", tableName, schema))
                        .url(request.getRequestURI())
                        .buildError();
                    throw new InternalServerErrorException(Response.serverError().entity(status).build());
                }

                // });
            } else {
                //no acceess
                final RestResponseStatus status = new RestResponseStatus.ResponseStatusBuilder()
                    .message(getMessage("catalog.datasource.forbidden"))
                    .url(request.getRequestURI())
                    .buildError();
                throw new InternalServerErrorException(Response.serverError().entity(status).build());
            }
        } catch (Exception e) {
            if (exceptionTransformer.causesInChain(e)) {
                throw new ThriftConnectionException(e);
            }

            final RestResponseStatus status = new RestResponseStatus.ResponseStatusBuilder()
                .message(getMessage("catalog.datasource.describeTable.error", tableName, schema))
                .url(request.getRequestURI())
                .buildError();
            throw new InternalServerErrorException(Response.serverError().entity(status).build());
        }

        return Response.ok(dataSetWithTableSchema).build();
    }


    @GET
    @Path("{id}/credentials")
    @ApiOperation("List credentials for a data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "List of credentials", response = DataSetTable.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "Data source does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to list credentials", response = RestResponseStatus.class)
                  })
    public Response listCredentials(@PathParam("id") final String dataSourceId) {
        final boolean encrypted = true;

        log.entry(dataSourceId, encrypted);
        log.debug("List tables for catalog:{} encrypted:{}", encrypted);

        try {
            final DataSource dataSource = findDataSource(dataSourceId, true);
            final Set<Principal> principals = SecurityContextUtil.getCurrentPrincipals();
            final Map<String, String> credProps = this.credentialManager.getCredentials(dataSource, encrypted, principals);
            DataSourceCredentials credentials = new DataSourceCredentials(credProps, encrypted);

            return Response.ok(log.exit(credentials)).build();
        } catch (final Exception e) {
            if (exceptionTransformer.causesInChain(e)) {
                throw new ThriftConnectionException(e);
            }

            log.error("Failed to retrieve credentials for datasource [{}] encrypted={}: " + e, dataSourceId, encrypted, e);

            final RestResponseStatus status = new RestResponseStatus.ResponseStatusBuilder()
                .message(getMessage("catalog.datasource.credentials.error", dataSourceId))
                .url(request.getRequestURI())
                .setDeveloperMessage(e)
                .buildError();
            throw new InternalServerErrorException(Response.serverError().entity(status).build());
        }
    }

    @GET
    @Path("{id}/plugin")
    @ApiOperation("Gets the plugin associates with the connector of the specified data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The plugin associates with the data source's connector", response = DataSetTable.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "Data source does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to obtain the plugin", response = RestResponseStatus.class)
                  })
    public Response getConnectorPlugin(@PathParam("id") final String dataSourceId) {
        log.entry(dataSourceId);

        final DataSource dataSource = findDataSource(dataSourceId, true);

        return log.exit(this.pluginController.getPlugin(dataSource.getConnector().getPluginId()));
    }


    @POST
    @Path("{id}/dataset")
    @ApiOperation("creates a new dataset for a datasource")
    public Response createDataSet(@PathParam("id") final String datasourceId) {
        log.entry(datasourceId);
        final DataSource dataSource = findDataSource(datasourceId, true);
        final DataSet dataSet = new DataSet();
        dataSet.setDataSource(dataSource);

        return dataSetController.createDataSet(dataSet);
    }

    /**
     * Gets the data source with the specified id.
     *
     * @throws NotFoundException if the data source does not exist
     */
    @Nonnull
    private DataSource findDataSource(@Nonnull final String id, boolean encryptCredentials) {
        return metadataService.read(() -> {
            // Require admin permission if the results should include unencrypted credentials.
            accessController.checkPermission(AccessController.SERVICES, encryptCredentials ? FeedServicesAccessControl.ACCESS_DATASOURCES : FeedServicesAccessControl.ADMIN_DATASOURCES);

            com.thinkbiganalytics.metadata.api.catalog.DataSource.ID dsId = dataSourceProvider.resolveId(id);
            return dataSourceProvider.find(dsId)
                .map(modelTransform.dataSourceToRestModel(true, encryptCredentials))
                .orElseThrow(() -> {
                    log.debug("Data source not found: {}", id);
                    return new NotFoundException(getMessage("catalog.datasource.notFound.id", id));
                });
        });
    }


    @GET
    @Path("{id}/actions/available")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of available actions that may be permitted or revoked on a data source.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A data source with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getAvailableActions(@PathParam("id") final String dataSourceIdStr) {
        log.debug("Get available actions for data source: {}", dataSourceIdStr);

        return this.securityService.getAvailableDataSourceActions(dataSourceIdStr)
            .map(g -> Response.ok(g).build())
            .orElseThrow(() -> new WebApplicationException("A data source with the given ID does not exist: " + dataSourceIdStr, Response.Status.NOT_FOUND));
    }

    @GET
    @Path("{id}/actions/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of actions permitted for the given username and/or groups.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A data source with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getAllowedActions(@PathParam("id") final String dataSourceIdStr, @QueryParam("user") final Set<String> userNames, @QueryParam("group") final Set<String> groupNames) {
        log.debug("Get allowed actions for data source: {}", dataSourceIdStr);

        Set<? extends Principal> users = Arrays.stream(this.securityTransform.asUserPrincipals(userNames)).collect(Collectors.toSet());
        Set<? extends Principal> groups = Arrays.stream(this.securityTransform.asGroupPrincipals(groupNames)).collect(Collectors.toSet());

        return this.securityService.getAllowedDataSourceActions(dataSourceIdStr, Stream.concat(users.stream(), groups.stream()).collect(Collectors.toSet()))
            .map(g -> Response.ok(g).build())
            .orElseThrow(() -> new WebApplicationException("A data source with the given ID does not exist: " + dataSourceIdStr, Response.Status.NOT_FOUND));
    }

    @GET
    @Path("{id}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of assigned members the data source's roles")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the role memberships.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A data source with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getRoleMemberships(@PathParam("id") final String dataSourceIdStr, @QueryParam("verbose") @DefaultValue("false") final boolean verbose) {
        return this.securityService.getDataSourceRoleMemberships(dataSourceIdStr)
            .map(m -> Response.ok(m).build())
            .orElseThrow(() -> new WebApplicationException("A data source with the given ID does not exist: " + dataSourceIdStr, Response.Status.NOT_FOUND));
    }

    @POST
    @Path("{id}/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the members of one of a data source's roles.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The permissions were changed successfully.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "No data source exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public Response postPermissionsChange(@PathParam("id") final String dataSourceIdStr, final RoleMembershipChange changes) {
        return this.securityService.changeDataSourceRoleMemberships(dataSourceIdStr, changes)
            .map(m -> Response.ok(m).build())
            .orElseThrow(() -> new WebApplicationException("Either a data source with the ID \"" + dataSourceIdStr + "\" does not exist or it does not have a role named \""
                                                           + changes.getRoleName() + "\"", Response.Status.NOT_FOUND));
    }
}
