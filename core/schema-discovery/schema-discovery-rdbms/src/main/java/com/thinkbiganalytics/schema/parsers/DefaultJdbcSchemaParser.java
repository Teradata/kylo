package com.thinkbiganalytics.schema.parsers;

/*-
 * #%L
 * kylo-schema-discovery-rdbms
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

import com.thinkbiganalytics.db.DataSourceProperties;
import com.thinkbiganalytics.discovery.schema.JdbcCatalog;
import com.thinkbiganalytics.discovery.schema.JdbcSchema;
import com.thinkbiganalytics.discovery.schema.JdbcSchemaParser;
import com.thinkbiganalytics.discovery.schema.JdbcTable;
import com.thinkbiganalytics.schema.DefaultJdbcCatalog;
import com.thinkbiganalytics.schema.DefaultJdbcSchema;
import com.thinkbiganalytics.schema.DefaultJdbcTable;
import com.thinkbiganalytics.schema.JdbcUtil;

import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Uses connection metadata to list catalog, schemas, and tables. Does not support search or paging.
 */
@Component
@Order(JdbcSchemaParser.LATE_ORDER)
public class DefaultJdbcSchemaParser implements JdbcSchemaParser {

    @Override
    @SuppressWarnings("RedundantThrows")  // may be thrown by subclass
    public boolean acceptsURL(@Nonnull final String url) throws SQLException {
        return true;
    }

    @Nonnull
    @Override
    public List<JdbcCatalog> listCatalogs(@Nonnull final Connection connection, @Nullable final String pattern, @Nullable final Pageable pageable) throws SQLException {
        try (final ResultSet resultSet = connection.getMetaData().getCatalogs()) {
            return JdbcUtil.transformResults(resultSet, DefaultJdbcCatalog.fromResultSet());
        }
    }

    @Nonnull
    @Override
    public List<JdbcSchema> listSchemas(@Nonnull final Connection connection, @Nullable final String catalog, @Nullable final String pattern, @Nullable final Pageable pageable)
        throws SQLException {
        try (final ResultSet resultSet = connection.getMetaData().getSchemas(catalog, null)) {
            return JdbcUtil.transformResults(resultSet, DefaultJdbcSchema.fromResultSet());
        }
    }

    @Nonnull
    @Override
    public List<JdbcTable> listTables(@Nonnull final Connection connection, @Nullable final String catalog, @Nullable final String schema, @Nullable final String pattern,
                                      @Nullable Pageable pageable) throws SQLException {
        try (final ResultSet resultSet = connection.getMetaData().getTables(catalog, schema, null, null)) {
            return JdbcUtil.transformResults(resultSet, DefaultJdbcTable.fromResultSet(connection.getMetaData()));
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("RedundantThrows")  // may be thrown by subclass
    public DataSourceProperties prepareDataSource(@Nonnull final DataSourceProperties properties, @Nullable final String catalog) throws SQLException {
        return properties;
    }
}
