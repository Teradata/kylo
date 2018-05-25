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
import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceProvider;
import com.thinkbiganalytics.kylo.catalog.file.CatalogFileManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(tags = "Feed Manager - Catalog", produces = "application/json")
@Path(DataSourceController.BASE)
@Produces(MediaType.APPLICATION_JSON)
public class DataSourceController extends AbstractCatalogController {

    private static final XLogger log = XLoggerFactory.getXLogger(DataSourceController.class);

    public static final String BASE = "/v1/catalog/datasource";

    @Inject
    DataSourceProvider dataSourceProvider;

    @Inject
    CatalogFileManager fileManager;

    @Inject
    MetadataAccess metadataService;

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

        final DataSource dataSource;
        try {
            dataSource = metadataService.commit(() -> dataSourceProvider.createDataSource(source));
        } catch (final CatalogException e) {
            log.debug("Cannot create data source from request: {}", source, e);
            throw new BadRequestException(getMessage(e));
        }

        return Response.ok(log.exit(dataSource)).build();
    }

    @GET
    @ApiOperation("Gets the specified data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data source", response = DataSource.class),
                      @ApiResponse(code = 404, message = "Data source was not found", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("{id}")
    public Response getDataSource(@PathParam("id") final String dataSourceId) {
        log.entry(dataSourceId);
        final DataSource dataSource = findDataSource(dataSourceId);
        return Response.ok(log.exit(dataSource)).build();
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
        final Page<DataSource> page = metadataService.read(() -> dataSourceProvider.findAllDataSources(pageRequest, filter));

        // Return results
        final SearchResult<DataSource> searchResult = new SearchResultImpl<>();
        searchResult.setData(page.getContent());
        searchResult.setRecordsTotal(page.getTotalElements());
        return Response.ok(searchResult).build();
    }

    @GET
    @Path("{id}/files")
    @ApiOperation("List files of a data set")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "List of files in path", response = DataSetFile.class, responseContainer = "List"),
                      @ApiResponse(code = 400, message = "A path is not valid", response = RestResponseStatus.class),
                      @ApiResponse(code = 403, message = "Access to the path is restricted", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "Data set does not exist", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Failed to list files", response = RestResponseStatus.class)
                  })
    public Response listFiles(@PathParam("id") final String dataSourceId, @QueryParam("path") final String path) {
        log.entry(dataSourceId, path);

        // List files at path
        final DataSource dataSource = findDataSource(dataSourceId);
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
            log.error("Failed to list data source files at path {}: {}", path, e, e);
            final RestResponseStatus status = new RestResponseStatus.ResponseStatusBuilder()
                .message(getMessage("catalog.datasource.listFiles.error", path))
                .url(request.getRequestURI())
                .setDeveloperMessage(e)
                .buildError();
            throw new InternalServerErrorException(Response.serverError().entity(status).build());
        }

        return Response.ok(log.exit(files)).build();
    }

    /**
     * Gets the data source with the specified id.
     *
     * @throws NotFoundException if the data source does not exist
     */
    @Nonnull
    private DataSource findDataSource(@Nonnull final String id) {
        return metadataService.read(() -> dataSourceProvider.findDataSource(id))
            .orElseThrow(() -> {
                log.debug("Data source not found: {}", id);
                return new NotFoundException(getMessage("catalog.datasource.notFound"));
            });
    }
}
