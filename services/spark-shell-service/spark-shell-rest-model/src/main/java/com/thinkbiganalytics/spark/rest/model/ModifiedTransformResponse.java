package com.thinkbiganalytics.spark.rest.model;

/*-
 * #%L
 * Spark Shell Service REST Model
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

public class ModifiedTransformResponse<T extends Object> {

    /**
     * Error message
     */
    private String message;

    /**
     * Progress of the transformation
     */
    private Double progress;

    /**
     * Result of a transformation
     */
    private T results;

    private TransformQueryResult originalResults;

    /**
     * Success status of a transformation
     */
    private TransformResponse.Status status;

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

    public ModifiedTransformResponse() {

    }

    public ModifiedTransformResponse(TransformResponse response) {
        this.originalResults = response.getResults();
        this.message = response.getMessage();
        this.progress = response.getProgress();
        this.status = response.getStatus();
        this.table = response.getTable();
        this.actualCols = response.getActualCols();
        this.actualRows = response.getActualRows();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public T getResults() {
        return results;
    }

    public void setResults(T results) {
        this.results = results;
    }

    public TransformQueryResult getOriginalResults() {
        return originalResults;
    }

    public void setOriginalResults(TransformQueryResult originalResults) {
        this.originalResults = originalResults;
    }

    public TransformResponse.Status getStatus() {
        return status;
    }

    public void setStatus(TransformResponse.Status status) {
        this.status = status;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Integer getActualRows() {
        return actualRows;
    }

    public void setActualRows(Integer actualRows) {
        this.actualRows = actualRows;
    }

    public Integer getActualCols() {
        return actualCols;
    }

    public void setActualCols(Integer actualCols) {
        this.actualCols = actualCols;
    }


}
