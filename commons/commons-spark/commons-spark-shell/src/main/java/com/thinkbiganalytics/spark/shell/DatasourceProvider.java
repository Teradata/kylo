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
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * Provides instances of {@link Datasource}.
 */
public class DatasourceProvider {

    /**
     * Map of id to data source.
     */
    @Nonnull
    private final Map<String, Datasource> datasources;

    /**
     * Constructs a {@code DatasourceProvider} with the specified data sources.
     *
     * @param datasources the data sources
     */
    public DatasourceProvider(@Nonnull final Collection<Datasource> datasources) {
        this.datasources = new HashMap<>(datasources.size());
        for (final Datasource datasource : datasources) {
            this.datasources.put(datasource.getId(), datasource);
        }
    }

    /**
     * Gets the data source with the specified id.
     *
     * @param id the data source id
     * @return the data source, if found
     */
    @Nonnull
    public Datasource findById(@Nonnull final String id) {
        final Datasource datasource = datasources.get(id);
        if (datasource != null) {
            return datasource;
        } else {
            throw new IllegalArgumentException("Datasource does not exist: " + id);
        }
    }

    /**
     * Gets the specified table from the specified data source.
     *
     * @param table      the table name
     * @param datasource the data source
     * @param sqlContext the Spark SQL context
     * @return the table dataset
     * @throws IllegalArgumentException if the data source does not provide tables
     */
    @Nonnull
    public final DataFrame getTableFromDatasource(@Nonnull final String table, @Nonnull final Datasource datasource, @Nonnull final SQLContext sqlContext) {
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

            return sqlContext.read().jdbc(jdbcDatasource.getDatabaseConnectionUrl(), table, properties);
        } else {
            throw new IllegalArgumentException("Datasource does not provide tables: " + datasource);
        }
    }

    /**
     * Gets the specified table from the specified data source.
     *
     * @param table        the table name
     * @param datasourceId the data source id
     * @param sqlContext   the Spark SQL context
     * @return the table dataset
     * @throws IllegalArgumentException if the data source does not exist or does not provide tables
     */
    @Nonnull
    @SuppressWarnings("unused")  // used by generated Scala code
    public final DataFrame getTableFromDatasource(@Nonnull final String table, @Nonnull final String datasourceId, @Nonnull final SQLContext sqlContext) {
        return getTableFromDatasource(table, findById(datasourceId), sqlContext);
    }
}
