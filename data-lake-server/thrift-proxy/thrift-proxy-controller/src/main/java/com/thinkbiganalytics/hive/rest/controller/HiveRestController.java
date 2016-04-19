package com.thinkbiganalytics.hive.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.db.model.query.QueryResult;
import com.thinkbiganalytics.db.model.schema.DatabaseMetadata;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.hive.service.HiveMetastoreService;
import com.thinkbiganalytics.hive.service.HiveService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by sr186054 on 2/12/16.
 */
@Component
@Path("/v1/hive")
public class HiveRestController {

    @Autowired
    HiveService hiveService;

    @Autowired
    HiveMetastoreService hiveMetadataService;
    
    public HiveService getHiveService(){
        return hiveService;
    }

    public HiveMetastoreService getHiveMetadataService(){
        return hiveMetadataService;
    }

    public HiveRestController(){

    }
    @PostConstruct
    private void init() {
        int i  = 0;
    }

    /*
    @DELETE
    @Path("/schemas/{schema}/tables/{table}")
    @Produces({MediaType.APPLICATION_JSON })
    public Response dropTable(@PathParam("schema") String schema, @PathParam("table") String table) {

        boolean success = false;
        try {
            success = getHiveService().dropTable(schema, table);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        String json ="{success:"+success+"}";
        return Response.ok(asJson(json)).build();
    }
    */

    @GET
    @Path("/table-columns")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getTableColumns() {
        List<DatabaseMetadata> list = null;
        try {
        list = getHiveMetadataService().getTableColumns();
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return Response.ok(asJson(list)).build();
    }

    @GET
    @Path("/browse/{schema}/{table}")
    @Produces({MediaType.APPLICATION_JSON })
    public Response browseTable(@PathParam("schema") String schema, @PathParam("table") String table, @QueryParam("where") String where,@QueryParam("limit") @DefaultValue("20") Integer limit) {
        QueryResult list = null;
        try {
            list = getHiveService().browse(schema, table, where, limit);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return Response.ok(asJson(list)).build();
    }


    @GET
    @Path("/query")
    @Produces({MediaType.APPLICATION_JSON })
    public Response browseTable(@QueryParam("query") String query) {
        QueryResult list = null;
        try {
            list = getHiveService().browse(query);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return Response.ok(asJson(list)).build();
    }


    @GET
    @Path("/query-result")
    @Produces({MediaType.APPLICATION_JSON })
    public Response queryResult(@QueryParam("query") String query) {
       QueryResult list = null;
        try {
            list = getHiveService().query(query);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return Response.ok(asJson(list)).build();
    }



    @GET
    @Path("/schemas/{schema}/tables/{table}")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getTableSchema(@PathParam("schema") String schema, @PathParam("table") String table) {

        TableSchema tableSchema = getHiveService().getTableSchema(schema, table);
        return Response.ok(asJson(tableSchema)).build();
    }

    @GET
    @Path("/schemas")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getSchemaNames()
    {
        List<String> schemas = getHiveService().getSchemaNames();
        return Response.ok(asJson(schemas)).build();
    }

    @GET
    @Path("/table-schemas")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getAllTableSchemas()
    {
      //  List<TableSchema> schemas = getHiveService().getAllTableSchemas();
        List<TableSchema> schemas = null;
        try {
            schemas = getHiveMetadataService().getTableSchemas();
        }catch(DataAccessException e){
            e.printStackTrace();
        }
        return Response.ok(asJson(schemas)).build();
    }



    @GET
    @Path("/tables")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getTables()
    {
        List<String> tables = null;
        //List<String> tables = getHiveService().getAllTables();
        try {
       tables = getHiveMetadataService().getAllTables();
        }catch(DataAccessException e){
            e.printStackTrace();
        }
        return Response.ok(asJson(tables)).build();
    }

    @GET
    @Path("/schemas/{schema}/tables")
    @Produces({MediaType.APPLICATION_JSON })
    public Response getTableNames(@PathParam("schema")String schema)
    {
        List<String> tables = getHiveService().getTables(schema);
        return Response.ok(asJson(tables)).build();
    }

    private String asJson(Object object){
        String json = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

}
