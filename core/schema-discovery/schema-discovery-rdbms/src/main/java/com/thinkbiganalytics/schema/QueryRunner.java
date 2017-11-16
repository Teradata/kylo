package com.thinkbiganalytics.schema;

/*-
 * #%L
 * kylo-schema-discovery-rdbms
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

import com.thinkbiganalytics.discovery.model.DefaultQueryResult;
import com.thinkbiganalytics.discovery.model.DefaultQueryResultColumn;
import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.discovery.util.ParserHelper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * Executes queries and returns the results.
 */
public class QueryRunner {

    /**
     * A JDBC data source.
     */
    @Nonnull
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructs a {@code QueryRunner} for the specified JDBC data source.
     */
    public QueryRunner(@Nonnull final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    /**
     * Constructs a {@code QueryRunner} for the specified JDBC template.
     */
    public QueryRunner(@Nonnull final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executes the specified SELECT query and returns the results.
     *
     * @param query the SELECT query
     * @return the query result
     * @throws DataAccessException if the query cannot be executed
     */
    public QueryResult query(String query) {
        // Validate the query
        if (!validateQuery(query)) {
            throw new DataRetrievalFailureException("Invalid query: " + query);
        }

        // Execute the query
        final DefaultQueryResult queryResult = new DefaultQueryResult(query);

        jdbcTemplate.query(query, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                // First-time initialization
                if (queryResult.isEmpty()) {
                    QueryRunner.this.initQueryResult(queryResult, rs.getMetaData());
                }

                // Add row to the result
                final Map<String, Object> row = new LinkedHashMap<>();
                for (final QueryResultColumn column : queryResult.getColumns()) {
                    row.put(column.getDisplayName(), rs.getObject(column.getHiveColumnLabel()));
                }
                queryResult.addRow(row);
            }
        });

        return queryResult;
    }

    /**
     * Initializes the query result with the specified metadata.
     *
     * @param queryResult the query result to initialize
     * @param rsMetaData  the result set metadata for the query
     * @throws SQLException if the metadata is not available
     */
    private void initQueryResult(@Nonnull final DefaultQueryResult queryResult, @Nonnull final ResultSetMetaData rsMetaData) throws SQLException {
        final List<QueryResultColumn> columns = new ArrayList<>();
        final Map<String, Integer> displayNameMap = new HashMap<>();

        for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
            final DefaultQueryResultColumn column = new DefaultQueryResultColumn();
            column.setField(rsMetaData.getColumnName(i));
            String displayName = rsMetaData.getColumnLabel(i);
            column.setHiveColumnLabel(displayName);
            //remove the table name if it exists
            displayName = StringUtils.contains(displayName, ".") ? StringUtils.substringAfterLast(displayName, ".") : displayName;
            Integer count = 0;
            if (displayNameMap.containsKey(displayName)) {
                count = displayNameMap.get(displayName);
                count++;
            }
            displayNameMap.put(displayName, count);
            column.setDisplayName(displayName + "" + (count > 0 ? count : ""));

            column.setTableName(StringUtils.substringAfterLast(rsMetaData.getColumnName(i), "."));
            column.setDataType(ParserHelper.sqlTypeToHiveType(rsMetaData.getColumnType(i)));
            column.setNativeDataType(rsMetaData.getColumnTypeName(i));

            int precision = rsMetaData.getPrecision(i);
            int scale = rsMetaData.getScale(i);
            if (scale != 0) {
                column.setPrecisionScale(precision + "," + scale);
            } else if (precision != 0) {
                column.setPrecisionScale(Integer.toString(precision));
            }

            columns.add(column);
        }

        queryResult.setColumns(columns);
    }

    /**
     * Tests that the specified query is a SHOW, SELECT, DESC, or DESCRIBE query.
     *
     * @param query the query to test
     * @return {@code true} if the query is valid, or {@code false} otherwise
     */
    private boolean validateQuery(@Nonnull final String query) {
        final String testQuery = StringUtils.trimToEmpty(query);
        return StringUtils.startsWithIgnoreCase(testQuery, "show")
            || StringUtils.startsWithIgnoreCase(testQuery, "select")
            || StringUtils.startsWithIgnoreCase(testQuery, "desc")
            || StringUtils.startsWithIgnoreCase(testQuery, "describe");
    }
}
