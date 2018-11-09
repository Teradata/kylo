package com.thinkbiganalytics.kylo.catalog.rest.controller;

import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.CatalogModelTransform;
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
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.catalog.ConnectorProvider;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.RoleMembershipChange;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
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
    private SecurityService securityService;

    @Inject
    private SecurityModelTransform securityTransform;

    @Inject
    ConnectorPluginManager pluginManager;
    
    @Inject
    private CatalogModelTransform modelTransform;

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
    public Response getConnector(@PathParam("id") final String connectorId,
                                 @QueryParam("encrypt") @DefaultValue("true") final boolean encryptCredentials) {
        log.entry(connectorId);
        
        return metadataService.read(() -> {
            com.thinkbiganalytics.metadata.api.catalog.Connector.ID connId = connectorProvider.resolveId(connectorId);
            
            return connectorProvider.find(connId)
                .map(modelTransform.connectorToRestModel(true, encryptCredentials))
                .map(conn -> Response.ok(log.exit(conn)).build())
                .orElseThrow(() -> {
                    log.debug("Connector not found: {}", connectorId);
                    return new NotFoundException(getMessage("catalog.connector.notFound.id", connectorId));
                });
        });
    }

    @GET
    @ApiOperation("Lists all connectors")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the connectors", response = Connector.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    public Response listConnectors(@QueryParam("inactive") @DefaultValue("false") boolean includeInactive) {
        log.entry();
        return metadataService.read(() -> {
            List<Connector> connectors = connectorProvider.findAll(includeInactive).stream()
                .map(modelTransform.connectorToRestModel())
                .collect(Collectors.toList());
            return Response.ok(log.exit(connectors)).build();
        });
    }

    @GET
    @ApiOperation("Gets the specified connector plugin")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the connector plugin", response = ConnectorPluginDescriptor.class),
                      @ApiResponse(code = 404, message = "Connector or plugin was not found", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Internal server error", response = RestResponseStatus.class)
                  })
    @Path("{id}/plugin")
    public Response getConnectorPlugin(@PathParam("id") final String connectorId) {
        log.entry(connectorId);
        
        String pluginId = metadataService.read(() -> {
            com.thinkbiganalytics.metadata.api.catalog.Connector.ID connId = connectorProvider.resolveId(connectorId);
            
            return connectorProvider.find(connId)
                .map(conn -> conn.getPluginId())
                .orElseThrow(() -> {
                    log.debug("Connector not found: {}", connectorId);
                    return new NotFoundException(getMessage("catalog.connector.notFound.id", connectorId));
                });
        });
        
        return pluginManager.getPlugin(pluginId)
            .map(plugin -> plugin.getDescriptor())
            .map(descr -> Response.ok(log.exit(descr)).build())
            .orElseThrow(() -> {
                log.debug("Connector plugin not found: {}", connectorId);
                return new NotFoundException(getMessage("catalog.connector.notFound.id", connectorId));
            });
    }


    @GET
    @Path("{id}/actions/available")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of available actions that may be permitted or revoked on a connector.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A connector with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getAvailableActions(@PathParam("id") final String connectorIdStr) {
        log.debug("Get available actions for connector: {}", connectorIdStr);

        return this.securityService.getAvailableConnectorActions(connectorIdStr)
            .map(g -> Response.ok(g).build())
            .orElseThrow(() -> new WebApplicationException("A connector with the given ID does not exist: " + connectorIdStr, Response.Status.NOT_FOUND));
    }

    @GET
    @Path("{id}/actions/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of actions permitted for the given username and/or groups.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A connector with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getAllowedActions(@PathParam("id") final String connectorIdStr, @QueryParam("user") final Set<String> userNames, @QueryParam("group") final Set<String> groupNames) {
        log.debug("Get allowed actions for connector: {}", connectorIdStr);

        Set<? extends Principal> users = Arrays.stream(this.securityTransform.asUserPrincipals(userNames)).collect(Collectors.toSet());
        Set<? extends Principal> groups = Arrays.stream(this.securityTransform.asGroupPrincipals(groupNames)).collect(Collectors.toSet());

        return this.securityService.getAllowedConnectorActions(connectorIdStr, Stream.concat(users.stream(), groups.stream()).collect(Collectors.toSet()))
            .map(g -> Response.ok(g).build())
            .orElseThrow(() -> new WebApplicationException("A connector with the given ID does not exist: " + connectorIdStr, Response.Status.NOT_FOUND));
    }

    @GET
    @Path("{id}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of assigned members the connector's roles")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the role memberships.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A connector with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getRoleMemberships(@PathParam("id") final String connectorIdStr, @QueryParam("verbose") @DefaultValue("false") final boolean verbose) {
        return this.securityService.getConnectorRoleMemberships(connectorIdStr)
            .map(m -> Response.ok(m).build())
            .orElseThrow(() -> new WebApplicationException("A connector with the given ID does not exist: " + connectorIdStr, Response.Status.NOT_FOUND));
    }

    @POST
    @Path("{id}/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the members of one of a connector's roles.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The permissions were changed successfully.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "No connector exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public Response postPermissionsChange(@PathParam("id") final String connectorIdStr, final RoleMembershipChange changes) {
        return this.securityService.changeConnectorRoleMemberships(connectorIdStr, changes)
            .map(m -> Response.ok(m).build())
            .orElseThrow(() -> new WebApplicationException("Either a connector with the ID \"" + connectorIdStr + "\" does not exist or it does not have a role named \""
                                                           + changes.getRoleName() + "\"", Response.Status.NOT_FOUND));
    }
}
