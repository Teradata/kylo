package com.thinkbiganalytics.kylo.catalog.spark.sources;

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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.MissingOptionException;
import com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc.JdbcRelationProvider;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.SQLConf;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import javax.annotation.Nonnull;

@SuppressWarnings("unchecked")
public class AbstractJdbcDataSetProviderTest {

    /**
     * Spark SQL reader
     */
    private DataFrameReader reader;

    /**
     * Set up tests.
     */
    @Before
    public void beforeEach() {
        reader = Mockito.mock(DataFrameReader.class);
    }

    /**
     * Verify reading from a JDBC table.
     */
    @Test
    public void read() {
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("jdbc");

        // Test reading
        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();
        provider.read(Mockito.mock(KyloCatalogClient.class), options);

        Mockito.verify(reader).format(JdbcRelationProvider.class.getName());
    }

    /**
     * Verify writing to a JDBC table.
     */
    @Test
    public void write() throws Exception {
        // Mock data set
        final DataFrame dataFrame = Mockito.mock(DataFrame.class);

        final SQLContext sqlContext = Mockito.mock(SQLContext.class);
        Mockito.when(sqlContext.conf()).thenReturn(new SQLConf());
        Mockito.when(dataFrame.sqlContext()).thenReturn(sqlContext);

        final StructField field1 = DataTypes.createStructField("col1", DataTypes.IntegerType, true);
        final StructField field2 = DataTypes.createStructField("col2", DataTypes.StringType, true);
        Mockito.when(dataFrame.schema()).thenReturn(DataTypes.createStructType(Arrays.asList(field1, field2)));

        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setOption("dbtable", "mytable");
        options.setOption("url", "jdbc:h2:mem:spark");

        // Test writing
        try (final Connection conn = DriverManager.getConnection("jdbc:h2:mem:spark")) {
            final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();
            provider.write(Mockito.mock(KyloCatalogClient.class), options, dataFrame);

            try (final Statement stmt = conn.createStatement(); final ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM mytable")) {
                Assert.assertTrue("Expected 2 rows; found 0 rows", rs.next());
                Assert.assertEquals("COL1", rs.getString(1));
                Assert.assertEquals("INTEGER(10)", rs.getString(2));

                Assert.assertTrue("Expected 2 rows; found 1 row", rs.next());
                Assert.assertEquals("COL2", rs.getString(1));
                Assert.assertEquals("CLOB(2147483647)", rs.getString(2));

                Assert.assertFalse("Expected 2 rows; fonud 3 rows", rs.next());
            }
        }
    }

    /**
     * Verify exception if dbtable is not defined.
     */
    @Test(expected = MissingOptionException.class)
    public void writeWithoutDbtable() {
        final DataSetOptions options = new DataSetOptions();
        options.setOption("url", "jdbc:h2:mem:");

        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();
        provider.write(Mockito.mock(KyloCatalogClient.class), options, Mockito.mock(DataFrame.class));
    }

    /**
     * Verify exception if url is not defined.
     */
    @Test(expected = MissingOptionException.class)
    public void writeWithoutUrl() {
        final DataSetOptions options = new DataSetOptions();
        options.setOption("dbtable", "mytable");

        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();
        provider.write(Mockito.mock(KyloCatalogClient.class), options, Mockito.mock(DataFrame.class));
    }

    /**
     * Mock JDBC data set provider for testing.
     */
    private class MockJdbcDataSetProvider extends AbstractJdbcDataSetProvider<DataFrame> {

        @Nonnull
        @Override
        protected DataFrameReader getDataFrameReader(@Nonnull final KyloCatalogClient<DataFrame> client, @Nonnull final DataSetOptions options) {
            return reader;
        }

        @Nonnull
        @Override
        @SuppressWarnings("ConstantConditions")
        protected DataFrameWriter getDataFrameWriter(@Nonnull final DataFrame dataSet, @Nonnull final DataSetOptions options) {
            return new DataFrameWriter(dataSet);
        }

        @Nonnull
        @Override
        protected DataFrame load(@Nonnull final DataFrameReader reader) {
            return Mockito.mock(DataFrame.class);
        }
    }
}
