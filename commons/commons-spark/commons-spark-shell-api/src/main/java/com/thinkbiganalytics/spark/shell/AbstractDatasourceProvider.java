package com.thinkbiganalytics.spark.shell;

/*-
 * #%L
 * Kylo Commons Spark Shell
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

import com.thinkbiganalytics.spark.rest.model.Datasource;
import com.thinkbiganalytics.spark.rest.model.JdbcDatasource;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.SQLContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * A standard data source provider compatible with multiple versions of Spark.
 *
 * @param <T> the Spark data set type
 */
public abstract class AbstractDatasourceProvider<T> implements DatasourceProvider<T> {

    /**
     * Map of id to data source.
     */
    @Nonnull
    private final Map<String, Datasource> datasources;

    /**
     * Constructs an {@code AbstractDatasourceProvider} with the specified data sources.
     *
     * @param datasources the data sources
     */
    public AbstractDatasourceProvider(@Nonnull final Collection<Datasource> datasources) {
        this.datasources = new HashMap<>(datasources.size());
        for (final Datasource datasource : datasources) {
            this.datasources.put(datasource.getId(), datasource);
        }
    }

    @Nonnull
    @Override
    public Datasource findById(@Nonnull final String id) {
        final Datasource datasource = datasources.get(id);
        if (datasource != null) {
            return datasource;
        } else {
            throw new IllegalArgumentException("Datasource does not exist: " + id);
        }
    }

    @Nonnull
    @Override
    public final T getTableFromDatasource(@Nonnull final String table, @Nonnull final Datasource datasource, @Nonnull final SQLContext sqlContext) {
        if (datasource instanceof JdbcDatasource) {
            final JdbcDatasource jdbcDatasource = (JdbcDatasource) datasource;
            final Properties properties = new Properties();
            properties.put("driver", jdbcDatasource.getDatabaseDriverClassName());
            if (StringUtils.isNotBlank(jdbcDatasource.getDatabaseUser())) {
                properties.put("user", jdbcDatasource.getDatabaseUser());
            }
            if (StringUtils.isNotBlank(jdbcDatasource.getPassword())) {
                properties.put("password", jdbcDatasource.getPassword());
            }

            return readJdbcTable(jdbcDatasource.getDatabaseConnectionUrl(), table, properties, sqlContext);
        } else {
            throw new IllegalArgumentException("Datasource does not provide tables: " + datasource);
        }
    }

    @Nonnull
    @Override
    public final T getTableFromDatasource(@Nonnull final String table, @Nonnull final String datasourceId, @Nonnull final SQLContext sqlContext) {
        return getTableFromDatasource(table, findById(datasourceId), sqlContext);
    }

    /**
     * Constructs a data set representing the specified database table accessible via JDBC.
     *
     * @param url        the JDBC connection URL
     * @param table      the table reference
     * @param properties the JDBC connection properties
     * @param sqlContext the Spark SQL context
     * @return the data set
     */
    @Nonnull
    protected abstract T readJdbcTable(@Nonnull String url, @Nonnull String table, @Nonnull Properties properties, @Nonnull SQLContext sqlContext);
}
