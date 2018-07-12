package com.thinkbiganalytics.kylo.catalog.rest.controller;

import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;

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

import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;
import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorPluginDescriptor;
import com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(tags = "Feed Manager - Catalog", produces = "application/json")
@Path(ConnectorPluginController.BASE)
@Produces(MediaType.APPLICATION_JSON)
public class ConnectorPluginController extends AbstractCatalogController {

    private final XLogger log = XLoggerFactory.getXLogger(ConnectorPluginController.class);

    public static final String BASE = "/v1/catalog/connector/plugin";
    
    @Inject
    private ConnectorPluginManager pluginManager;

    @GET
    @ApiOperation("Gets the specified connector descriptor from the plugin")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the connector descriptor", response = Connector.class),
                      @ApiResponse(code = 404, message = "Connector plugin was not found", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("{id}")
    public Response getPlugin(@PathParam("id") final String pluginId) {
        log.entry(pluginId);
        return this.pluginManager.getPlugin(pluginId)
            .map(plugin -> Response.ok(log.exit(plugin.getDescriptor())).build())
            .orElse(Response.status(Status.NOT_FOUND).entity(log.exit("No connector plugin exits with the ID: " + pluginId)).build());
    }

    @GET
    @ApiOperation("Lists all connector descriptors from all plugins")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the connector descriptors from all plugins", response = Connector.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    public Response listPlugins() {
        log.entry();
        final List<ConnectorPluginDescriptor> descriptors = this.pluginManager.getPlugins().stream()
                        .map(ConnectorPlugin::getDescriptor)
                        .collect(Collectors.toList());
        return Response.ok(log.exit(descriptors)).build();
    }
}
