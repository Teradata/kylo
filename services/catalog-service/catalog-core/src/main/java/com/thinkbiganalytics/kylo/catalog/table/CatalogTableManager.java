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
import com.thinkbiganalytics.db.DataSourceProperties;
import com.thinkbiganalytics.db.PoolingDataSourceService;
import com.thinkbiganalytics.discovery.schema.JdbcCatalog;
import com.thinkbiganalytics.discovery.schema.JdbcSchema;
import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.kylo.catalog.dataset.DataSetUtil;
import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceUtil;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTable;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.util.HadoopClassLoader;
import com.thinkbiganalytics.schema.DBSchemaParser;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages listing catalogs, schemas, and tables for data sets.
 */
@Component
public class CatalogTableManager {

    private static final Logger log = LoggerFactory.getLogger(CatalogTableManager.class);

    /**
     * Hadoop configuration with default values
     */
    @Nonnull
    private final Configuration defaultConf;

    /**
     * Constructs a {@code CatalogTableManager}.
     */
    public CatalogTableManager() {
        defaultConf = new Configuration();
        defaultConf.size();  // causes defaults to be loaded
    }

    /**
     * Lists the catalogs, schemas, or tables for the specified data source.
     *
     * <p>First looks for catalogs, and if there are none then looks for schemas, and if both are empty then looks for tables.</p>
     */
    @Nonnull
    public List<DataSetTable> listCatalogsOrTables(@Nonnull final DataSource dataSource, @Nullable final String catalogName, @Nullable final String schemaName)
        throws SQLException {
        return isolatedFunction(DataSourceUtil.mergeTemplates(dataSource), jdbcSource -> {
            final DBSchemaParser parser = new DBSchemaParser(jdbcSource, new KerberosTicketConfiguration());
            final String tableCatalog;
            final String tableSchema;

            if (catalogName == null) {
                final List<JdbcCatalog> catalogs = parser.listCatalogs(null, null);
                if (catalogs.isEmpty()) {
                    tableCatalog = null;
                } else if (catalogs.size() == 1 && catalogs.get(0).getCatalog().isEmpty()) {
                    tableCatalog = "";
                } else {
                    return catalogs.stream()
                        .map(catalog -> {
                            final DataSetTable entry = new DataSetTable();
                            entry.setName(catalog.getCatalog());
                            entry.setType("CATALOG");
                            return entry;
                        })
                        .collect(Collectors.toList());
                }
            } else {
                tableCatalog = catalogName;
            }

            if (schemaName == null) {
                final List<JdbcSchema> schemas = parser.listSchemas(tableCatalog, null, null);
                if (schemas.isEmpty()) {
                    tableSchema = null;
                } else if (schemas.size() == 1 && schemas.get(0).getSchema().isEmpty()) {
                    tableSchema = "";
                } else {
                    return schemas.stream()
                        .map(schema -> {
                            final DataSetTable entry = new DataSetTable();
                            entry.setCatalog(schema.getCatalog());
                            entry.setName(schema.getSchema());
                            entry.setType("SCHEMA");
                            return entry;
                        })
                        .collect(Collectors.toList());
                }
            } else {
                tableSchema = schemaName;
            }

            return parser.listTables(tableCatalog, tableSchema, null, null).stream()
                .map(table -> {
                    final DataSetTable entry = new DataSetTable();
                    entry.setCatalog(table.getCatalog());
                    entry.setName(table.getName());
                    entry.setQualifiedIdentifier(table.getQualifiedIdentifier());
                    entry.setRemarks(table.getRemarks());
                    entry.setSchema(table.getSchema());
                    entry.setType(table.getType());
                    return entry;
                })
                .collect(Collectors.toList());
        });
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
    protected <R> R isolatedFunction(@Nonnull final DataSetTemplate template, @Nonnull final DataSourceFunction<R> function) throws SQLException {
        final Configuration conf = DataSetUtil.getConfiguration(template, defaultConf);
        final HadoopClassLoader classLoader = new HadoopClassLoader(conf);
        if (template.getJars() != null) {
            log.debug("Adding jars to HadoopClassLoader: {}", template.getJars());
            classLoader.addJars(template.getJars());
        }

        final DataSourceProperties properties = getDataSourceProperties(template, classLoader);
        return function.apply(PoolingDataSourceService.getDataSource(properties));
    }
}
