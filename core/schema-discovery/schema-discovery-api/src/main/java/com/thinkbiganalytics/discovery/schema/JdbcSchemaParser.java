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

import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides database metadata and schema information from JDBC.
 */
public interface JdbcSchemaParser {

    /**
     * Retrieves the catalog names available in this database.
     */
    @Nonnull
    List<JdbcCatalog> listCatalogs(@Nullable String pattern, @Nullable Pageable pageable) throws SQLException;

    /**
     * Retrieves the schema names available in this database.
     */
    @Nonnull
    List<JdbcSchema> listSchemas(@Nullable String catalog, @Nullable String pattern, @Nullable Pageable pageable) throws SQLException;

    /**
     * Retrieves a description of the tables available in the given catalog.
     */
    @Nonnull
    List<JdbcTable> listTables(@Nullable String catalog, @Nullable String schema, @Nullable String pattern, @Nullable Pageable pageable) throws SQLException;
}
