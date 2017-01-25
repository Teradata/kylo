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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;

import java.security.AccessControlException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

@Api(tags = "Feed Manager: Tables", produces = "application/json")
@Path("/v1/hive")
public class HiveRestController {

    private static final Logger log = LoggerFactory.getLogger(HiveRestController.class);

    @Autowired
    Environment env;

    @Autowired
    HiveService hiveService;

    @Autowired
    HiveMetastoreService hiveMetadataService;

    public HiveService getHiveService() {
        return hiveService;
    }

    public HiveMetastoreService getHiveMetadataService() {
        return hiveMetadataService;
    }

    public HiveRestController() {

    }

    @PostConstruct
    private void init() {

    }


    @GET
    @Path("/test-connection")
    @Produces({MediaType.TEXT_PLAIN})
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
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTableColumns() {
        List<DatabaseMetadata> list = null;
        try {
            boolean userImpersonationEnabled = Boolean.valueOf(env.getProperty("hive.userImpersonation.enabled"));
            if (userImpersonationEnabled) {
                List<String> tables = getHiveService().getAllTablesForImpersonatedUser();
                list = getHiveMetadataService().getTableColumns(tables);
            } else {
                list = getHiveMetadataService().getTableColumns(null);
            }

        } catch (DataAccessException e) {
            log.error("Error Querying Hive Tables  for columns from the Metastore ", e);
            throw e;
        }
        return Response.ok(asJson(list)).build();
    }

    @GET
    @Path("/browse/{schema}/{table}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response browseTable(@PathParam("schema") String schema, @PathParam("table") String table, @QueryParam("where") String where, @QueryParam("limit") @DefaultValue("20") Integer limit) {
        QueryResult list = null;
        try {
            list = getHiveService().browse(schema, table, where, limit);
        } catch (DataAccessException e) {
            log.error("Error Querying Hive Tables  for schema: " + schema + ", table: " + table + " where: " + where + ", limit: " + limit, e);
            throw e;
        }
        return Response.ok(asJson(list)).build();
    }


    @GET
    @Path("/query")
    @Produces({MediaType.APPLICATION_JSON})
    public Response browseTable(@QueryParam("query") String query) {
        QueryResult list = null;
        try {
            list = getHiveService().browse(query);
        } catch (DataAccessException e) {
            log.error("Error Querying Hive for query: " + query, e);
            throw e;
        }
        return Response.ok(asJson(list)).build();
    }


    @GET
    @Path("/query-result")
    @Produces({MediaType.APPLICATION_JSON})
    public Response queryResult(@QueryParam("query") String query) {
        QueryResult list = null;
        try {
            list = getHiveService().query(query);
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
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTableSchema(@PathParam("schema") String schema, @PathParam("table") String table) {

        TableSchema tableSchema = getHiveService().getTableSchema(schema, table);
        return Response.ok(asJson(tableSchema)).build();
    }

    @GET
    @Path("/schemas")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSchemaNames() {
        List<String> schemas = getHiveService().getSchemaNames();
        return Response.ok(asJson(schemas)).build();
    }

    @GET
    @Path("/table-schemas")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllTableSchemas() {
        //  List<TableSchema> schemas = getHiveService().getAllTableSchemas();
        List<TableSchema> schemas = null;
        try {
            schemas = getHiveMetadataService().getTableSchemas();
        } catch (DataAccessException e) {
            log.error("Error listing Hive Table schemas from the metastore ", e);
            throw e;
        }
        return Response.ok(asJson(schemas)).build();
    }


    @GET
    @Path("/tables")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTables() {
        List<String> tables = null;
        boolean userImpersonationEnabled = Boolean.valueOf(env.getProperty("hive.userImpersonation.enabled"));
        if (userImpersonationEnabled) {
            tables = getHiveService().getAllTablesForImpersonatedUser();
        } else {
            try {
                tables = getHiveMetadataService().getAllTables();
            } catch (DataAccessException e) {
                log.error("Error listing Hive Tables from the metastore ", e);
                throw e;
            }
        }
        return Response.ok(asJson(tables)).build();
    }

    @GET
    @Path("/schemas/{schema}/tables")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTableNames(@PathParam("schema") String schema) {
        boolean userImpersonationEnabled = Boolean.valueOf(env.getProperty("hive.userImpersonation.enabled"));
        List<String> tables = null;
        if (userImpersonationEnabled) {
            tables = getHiveService().getTablesForImpersonatedUser(schema);
        } else {
            tables = getHiveService().getTables(schema);
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
