package com.thinkbiganalytics.discovery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 3/31/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultQueryResult implements QueryResult {

    private String query;

    public DefaultQueryResult() {

    }

    public DefaultQueryResult(@JsonProperty("query") String query) {
        this.query = query;
    }

    @JsonDeserialize(contentAs=DefaultQueryResultColumn.class)
    @JsonSerialize(contentAs=DefaultQueryResultColumn.class)
    private List<QueryResultColumn> columns;

    @JsonDeserialize(contentAs=DefaultQueryResultColumn.class)
    @JsonSerialize(contentAs=DefaultQueryResultColumn.class)
    private Map<String, QueryResultColumn> columnFieldMap;

    @JsonDeserialize(contentAs=DefaultQueryResultColumn.class)
    @JsonSerialize(contentAs=DefaultQueryResultColumn.class)
    private Map<String, QueryResultColumn> columnDisplayNameMap;

    private List<Map<String, Object>> rows;

    @Override
    public List<QueryResultColumn> getColumns() {
        return columns;
    }

    @Override
    public boolean isEmpty() {
        return this.getRows().isEmpty();
    }

    @Override
    public void setColumns(List<QueryResultColumn> columns) {
        this.columns = columns;
        this.columnFieldMap = new HashMap<>();
        this.columnDisplayNameMap = new HashMap<>();
        if (columns != null) {
            int index = 0;
            for (QueryResultColumn column : columns) {
                column.setIndex(index);
                columnFieldMap.put(column.getField(), column);
                columnDisplayNameMap.put(column.getDisplayName(), column);
                index++;
            }
        }
    }

    @Override
    public List<Map<String, Object>> getRows() {
        if (rows == null) {
            rows = new ArrayList<>();
        }
        return rows;
    }

    @Override
    public void addRow(Map<String, Object> data) {
        getRows().add(data);
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Map<String, QueryResultColumn> getColumnFieldMap() {
        return columnFieldMap;
    }

    @Override
    public Map<String, QueryResultColumn> getColumnDisplayNameMap() {
        return columnDisplayNameMap;
    }
}



