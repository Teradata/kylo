package com.thinkbiganalytics.discovery.schema;

/*-
 * #%L
 * kylo-schema-discovery-api
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

import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides database metadata and schema information from JDBC.
 */
@Order(JdbcSchemaParser.DEFAULT_ORDER)
public interface JdbcSchemaParser {

    int DEFAULT_ORDER = 0;
    int EARLY_ORDER = DEFAULT_ORDER - 100;
    int LATE_ORDER = DEFAULT_ORDER + 100;

    /**
     * Indicates if this parser can retrieve the schema of connections using the specified URL.
     *
     * <p>Typically this only verifies that the sub-protocol is supported.</p>
     *
     * @param url URL of the database
     * @return {@code true} if the parser recognizes the given URL, or {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean acceptsURL(@Nonnull String url) throws SQLException;

    /**
     * Retrieves the catalog names available in this database.
     */
    @Nonnull
    List<JdbcCatalog> listCatalogs(@Nonnull Connection connection, @Nullable String pattern, @Nullable Pageable pageable) throws SQLException;

    /**
     * Retrieves the schema names available in this database.
     */
    @Nonnull
    List<JdbcSchema> listSchemas(@Nonnull Connection connection, @Nullable String catalog, @Nullable String pattern, @Nullable Pageable pageable) throws SQLException;

    /**
     * Retrieves a description of the tables available in the given catalog.
     */
    @Nonnull
    List<JdbcTable> listTables(@Nonnull Connection connection, @Nullable String catalog, @Nullable String schema, @Nullable String pattern, @Nullable Pageable pageable) throws SQLException;


    /**
     *  List tables for the connection matching a supplied pattern.
     *  If no pattern is supplied all tables are returned
     * @param connection
     * @param schema
     * @param pattern
     * @return
     * @throws SQLException
     */
    List<JdbcTable> listTables(@Nonnull final Connection connection, @Nullable final String schema, @Nullable final String pattern) throws SQLException ;

    /**
     * Modifies the data source properties, if necessary, to connect to the specified catalog.
     *
     * @param properties data source properties
     * @param catalog    name of the catalog, or {@code null} for none
     * @return the modified data source properties, or the original if no changes were needed
     * @throws SQLException if a database access error occurs
     */
    @Nonnull
    DataSourceProperties prepareDataSource(@Nonnull DataSourceProperties properties, @Nullable String catalog) throws SQLException;
}
