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
import com.thinkbiganalytics.kylo.catalog.spark.SparkSqlUtilV2;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.execution.datasources.FileFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.collection.Seq;
import scala.collection.Seq$;

/**
 * A data set provider that can read from and write to Spark 2 data sources.
 */
public class SparkDataSetProviderV2 extends AbstractSparkDataSetProvider<Dataset<Row>> {

    @Nonnull
    @Override
    protected DataFrameReader getDataFrameReader(@Nonnull final KyloCatalogClient<Dataset<Row>> client, @Nonnull final DataSetOptions options) {
        return ((KyloCatalogClientV2) client).getSparkSession().read();
    }

    @Nonnull
    @Override
    protected DataFrameWriter getDataFrameWriter(@Nonnull final Dataset<Row> dataSet, @Nonnull final DataSetOptions options) {
        return SparkSqlUtilV2.prepareDataFrameWriter(dataSet.write(), options);
    }

    @Nonnull
    @Override
    protected Configuration getHadoopConfiguration(@Nonnull final KyloCatalogClient<Dataset<Row>> client) {
        return ((KyloCatalogClientV2) client).getSparkSession().sparkContext().hadoopConfiguration();
    }

    @Override
    protected boolean isFileFormat(@Nonnull final Class<?> formatClass) {
        return FileFormat.class.isAssignableFrom(formatClass);
    }

    @Nonnull
    @Override
    protected Dataset<Row> load(@Nonnull final DataFrameReader reader, @Nullable final Seq<String> paths) {
        //noinspection unchecked
        return reader.load((paths != null) ? paths : (Seq<String>) Seq$.MODULE$.empty());
    }
}
