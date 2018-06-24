package com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc;

/*-
 * #%L
 * Kylo Catalog Core
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

import com.google.common.base.Throwables;
import com.thinkbiganalytics.kylo.catalog.spark.DataSourceResourceLoader;

import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.execution.datasources.jdbc.JDBCRelation;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;

import scala.Tuple2;
import scala.collection.immutable.Map;
import scala.collection.immutable.Map$;

public class JdbcRelationProviderTest {

    /**
     * Verify creating a JdbcRelation using a custom class loader.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testWithClassLoader() {
        // Create parameters map
        final Map<String, String> parameters = (Map<String, String>) Map$.MODULE$.<String, String>newBuilder()
            .$plus$eq(new Tuple2<>("dbtable", "mytable"))
            .$plus$eq(new Tuple2<>("url", "jdbc:h2:mem:spark"))
            .result();

        // Test creating a JDBC relation
        final DataSourceResourceLoader classLoader = new DataSourceResourceLoader(Mockito.mock(SparkContext.class), Thread.currentThread().getContextClassLoader());
        final AtomicReference<JdbcRelation> relation = new AtomicReference<>();
        classLoader.runWithThreadContext(new Runnable() {
            @Override
            public void run() {
                try (final Connection conn = DriverManager.getConnection("jdbc:h2:mem:spark"); final Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE TABLE mytable (col1 VARCHAR)");

                    final JdbcRelationProvider provider = new JdbcRelationProvider();
                    relation.set((JdbcRelation) provider.createRelation(Mockito.mock(SQLContext.class), parameters));
                } catch (final Exception e) {
                    Throwables.propagate(e);
                }
            }
        });

        Assert.assertNotNull("Expected relation to be created", relation.get());
        Assert.assertEquals(JDBCRelation.class, relation.get().getDelegate().getClass());
        Assert.assertEquals(classLoader, relation.get().getLoader());
    }

    /**
     * Verify creating a JdbcRelation using the default class loader.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testWithoutClassLoader() throws Exception {
        // Create parameters map
        final Map<String, String> parameters = (Map<String, String>) Map$.MODULE$.<String, String>newBuilder()
            .$plus$eq(new Tuple2<>("dbtable", "mytable"))
            .$plus$eq(new Tuple2<>("url", "jdbc:h2:mem:spark"))
            .result();

        // Test creating a JDBC relation
        try (final Connection conn = DriverManager.getConnection("jdbc:h2:mem:spark"); final Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE mytable (col1 VARCHAR)");

            final JdbcRelationProvider provider = new JdbcRelationProvider();
            final JdbcRelation relation = (JdbcRelation) provider.createRelation(Mockito.mock(SQLContext.class), parameters);
            Assert.assertEquals(JDBCRelation.class, relation.getDelegate().getClass());
            Assert.assertNull("Expected null class loader", relation.getLoader());
        }
    }
}
