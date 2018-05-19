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

import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceProvider;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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
public class DataSourceController {

    public static final String BASE = "/v1/catalog/datasource";

    @Inject
    DataSourceProvider dataSourceProvider;

    @Autowired
    @Qualifier("catalogMessages")
    MessageSource messages;

    @Inject
    MetadataAccess metadataService;

    @Inject
    HttpServletRequest request;

    @GET
    @ApiOperation("Gets the specified data source")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data source", response = DataSource.class),
                      @ApiResponse(code = 404, message = "Data source was not found", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("{id}")
    public Response getDataSource(@PathParam("id") final String dataSourceId) {
        final DataSource dataSource = metadataService.read(() -> dataSourceProvider.findDataSource(dataSourceId).orElseThrow(() -> new NotFoundException(getMessage("catalog.datasource.notFound"))));
        return Response.ok(dataSource).build();
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
        if (start != null && start < 0) {
            throw new BadRequestException(getMessage("catalog.datasource.getDataSources.invalidStart"));
        }
        if (limit != null && limit < 1) {
            throw new BadRequestException(getMessage("catalog.datasource.getDataSources.invalidLimit"));
        }

        final PageRequest pageRequest = new PageRequest((start != null) ? start : 0, (limit != null) ? limit : Integer.MAX_VALUE);
        final Page<DataSource> page = metadataService.read(() -> dataSourceProvider.findAllDataSources(pageRequest, filter));

        final SearchResult<DataSource> searchResult = new SearchResultImpl<>();
        searchResult.setData(page.getContent());
        searchResult.setRecordsTotal(page.getTotalElements());
        return Response.ok(searchResult).build();
    }

    /**
     * Gets the specified message in the current locale.
     */
    @Nonnull
    private String getMessage(@Nonnull final String code) {
        return messages.getMessage(code, null, RequestContextUtils.getLocale(request));
    }
}
