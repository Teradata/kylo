package com.thinkbiganalytics.kylo.catalog.spark.sources;

/*-
 * #%L
 * Kylo Catalog for Spark 1
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
import com.thinkbiganalytics.kylo.catalog.spark.KyloCatalogClientV1;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.Accumulable;
import org.apache.spark.AccumulableParam;
import org.apache.spark.JavaSparkListener;
import org.apache.spark.scheduler.SparkListener;
import org.apache.spark.scheduler.SparkListenerEvent;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.sources.HadoopFsRelationProvider;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Function1;
import scala.Unit;
import scala.collection.Seq;

/**
 * A data set provider that can read from and write to Spark 1 data sources.
 */
public class SparkDataSetProviderV1 extends AbstractSparkDataSetProvider<DataFrame> {

    @Nonnull
    @Override
    protected <R, P1> Accumulable<R, P1> accumulable(@Nonnull final R initialValue, @Nonnull final String name, @Nonnull final AccumulableParam<R, P1> param,
                                                     @Nonnull final KyloCatalogClient<DataFrame> client) {
        return DataSetProviderUtilV1.accumulable(initialValue, name, param, client);
    }

    @Nonnull
    @Override
    protected DataFrameReader getDataFrameReader(@Nonnull final KyloCatalogClient<DataFrame> client, @Nonnull final DataSetOptions options) {
        return DataSetProviderUtilV1.getDataFrameReader(client);
    }

    @Nonnull
    @Override
    protected DataFrameWriter getDataFrameWriter(@Nonnull final DataFrame dataSet, @Nonnull final DataSetOptions options) {
        return DataSetProviderUtilV1.getDataFrameWriter(dataSet);
    }

    @Nonnull
    @Override
    public Configuration getHadoopConfiguration(@Nonnull final KyloCatalogClient<DataFrame> client) {
        return ((KyloCatalogClientV1) client).getSQLContext().sparkContext().hadoopConfiguration();
    }

    @Override
    public boolean isFileFormat(@Nonnull final Class<?> formatClass) {
        return HadoopFsRelationProvider.class.isAssignableFrom(formatClass);
    }

    @Nonnull
    @Override
    protected DataFrame load(@Nonnull final DataFrameReader reader, @Nullable final Seq<String> paths) {
        return DataSetProviderUtilV1.load(reader, paths);
    }

    @Nonnull
    @Override
    protected DataFrame map(@Nonnull final DataFrame dataSet, @Nonnull final String fieldName, @Nonnull final Function1 function, @Nonnull final DataType returnType) {
        return DataSetProviderUtilV1.map(dataSet, fieldName, function, returnType);
    }

    @Override
    protected void onJobEnd(@Nonnull final Function1<SparkListenerJobEnd, Unit> function, @Nonnull final KyloCatalogClient<DataFrame> client) {
        final SparkListener listener = new JavaSparkListener() {
            @Override
            public void onJobEnd(@Nonnull final SparkListenerJobEnd jobEnd) {
                function.apply(jobEnd);
            }

            @SuppressWarnings("unused")  // method required for CDH 5.8+
            public void onOtherEvent(@Nonnull final SparkListenerEvent event) {
                // ignored
            }
        };
        ((KyloCatalogClientV1) client).getSQLContext().sparkContext().addSparkListener(listener);
    }

    @Nonnull
    @Override
    protected StructType schema(@Nonnull final DataFrame dataSet) {
        return DataSetProviderUtilV1.schema(dataSet);
    }

    @Nonnull
    @Override
    protected DataFrame union(@Nonnull final DataFrame left, @Nonnull final DataFrame right) {
        return left.unionAll(right);
    }
}
