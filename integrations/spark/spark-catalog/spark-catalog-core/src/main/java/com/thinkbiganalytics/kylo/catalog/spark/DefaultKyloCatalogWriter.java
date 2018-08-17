package com.thinkbiganalytics.kylo.catalog.spark;

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
import com.google.common.collect.Lists;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogWriter;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.sql.SaveMode;

import java.util.Arrays;
import java.util.Collections;

import javax.annotation.Nonnull;

import scala.Option;

/**
 * Saves a Spark {@code DataFrame} to an external system.
 *
 * @param <T> Spark {@code DataFrame} class
 * @see KyloCatalogClient#write(Object)
 */
class DefaultKyloCatalogWriter<T> extends AbstractDataSetOptionsAccess<KyloCatalogWriter<T>> implements KyloCatalogWriter<T> {

    /**
     * Kylo catalog client
     */
    @Nonnull
    private final KyloCatalogClient<T> client;

    /**
     * Source data set
     */
    @Nonnull
    private final T dataSet;

    /**
     * Constructs a {@code DefaultKyloCatalogWriter} for the specified Kylo catalog client and source data set.
     */
    DefaultKyloCatalogWriter(@Nonnull final KyloCatalogClient<T> client, @Nonnull final Configuration hadoopConfiguration, @Nonnull final DataSourceResourceLoader resourceLoader,
                             @Nonnull final T dataSet) {
        super(new DataSetOptions(), hadoopConfiguration, resourceLoader);
        this.client = client;
        this.dataSet = dataSet;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> bucketBy(final int numBuckets, @Nonnull final String colName, final String... colNames) {
        options.setNumBuckets(numBuckets);
        options.setBucketColumnNames((colNames != null && colNames.length > 0) ? Lists.asList(colName, colNames) : Collections.singletonList(colName));
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> mode(@Nonnull final String saveMode) {
        mode(SparkUtil.toSaveMode(saveMode));
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> mode(@Nonnull final SaveMode saveMode) {
        options.setMode(saveMode);
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> partitionBy(@Nonnull final String... colNames) {
        options.setPartitioningColumns(Arrays.asList(colNames));
        return this;
    }

    @Override
    public void save() {
        Preconditions.checkNotNull(options.getFormat(), "Format must be defined");

        final Option<DataSetProvider<T>> provider = client.getDataSetProvider(options.getFormat());
        if (!provider.isDefined()) {
            throw new IllegalStateException("Format is not supported: " + options.getFormat());
        }

        resourceLoader.runWithThreadContext(new Runnable() {
            @Override
            public void run() {
                provider.get().write(client, options, dataSet);
            }
        });
    }

    @Override
    public void save(@Nonnull final String path) {
        options.setOption(KyloCatalogConstants.PATH_OPTION, path);
        save();
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> sortBy(@Nonnull final String colName, final String... colNames) {
        options.setSortColumnNames((colNames != null && colNames.length > 0) ? Lists.asList(colName, colNames) : Collections.singletonList(colName));
        return this;
    }
}
