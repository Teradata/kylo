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
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogException;
import com.thinkbiganalytics.kylo.catalog.api.MissingOptionException;
import com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc.JdbcHighWaterMark;
import com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc.JdbcRelationProvider;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.spark.Accumulable;
import org.apache.spark.AccumulableParam;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.SQLConf;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.catalyst.expressions.ScalaUDF;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.joda.time.DateTimeUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;

import javax.annotation.Nonnull;

import scala.Function1;

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
     * Verify creating a high water mark.
     */
    @Test
    public void createHighWaterMark() {
        final KyloCatalogClient client = Mockito.mock(KyloCatalogClient.class);

        // Test creating high water mark
        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();
        final JdbcHighWaterMark highWaterMark = provider.createHighWaterMark("mockWaterMark", client);

        Assert.assertEquals("mockWaterMark", highWaterMark.getName());
        Assert.assertNull("Expected initial value to be null", highWaterMark.getValue());

        // Test adding a value
        highWaterMark.accumulate(1525014303000L);
        Mockito.verify(client).setHighWaterMarks(Collections.singletonMap("mockWaterMark", "2018-04-29T15:05:03"));
    }

    /**
     * Verify creating a high water mark with an existing value.
     */
    @Test
    public void createHighWaterMarkWithExisting() {
        // Mock Kylo Catalog client
        final KyloCatalogClient client = Mockito.mock(KyloCatalogClient.class);
        Mockito.when(client.getHighWaterMarks()).thenReturn(Collections.singletonMap("mockWaterMark", "2018-04-29T15:05:03"));

        // Test creating high water mark
        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();
        final JdbcHighWaterMark highWaterMark = provider.createHighWaterMark("mockWaterMark", client);

        Assert.assertEquals("mockWaterMark", highWaterMark.getName());
        Assert.assertEquals(new Long(1525014303000L), highWaterMark.getValue());
        Mockito.reset(client);

        // Test adding a value
        highWaterMark.accumulate(86400000L);
        Mockito.verifyZeroInteractions(client);

        highWaterMark.accumulate(1532528828000L);
        Mockito.verify(client).setHighWaterMarks(Collections.singletonMap("mockWaterMark", "2018-07-25T14:27:08"));
    }

    /**
     * Verify filtering for new rows.
     */
    @Test
    public void filterByDateTime() {
        DateTimeUtils.setCurrentMillisFixed(1524960000000L);

        // Mock data set
        final DataFrame dataSet = Mockito.mock(DataFrame.class);

        final DataFrame filterDataSet = Mockito.mock(DataFrame.class);
        Mockito.when(dataSet.filter(Mockito.any(Column.class))).thenReturn(filterDataSet);

        // Test filtering by date time
        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();
        final DataFrame newDataSet = provider.filterByDateTime(dataSet, "mockField", null, null);
        Assert.assertEquals(filterDataSet, newDataSet);

        // Test condition
        final ArgumentCaptor<Column> conditionCaptor = ArgumentCaptor.forClass(Column.class);
        Mockito.verify(dataSet).filter(conditionCaptor.capture());
        Assert.assertEquals("(mockField < 1524960000000000)", conditionCaptor.getValue().toString());
    }

    /**
     * Verify filtering for new rows.
     */
    @Test
    public void filterByDateTimeWithOverlap() {
        DateTimeUtils.setCurrentMillisFixed(1524960000000L);

        // Mock data set
        final DataFrame dataSet = Mockito.mock(DataFrame.class);

        final DataFrame filterDataSet = Mockito.mock(DataFrame.class);
        Mockito.when(dataSet.filter(Mockito.any(Column.class))).thenReturn(filterDataSet);

        // Test filtering by date time
        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();
        final DataFrame newDataSet = provider.filterByDateTime(dataSet, "mockField", 1524873600000L, 60000L);
        Assert.assertEquals(filterDataSet, newDataSet);

        // Test condition
        final ArgumentCaptor<Column> conditionCaptor = ArgumentCaptor.forClass(Column.class);
        Mockito.verify(dataSet).filter(conditionCaptor.capture());
        Assert.assertEquals("((mockField > 1524873540000000) && (mockField < 1524959940000000))", conditionCaptor.getValue().toString());
    }

    /**
     * Verify filtering for new rows.
     */
    @Test
    public void filterByDateTimeWithStart() {
        DateTimeUtils.setCurrentMillisFixed(1524960000000L);

        // Mock data set
        final DataFrame dataSet = Mockito.mock(DataFrame.class);

        final DataFrame filterDataSet = Mockito.mock(DataFrame.class);
        Mockito.when(dataSet.filter(Mockito.any(Column.class))).thenReturn(filterDataSet);

        // Test filtering by date time
        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();
        final DataFrame newDataSet = provider.filterByDateTime(dataSet, "mockField", 1524873600000L, null);
        Assert.assertEquals(filterDataSet, newDataSet);

        // Test condition
        final ArgumentCaptor<Column> conditionCaptor = ArgumentCaptor.forClass(Column.class);
        Mockito.verify(dataSet).filter(conditionCaptor.capture());
        Assert.assertEquals("((mockField > 1524873600000000) && (mockField < 1524960000000000))", conditionCaptor.getValue().toString());
    }

    /**
     * Verify parsing the overlap option.
     */
    @Test
    public void getOverlap() {
        // Test null overlap
        final DataSetOptions options = new DataSetOptions();
        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();

        Assert.assertNull("Expected overlap to be null", provider.getOverlap(options));

        // Test non-null overlap
        options.setOption("overlap", "60");
        Assert.assertEquals(new Long(60000), provider.getOverlap(options));

        // Test negative overlap
        options.setOption("overlap", "-5");
        Assert.assertEquals(new Long(5000), provider.getOverlap(options));
    }

    /**
     * Verify exception for an invalid overlap value.
     */
    @Test(expected = KyloCatalogException.class)
    public void getOverlapWithInvalid() {
        final DataSetOptions options = new DataSetOptions();
        options.setOption("overlap", "a");

        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();
        provider.getOverlap(options);
    }

    /**
     * Verify updating a high water mark for a date column.
     */
    @Test
    public void updateHighWaterMarkWithDate() {
        // Mock data set
        final DataFrame dataSet = Mockito.mock(DataFrame.class);
        Mockito.when(dataSet.col("mockField")).thenReturn(new Column("mockField"));

        final StructField field = DataTypes.createStructField("mockField", DataTypes.DateType, true);
        Mockito.when(dataSet.schema()).thenReturn(DataTypes.createStructType(Collections.singletonList(field)));

        final DataFrame mapDataSet = Mockito.mock(DataFrame.class);
        Mockito.when(dataSet.withColumn(Mockito.eq("mockField"), Mockito.any(Column.class))).thenReturn(mapDataSet);

        // Test updating high water mark
        final KyloCatalogClient client = Mockito.mock(KyloCatalogClient.class);
        final JdbcHighWaterMark highWaterMark = new JdbcHighWaterMark("mockWaterMark", client);
        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();

        final DataFrame newDataSet = provider.updateHighWaterMark(dataSet, "mockField", highWaterMark, client);
        Assert.assertEquals(mapDataSet, newDataSet);

        // Test replaced column
        final ArgumentCaptor<Column> newColumn = ArgumentCaptor.forClass(Column.class);
        Mockito.verify(dataSet).withColumn(Mockito.eq("mockField"), newColumn.capture());
        Assert.assertTrue("Expected new column to be a UDF", newColumn.getValue().expr() instanceof ScalaUDF);
    }

    /**
     * Verify updating a high water mark for a invalid column.
     */
    @Test(expected = KyloCatalogException.class)
    public void updateHighWaterMarkWithInvalid() {
        // Mock data set
        final DataFrame dataSet = Mockito.mock(DataFrame.class);

        final StructField field = DataTypes.createStructField("mockField", DataTypes.StringType, true);
        Mockito.when(dataSet.schema()).thenReturn(DataTypes.createStructType(Collections.singletonList(field)));

        // Test updating high water mark
        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();
        provider.updateHighWaterMark(dataSet, "mockField", Mockito.mock(JdbcHighWaterMark.class), Mockito.mock(KyloCatalogClient.class));
    }

    /**
     * Verify updating a high water mark for a timestamp column.
     */
    @Test
    public void updateHighWaterMarkWithTimestamp() {
        // Mock data set
        final DataFrame dataSet = Mockito.mock(DataFrame.class);
        Mockito.when(dataSet.col("mockField")).thenReturn(new Column("mockField"));

        final StructField field = DataTypes.createStructField("mockField", DataTypes.TimestampType, true);
        Mockito.when(dataSet.schema()).thenReturn(DataTypes.createStructType(Collections.singletonList(field)));

        final DataFrame mapDataSet = Mockito.mock(DataFrame.class);
        Mockito.when(dataSet.withColumn(Mockito.eq("mockField"), Mockito.any(Column.class))).thenReturn(mapDataSet);

        // Test updating high water mark
        final KyloCatalogClient client = Mockito.mock(KyloCatalogClient.class);
        final JdbcHighWaterMark highWaterMark = new JdbcHighWaterMark("mockWaterMark", client);
        final MockJdbcDataSetProvider provider = new MockJdbcDataSetProvider();

        final DataFrame newDataSet = provider.updateHighWaterMark(dataSet, "mockField", highWaterMark, client);
        Assert.assertEquals(mapDataSet, newDataSet);

        // Test replaced column
        final ArgumentCaptor<Column> newColumn = ArgumentCaptor.forClass(Column.class);
        Mockito.verify(dataSet).withColumn(Mockito.eq("mockField"), newColumn.capture());
        Assert.assertTrue("Expected new column to be a UDF", newColumn.getValue().expr() instanceof ScalaUDF);
    }

    /**
     * Mock JDBC data set provider for testing.
     */
    private class MockJdbcDataSetProvider extends AbstractJdbcDataSetProvider<DataFrame> {

        @Nonnull
        @Override
        protected <R, P1> Accumulable<R, P1> accumulable(@Nonnull final R initialValue, @Nonnull final String name, @Nonnull final AccumulableParam<R, P1> param,
                                                         @Nonnull final KyloCatalogClient<DataFrame> client) {
            return DataSetProviderUtil.accumulable(initialValue, name, param);
        }

        @Nonnull
        @Override
        protected DataFrame filter(@Nonnull final DataFrame dataSet, @Nonnull final Column condition) {
            return dataSet.filter(condition);
        }

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
            return DataSetProviderUtil.load();
        }

        @Nonnull
        @Override
        protected DataFrame map(@Nonnull final DataFrame dataSet, @Nonnull final String fieldName, @Nonnull final Function1 function, @Nonnull final DataType returnType) {
            return DataSetProviderUtil.map(dataSet, fieldName, function, returnType);
        }

        @Nonnull
        @Override
        protected StructType schema(@Nonnull final DataFrame dataSet) {
            return DataSetProviderUtil.schema(dataSet);
        }
    }
}
