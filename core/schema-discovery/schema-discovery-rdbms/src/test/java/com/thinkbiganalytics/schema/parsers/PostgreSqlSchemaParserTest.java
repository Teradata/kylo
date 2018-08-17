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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class PostgreSqlSchemaParserTest {

    /**
     * Verify listing catalogs from the {@code postgres} database.
     */
    @Test
    public void listCatalogsWithPostgres() throws SQLException {
        try (final Connection h2c = DriverManager.getConnection("jdbc:h2:mem:postgresql")) {
            // Setup H2 tables
            final Statement h2s = h2c.createStatement();
            h2s.execute("CREATE TABLE pg_database (datname VARCHAR(16))");
            h2s.execute("INSERT INTO pg_database VALUES ('kylo'), ('postgres'), ('test')");

            // Mock connection
            final Connection connection = Mockito.mock(Connection.class);
            Mockito.when(connection.createStatement()).thenReturn(h2c.createStatement());
            Mockito.when(connection.getCatalog()).thenReturn("postgres");

            // Test listing catalogs
            final JdbcSchemaParser parser = new PostgreSqlSchemaParser();
            final List<JdbcCatalog> catalogs = parser.listCatalogs(connection, null, null);
            Assert.assertEquals("kylo", catalogs.get(0).getCatalog());
            Assert.assertEquals("postgres", catalogs.get(1).getCatalog());
            Assert.assertEquals("test", catalogs.get(2).getCatalog());
            Assert.assertEquals(3, catalogs.size());
        }
    }

    /**
     * Verify listing catalogs from a user-created database.
     */
    @Test
    public void listCatalogsWithOther() throws SQLException {
        // Mock connection
        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.getCatalog()).thenReturn("kylo");

        // Test listing catalogs
        final JdbcSchemaParser parser = new PostgreSqlSchemaParser();
        final List<JdbcCatalog> catalogs = parser.listCatalogs(connection, null, null);
        Assert.assertEquals(Collections.emptyList(), catalogs);
    }

    /**
     * Verify updating data source info with a catalog name.
     */
    @Test
    public void prepareDataSourceWithBasicInfo() throws SQLException {
        // Mock data source properties
        final Properties properties = new Properties();
        properties.put("PGHOST", "server");
        properties.put("PGPORT", "5000");

        final DataSourceProperties dataSource = new DataSourceProperties("user", "password", "jdbc:postgresql:");
        dataSource.setProperties(properties);

        // Test preparing data source with no catalog
        final JdbcSchemaParser parser = new PostgreSqlSchemaParser();

        DataSourceProperties prepared = parser.prepareDataSource(dataSource, null);
        Assert.assertNotEquals(dataSource, prepared);
        Assert.assertEquals("postgres", prepared.getProperties().getProperty("PGDBNAME"));
        Assert.assertEquals(dataSource.getUrl(), prepared.getUrl());

        // Test preparing data source with user-defined catalog
        prepared = parser.prepareDataSource(dataSource, "kylo");
        Assert.assertNotEquals(dataSource, prepared);
        Assert.assertEquals("kylo", prepared.getProperties().getProperty("PGDBNAME"));
        Assert.assertEquals(dataSource.getUrl(), prepared.getUrl());
    }

    /**
     * Verify updating data source URL with a catalog name.
     */
    @Test
    public void prepareDataSourceWithBasicUrl() throws SQLException {
        // Test preparing data source with no catalog
        DataSourceProperties dataSource = new DataSourceProperties("user", "password", "jdbc:postgresql://localhost:5432");
        final JdbcSchemaParser parser = new PostgreSqlSchemaParser();
        DataSourceProperties prepared = parser.prepareDataSource(dataSource, null);

        Assert.assertNotEquals(dataSource, prepared);
        Assert.assertEquals("jdbc:postgresql://localhost:5432/postgres", prepared.getUrl());

        // Test preparing data source with user-defined catalog
        dataSource = new DataSourceProperties("user", "password", "jdbc:postgresql://localhost:5432?flag=true");
        prepared = parser.prepareDataSource(dataSource, "kylo");

        Assert.assertNotEquals(dataSource, prepared);
        Assert.assertEquals("jdbc:postgresql://localhost:5432/kylo?flag=true", prepared.getUrl());
    }

    /**
     * Verify ignoring catalog name if already specified by the data source info.
     */
    @Test
    public void prepareDataSourceWithDatabaseInfo() throws SQLException {
        // Mock data source properties
        final Properties properties = new Properties();
        properties.put("PGDBNAME", "kylo");
        properties.put("PGHOST", "server");
        properties.put("PGPORT", "5000");

        final DataSourceProperties dataSource = new DataSourceProperties("user", "password", "jdbc:postgresql:");
        dataSource.setProperties(properties);

        // Test preparing data source with existing catalog
        final JdbcSchemaParser parser = new PostgreSqlSchemaParser();
        final DataSourceProperties prepared = parser.prepareDataSource(dataSource, null);
        Assert.assertEquals(dataSource, prepared);
        Assert.assertEquals("kylo", prepared.getProperties().getProperty("PGDBNAME"));
        Assert.assertEquals(dataSource.getUrl(), prepared.getUrl());
    }

    /**
     * Verify ignoring catalog name if already specified by the data source URL.
     */
    @Test
    public void prepareDataSourceWithDatabaseUrl() throws SQLException {
        // Test preparing data source with existing catalog
        DataSourceProperties dataSource = new DataSourceProperties("user", "password", "jdbc:postgresql://localhost:5432/kylo");
        final JdbcSchemaParser parser = new PostgreSqlSchemaParser();
        DataSourceProperties prepared = parser.prepareDataSource(dataSource, null);

        Assert.assertEquals(dataSource, prepared);
        Assert.assertEquals(dataSource.getUrl(), prepared.getUrl());
    }
}
