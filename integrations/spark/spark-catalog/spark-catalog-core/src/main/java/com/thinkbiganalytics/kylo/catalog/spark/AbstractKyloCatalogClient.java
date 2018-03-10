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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogReader;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogWriter;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkContext;

import java.io.IOException;
import java.net.URLStreamHandlerFactory;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Option;

/**
 * Base implementation of a {@link KyloCatalogClient} backed by a Spark engine.
 *
 * @param <T> Spark {@code DataFrame} class
 * @see AbstractKyloCatalogClientBuilder
 */
abstract class AbstractKyloCatalogClient<T> implements KyloCatalogClient<T> {

    /**
     * List of data set providers to try in-order
     */
    @Nonnull
    private final List<DataSetProvider<T>> dataSetProviders;

    /**
     * Hadoop configuration
     */
    @Nonnull
    private final Configuration hadoopConfiguration;

    /**
     * Indicates the client has been closed
     */
    private boolean isClosed = false;

    /**
     * Loads resources for accessing data sets
     */
    @Nonnull
    private final DataSourceResourceLoader resourceLoader;

    /**
     * Constructs an {@code AbstractKyloCatalogClient}.
     *
     * @param sparkContext            Spark context
     * @param dataSetProviders        list of data set providers to search in-order
     * @param urlStreamHandlerFactory provides access to JAR files in Hadoop
     */
    AbstractKyloCatalogClient(@Nonnull final SparkContext sparkContext, @Nonnull final List<DataSetProvider<T>> dataSetProviders, @Nullable final URLStreamHandlerFactory urlStreamHandlerFactory) {
        hadoopConfiguration = sparkContext.hadoopConfiguration();
        this.dataSetProviders = dataSetProviders;
        resourceLoader = DataSourceResourceLoader.create(urlStreamHandlerFactory, sparkContext);
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
        resourceLoader.close();
    }

    @Nonnull
    @Override
    public Option<DataSetProvider<T>> getDataSetProvider(@Nonnull final String format) {
        for (final DataSetProvider<T> provider : dataSetProviders) {
            if (provider.supportsFormat(format)) {
                return Option.apply(provider);
            }
        }
        //noinspection RedundantTypeArguments
        return Option.<DataSetProvider<T>>empty();
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Nonnull
    @Override
    public KyloCatalogReader<T> read() {
        return new DefaultKyloCatalogReader<>(this, hadoopConfiguration, resourceLoader);
    }

    @Nonnull
    @Override
    public KyloCatalogWriter<T> write(@Nonnull T df) {
        return new DefaultKyloCatalogWriter<>(this, hadoopConfiguration, resourceLoader, df);
    }
}
