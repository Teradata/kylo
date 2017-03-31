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
import com.thinkbiganalytics.feedmgr.nifi.DBCPConnectionPoolTableInfo;
import com.thinkbiganalytics.feedmgr.rest.Model;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinitionProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceCriteria;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.UserDatasource;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.security.AccessControlException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@Api(tags = "Feed Manager - Data Sources", produces = "application/json")
@Component
@Path("/v1/metadata/datasource")
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Data Sources", description = "manages data sources"))
public class DatasourceController {

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
    private DBCPConnectionPoolTableInfo dbcpConnectionPoolTableInfo;

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
            accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_DATASOURCES);

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
            accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_DATASOURCES);
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
            accessController.checkPermission(AccessController.SERVICES, sensitive ? FeedsAccessControl.ADMIN_DATASOURCES : FeedsAccessControl.ACCESS_DATASOURCES);

            com.thinkbiganalytics.metadata.api.datasource.Datasource.ID id = this.datasetProvider.resolve(idStr);
            com.thinkbiganalytics.metadata.api.datasource.Datasource ds = this.datasetProvider.getDatasource(id);

            if (ds != null) {
                return datasourceTransform.toDatasource(ds, sensitive ? DatasourceModelTransform.Level.ADMIN : DatasourceModelTransform.Level.FULL);
            } else {
                throw new NotFoundException("No datasource exists with the given ID: " + idStr);
            }
        });
    }

    /**
     * Deletes the datasource with the specified id.
     *
     * @param idStr the datasource id
     * @throws AccessControlException if the user does not have the {@code EDIT_DATASOURCES} permission
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
            accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_DATASOURCES);

            final com.thinkbiganalytics.metadata.api.datasource.Datasource.ID id = datasetProvider.resolve(idStr);
            final com.thinkbiganalytics.metadata.api.datasource.Datasource datasource = datasetProvider.getDatasource(id);
            if (datasource == null) {
                throw new NotFoundException("No datasource exists with the given ID: " + idStr);
            }
            if (datasource instanceof com.thinkbiganalytics.metadata.api.datasource.JdbcDatasource) {
                ((com.thinkbiganalytics.metadata.api.datasource.JdbcDatasource) datasource).getControllerServiceId()
                    .ifPresent(controllerServiceId -> nifiRestClient.controllerServices().disableAndDeleteAsync(controllerServiceId));
            }
            if (datasource instanceof com.thinkbiganalytics.metadata.api.datasource.UserDatasource) {
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
            accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_DATASOURCES);

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
        return metadata.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_DATASOURCES);

            final com.thinkbiganalytics.metadata.api.datasource.Datasource datasource = datasetProvider.getDatasource(datasetProvider.resolve(idStr));
            if (datasource instanceof com.thinkbiganalytics.metadata.api.datasource.JdbcDatasource) {
                final List<String> tables = dbcpConnectionPoolTableInfo.getTableNamesForDatasource((com.thinkbiganalytics.metadata.api.datasource.JdbcDatasource) datasource, schema, tableName);
                return Response.ok(tables).build();
            } else {
                throw new NotFoundException("No JDBC datasource exists with the given ID: " + idStr);
            }
        });
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
        return metadata.read(() -> {
            accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_DATASOURCES);

            final com.thinkbiganalytics.metadata.api.datasource.Datasource datasource = datasetProvider.getDatasource(datasetProvider.resolve(idStr));
            if (datasource instanceof com.thinkbiganalytics.metadata.api.datasource.JdbcDatasource) {
                final TableSchema tableSchema = dbcpConnectionPoolTableInfo.describeTableForDatasource((com.thinkbiganalytics.metadata.api.datasource.JdbcDatasource) datasource, schema, tableName);
                return Response.ok(tableSchema).build();
            } else {
                throw new NotFoundException("No JDBC datasource exists with the given ID: " + idStr);
            }
        });
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
                criteria.type(com.thinkbiganalytics.metadata.api.datasource.JdbcDatasource.class);
            }
        }

        return criteria;
    }
}
