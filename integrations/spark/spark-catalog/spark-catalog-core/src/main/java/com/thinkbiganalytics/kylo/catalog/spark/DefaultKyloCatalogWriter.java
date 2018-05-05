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

import com.google.common.annotations.VisibleForTesting;
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
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Saves a Spark {@code DataFrame} to an external system.
 *
 * @param <T> Spark {@code DataFrame} class
 * @see KyloCatalogClient#write(Object)
 */
class DefaultKyloCatalogWriter<T> implements KyloCatalogWriter<T> {

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
     * Hadoop configuration
     */
    @Nonnull
    private final Configuration hadoopConfiguration;

    /**
     * Write options
     */
    @Nonnull
    private final DataSetOptions options = new DataSetOptions();

    /**
     * Loads resources for accessing data sets
     */
    @Nonnull
    private final DataSourceResourceLoader resourceLoader;

    /**
     * Constructs a {@code DefaultKyloCatalogWriter} for the specified Kylo catalog client and source data set.
     */
    DefaultKyloCatalogWriter(@Nonnull final KyloCatalogClient<T> client, @Nonnull final Configuration hadoopConfiguration, @Nonnull final DataSourceResourceLoader resourceLoader,
                             @Nonnull final T dataSet) {
        this.client = client;
        this.hadoopConfiguration = hadoopConfiguration;
        this.resourceLoader = resourceLoader;
        this.dataSet = dataSet;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> addFile(@Nullable final String path) {
        if (path != null) {
            resourceLoader.addFile(path);
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> addFiles(@Nullable final List<String> paths) {
        if (paths != null) {
            for (final String path : paths) {
                addFile(path);
            }
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> addFiles(@Nullable final Seq<String> paths) {
        if (paths != null) {
            addFiles(JavaConversions.seqAsJavaList(paths));
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> addJar(@Nullable final String path) {
        if (path != null && resourceLoader.addJar(path)) {
            options.addJar(path);
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> addJars(@Nullable final List<String> paths) {
        if (paths != null && resourceLoader.addJars(paths)) {
            options.addJars(paths);
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> addJars(@Nullable final Seq<String> paths) {
        if (paths != null) {
            addJars(JavaConversions.seqAsJavaList(paths));
        }
        return this;
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
    public DefaultKyloCatalogWriter<T> format(@Nonnull final String source) {
        options.setFormat(source);
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
    public DefaultKyloCatalogWriter<T> option(@Nonnull final String key, final double value) {
        return option(key, Double.toString(value));
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> option(@Nonnull final String key, final long value) {
        return option(key, Long.toString(value));
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> option(@Nonnull final String key, final boolean value) {
        return option(key, Boolean.toString(value));
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> option(@Nonnull final String key, @Nullable final String value) {
        if (key.startsWith(KyloCatalogConstants.HADOOP_CONF_PREFIX)) {
            hadoopConfiguration.set(key.substring(KyloCatalogConstants.HADOOP_CONF_PREFIX.length()), value);
        }
        options.setOption(key, value);
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> options(@Nullable final java.util.Map<String, String> options) {
        if (options != null) {
            for (final Map.Entry<String, String> entry : options.entrySet()) {
                option(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogWriter<T> options(@Nullable final scala.collection.Map<String, String> options) {
        if (options != null) {
            options(JavaConversions.mapAsJavaMap(options));
        }
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

    /**
     * Gets the data set options.
     */
    @Nonnull
    @VisibleForTesting
    DataSetOptions getOptions() {
        return options;
    }
}
