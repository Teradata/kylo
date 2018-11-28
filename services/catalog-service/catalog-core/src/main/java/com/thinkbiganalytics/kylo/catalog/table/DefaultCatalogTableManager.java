package com.thinkbiganalytics.kylo.catalog.table;

/*-
 * #%L
 * kylo-catalog-core
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.db.DataSourceProperties;
import com.thinkbiganalytics.db.PoolingDataSourceService;
import com.thinkbiganalytics.discovery.model.CatalogTableSchema;
import com.thinkbiganalytics.discovery.schema.JdbcCatalog;
import com.thinkbiganalytics.discovery.schema.JdbcSchema;
import com.thinkbiganalytics.discovery.schema.JdbcSchemaParser;
import com.thinkbiganalytics.discovery.schema.JdbcTable;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.hive.service.HiveMetastoreService;
import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.kerberos.KerberosUtil;
import com.thinkbiganalytics.kylo.catalog.dataset.DataSetUtil;
import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceUtil;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTable;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.util.HadoopClassLoader;
import com.thinkbiganalytics.schema.DBSchemaParser;
import com.thinkbiganalytics.schema.DefaultJdbcTable;
import com.thinkbiganalytics.schema.JdbcSchemaParserProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages listing catalogs, schemas, and tables for data sets.
 */
