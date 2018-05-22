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
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.spark.model.TransformResult;
import com.thinkbiganalytics.spark.rest.model.PageSpec;
import com.thinkbiganalytics.spark.rest.model.TransformQueryResult;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.rest.model.TransformValidationResult;
import com.thinkbiganalytics.spark.service.DataSetConverterService;

import jline.internal.Preconditions;

import org.apache.spark.sql.Row;

import java.util.List;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.collection.JavaConversions;

/**
 * Transforms a result into a response.
 */
public class ResponseStage implements Function<TransformResult, TransformResponse> {

    /**
     * Data set converter service
     */
    @Nonnull
    private final DataSetConverterService converterService;

    /**
     * Destination table name.
     */
    @Nonnull
    private final String table;

    private PageSpec pageSpec;

    /**
     * Constructs a {@code ResponseStage}.
     */
    public ResponseStage(@Nonnull final String table, @Nonnull final DataSetConverterService converterService, @Nullable final PageSpec pageSpec) {
        this.table = table;
        this.converterService = converterService;
        this.pageSpec = pageSpec;
    }

    static class CalculatedPage {
        boolean isPaged = false;
        int startIndex = 0;
        int actualEnd;

        CalculatedPage(Integer availableItems, Integer requestedStart, Integer requestedItems) {

            isPaged = (availableItems != null && availableItems > 0 && requestedStart != null && requestedStart >= 0 && requestedItems != null && requestedItems >= 0);
            int maxIndex = (availableItems == 0 ? 0 : availableItems);
            if (isPaged) {
                // Ensure start is within dataset
                startIndex = (requestedStart < maxIndex  ? requestedStart : (maxIndex - 1));

                // Ensure final index is within outer boundary +1 of dataset
                actualEnd = ((startIndex + requestedItems) <= availableItems ? startIndex + requestedItems : maxIndex);
            } else {
                // Default to all items
                startIndex = 0;
                actualEnd = maxIndex;
            }
        }

        /**
         * Generate array for indices for the start (inclusive) and end (exclusive)
         */
        scala.collection.immutable.List<Object> indices() {
            // Generate the indices
            List<Object> indicesArray = new Vector<>();
            for (int i = startIndex; i < actualEnd; i++) {
                indicesArray.add(i);
            }
            return JavaConversions.asScalaBuffer(indicesArray).toList();
        }
    }

    private List<Row> toRowSelection(List<Row> allRows, CalculatedPage page) {
        if (page.isPaged) {
            return allRows.subList(page.startIndex, page.actualEnd);
        }
        return allRows;
    }

    private List<QueryResultColumn> toColumnSelection(List<QueryResultColumn> allColumns, CalculatedPage page) {
        if (page.isPaged) {
            return allColumns.subList(page.startIndex, page.actualEnd);
        }
        return allColumns;
    }

    @Nonnull
    @Override
    public TransformResponse apply(@Nullable final TransformResult result) {
        Preconditions.checkNotNull(result);
        result.getDataSet().registerTempTable(table);

        // Transform data set into rows
        final QueryResultRowTransform rowTransform = new QueryResultRowTransform(result.getDataSet().schema(), table, converterService);

        List<Row> allRows = result.getDataSet().collectAsList();
        List<QueryResultColumn> allColumns = result.getColumns();
        List<List<Object>> rows;
        List<QueryResultColumn> columnSelection;
        CalculatedPage rowPage = null;

        if (pageSpec != null) {
            rowPage = new CalculatedPage(allRows.size(), pageSpec.getFirstRow(), pageSpec.getNumRows());
            final CalculatedPage colPage = new CalculatedPage(allColumns.size(), pageSpec.getFirstCol(), pageSpec.getNumCols());

            List<Row> rowSelection = toRowSelection(allRows, rowPage);
            columnSelection = toColumnSelection(allColumns, colPage);
            rows = Lists.transform(rowSelection, new Function<Row, List<Object>>() {
                @Nullable
                @Override
                public List<Object> apply(@Nullable Row row) {
                    return (row != null) ? rowTransform.convertPagedRow(row, colPage.indices()) : null;
                }
            });
        } else {
            columnSelection = allColumns;
            rows = Lists.transform(result.getDataSet().collectAsList(), new Function<Row, List<Object>>() {
                @Nullable
                @Override
                public List<Object> apply(@Nullable Row row) {
                    return (row != null) ? rowTransform.convertRow(row) : null;
                }
            });
        }

        // Build the query result
        final TransformQueryResult queryResult = new TransformQueryResult();
        queryResult.setColumns(columnSelection);
        queryResult.setRows(rows);
        queryResult.setValidationResults(toPagedValidation(rowPage, result.getValidationResults()));

        // Build the response
        final TransformResponse response = new TransformResponse();
        response.setProfile(result.getProfile());
        response.setResults(queryResult);
        response.setStatus(TransformResponse.Status.SUCCESS);
        response.setTable(table);
        response.setActualCols(allColumns.size());
        response.setActualRows(allRows.size());
        return response;
    }

    private List<List<TransformValidationResult>> toPagedValidation(CalculatedPage rowPage, List<List<TransformValidationResult>> validationResults) {

        if (rowPage == null) return validationResults;
        if (validationResults != null && validationResults.size() >= rowPage.actualEnd) {
            return validationResults.subList(rowPage.startIndex, rowPage.actualEnd);
        }
        return null;
    }

}
