package com.thinkbiganalytics.spark.model;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import com.thinkbiganalytics.spark.rest.model.TransformValidationResult;

import java.util.List;

/**
 * Result of a wrangler transformation.
 */
public class TransformResult {

    /**
     * Columns in the data set.
     */
    private List<QueryResultColumn> columns;

    /**
     * Spark SQL data set.
     */
    private DataSet dataSet;

    /**
     * Profile statistics.
     */
    private List<OutputRow> profile;

    /**
     * Validation results.
     */
    private List<List<TransformValidationResult>> validationResults;

    public List<QueryResultColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<QueryResultColumn> columns) {
        this.columns = columns;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public List<OutputRow> getProfile() {
        return profile;
    }

    public void setProfile(List<OutputRow> profile) {
        this.profile = profile;
    }

    public List<List<TransformValidationResult>> getValidationResults() {
        return validationResults;
    }

    public void setValidationResults(List<List<TransformValidationResult>> validationResults) {
        this.validationResults = validationResults;
    }
}
