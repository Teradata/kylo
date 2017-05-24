package com.thinkbiganalytics.hive.rest.controller;

/*-
 * #%L
 * thinkbig-thrift-proxy-controller
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.discovery.schema.DatabaseMetadata;
import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.hive.service.HiveMetastoreService;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;

import java.security.AccessControlException;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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

@Api(tags = "Feed Manager - Tables", produces = "application/json")
@Path(HiveRestController.BASE)
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - Tables", description = "access to Hive tables"))
public class HiveRestController {

    private static final Logger log = LoggerFactory.getLogger(HiveRestController.class);

    public static final String BASE = "/v1/hive";

    @Autowired
    private Environment env;

    @Autowired
    private HiveService hiveService;

    @Autowired
    private HiveMetastoreService hiveMetadataService;

    @GET
    @Path("/test-connection")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Verifies the connection to Hive.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the connection status.", response = Boolean.class)
    )
    public Response testConnection() {
        boolean valid = false;
        try {
            valid = hiveService.testConnection();
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception ", e);
        }
        return Response.ok(valid).build();
    }

    @GET
    @Path("/table-columns")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists all columns from all tables.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the list of columns.", response = DatabaseMetadata.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "Hive is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getTableColumns() {
        List<DatabaseMetadata> list;
        try {
            boolean userImpersonationEnabled = Boolean.valueOf(env.getProperty("hive.userImpersonation.enabled"));
            if (userImpersonationEnabled) {
                List<String> tables = hiveService.getAllTablesForImpersonatedUser();
                list = hiveMetadataService.getTableColumns(tables);
            } else {
                list = hiveMetadataService.getTableColumns(null);
            }

        } catch (DataAccessException e) {
            log.error("Error Querying Hive Tables  for columns from the Metastore ", e);
            throw e;
        }
        return Response.ok(asJson(list)).build();
    }

    @GET
    @Path("/browse/{schema}/{table}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Queries the specified table.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the result.", response = QueryResult.class),
                      @ApiResponse(code = 500, message = "Hive is unavailable.", response = RestResponseStatus.class)
                  })
    public Response browseTable(@PathParam("schema") String schema, @PathParam("table") String table, @QueryParam("where") String where, @QueryParam("limit") @DefaultValue("20") Integer limit) {
        QueryResult list;
        try {
            list = hiveService.browse(schema, table, where, limit);
        } catch (DataAccessException e) {
            log.error("Error Querying Hive Tables  for schema: " + schema + ", table: " + table + " where: " + where + ", limit: " + limit, e);
            throw e;
        }
        return Response.ok(asJson(list)).build();
    }


    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Executes a Hive query.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the result.", response = QueryResult.class),
                      @ApiResponse(code = 500, message = "Hive is unavailable.", response = RestResponseStatus.class)
                  })
    public Response browseTable(@QueryParam("query") String query) {
        QueryResult list;
        try {
            list = hiveService.browse(query);
        } catch (DataAccessException e) {
            log.error("Error Querying Hive for query: " + query, e);
            throw e;
        }
        return Response.ok(asJson(list)).build();
    }


    @GET
    @Path("/query-result")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Executes a Hive query.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the result.", response = QueryResult.class),
                      @ApiResponse(code = 500, message = "Hive is unavailable.", response = RestResponseStatus.class)
                  })
    public Response queryResult(@QueryParam("query") String query) {
        QueryResult list;
        try {
            list = hiveService.query(query);
        } catch (DataAccessException e) {
            if (e.getCause().getMessage().contains("HiveAccessControlException Permission denied")) {
                throw new AccessControlException("You do not have permission to execute this hive query");
            } else {
                log.error("Error Querying Hive for query: " + query);
                throw e;
            }

        }
        return Response.ok(asJson(list)).build();
    }


    @GET
    @Path("/schemas/{schema}/tables/{table}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the schema of the specified table.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the table schema.", response = TableSchema.class),
                      @ApiResponse(code = 500, message = "Hive is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getTableSchema(@PathParam("schema") String schema, @PathParam("table") String table) {

        TableSchema tableSchema = hiveService.getTableSchema(schema, table);
        return Response.ok(asJson(tableSchema)).build();
    }

    @GET
    @Path("/schemas")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists the databases in Hive.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the databases.", response = String.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "Hive is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getSchemaNames() {
        List<String> schemas = hiveService.getSchemaNames();
        return Response.ok(asJson(schemas)).build();
    }

    @GET
    @Path("/table-schemas")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the schema of every table.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the table schemas.", response = TableSchema.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "Hive is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getAllTableSchemas() {
        //  List<TableSchema> schemas = hiveService.getAllTableSchemas();
        List<TableSchema> schemas;
        try {
            schemas = hiveMetadataService.getTableSchemas();
        } catch (DataAccessException e) {
            log.error("Error listing Hive Table schemas from the metastore ", e);
            throw e;
        }
        return Response.ok(asJson(schemas)).build();
    }


    @GET
    @Path("/tables")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists every table in Hive.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the table names.", response = String.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "Hive is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getTables() {
        List<String> tables;
        boolean userImpersonationEnabled = Boolean.valueOf(env.getProperty("hive.userImpersonation.enabled"));
        if (userImpersonationEnabled) {
            tables = hiveService.getAllTablesForImpersonatedUser();
        } else {
            try {
                tables = hiveMetadataService.getAllTables();
            } catch (DataAccessException e) {
                log.error("Error listing Hive Tables from the metastore ", e);
                throw e;
            }
        }
        return Response.ok(asJson(tables)).build();
    }

    @GET
    @Path("/schemas/{schema}/tables")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists the tables in the specified database.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the table names.", response = String.class, responseContainer = "List"),
                      @ApiResponse(code = 500, message = "Hive is unavailable.", response = RestResponseStatus.class)
                  })
    public Response getTableNames(@PathParam("schema") String schema) {
        boolean userImpersonationEnabled = Boolean.valueOf(env.getProperty("hive.userImpersonation.enabled"));
        List<String> tables;
        if (userImpersonationEnabled) {
            tables = hiveService.getTablesForImpersonatedUser(schema);
        } else {
            tables = hiveService.getTables(schema);
        }
        return Response.ok(asJson(tables)).build();
    }

    private String asJson(Object object) {
        String json = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON String ", e);
        }
        return json;
    }
}
