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

import com.thinkbiganalytics.kylo.catalog.CatalogException;
import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;
import com.thinkbiganalytics.kylo.catalog.credential.api.DataSourceCredentialManager;
import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceUtil;
import com.thinkbiganalytics.kylo.catalog.file.CatalogFileManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.CatalogModelTransform;
import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorTab;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTable;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSourceCredentials;
import com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin;
import com.thinkbiganalytics.kylo.catalog.table.CatalogTableManager;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.catalog.Connector;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSetProvider;
import com.thinkbiganalytics.metadata.api.catalog.DataSourceProvider;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;
import com.thinkbiganalytics.security.context.SecurityContextUtil;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

    public enum CredentialMode {NONE, EMBED, ATTACH};
    
    @Inject
    private ConnectorProvider connectorProvider;

    @Inject
    private DataSourceProvider dataSourceProvider;

    @Inject
    private DataSetProvider dataSetProvider;
    
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


    @POST
    @ApiOperation("Create a new data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Data source created", response = DataSource.class),
                      @ApiResponse(code = 400, message = "Invalid connector", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createDataSource(@Nonnull final DataSource source) {
        log.entry(source);
        
        return metadataService.commit(() -> {
            Connector.ID connId = connectorProvider.resolveId(source.getConnector().getId());
            
            return connectorProvider.find(connId)
                .map(conn -> dataSourceProvider.create(connId, source.getTitle()))
                .map(domain -> {
                    modelTransform.updateDataSource(source, domain);
                    return domain;
                })
                .map(modelTransform.dataSourceToRestModel())
                .map(dataSource -> Response.ok(log.exit(dataSource)).build())
                .orElseThrow(() -> {
                    log.debug("Connector not found with ID: {}", connId);
                    return new BadRequestException(getMessage("catalog.connector.notFound"));
                });
        });
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
        if (".browse".equals(sref)) {
            doListFiles(DataSourceUtil.getPaths(dataSource).orElseThrow(IllegalStateException::new).get(0), dataSource);
        } else if (".connection".equals(sref)) {
            doListTables(null, null, dataSource);
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
                                  @QueryParam("credentials") @DefaultValue("embed") final String credentialMode,  // TODO Change default to be "none"
                                  @QueryParam("encrypt") @DefaultValue("true") final boolean encryptCredentials) {
        log.entry(dataSourceId);
        CredentialMode mode;

        try {
            mode = CredentialMode.valueOf(credentialMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(log.exit(Status.BAD_REQUEST)).entity(getMessage("catalog.datasource.credential.arg.invalid", credentialMode)).build();
        }

        final Set<Principal> principals = SecurityContextUtil.getCurrentPrincipals();
        DataSource dataSource = findDataSource(dataSourceId);

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
            com.thinkbiganalytics.metadata.api.catalog.DataSource.ID domainId = dataSourceProvider.resolveId(dataSourceId);
            dataSourceProvider.deleteById(domainId);
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
    public Response getDataSources(@QueryParam("connector") final String connectorId, @QueryParam("filter") final String filter, @QueryParam("limit") final Integer limit,
                                   @QueryParam("start") final Integer start) {
        log.entry(connectorId, filter, limit, start);

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
            Page<com.thinkbiganalytics.metadata.api.catalog.DataSource> domainPage = dataSourceProvider.findPage(pageRequest, filter);
            final Page<DataSource> page = domainPage.map(modelTransform.convertDataSourceToRestModel());

            // Return results
            final SearchResult<DataSource> searchResult = new SearchResultImpl<>();
            searchResult.setData(page.getContent());
            searchResult.setRecordsTotal(page.getTotalElements());
            return Response.ok(searchResult).build();
        });
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
            final DataSource dataSource = findDataSource(dataSourceId);
            return Response.ok(log.exit(doListFiles(path, dataSource))).build();
        });
    }

    private List<DataSetFile> doListFiles(@QueryParam("path") String path, DataSource dataSource) {
        final List<DataSetFile> files;

        try {
            log.debug("Listing files at path: {}", path);
            files = fileManager.listFiles(new URI(path), dataSource);
        } catch (final AccessDeniedException e) {
            log.debug("Access denied accessing path: {}: {}", path, e, e);
            throw new ForbiddenException(getMessage("catalog.datasource.listFiles.forbidden", path));
        } catch (final CatalogException e) {
            log.debug("Catalog exception when accessing path: {}: {}", path, e, e);
            throw new BadRequestException(getMessage(e));
        } catch (final URISyntaxException e) {
            log.debug("Path not a valid URI: {}", path, e);
            throw new BadRequestException(getMessage("catalog.datasource.listFiles.invalidPath", path));
        } catch (final Exception e) {
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
    public Response listTables(@PathParam("id") final String dataSourceId, @QueryParam("catalog") final String catalogName, @QueryParam("schema") final String schemaName) {
        log.entry(dataSourceId, catalogName, schemaName);

        return metadataService.read(() -> {
            // List tables
            final DataSource dataSource = findDataSource(dataSourceId);
            final List<DataSetTable> tables = doListTables(catalogName, schemaName, dataSource);
    
            return Response.ok(log.exit(tables)).build();
        });
    }

    private List<DataSetTable> doListTables(@QueryParam("catalog") String catalogName, @QueryParam("schema") String schemaName, DataSource dataSource) {
        final List<DataSetTable> tables;

        try {
            log.debug("List tables for catalog:{} schema:{}", catalogName, schemaName);
            tables = tableManager.listCatalogsOrTables(dataSource, catalogName, schemaName);
        } catch (final Exception e) {
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
    @Path("{id}/credentials")
    @ApiOperation("List credentials for a data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "List of credentials", response = DataSetTable.class, responseContainer = "List"),
                      @ApiResponse(code = 404, message = "Data source does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to list credentials", response = RestResponseStatus.class)
                  })
    public Response listCredentials(@PathParam("id") final String dataSourceId,
                                    @QueryParam("encrypt") @DefaultValue("true") final boolean encrypted) {
        log.entry(dataSourceId, encrypted);
        log.debug("List tables for catalog:{} encrypted:{}", encrypted);

        try {
            final DataSource dataSource = findDataSource(dataSourceId);
            final Set<Principal> principals = SecurityContextUtil.getCurrentPrincipals();
            final Map<String, String> credProps = this.credentialManager.getCredentials(dataSource, encrypted, principals);
            DataSourceCredentials credentials = new DataSourceCredentials(credProps, encrypted);

            return Response.ok(log.exit(credentials)).build();
        } catch (final Exception e) {
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

        final DataSource dataSource = findDataSource(dataSourceId);

        return log.exit(this.pluginController.getPlugin(dataSource.getConnector().getPluginId()));
    }



    @POST
    @Path("{id}/dataset")
    @ApiOperation("creates a new dataset for a datasource")
    public Response createDataSet(@PathParam("id") final String datasourceId) {
        log.entry(datasourceId);
        final DataSource dataSource = findDataSource(datasourceId);
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
    private DataSource findDataSource(@Nonnull final String id) {
        return metadataService.read(() -> {
            com.thinkbiganalytics.metadata.api.catalog.DataSource.ID dsId = dataSourceProvider.resolveId(id);
            return dataSourceProvider.find(dsId)
                .map(modelTransform.dataSourceToRestModel())
                .orElseThrow(() -> {
                    log.debug("Data source not found: {}", id);
                    return new NotFoundException(getMessage("catalog.datasource.notFound.id", id));
                });
        });
    }
}
