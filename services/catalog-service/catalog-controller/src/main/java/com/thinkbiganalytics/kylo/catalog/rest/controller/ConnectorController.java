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

import com.thinkbiganalytics.kylo.catalog.connector.ConnectorProvider;
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(tags = "Feed Manager - Catalog", produces = "application/json")
@Path(ConnectorController.BASE)
@Produces(MediaType.APPLICATION_JSON)
public class ConnectorController extends AbstractCatalogController {

    private final XLogger log = XLoggerFactory.getXLogger(ConnectorController.class);

    public static final String BASE = "/v1/catalog/connector";

    @Inject
    ConnectorProvider connectorProvider;

    @Inject
    MetadataAccess metadataService;

    @GET
    @ApiOperation("Gets the specified connector")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the connector", response = Connector.class),
                      @ApiResponse(code = 404, message = "Connector was not found", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("{id}")
    public Response getConnector(@PathParam("id") final String connectorId) {
        log.entry(connectorId);
        final Connector connector = metadataService.read(() -> connectorProvider.findConnector(connectorId))
            .orElseThrow(() -> {
                log.debug("Connector not found: {}", connectorId);
                return new NotFoundException(getMessage("catalog.controller.notFound"));
            });
        return Response.ok(log.exit(connector)).build();
    }

    @GET
    @ApiOperation("Lists all connectors")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the connectors", response = Connector.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    public Response listConnectors() {
        log.entry();
        final List<Connector> connectors = metadataService.read(() -> connectorProvider.findAllConnectors());
        return Response.ok(log.exit(connectors)).build();
    }
}
