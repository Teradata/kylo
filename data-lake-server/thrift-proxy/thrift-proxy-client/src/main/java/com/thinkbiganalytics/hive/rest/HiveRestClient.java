package com.thinkbiganalytics.hive.rest;


import com.thinkbiganalytics.db.model.query.QueryResult;
import com.thinkbiganalytics.db.model.schema.DatabaseMetadata;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyClientException;
import com.thinkbiganalytics.rest.JerseyRestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.WebTarget;

/**
 * Created by sr186054 on 1/9/16.
 */
public class HiveRestClient extends JerseyRestClient {
    private String apiPath = "/api/v1/hive";


    public HiveRestClient(JerseyClientConfig config) {
        super(config);
    }

    protected WebTarget getBaseTarget() {
        WebTarget target = super.getBaseTarget();
        return target.path(apiPath);
    }

    public List<String> getSchemas() throws JerseyClientException {
        List<String> list = get("/schemas", null, List.class);
         return list;
    }

    public List<TableSchema> getAllTableSchemas() throws JerseyClientException {
        List<TableSchema> list = get("/table-schemas", null, List.class);
        return list;
    }

    public List<DatabaseMetadata> getTablesAndColumns() throws JerseyClientException {
        List<DatabaseMetadata> list = get("/table-columns", null, List.class);
        return list;
    }


    public List<String> getTables() throws JerseyClientException {
        List<String> list = get("/tables", null, List.class);
        return list;
    }

    public TableSchema getTable(String schema, String table) throws JerseyClientException {
        TableSchema tableSchema = get("/schemas/"+schema+"/tables/"+table, null, TableSchema.class);
        return tableSchema;
    }
    public QueryResult browse(String schema, String table, String where) throws JerseyClientException {
        Map<String,Object>params = new HashMap<>();
        params.put("where",where);
        QueryResult rows = get("/browse/"+schema+"/"+table, params, QueryResult.class);
        return rows;
    }

    public QueryResult query(String query) throws JerseyClientException {
        Map<String,Object>params = new HashMap<>();
        params.put("query",query);
        QueryResult rows = get("/query", params, QueryResult.class);
        return rows;
    }

    public QueryResult queryResult(String query) throws JerseyClientException {
        Map<String,Object>params = new HashMap<>();
        params.put("query",query);
        QueryResult rows = get("/query-result", params, QueryResult.class);
        return rows;
    }






}


