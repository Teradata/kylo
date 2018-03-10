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

import com.google.common.base.Preconditions;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.spark.SparkSqlUtil;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.spark.sql.DataFrameWriter;

import javax.annotation.Nonnull;

import scala.Option;

/**
 * Base implementation of a data set provider that can read from and write to Hive tables.
 *
 * @param <T> Spark {@code DataFrame} class
 */
abstract class AbstractHiveDataSetProvider<T> implements DataSetProvider<T> {

    @Override
    public final boolean supportsFormat(@Nonnull final String format) {
        return KyloCatalogConstants.HIVE_FORMAT.equalsIgnoreCase(format);
    }

    @Nonnull
    @Override
    public final T read(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options) {
        final Option<String> path = options.getOption(KyloCatalogConstants.PATH_OPTION);
        if (path.isDefined()) {
            return loadFromTable(client, path.get());
        } else {
            throw new IllegalStateException("Table name must be defined as path");
        }
    }

    @Override
    public final void write(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options, @Nonnull final T dataSet) {
        final Option<String> pathOption = options.getOption(KyloCatalogConstants.PATH_OPTION);
        final String tableName = (pathOption.isDefined()) ? pathOption.get() : null;
        Preconditions.checkNotNull(tableName, "Table name must be defined as path");

        final DataFrameWriter writer = SparkSqlUtil.prepareDataFrameWriter(getDataFrameWriter(dataSet, options), options);
        if (KyloCatalogConstants.HIVE_FORMAT.equals(options.getFormat())) {
            writer.format("orc");  // set default output format
        }
        writer.saveAsTable(tableName);
    }

    /**
     * Gets a writer for the specified data set.
     *
     * <p>The options, format, mode, and partitioning will be applied to the writer before saving.</p>
     */
    @Nonnull
    protected abstract DataFrameWriter getDataFrameWriter(@Nonnull T dataSet, @Nonnull DataSetOptions options);

    /**
     * Reads in the specified Hive table.
     */
    @Nonnull
    protected abstract T loadFromTable(@Nonnull KyloCatalogClient<T> client, @Nonnull String tableName);
}
