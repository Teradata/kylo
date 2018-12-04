package com.thinkbiganalytics.kylo.catalog.spark.sources;

/*-
 * #%L
 * Kylo Catalog for Spark 2
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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
import com.thinkbiganalytics.kylo.catalog.spark.KyloCatalogClientV2;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.Accumulable;
import org.apache.spark.AccumulableParam;
import org.apache.spark.scheduler.SparkListener;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.execution.datasources.FileFormat;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Function1;
import scala.Unit;
import scala.collection.Seq;

/**
 * A data set provider that can read from and write to Spark 2 data sources.
 */
public class SparkDataSetProviderV2 extends AbstractSparkDataSetProvider<Dataset<Row>> {

    @Nonnull
    @Override
    protected <R, P1> Accumulable<R, P1> accumulable(@Nonnull final R initialValue, @Nonnull final String name, @Nonnull final AccumulableParam<R, P1> param,
                                                     @Nonnull final KyloCatalogClient<Dataset<Row>> client) {
        return DataSetProviderUtilV2.accumulable(initialValue, name, param, client);
    }

    @Nonnull
    @Override
    protected DataFrameReader getDataFrameReader(@Nonnull final KyloCatalogClient<Dataset<Row>> client, @Nonnull final DataSetOptions options) {
        return DataSetProviderUtilV2.getDataFrameReader(client);
    }

    @Nonnull
    @Override
    protected DataFrameWriter getDataFrameWriter(@Nonnull final Dataset<Row> dataSet, @Nonnull final DataSetOptions options) {
        return DataSetProviderUtilV2.getDataFrameWriter(dataSet, options);
    }

    @Nonnull
    @Override
    public Configuration getHadoopConfiguration(@Nonnull final KyloCatalogClient<Dataset<Row>> client) {
        return ((KyloCatalogClientV2) client).getSparkSession().sparkContext().hadoopConfiguration();
    }

    @Override
    public boolean isFileFormat(@Nonnull final Class<?> formatClass) {
        return FileFormat.class.isAssignableFrom(formatClass);
    }

    @Nonnull
    @Override
    protected Dataset<Row> load(@Nonnull final DataFrameReader reader, @Nullable final Seq<String> paths) {
        return DataSetProviderUtilV2.load(reader, paths);
    }

    @Nonnull
    @Override
    protected Dataset<Row> map(@Nonnull final Dataset<Row> dataSet, @Nonnull final String fieldName, @Nonnull final Function1 function, @Nonnull final DataType returnType) {
        return DataSetProviderUtilV2.map(dataSet, fieldName, function, returnType);
    }

    @Override
    protected void onJobEnd(@Nonnull final Function1<SparkListenerJobEnd, Unit> function, @Nonnull final KyloCatalogClient<Dataset<Row>> client) {
        final SparkListener listener = new SparkListener() {
            @Override
            public void onJobEnd(@Nonnull final SparkListenerJobEnd jobEnd) {
                function.apply(jobEnd);
            }
        };
        ((KyloCatalogClientV2) client).getSparkSession().sparkContext().addSparkListener(listener);
    }

    @Nonnull
    @Override
    protected StructType schema(@Nonnull final Dataset<Row> dataSet) {
        return DataSetProviderUtilV2.schema(dataSet);
    }

    @Nonnull
    @Override
    protected Dataset<Row> union(@Nonnull final Dataset<Row> left, @Nonnull final Dataset<Row> right) {
        return left.union(right);
    }
}
