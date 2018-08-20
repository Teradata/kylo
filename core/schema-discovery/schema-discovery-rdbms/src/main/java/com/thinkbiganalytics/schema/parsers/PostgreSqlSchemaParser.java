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
import com.thinkbiganalytics.discovery.schema.JdbcSchemaParser;
import com.thinkbiganalytics.schema.DefaultJdbcCatalog;
import com.thinkbiganalytics.schema.JdbcUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Lists the catalogs, schemas, and tables of PostgreSQL servers.
 */
@Component
@Order(JdbcSchemaParser.DEFAULT_ORDER)
public class PostgreSqlSchemaParser extends DefaultJdbcSchemaParser {

    /**
     * Name of the system database
     */
    private static final String POSTGRES = "postgres";

    /**
     * JDBC driver protocol
     */
    private static final String PROTOCOL = "jdbc:postgresql:";

    @Override
    public boolean acceptsURL(@Nonnull final String url) {
        return url.startsWith(PROTOCOL);
    }

    /**
     * Returns all databases names if connected to the {@code postgres} database, otherwise returns the current database name.
     */
    @Nonnull
    @Override
    public List<JdbcCatalog> listCatalogs(@Nonnull final Connection connection, @Nullable final String pattern, @Nullable final Pageable pageable) throws SQLException {
        if (StringUtils.equals(connection.getCatalog(), POSTGRES)) {
            try (final Statement statement = connection.createStatement();
                 final ResultSet resultSet = statement.executeQuery("SELECT datname AS TABLE_CAT FROM pg_database")) {
                return JdbcUtil.transformResults(resultSet, DefaultJdbcCatalog.fromResultSet());
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Nonnull
    @Override
    public DataSourceProperties prepareDataSource(@Nonnull final DataSourceProperties properties, @Nullable final String catalog) throws SQLException {
        // Verify parameters
        final Properties info = (properties.getProperties() != null) ? properties.getProperties() : new Properties();
        final String url = properties.getUrl();

        if (!acceptsURL(url)) {
            throw new SQLException("URL not supported: " + url);
        }

        // Add catalog to properties
        if (url.startsWith(PROTOCOL + "//")) {
            final String[] urlSplit = url.split("\\?", 2);
            final String[] pathSplit = urlSplit[0].substring(PROTOCOL.length() + 2).split("/", 2);

            if (pathSplit.length == 1 || StringUtils.equalsAny(pathSplit[1], "", "/")) {
                // Append catalog to URL
                String catalogUrl = PROTOCOL + "//" + pathSplit[0] + "/" + (catalog != null ? urlEncode(catalog) : POSTGRES);
                if (urlSplit.length == 2) {
                    catalogUrl += "?" + urlSplit[1];
                }

                // Clone data source with new URL
                final DataSourceProperties catalogProperties = new DataSourceProperties(properties);
                catalogProperties.setUrl(catalogUrl);
                return catalogProperties;
            } else {
                // Use existing database name
                return properties;
            }
        } else if (StringUtils.isEmpty(info.getProperty("PGDBNAME"))) {
            // Clone data source with new database name
            final Properties catalogInfo = new Properties(info);
            catalogInfo.setProperty("PGDBNAME", (catalog != null ? catalog : POSTGRES));

            final DataSourceProperties catalogProperties = new DataSourceProperties(properties);
            catalogProperties.setProperties(catalogInfo);
            return catalogProperties;
        } else {
            // Use existing database name
            return properties;
        }
    }

    /**
     * Quotes the specified string so that it can be used as a URL component.
     *
     * @param s {@code String} to be translated
     * @return the translated string
     * @throws SQLException if the string cannot be translated
     */
    @Nonnull
    private String urlEncode(@Nonnull final String s) throws SQLException {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new SQLException(e);
        }
    }
}
