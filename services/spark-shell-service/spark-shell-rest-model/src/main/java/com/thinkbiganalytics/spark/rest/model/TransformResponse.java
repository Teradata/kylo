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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

import java.util.List;

/**
 * The result of a Spark transformation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransformResponse extends SimpleResponse {

    /**
     * Profiled column statistics.
     */
    private List<OutputRow> profile;

    /**
     * Progress of the transformation
     */
    private Double progress;

    /**
     * Result of a transformation
     */
    private TransformQueryResult results;

    /**
     * Table name with the results
     */
    private String table;

    /**
     * Actual rows in underlying resultset (may be different than results returned to client due to paging)
     */
    private Integer actualRows;

    /**
     * Actual cols in underlying resultset (may be different than results returned to client due to paging)
     */
    private Integer actualCols;

    /**
     * Gets the column statistics.
     *
     * @return the column statistics
     */
    public List<OutputRow> getProfile() {
        return profile;
    }

    /**
     * Sets the column statistics.
     *
     * @param profile the column statistics
     */
    public void setProfile(List<OutputRow> profile) {
        this.profile = profile;
    }

    /**
     * Gets the progress of the transformation.
     *
     * @return the transformation progress
     */
    public Double getProgress() {
        return progress;
    }

    /**
     * Sets the progress of the transformation.
     *
     * @param progress the transformation progress
     */
    public void setProgress(Double progress) {
        this.progress = progress;
    }

    /**
     * Gets the results of this transformation.
     *
     * @return the results
     */
    public TransformQueryResult getResults() {
        return results;
    }

    /**
     * Sets the results of this transformation.
     *
     * @param results the results
     */
    public void setResults(TransformQueryResult results) {
        this.results = results;
    }

    /**
     * Gets the table with the results.
     *
     * @return the table name
     */
    public String getTable() {
        return table;
    }

    /**
     * Sets the table with the results.
     *
     * @param table the table name
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * Actual rows returned by the query but may be different than results due to paging
     *
     * @return rows
     */
    public Integer getActualRows() {
        return actualRows;
    }

    public void setActualRows(Integer actualRows) {
        this.actualRows = actualRows;
    }

    /**
     * Actual cols returned by the query but may be different than results due to paging
     *
     * @return cols
     */
    public Integer getActualCols() {
        return actualCols;
    }

    public void setActualCols(Integer actualCols) {
        this.actualCols = actualCols;
    }
}
