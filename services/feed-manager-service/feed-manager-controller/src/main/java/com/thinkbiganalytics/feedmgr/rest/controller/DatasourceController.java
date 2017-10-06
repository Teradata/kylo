package com.thinkbiganalytics.feedmgr.rest.controller;

/*-
 * #%L
 * thinkbig-metadata-rest-controller
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

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.Formatters;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.nifi.controllerservice.DBCPConnectionPoolService;
import com.thinkbiganalytics.feedmgr.rest.Model;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinitionProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.JdbcDatasourceDetails;
import com.thinkbiganalytics.metadata.api.security.AccessControlled;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceCriteria;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.UserDatasource;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.RoleMembershipChange;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.AccessControlException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@Api(tags = "Feed Manager - Data Sources", produces = "application/json")
@Component
@Path("/v1/metadata/datasource")
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Data Sources", description = "manages data sources"))
public class DatasourceController {

    private static final Logger log = LoggerFactory.getLogger(DatasourceController.class);

    /**
     * Ensures the user has the correct permissions.
     */
    @Inject
    private AccessController accessController;

    @Inject
    private DatasourceProvider datasetProvider;

    @Inject
    private DatasourceDefinitionProvider datasourceDefinitionProvider;

    /**
     * The {@code Datasource} transformer
     */
    @Inject
    private DatasourceModelTransform datasourceTransform;

    @Inject
    private MetadataAccess metadata;

    /**
     * NiFi REST client
     */
    @Inject
    private NiFiRestClient nifiRestClient;

    /**
     * Provides table lists and schemas from JDBC connections.
     */
    @Inject
    private DBCPConnectionPoolService dbcpConnectionPoolTableInfo;

    @Inject
    private SecurityService securityService;

    @Inject
    private SecurityModelTransform securityTransform;

    /**
     * Gets a list of datasource that match the criteria provided.
     *
     * @param name   the name of a data source
     * @param owner  the owner of a data source
     * @param on     the time of the data source
     * @param after  to specify data source to created after the time given
     * @param before to specify data source to created after the time given
     * @param type   the type of the data source
     * @return a list of data sources
     * @throws AccessControlException if the user does not have the {@code ACCESS_DATASOURCES} permission
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the data sources matching the provided criteria.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data sources.", response = Datasource.class, responseContainer = "List"),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Kylo is unavailable.", response = RestResponseStatus.class)
                  })
    public List<Datasource> getDatasources(@QueryParam(DatasourceCriteria.NAME) final String name,
                                           @QueryParam(DatasourceCriteria.OWNER) final String owner,
                                           @QueryParam(DatasourceCriteria.ON) final String on,
                                           @QueryParam(DatasourceCriteria.AFTER) final String after,
                                           @QueryParam(DatasourceCriteria.BEFORE) final String before,
                                           @QueryParam(DatasourceCriteria.TYPE) final String type) {
        return this.metadata.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_DATASOURCES);

            com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria criteria = createDatasourceCriteria(name, owner, on, after, before, type);
            return datasetProvider.getDatasources(criteria).stream()
                .map(ds -> datasourceTransform.toDatasource(ds, DatasourceModelTransform.Level.CONNECTIONS))
                .collect(Collectors.toList());
        });
    }

    /**
     * Updates the specified data source.
     *
     * @param datasource the data source
     * @return the data source
     * @throws AccessControlException if the user does not have the {@code EDIT_DATASOURCES} permission
     */
    @POST
    @ApiOperation("Updates the specified data source.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data source.", response = Datasource.class),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Kylo is unavailable.", response = RestResponseStatus.class)
                  })
    public Datasource postDatasource(@Nonnull final UserDatasource datasource) {
        return metadata.commit(() -> {
            accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_DATASOURCES);

            datasourceTransform.toDomain(datasource);
            if (datasource instanceof JdbcDatasource) {
                ((JdbcDatasource) datasource).setPassword(null);
            }
            return datasource;
        });
    }

    /**
     * Gets the datasource with the id provided.
     *
     * @param idStr     the datasource id
     * @param sensitive {@code true} to include sensitive fields in the response, or {@code false} otherwise
     * @return the datasource object
     * @throws AccessControlException if the user does not have the {@code ACCESS_DATASOURCES} permission
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the data source with the provided id.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data source.", response = Datasource.class),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The data source does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Kylo is unavailable.", response = RestResponseStatus.class)
                  })
    public Datasource getDatasource(@PathParam("id") String idStr, @QueryParam("sensitive") boolean sensitive) {
        return this.metadata.read(() -> {
            // Check permissions
            accessController.checkPermission(AccessController.SERVICES, sensitive ? FeedServicesAccessControl.ADMIN_DATASOURCES : FeedServicesAccessControl.ACCESS_DATASOURCES);

            com.thinkbiganalytics.metadata.api.datasource.Datasource.ID id = this.datasetProvider.resolve(idStr);
            com.thinkbiganalytics.metadata.api.datasource.Datasource ds = this.datasetProvider.getDatasource(id);

            if (ds != null) {
                final Datasource restModel = datasourceTransform.toDatasource(ds, sensitive ? DatasourceModelTransform.Level.ADMIN : DatasourceModelTransform.Level.FULL);
                if (ds instanceof AccessControlled) {
                    securityTransform.applyAccessControl((AccessControlled) ds, restModel);
                }
                return restModel;
            } else {
                throw new NotFoundException("No datasource exists with the given ID: " + idStr);
            }
        });
    }

    /**
     * Deletes the datasource with the specified id.
     *
     * @param idStr the datasource id
     */
    @DELETE
    @Path("{id}")
    @ApiOperation("Deletes the data source with the provided id.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The data source was deleted."),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The data source does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Kylo is unavailable.", response = RestResponseStatus.class)
                  })
    public void deleteDatasource(@PathParam("id") final String idStr) {
        metadata.commit(() -> {
            final com.thinkbiganalytics.metadata.api.datasource.Datasource.ID id = datasetProvider.resolve(idStr);
            final com.thinkbiganalytics.metadata.api.datasource.Datasource datasource = datasetProvider.getDatasource(id);
            if (datasource == null) {
                throw new NotFoundException("No datasource exists with the given ID: " + idStr);
            }
            if (datasource instanceof com.thinkbiganalytics.metadata.api.datasource.UserDatasource) {
                final com.thinkbiganalytics.metadata.api.datasource.UserDatasource userDatasource = (com.thinkbiganalytics.metadata.api.datasource.UserDatasource) datasource;
                userDatasource.getDetails().ifPresent(details -> {
                    if (details instanceof JdbcDatasourceDetails) {
                        ((JdbcDatasourceDetails) details).getControllerServiceId()
                            .ifPresent(controllerServiceId -> nifiRestClient.controllerServices().disableAndDeleteAsync(controllerServiceId));
                    }
                });
                datasetProvider.removeDatasource(id);
            }
        });
    }

    /**
     * get the datasource definitions
     *
     * @return the set of datasource definitions
     * @throws AccessControlException if the user does not have the {@code ACCESS_DATASOURCES} permission
     */
    @GET
    @Path("/datasource-definitions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the data source definitions.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the data source definitions.", response = DatasourceDefinition.class, responseContainer = "Set"),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "Kylo is unavailable.", response = RestResponseStatus.class)
                  })
    public Set<DatasourceDefinition> getDatasourceDefinitions() {
        return this.metadata.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_DATASOURCES);

            Set<com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition> datasourceDefinitions = this.datasourceDefinitionProvider.getDatasourceDefinitions();
            if (datasourceDefinitions != null) {
                return new HashSet<>(Collections2.transform(datasourceDefinitions, Model.DOMAIN_TO_DS_DEFINITION));
            }
            return null;
        });
    }

    /**
     * Gets the table names from the specified data source.
     *
     * @param idStr  the data source id
     * @param schema the schema name, or {@code null} for all schemas
     * @return the list of table names
     */
    @GET
    @Path("{id}/tables")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the table names from the data source.", notes = "Connects to the database specified by the data source.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the table names.", response = String.class, responseContainer = "List"),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "A JDBC data source with that id does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "NiFi or the database are unavailable.", response = RestResponseStatus.class)
                  })
    public Response getTableNames(@PathParam("id") final String idStr, @QueryParam("schema") final String schema, @QueryParam("tableName") final String tableName) {
        // Verify user has access to data source
        final Optional<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> id = metadata.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_DATASOURCES);

            final com.thinkbiganalytics.metadata.api.datasource.Datasource datasource = datasetProvider.getDatasource(datasetProvider.resolve(idStr));
            return Optional.ofNullable(datasource).map(com.thinkbiganalytics.metadata.api.datasource.Datasource::getId);
        });

        // Retrieve table names using system user
        return metadata.read(() -> {
            final List<String> tables = id.map(datasetProvider::getDatasource)
                .map(ds -> datasourceTransform.toDatasource(ds, DatasourceModelTransform.Level.ADMIN))
                .filter(JdbcDatasource.class::isInstance)
                .map(JdbcDatasource.class::cast)
                .map(datasource -> dbcpConnectionPoolTableInfo.getTableNamesForDatasource(datasource, schema, tableName))
                .orElseThrow(() -> new NotFoundException("No JDBC datasource exists with the given ID: " + idStr));
            return Response.ok(tables).build();
        }, MetadataAccess.SERVICE);
    }

    /**
     * Gets the schema of the specified table using the specified data source.
     *
     * @param idStr     the data source id
     * @param tableName the table name
     * @param schema    the schema name, or {@code null} to search all schemas
     * @return the table and field details
     */
    @GET
    @Path("{id}/tables/{tableName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the schema of the specified table.", notes = "Connects to the database specified by the data source.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the table schema.", response = TableSchema.class),
                      @ApiResponse(code = 403, message = "Access denied.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "A JDBC data source with that id does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "NiFi or the database are unavailable.", response = RestResponseStatus.class)
                  })
    public Response describeTable(@PathParam("id") final String idStr, @PathParam("tableName") final String tableName, @QueryParam("schema") final String schema) {
        // Verify user has access to data source
        final Optional<com.thinkbiganalytics.metadata.api.datasource.Datasource.ID> id = metadata.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_DATASOURCES);

            final com.thinkbiganalytics.metadata.api.datasource.Datasource datasource = datasetProvider.getDatasource(datasetProvider.resolve(idStr));
            return Optional.ofNullable(datasource).map(com.thinkbiganalytics.metadata.api.datasource.Datasource::getId);
        });

        // Retrieve table description using system user
        return metadata.read(() -> {
            final TableSchema tableSchema = id.map(datasetProvider::getDatasource)
                .map(ds -> datasourceTransform.toDatasource(ds, DatasourceModelTransform.Level.ADMIN))
                .filter(JdbcDatasource.class::isInstance)
                .map(JdbcDatasource.class::cast)
                .map(datasource -> dbcpConnectionPoolTableInfo.describeTableForDatasource(datasource, schema, tableName))
                .orElseThrow(() -> new NotFoundException("No JDBC datasource exists with the given ID: " + idStr));
            return Response.ok(tableSchema).build();
        }, MetadataAccess.SERVICE);
    }

    @GET
    @Path("{id}/actions/available")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of available actions that may be permitted or revoked on a data source.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A data source with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getAvailableActions(@PathParam("id") final String datasourceIdStr) {
        log.debug("Get available actions for data source: {}", datasourceIdStr);

        return this.securityService.getAvailableDatasourceActions(datasourceIdStr)
            .map(g -> Response.ok(g).build())
            .orElseThrow(() -> new WebApplicationException("A data source with the given ID does not exist: " + datasourceIdStr, Response.Status.NOT_FOUND));
    }

    @GET
    @Path("{id}/actions/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of actions permitted for the given username and/or groups.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the actions.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A data source with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getAllowedActions(@PathParam("id") final String datasourceIdStr, @QueryParam("user") final Set<String> userNames, @QueryParam("group") final Set<String> groupNames) {
        log.debug("Get allowed actions for data source: {}", datasourceIdStr);

        Set<? extends Principal> users = Arrays.stream(this.securityTransform.asUserPrincipals(userNames)).collect(Collectors.toSet());
        Set<? extends Principal> groups = Arrays.stream(this.securityTransform.asGroupPrincipals(groupNames)).collect(Collectors.toSet());

        return this.securityService.getAllowedDatasourceActions(datasourceIdStr, Stream.concat(users.stream(), groups.stream()).collect(Collectors.toSet()))
            .map(g -> Response.ok(g).build())
            .orElseThrow(() -> new WebApplicationException("A data source with the given ID does not exist: " + datasourceIdStr, Response.Status.NOT_FOUND));
    }

    @POST
    @Path("{id}/actions/allowed")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the permissions for a data source using the supplied permission change request.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The permissions were changed successfully.", response = ActionGroup.class),
                      @ApiResponse(code = 400, message = "The type is not valid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "No data source exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public Response postPermissionsChange(@PathParam("id") final String datasourceIdStr, final PermissionsChange changes) {

        return this.securityService.changeDatasourcePermissions(datasourceIdStr, changes)
            .map(g -> Response.ok(g).build())
            .orElseThrow(() -> new WebApplicationException("A data source with the given ID does not exist: " + datasourceIdStr, Response.Status.NOT_FOUND));
    }

    @GET
    @Path("{id}/actions/change")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Constructs and returns a permission change request for a set of users/groups containing the actions that the requester may permit or revoke.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the change request that may be modified by the client and re-posted.", response = PermissionsChange.class),
                      @ApiResponse(code = 400, message = "The type is not valid.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "No data source exists with the specified ID.", response = RestResponseStatus.class)
                  })
    public Response getAllowedPermissionsChange(@PathParam("id") final String datasourceIdStr,
                                                @QueryParam("type") final String changeType,
                                                @QueryParam("user") final Set<String> userNames,
                                                @QueryParam("group") final Set<String> groupNames) {
        if (StringUtils.isBlank(changeType)) {
            throw new WebApplicationException("The query parameter \"type\" is required", Response.Status.BAD_REQUEST);
        }

        Set<? extends Principal> users = Arrays.stream(this.securityTransform.asUserPrincipals(userNames)).collect(Collectors.toSet());
        Set<? extends Principal> groups = Arrays.stream(this.securityTransform.asGroupPrincipals(groupNames)).collect(Collectors.toSet());

        return this.securityService.createDatasourcePermissionChange(datasourceIdStr,
                                                                     PermissionsChange.ChangeType.valueOf(changeType.toUpperCase()),
                                                                     Stream.concat(users.stream(), groups.stream()).collect(Collectors.toSet()))
            .map(p -> Response.ok(p).build())
            .orElseThrow(() -> new WebApplicationException("A data source with the given ID does not exist: " + datasourceIdStr, Response.Status.NOT_FOUND));
    }

    @GET
    @Path("{id}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of assigned members the data source's roles")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the role memberships.", response = ActionGroup.class),
                      @ApiResponse(code = 404, message = "A data source with the given ID does not exist.", response = RestResponseStatus.class)
                  })
    public Response getRoleMemberships(@PathParam("id") final String datasourceIdStr, @QueryParam("verbose") @DefaultValue("false") final boolean verbose) {
        return this.securityService.getDatasourceRoleMemberships(datasourceIdStr)
            .map(m -> Response.ok(m).build())
            .orElseThrow(() -> new WebApplicationException("A data source with the given ID does not exist: " + datasourceIdStr, Response.Status.NOT_FOUND));
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
    public Response postPermissionsChange(@PathParam("id") final String datasourceIdStr, final RoleMembershipChange changes) {
        return this.securityService.changeDatasourceRoleMemberships(datasourceIdStr, changes)
            .map(m -> Response.ok(m).build())
            .orElseThrow(() -> new WebApplicationException("Either a data source with the ID \"" + datasourceIdStr + "\" does not exist or it does not have a role the named \""
                                                           + changes.getRoleName() + "\"", Response.Status.NOT_FOUND));
    }

    private com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria createDatasourceCriteria(String name,
                                                                                                      String owner,
                                                                                                      String on,
                                                                                                      String after,
                                                                                                      String before,
                                                                                                      String type) {
        com.thinkbiganalytics.metadata.api.datasource.DatasourceCriteria criteria = datasetProvider.datasetCriteria();

        if (StringUtils.isNotEmpty(name)) {
            criteria.name(name);
        }
        //        if (StringUtils.isNotEmpty(owner)) criteria.owner(owner);  // TODO implement
        if (StringUtils.isNotEmpty(on)) {
            criteria.createdOn(Formatters.parseDateTime(on));
        }
        if (StringUtils.isNotEmpty(after)) {
            criteria.createdAfter(Formatters.parseDateTime(after));
        }
        if (StringUtils.isNotEmpty(before)) {
            criteria.createdBefore(Formatters.parseDateTime(before));
        }
        if (StringUtils.isNotEmpty(type)) {
            if ("UserDatasource".equalsIgnoreCase(type)) {
                criteria.type(com.thinkbiganalytics.metadata.api.datasource.UserDatasource.class);
            }
        }

        return criteria;
    }
}
