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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.thinkbiganalytics.discovery.model.DefaultQueryResult;
import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

import java.util.List;

/**
 * The result of a Spark transformation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransformResponse {

    /**
     * Error message
     */
    private String message;

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
     * Success status of a transformation
     */
    private Status status;

    /**
     * Table name with the results
     */
    private String table;

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message.
     *
     * @param message the error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

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
     * Gets the status of this transformation.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of this transformation.
     *
     * @param status the status
     */
    public void setStatus(Status status) {
        this.status = status;
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
     * Success status of a transformation.
     */
    public enum Status {
        /**
         * Transformation resulted in an error
         */
        ERROR,

        /**
         * Transformation is in-progress
         */
        PENDING,

        /**
         * Transformation was successful
         */
        SUCCESS;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}
