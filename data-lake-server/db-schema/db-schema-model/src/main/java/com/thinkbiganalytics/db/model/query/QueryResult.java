package com.thinkbiganalytics.db.model.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 3/31/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResult {

    private String query;

    public QueryResult(){

    }

    public QueryResult(@JsonProperty("query") String query) {
        this.query = query;
    }

    private List<QueryResultColumn> columns;

    private Map<String, QueryResultColumn> columnFieldMap;

    private Map<String, QueryResultColumn> columnDisplayNameMap;


    private List<Map<String,Object>> rows;

    public List<QueryResultColumn> getColumns() {
        return columns;
    }

    public boolean isEmpty(){
        return this.getRows().isEmpty();
    }

    public void setColumns(List<QueryResultColumn> columns) {
        this.columns = columns;
        this.columnFieldMap = new HashMap<>();
        this.columnDisplayNameMap = new HashMap<>();
        if(columns != null) {
            int index=0;
            for(QueryResultColumn column: columns){
                column.setIndex(index);
                columnFieldMap.put(column.getField(), column);
                columnDisplayNameMap.put(column.getDisplayName(),column);
                index++;
            }
        }
    }

    public List<Map<String,Object>> getRows() {
        if(rows == null){
            rows = new ArrayList<>();
        }
        return rows;
    }

    public void addRow( Map<String,Object> data){
         getRows().add(data);
    }

    public String getQuery() {
        return query;
    }

    public Map<String, QueryResultColumn> getColumnFieldMap() {
        return columnFieldMap;
    }

    public Map<String, QueryResultColumn> getColumnDisplayNameMap() {
        return columnDisplayNameMap;
    }
}
