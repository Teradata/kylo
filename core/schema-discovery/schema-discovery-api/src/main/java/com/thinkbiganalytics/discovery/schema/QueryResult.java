package com.thinkbiganalytics.discovery.schema;

/*-
 * #%L
 * thinkbig-schema-discovery-api
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

import java.util.List;
import java.util.Map;

/**
 * Represents a query result
 */
public interface QueryResult {

    /**
     * Get columns in query result
     *
     * @return list of {@link QueryResultColumn}
     */
    List<QueryResultColumn> getColumns();

    /**
     * Set columns in query result
     *
     * @param columns list of {@link QueryResultColumn}
     */
    void setColumns(List<QueryResultColumn> columns);

    /**
     * Whether query result is empty
     *
     * @return true/false indicating if query result is empty
     */
    boolean isEmpty();

    /**
     * Get rows in query result
     *
     * @return list of rows
     */
    List<Map<String, Object>> getRows();

    /**
     * Add row in query result
     *
     * @param data a row
     */
    void addRow(Map<String, Object> data);

    /**
     * Get query string
     *
     * @return query
     */
    String getQuery();

    /**
     * Get column field map
     *
     * @return map
     */
    Map<String, QueryResultColumn> getColumnFieldMap();

    /**
     * Get column display name map
     *
     * @return map
     */
    Map<String, QueryResultColumn> getColumnDisplayNameMap();
}
