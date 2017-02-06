package com.thinkbiganalytics.discovery.model;

/*-
 * #%L
 * thinkbig-schema-discovery-model2
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
 * Model used to pass the query results
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultQueryResult implements QueryResult {

    private String query;

    @JsonDeserialize(contentAs = DefaultQueryResultColumn.class)
    @JsonSerialize(contentAs = DefaultQueryResultColumn.class)
    private List<QueryResultColumn> columns;

    @JsonDeserialize(contentAs = DefaultQueryResultColumn.class)
    @JsonSerialize(contentAs = DefaultQueryResultColumn.class)
    private Map<String, QueryResultColumn> columnFieldMap;

    @JsonDeserialize(contentAs = DefaultQueryResultColumn.class)
    @JsonSerialize(contentAs = DefaultQueryResultColumn.class)
    private Map<String, QueryResultColumn> columnDisplayNameMap;

    private List<Map<String, Object>> rows;

    /**
     * constructor
     *
     * @param query the query string
     */
    public DefaultQueryResult(@JsonProperty("query") String query) {
        this.query = query;
    }

    @Override
    public List<QueryResultColumn> getColumns() {
        return columns;
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
    public boolean isEmpty() {
        return this.getRows().isEmpty();
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



