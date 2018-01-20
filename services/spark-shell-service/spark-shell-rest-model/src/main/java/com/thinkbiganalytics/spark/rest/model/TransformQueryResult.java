package com.thinkbiganalytics.spark.rest.model;

/*-
 * #%L
 * Spark Shell Service REST Model
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thinkbiganalytics.discovery.model.DefaultQueryResultColumn;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;

import java.util.List;

/**
 * Model used to pass the query results
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransformQueryResult {

    @JsonDeserialize(contentAs = DefaultQueryResultColumn.class)
    @JsonSerialize(contentAs = DefaultQueryResultColumn.class)
    private List<QueryResultColumn> columns;

    private List<List<Object>> rows;

    private List<List<TransformValidationResult>> validationResults;

    /**
     * Get columns in query result
     *
     * @return list of {@link QueryResultColumn}
     */
    public List<QueryResultColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<QueryResultColumn> columns) {
        this.columns = columns;
    }

    /**
     * Get rows in query result
     *
     * @return list of rows
     */
    public List<List<Object>> getRows() {
        return rows;
    }

    public void setRows(List<List<Object>> rows) {
        this.rows = rows;
    }

    /**
     * Gets the list of validation results for each row.
     */
    public List<List<TransformValidationResult>> getValidationResults() {
        return validationResults;
    }

    public void setValidationResults(List<List<TransformValidationResult>> validationResults) {
        this.validationResults = validationResults;
    }
}
