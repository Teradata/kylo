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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.discovery.model.DefaultQueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Model used to pass the query results
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransformQueryResult extends DefaultQueryResult {

    /**
     * Validator reject reasons for each row.
     */
    private List<List<TransformValidationResult>> validationResults;

    public TransformQueryResult(@JsonProperty("query") final String query) {
        super(query);
    }

    /**
     * Adds the specified row and validation results to this result.
     */
    public void addRow(@Nonnull final Map<String, Object> row, @Nullable final List<TransformValidationResult> rejectReasons) {
        addRow(row);

        if (validationResults == null) {
            validationResults = new ArrayList<>();
        }
        validationResults.add(rejectReasons);
    }

    /**
     * Gets the list of validation results for each row.
     */
    public List<List<TransformValidationResult>> getValidationResults() {
        return validationResults;
    }
}