@Component
public class DefaultCatalogTableManager implements CatalogTableManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultCatalogTableManager.class);

    /**
     * Hadoop configuration with default values
     */
    @Nonnull
    private final Configuration defaultConf;

    /**
     * Hive metastore service
     */
    @Nonnull
    private final HiveMetastoreService hiveMetastoreService;

    /**
     * Factory for JdbcSchemaParser instances
     */
    @Nonnull
    private final JdbcSchemaParserProvider schemaParserProvider;

    @Value("${kylo.catalog.table-autocomplete.cache-expire-time-min:20}")
    private Long TABLE_CACHE_EXPIRE_TIME_MINUTES = 20L;

    /**
     * should the results of the table filter cache
     */
    @Value("${kylo.catalog.table-autocomplete.cache-enabled:false}")
    private boolean cacheEnabled = false;

    /**
     * should the autocomplete table search filter on just table names, or table names and schema
     * if TABLES_AND_SCHEMA is chosen and a search is done without a "." delinating between the schema and table name, then the system will load in all tables and filter for both.
     * if you choose this option it is recommended you enable the cache mode above
     */
    @Value("${kylo.catalog.table-autocomplete.filter-mode:TABLES}")
    private FilterMode filterMode;

    /**
     * How should the backend filter.
     * TABLES = this will cache and query each result
     * TABLES_AND_SCHEMA = this will first query for all tables, store that in cache, and then use java filtering on a match
     */
    public static enum FilterMode {
        TABLES, TABLES_AND_SCHEMA
    }

    private LoadingCache<JdbcTableCacheKey, List<JdbcTable>>
        datasourceTables = CacheBuilder.newBuilder().expireAfterWrite(TABLE_CACHE_EXPIRE_TIME_MINUTES, TimeUnit.MINUTES).build(new CacheLoader<JdbcTableCacheKey, List<JdbcTable>>() {
        @Override
        public List<JdbcTable> load(JdbcTableCacheKey key) throws Exception {
            return fetchTables(key);
        }
    });


    /**
     * Constructs a {@code CatalogTableManager}.
     */
    @Autowired
    public DefaultCatalogTableManager(@Nonnull @Qualifier("hiveMetastoreService") final HiveMetastoreService hiveMetastoreService, @Nonnull final JdbcSchemaParserProvider schemaParserProvider) {
        this.hiveMetastoreService = hiveMetastoreService;
        this.schemaParserProvider = schemaParserProvider;

        defaultConf = new Configuration();
        defaultConf.size();  // causes defaults to be loaded
    }

    @Nonnull
    @Override
    public CatalogTableSchema describeTable(@Nonnull final DataSource dataSource, @Nullable final String schemaName, @Nullable final String tableName) throws SQLException {
        final DataSetTemplate template = DataSourceUtil.mergeTemplates(dataSource);
        if (Objects.equals("hive", template.getFormat())) {
            final TableSchema tableSchema = hiveMetastoreService.getTable(schemaName, tableName);
            final CatalogTableSchema catalogTableSchema = new CatalogTableSchema(tableSchema);

            // Get table metadata
            if (StringUtils.isNotEmpty(tableSchema.getName())) {
                final DefaultJdbcTable jdbcTable = new DefaultJdbcTable(tableSchema.getName(), "TABLE");
                jdbcTable.setCatalog(tableSchema.getDatabaseName());
                jdbcTable.setCatalog(tableSchema.getDatabaseName());
                jdbcTable.setRemarks(tableSchema.getDescription());
                jdbcTable.setSchema(tableSchema.getSchemaName());
                jdbcTable.setCatalogSeparator(".");
                jdbcTable.setIdentifierQuoteString("`");
                catalogTableSchema.setTable(createTable(jdbcTable));
            }

            return catalogTableSchema;
        } else if (Objects.equals("jdbc", template.getFormat())) {

            return isolatedFunction(template, schemaName, (connection, schemaParser) -> {
                final javax.sql.DataSource ds = new SingleConnectionDataSource(connection, true);
                final DBSchemaParser tableSchemaParser = new DBSchemaParser(ds, new KerberosTicketConfiguration());
                final TableSchema tableSchema = tableSchemaParser.describeTable(schemaName, tableName);
                if (tableSchema != null) {
                    // Get table metadata
                    final DefaultJdbcTable jdbcTable = new DefaultJdbcTable(tableSchema.getName(), "TABLE");
                    jdbcTable.setCatalog(tableSchema.getDatabaseName());
                    jdbcTable.setCatalog(tableSchema.getDatabaseName());
                    jdbcTable.setRemarks(tableSchema.getDescription());
                    jdbcTable.setSchema(tableSchema.getSchemaName());
                    jdbcTable.setMetaData(connection.getMetaData());

                    // Return table schema
                    final CatalogTableSchema catalogTableSchema = new CatalogTableSchema(tableSchema);
                    catalogTableSchema.setTable(createTable(jdbcTable));
                    return catalogTableSchema;
                } else {
                    return null;
                }
            });
        } else {
            throw new IllegalArgumentException("Unsupported format: " + template.getFormat());
        }
    }

    @Nonnull
    @Override
    public List<String> getTableNames(@Nonnull final DataSource dataSource, @Nullable final String schemaName, @Nullable final String tableName) throws SQLException {
        final DataSetTemplate template = DataSourceUtil.mergeTemplates(dataSource);
        return isolatedFunction(template, schemaName, (connection, schemaParser) -> {
            final javax.sql.DataSource ds = new SingleConnectionDataSource(connection, true);
            final DBSchemaParser tableSchemaParser = new DBSchemaParser(ds, new KerberosTicketConfiguration());
            return tableSchemaParser.listTables(schemaName, tableName);
        });
    }

    @Nonnull
    @Override
    public List<DataSetTable> listCatalogsOrTables(@Nonnull final DataSource dataSource, @Nullable final String catalogName, @Nullable final String schemaName)
        throws SQLException {
        final DataSetTemplate template = DataSourceUtil.mergeTemplates(dataSource);
        if (Objects.equals("hive", template.getFormat())) {
            return listHiveCatalogsOrTables(schemaName, false);
        } else if (Objects.equals("jdbc", template.getFormat())) {
            return isolatedFunction(template, catalogName, (connection, schemaParser) -> listJdbcCatalogsOrTables(connection, schemaParser, catalogName, schemaName));
        } else {
            throw new IllegalArgumentException("Unsupported format: " + template.getFormat());
        }
    }

    /**
     * Search a datasource for table names matching the supplied filter
     */
    @Nonnull
    @Override
    public List<DataSetTable> listTables(@Nonnull final DataSource dataSource, @Nullable String filter)
        throws Exception {
        final DataSetTemplate template = DataSourceUtil.mergeTemplates(dataSource);

        if (Objects.equals("hive", template.getFormat())) {
            return listHiveCatalogsOrTables(filter, true);
        } else if (Objects.equals("jdbc", template.getFormat())) {
            JdbcTableCacheKey cacheKey = new JdbcTableCacheKey(template, filter, this.filterMode);

            final String schemaFilter = cacheKey.getSchemaJavaFilter();
            final String tableFilter = cacheKey.getTableJavaFilter();
            final String generalFilter = cacheKey.getJavaFilter();
            long start = System.currentTimeMillis();

            List<JdbcTable> tables = null;
            if (cacheEnabled) {
                tables = datasourceTables.get(cacheKey);
            } else {
                tables = fetchTables(cacheKey);
            }
            long stop = System.currentTimeMillis();
            log.debug("Time to query for {} ms, {}", (stop - start), cacheKey);

            List<DataSetTable> filteredTables = tables.stream()
                .filter(jdbcTable -> {
                    if (filterMode == FilterMode.TABLES) {
                        //if we are just searching on tables then the backend already took care of the filtering, just return it
                        return true;
                    } else {
                        boolean match = true;
                        if (jdbcTable.getSchema() != null && StringUtils.isNotBlank(schemaFilter)) {
                            match = jdbcTable.getSchema().toLowerCase().contains(schemaFilter);
                        } else if (jdbcTable.getCatalog() != null && StringUtils.isNotBlank(schemaFilter)) {
                            match = jdbcTable.getCatalog().toLowerCase().contains(schemaFilter);
                        }

                        if (jdbcTable.getName() != null && StringUtils.isNotBlank(tableFilter)) {
                            match &= jdbcTable.getName().toLowerCase().contains(tableFilter);
                        }

                        if (StringUtils.isNotBlank(generalFilter)) {
                            match =
                                jdbcTable.getName().toLowerCase().contains(generalFilter) || (jdbcTable.getSchema() != null && jdbcTable.getSchema().toLowerCase().contains(generalFilter)) || (
                                    jdbcTable.getCatalog() != null && jdbcTable.getCatalog().toLowerCase().contains(generalFilter));
                        }
                        return match;

                    }
                })
                .map(this::createTable)
                .collect(Collectors.toList());
            log.debug("listTables returning {} out of {} .  Time to query for {} ms, {}", filteredTables, tables, (stop - start), cacheKey);
            return filteredTables;

        } else {
            throw new IllegalArgumentException("Unsupported format: " + template.getFormat());
        }
    }

    private List<JdbcTable> fetchTables(JdbcTableCacheKey key) throws SQLException {
        return isolatedFunction(key.getTemplate(), key.getFilter(), (connection, schemaParser) -> schemaParser.listTables(connection, key.getSchemaPattern(), key.getTablePattern()));
    }


    public void invalidateCache(DataSetTemplate template) {
        Set<JdbcTableCacheKey> keysToInvalidate = new HashSet<>();
        for (JdbcTableCacheKey key : datasourceTables.asMap().keySet()) {
            if (key.getTemplate().equals(template)) {
                keysToInvalidate.add(key);
            }
        }
        for (JdbcTableCacheKey key : keysToInvalidate) {
            datasourceTables.invalidate(key);
        }
    }


    /**
     * Converts a JDBC catalog to a data set model.
     */
    @Nonnull
    private DataSetTable createCatalog(@Nonnull final JdbcCatalog catalog) {
        final DataSetTable entry = new DataSetTable();
        entry.setName(catalog.getCatalog());
        entry.setType("CATALOG");
        return entry;
    }

    /**
     * Converts a JDBC schema to a data set model.
     */
    @Nonnull
    private DataSetTable createSchema(@Nonnull final JdbcSchema schema) {
        final DataSetTable entry = new DataSetTable();
        entry.setCatalog(schema.getCatalog());
        entry.setName(schema.getSchema());
        entry.setType("SCHEMA");
        return entry;
    }

    /**
     * Converts a JDBC table to a data set model.
     */
    @Nonnull
    private DataSetTable createTable(@Nonnull final JdbcTable table) {
        final DataSetTable entry = new DataSetTable();
        entry.setCatalog(table.getCatalog());
        entry.setName(table.getName());
        entry.setQualifiedIdentifier(table.getQualifiedIdentifier());
        entry.setRemarks(table.getRemarks());
        entry.setSchema(table.getSchema());
        entry.setType(table.getType());
        return entry;
    }

    /**
     * Lists the Hive schemas or tables.
     */
    @Nonnull
    private List<DataSetTable> listHiveCatalogsOrTables(@Nullable final String schemaName, boolean searchAll) {
        if (schemaName == null) {
            return hiveMetastoreService.listSchemas(null, null, null).stream()
                .map(this::createSchema)
                .collect(Collectors.toList());
        } else {
            String schema = null;
            String pattern = null;
            if (searchAll) {
                pattern = schemaName;
            } else {
                schema = schemaName;
            }
            return hiveMetastoreService.listTables(null, schema, pattern, null).stream()
                .map(this::createTable)
                .collect(Collectors.toList());
        }
    }

    /**
     * Lists the catalogs, schemas, or tables for the specified data source.
     */
    @Nonnull
    private List<DataSetTable> listJdbcCatalogsOrTables(@Nonnull final Connection connection, @Nonnull final JdbcSchemaParser parser, @Nullable final String catalogName,
                                                        @Nullable final String schemaName) throws SQLException {
        final String tableCatalog;
        final String tableSchema;

        if (catalogName == null) {
            final List<JdbcCatalog> catalogs = parser.listCatalogs(connection, null, null);
            if (catalogs.isEmpty()) {
                tableCatalog = null;
            } else if (catalogs.size() == 1 && catalogs.get(0).getCatalog().isEmpty()) {
                tableCatalog = "";
            } else {
                return catalogs.stream()
                    .map(this::createCatalog)
                    .collect(Collectors.toList());
            }
        } else {
            tableCatalog = catalogName;
        }

        if (schemaName == null) {
            final List<JdbcSchema> schemas = parser.listSchemas(connection, tableCatalog, null, null);
            if (schemas.isEmpty()) {
                tableSchema = null;
            } else if (schemas.size() == 1 && schemas.get(0).getSchema().isEmpty()) {
                tableSchema = "";
            } else {
                return schemas.stream()
                    .map(this::createSchema)
                    .collect(Collectors.toList());
            }
        } else {
            tableSchema = schemaName;
        }

        return parser.listTables(connection, tableCatalog, tableSchema, null, null).stream()
            .map(this::createTable)
            .collect(Collectors.toList());
    }


    /**
     * Creates the properties for a data source from the specified template.
     */
    @Nonnull
    private DataSourceProperties getDataSourceProperties(@Nonnull final DataSetTemplate template, @Nonnull final ClassLoader classLoader) {
        // Extract options from template
        final String user = template.getOptions().get("user");
        final String password = template.getOptions().get("password");
        final String url = template.getOptions().get("url");
        final String driverClassName = template.getOptions().get("driver");
        final String validationQuery = DatabaseType.fromJdbcConnectionString(url).getValidationQuery();
        final Properties dbProps = new Properties();
        dbProps.putAll(template.getOptions());

        // Create data source properties
        final DataSourceProperties dsProps = new DataSourceProperties(user, password, url, driverClassName, StringUtils.isNotEmpty(validationQuery), validationQuery);
        dsProps.setDriverClassLoader(classLoader);
        dsProps.setProperties(dbProps);
        return dsProps;
    }

    /**
     * Executes the specified function in a separate class loader containing the jars of the specified template.
     */
    @VisibleForTesting
    protected <R> R isolatedFunction(@Nonnull final DataSetTemplate template, @Nullable final String catalog, @Nonnull final SqlSchemaFunction<R> function) throws SQLException {
        // Get Hadoop configuration
        final Configuration conf = DataSetUtil.getConfiguration(template, defaultConf);
        final HadoopClassLoader classLoader = new HadoopClassLoader(conf);
        if (template.getJars() != null) {
            log.debug("Adding jars to HadoopClassLoader: {}", template.getJars());
            classLoader.addJars(template.getJars());
        }

        // Get data source configuration
        DataSourceProperties properties = getDataSourceProperties(template, classLoader);
        final JdbcSchemaParser schemaParser = schemaParserProvider.getSchemaParser(properties.getUrl());
        properties = schemaParser.prepareDataSource(properties, catalog);

        // Connect to data source
        final javax.sql.DataSource dataSource = PoolingDataSourceService.getDataSource(properties);
        try (final Connection connection = KerberosUtil.getConnectionWithOrWithoutKerberos(dataSource, new KerberosTicketConfiguration())) {
            return function.apply(connection, schemaParser);
        }
    }
}
