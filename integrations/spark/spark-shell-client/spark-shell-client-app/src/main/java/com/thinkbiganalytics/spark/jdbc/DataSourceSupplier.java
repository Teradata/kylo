package com.thinkbiganalytics.spark.jdbc;

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

import com.google.common.base.Supplier;
import com.thinkbiganalytics.db.PoolingDataSourceService;
import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.spark.rest.model.JdbcDatasource;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * Supplies a data source created from a {@link JdbcDatasource} configuration.
 */
public class DataSourceSupplier implements Serializable, Supplier<DataSource> {

    private static final long serialVersionUID = 7067196043774351895L;

    /**
     * Cached data source instance
     */
    @Nullable
    private transient DataSource cache;

    /**
     * JDBC data source configuration
     */
    @Nonnull
    private final JdbcDatasource config;

    /**
     * Constructs a {@code DataSourceSupplier} with the specified configuration.
     */
    public DataSourceSupplier(@Nonnull final JdbcDatasource config) {
        this.config = config;
    }

    @Override
    public DataSource get() {
        if (cache == null) {
            // Get validation query
            final DatabaseType databaseType = DatabaseType.fromJdbcConnectionString(config.getDatabaseConnectionUrl());
            final String validationQuery = databaseType.getValidationQuery();

            // Build data source
            final PoolingDataSourceService.DataSourceProperties dataSourceProperties = new PoolingDataSourceService.DataSourceProperties(
                config.getDatabaseUser(), config.getPassword(), config.getDatabaseConnectionUrl(), config.getDatabaseDriverClassName(),
                (validationQuery != null), validationQuery);
            dataSourceProperties.setDriverLocation(config.getDatabaseDriverLocation());
            cache = PoolingDataSourceService.getDataSource(dataSourceProperties);
        }
        return cache;
    }
}
