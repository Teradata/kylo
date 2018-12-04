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
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.Accumulable;
import org.apache.spark.AccumulableParam;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.catalyst.expressions.ScalaUDF;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Function1;
import scala.Unit;
import scala.collection.Seq;

public class AbstractSparkDataSetProviderTest {

    /**
     * Hadoop configuration for {@link AbstractSparkDataSetProvider#getHadoopConfiguration(KyloCatalogClient)}
     */
    private final Configuration conf = new Configuration(false);

    /**
     * Spark data frame
     */
    private DataFrame dataSet;

    /**
     * File format indicator for {@link AbstractSparkDataSetProvider#isFileFormat(Class)}
     */
    private boolean isFileFormat = false;

    /**
     * Verify reading a data set.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void read() {
        // Mock data set
        dataSet = Mockito.mock(DataFrame.class);

        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("text");
        options.setOption(KyloCatalogConstants.PATH_OPTION, "/mock/path/file.txt");

        // Test reading
        final MockSparkDataSetProvider provider = new MockSparkDataSetProvider();
        final DataFrame df = provider.read(Mockito.mock(KyloCatalogClient.class), options);
        Mockito.verifyZeroInteractions(df);
    }

    /**
     * Verify reading a data set and deleting the source file.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void readDeleteSourceFile() {
        isFileFormat = true;

        // Mock data set
        dataSet = Mockito.mock(DataFrame.class);
        Mockito.when(dataSet.col("value")).thenReturn(new Column("value"));

        final StructType schema = DataTypes.createStructType(Collections.singletonList(DataTypes.createStructField("value", DataTypes.StringType, true)));
        Mockito.when(dataSet.schema()).thenReturn(schema);

        final DataFrame mapDataSet = Mockito.mock(DataFrame.class);
        Mockito.when(dataSet.withColumn(Mockito.eq("value"), Mockito.any(Column.class))).thenReturn(mapDataSet);

        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("text");
        options.setOption(KyloCatalogConstants.PATH_OPTION, "/mock/path/file.txt");
        options.setOption("keepSourceFile", "FALSE");

        // Test reading
        final MockSparkDataSetProvider provider = new MockSparkDataSetProvider();
        final DataFrame df = provider.read(Mockito.mock(KyloCatalogClient.class), options);
        Assert.assertEquals(mapDataSet, df);

        final ArgumentCaptor<Column> newColumn = ArgumentCaptor.forClass(Column.class);
        Mockito.verify(dataSet).withColumn(Mockito.eq("value"), newColumn.capture());
        Assert.assertTrue("Expected new column to be a UDF", newColumn.getValue().expr() instanceof ScalaUDF);
    }

    /**
     * Mock {@code AbstractSparkDataSetProvider} for testing.
     */
    private class MockSparkDataSetProvider extends AbstractSparkDataSetProvider<DataFrame> {

        @Nonnull
        @Override
        protected <R, P1> Accumulable<R, P1> accumulable(@Nonnull final R initialValue, @Nonnull final String name, @Nonnull final AccumulableParam<R, P1> param,
                                                         @Nonnull final KyloCatalogClient<DataFrame> client) {
            return DataSetProviderUtil.accumulable(initialValue, name, param);
        }

        @Nonnull
        @Override
        protected DataFrameReader getDataFrameReader(@Nonnull final KyloCatalogClient<DataFrame> client, @Nonnull final DataSetOptions options) {
            return Mockito.mock(DataFrameReader.class);
        }

        @Nonnull
        @Override
        protected DataFrameWriter getDataFrameWriter(@Nonnull final DataFrame dataSet, @Nonnull final DataSetOptions options) {
            return new DataFrameWriter(dataSet);
        }

        @Nonnull
        @Override
        public Configuration getHadoopConfiguration(@Nonnull final KyloCatalogClient<DataFrame> client) {
            return conf;
        }

        @Override
        public boolean isFileFormat(@Nonnull final Class<?> formatClass) {
            return isFileFormat;
        }

        @Nonnull
        @Override
        protected DataFrame load(@Nonnull final DataFrameReader reader, @Nullable final Seq<String> paths) {
            return dataSet;
        }

        @Nonnull
        @Override
        protected DataFrame map(@Nonnull final DataFrame dataSet, @Nonnull final String fieldName, @Nonnull final Function1 function, @Nonnull final DataType returnType) {
            return DataSetProviderUtil.map(dataSet, fieldName, function, returnType);
        }

        @Override
        protected void onJobEnd(@Nonnull final Function1<SparkListenerJobEnd, Unit> function, @Nonnull final KyloCatalogClient<DataFrame> client) {
            // ignored
        }

        @Nonnull
        @Override
        protected StructType schema(@Nonnull final DataFrame dataSet) {
            return DataSetProviderUtil.schema(dataSet);
        }

        @Nonnull
        @Override
        protected DataFrame union(@Nonnull final DataFrame left, @Nonnull final DataFrame right) {
            return left;
        }
    }
}
