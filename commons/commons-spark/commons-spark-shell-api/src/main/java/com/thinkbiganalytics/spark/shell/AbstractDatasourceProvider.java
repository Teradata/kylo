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

import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
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

    @Nonnull
    private final Map<String, DataSource> catalogDataSources;

    private final Map<String, String> legacyDatasourceCatalogDataSetId;


    public abstract CatalogDataSetProvider getCatalogDataSetProvider();


    /**
     * Constructs an {@code AbstractDatasourceProvider} with the specified data sources.
     *
     * @param datasources the data sources
     */
    public AbstractDatasourceProvider(@Nonnull final Collection<Datasource> datasources, final Collection<DataSource> catalogDataSources) {
        this.datasources = new HashMap<>(datasources.size());
        this.catalogDataSources = new HashMap<>(catalogDataSources.size());
        legacyDatasourceCatalogDataSetId = new HashMap<>();
        for (final Datasource datasource : datasources) {
            this.datasources.put(datasource.getId(), datasource);
        }

        for(final DataSource catalogDataSource:catalogDataSources){
            this.catalogDataSources.put(catalogDataSource.getId(),catalogDataSource);
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
    public DataSource findCatalogDataSourceById(@Nonnull final String id) {
        final DataSource datasource = catalogDataSources.get(id);
        if (datasource != null) {
            return datasource;
        } else {
            throw new IllegalArgumentException("DataSource does not exist: " + id);
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
    public final T getTableFromCatalogDataSource(@Nonnull final String table, @Nonnull final DataSource datasource, @Nonnull final SQLContext sqlContext) {
        if ("jdbc".equalsIgnoreCase(datasource.getConnector().getPluginId())) {
            final Properties properties = new Properties();
            String driver = datasource.getTemplate().getOptions().get("driver");
            String url = datasource.getTemplate().getOptions().get("url");
            String user = datasource.getTemplate().getOptions().get("user");
            String password = datasource.getTemplate().getOptions().get("password");
            properties.put("driver", driver);
            if (StringUtils.isNotBlank(user)) {
                properties.put("user", user);
            }
            if (StringUtils.isNotBlank(password)) {
                properties.put("password", password);
            }
            return readJdbcTable(url, table, properties, sqlContext);
        } else {
            throw new IllegalArgumentException("Datasource does not provide tables: " + datasource);
        }
    }

    @Nonnull
    @Override
    public final T getTableFromCatalogDataSource(@Nonnull final String table, @Nonnull final String catalogDataSourceId, @Nonnull final SQLContext sqlContext) {
        return getTableFromCatalogDataSource(table, findCatalogDataSourceById(catalogDataSourceId), sqlContext);
    }

    /**
     * the key for the legacyDatasourceCatalogDataSetId map
     */
    private String datasourceDataSetMapKey(String table, String datasourceId) {
        return table + "-" + datasourceId;
    }

    /**
     * Remap a UserDatasource using a given table and datasource id to a Catalog DataSet
     */
    public final void remapDatasourceToDataSet(final String table, final String datasourceId, String catalogDataSetId) {
        this.legacyDatasourceCatalogDataSetId.put(datasourceDataSetMapKey(table, datasourceId), catalogDataSetId);
    }

    @Nonnull
    @Override
    public final T getTableFromDatasource(@Nonnull final String table, @Nonnull final String datasourceId, @Nonnull final SQLContext sqlContext) {
        final String dataSetKey = datasourceDataSetMapKey(table, datasourceId);
        if (legacyDatasourceCatalogDataSetId.containsKey(dataSetKey)) {
            final DataSet dataSet;
            try {
                dataSet = this.getCatalogDataSetProvider().findById(dataSetKey);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Unable to get Data Set Datasource from LegacyDatasource for  table:" + table + " and datasouce: " + datasourceId);
            }
            //now get the DataSource from the dataSet
            //noinspection unchecked
            return (T) getCatalogDataSetProvider().readDataSet(dataSet);
        } else if (datasources.containsKey(datasourceId)) {
            return getTableFromDatasource(table, findById(datasourceId), sqlContext);
        } else if (catalogDataSources.containsKey(datasourceId)) {
            return getTableFromCatalogDataSource(table, findCatalogDataSourceById(datasourceId), sqlContext);
        } else {
            throw new IllegalArgumentException("DataSource does not exist: " + datasourceId);
        }
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
