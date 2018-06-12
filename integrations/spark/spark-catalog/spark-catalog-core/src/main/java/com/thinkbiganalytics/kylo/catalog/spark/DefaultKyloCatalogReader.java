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
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogException;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogReader;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.sql.types.StructType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Loads a Spark {@code DataFrame} from an external system.
 *
 * @param <T> Spark {@code DataFrame} class
 * @see KyloCatalogClient#read()
 */
class DefaultKyloCatalogReader<T> implements KyloCatalogReader<T> {

    /**
     * Kylo catalog client
     */
    @Nonnull
    private final KyloCatalogClient<T> client;

    /**
     * Hadoop configuration
     */
    @Nonnull
    private final Configuration hadoopConfiguration;

    /**
     * Read options
     */
    @Nonnull
    private final DataSetOptions options = new DataSetOptions();

    /**
     * Loads resources for accessing data sets
     */
    @Nonnull
    private final DataSourceResourceLoader resourceLoader;

    /**
     * Constructs a {@code DefaultKyloCatalogReader} for the specified Kylo catalog client.
     */
    DefaultKyloCatalogReader(@Nonnull final KyloCatalogClient<T> client, @Nonnull final Configuration hadoopConfiguration, @Nonnull final DataSourceResourceLoader resourceLoader) {
        this.client = client;
        this.hadoopConfiguration = hadoopConfiguration;
        this.resourceLoader = resourceLoader;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> addFile(@Nullable final String path) {
        if (path != null) {
            resourceLoader.addFile(path);
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> addFiles(@Nullable final List<String> paths) {
        if (paths != null) {
            for (final String path : paths) {
                addFile(path);
            }
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> addFiles(@Nullable final Seq<String> paths) {
        if (paths != null) {
            addFiles(JavaConversions.seqAsJavaList(paths));
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> addJar(@Nullable final String path) {
        if (path != null && resourceLoader.addJar(path)) {
            options.addJar(path);
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> addJars(@Nullable final List<String> paths) {
        if (paths != null && resourceLoader.addJars(paths)) {
            options.addJars(paths);
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> addJars(@Nullable final Seq<String> paths) {
        if (paths != null) {
            addJars(JavaConversions.seqAsJavaList(paths));
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> format(@Nonnull final String source) {
        options.setFormat(source);
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> option(@Nonnull final String key, final double value) {
        return option(key, Double.toString(value));
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> option(@Nonnull final String key, final long value) {
        return option(key, Long.toString(value));
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> option(@Nonnull final String key, final boolean value) {
        return option(key, Boolean.toString(value));
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> option(@Nonnull final String key, @Nullable final String value) {
        if (key.startsWith(KyloCatalogConstants.HADOOP_CONF_PREFIX)) {
            hadoopConfiguration.set(key.substring(KyloCatalogConstants.HADOOP_CONF_PREFIX.length()), value);
        }
        options.setOption(key, value);
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> options(@Nullable final java.util.Map<String, String> options) {
        if (options != null) {
            for (final Map.Entry<String, String> entry : options.entrySet()) {
                option(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> options(@Nullable final scala.collection.Map<String, String> options) {
        if (options != null) {
            options(JavaConversions.mapAsJavaMap(options));
        }
        return this;
    }

    @Nonnull
    @Override
    public DefaultKyloCatalogReader<T> schema(@Nullable final StructType schema) {
        options.setSchema(schema);
        return this;
    }

    @Nonnull
    @Override
    public T load(@Nonnull final String... paths) {
        options.setPaths(Arrays.asList(paths));
        return load();
    }

    @Nonnull
    @Override
    public T load(@Nonnull final String path) {
        options.setOption(KyloCatalogConstants.PATH_OPTION, path);
        return load();
    }

    @Nonnull
    @Override
    public T load() {
        Preconditions.checkNotNull(options.getFormat(), "Format must be defined");

        // Find data set provider
        final Option<DataSetProvider<T>> provider = client.getDataSetProvider(options.getFormat());
        if (!provider.isDefined()) {
            throw new KyloCatalogException("Format is not supported: " + options.getFormat());
        }

        // Load data set
        try {
            return resourceLoader.runWithThreadContext(new Callable<T>() {
                @Override
                public T call() {
                    return provider.get().read(client, options);
                }
            });
        } catch (final Exception e) {
            throw new KyloCatalogException("Unable to load '" + options.getFormat() + "' source: " + e, e);
        }
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
