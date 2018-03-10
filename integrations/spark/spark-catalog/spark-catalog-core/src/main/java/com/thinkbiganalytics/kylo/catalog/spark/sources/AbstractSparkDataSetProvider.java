package com.thinkbiganalytics.kylo.catalog.spark.sources;

/*-
 * #%L
 * Kylo Catalog Core
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
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.spark.SparkSqlUtil;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Base implementation of a data set provider that can read from and write to any Spark data source.
 *
 * @param <T> Spark {@code DataFrame} class
 */
abstract class AbstractSparkDataSetProvider<T> implements DataSetProvider<T> {

    @Override
    public final boolean supportsFormat(@Nonnull final String source) {
        // supports any format that doesn't have another implementation
        return !KyloCatalogConstants.HIVE_FORMAT.equalsIgnoreCase(source);
    }

    @Nonnull
    @Override
    public final T read(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options) {
        final DataFrameReader reader = SparkSqlUtil.prepareDataFrameReader(getDataFrameReader(client, options), options);
        final Seq<String> paths = (options.getPaths() != null) ? JavaConversions.asScalaBuffer(options.getPaths()) : null;
        return load(reader, paths);
    }

    @Override
    public final void write(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options, @Nonnull final T dataSet) {
        final DataFrameWriter writer = SparkSqlUtil.prepareDataFrameWriter(getDataFrameWriter(dataSet, options), options);
        writer.save();
    }

    /**
     * Gets a reader from the specified client.
     *
     * <p>The options, format, and scheme will be applied to the reader before loading.</p>
     */
    @Nonnull
    protected abstract DataFrameReader getDataFrameReader(@Nonnull KyloCatalogClient<T> client, @Nonnull DataSetOptions options);

    /**
     * Gets a writer for the specified data set.
     *
     * <p>The options, format, mode, and partitioning will be applied to the writer before saving.</p>
     */
    @Nonnull
    protected abstract DataFrameWriter getDataFrameWriter(@Nonnull T dataSet, @Nonnull DataSetOptions options);

    /**
     * Loads a data set using the specified reader and paths.
     */
    @Nonnull
    protected abstract T load(@Nonnull DataFrameReader reader, @Nullable Seq<String> paths);
}
