package com.thinkbiganalytics.kylo.catalog.table;

/*-
 * #%L
 * kylo-catalog-api
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

import com.thinkbiganalytics.discovery.model.CatalogTableSchema;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTable;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages listing catalogs, schemas, and tables for data sets.
 */
public interface CatalogTableManager {

    /**
     * Describes the specified database table accessed through the specified data source.
     *
     * @deprecated Used only for compatibility with NiFi controller services
     */
    @Nonnull
    CatalogTableSchema describeTable(@Nonnull final DataSource dataSource, @Nullable final String schemaName, @Nullable final String tableName) throws SQLException;

    /**
     * Returns a list of table names for the specified data source.
     *
     * @deprecated Used only for compatibility with NiFi controller services
     */
    @Nonnull
    List<String> getTableNames(@Nonnull final DataSource dataSource, @Nullable final String schemaName, @Nullable final String tableName) throws SQLException;

    /**
     * Lists the catalogs, schemas, or tables for the specified data source.
     *
     * <p>First looks for catalogs, and if there are none then looks for schemas, and if both are empty then looks for tables.</p>
     */
    @Nonnull
    List<DataSetTable> listCatalogsOrTables(@Nonnull final DataSource dataSource, @Nullable final String catalogName, @Nullable final String schemaName) throws SQLException;


    /**
     * List tables for the datasource matching the supplied filter
     */
    @Nonnull
    List<DataSetTable> listTables(@Nonnull final DataSource dataSource, @Nullable final String filter) throws Exception;
}
