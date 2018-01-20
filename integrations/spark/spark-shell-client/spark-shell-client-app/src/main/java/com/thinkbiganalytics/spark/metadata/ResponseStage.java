package com.thinkbiganalytics.spark.metadata;

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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.spark.model.TransformResult;
import com.thinkbiganalytics.spark.rest.model.TransformQueryResult;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import jline.internal.Preconditions;

import org.apache.spark.sql.Row;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Transforms a result into a response.
 */
public class ResponseStage implements Function<TransformResult, TransformResponse> {

    /**
     * Destination table name.
     */
    @Nonnull
    private final String table;

    /**
     * Constructs a {@code ResponseStage}.
     */
    public ResponseStage(@Nonnull final String table) {
        this.table = table;
    }

    @Nonnull
    @Override
    public TransformResponse apply(@Nullable final TransformResult result) {
        Preconditions.checkNotNull(result);

        // Transform data set into rows
        final QueryResultRowTransform rowTransform = new QueryResultRowTransform(result.getDataSet().schema(), table);
        final List<List<Object>> rows = Lists.transform(result.getDataSet().collectAsList(), new Function<Row, List<Object>>() {
            @Nullable
            @Override
            public List<Object> apply(@Nullable Row row) {
                return (row != null) ? rowTransform.convertRow(row) : null;
            }
        });

        // Build the query result
        final TransformQueryResult queryResult = new TransformQueryResult();
        queryResult.setColumns(result.getColumns());
        queryResult.setRows(rows);
        queryResult.setValidationResults(result.getValidationResults());

        // Build the response
        final TransformResponse response = new TransformResponse();
        response.setProfile(result.getProfile());
        response.setResults(queryResult);
        response.setStatus(TransformResponse.Status.SUCCESS);
        response.setTable(table);
        return response;
    }
}
