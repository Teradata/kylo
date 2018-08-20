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

import com.google.common.base.Optional;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogException;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogReader;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogWriter;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.sources.DataSourceRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Option;

/**
 * Base implementation of a {@link KyloCatalogClient} backed by a Spark engine.
 *
 * @param <T> Spark {@code DataFrame} class
 * @see AbstractKyloCatalogClientBuilder
 */
public abstract class AbstractKyloCatalogClient<T> implements KyloCatalogClient<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractKyloCatalogClient.class);

    /**
     * Map of data set identifier to data set
     */
    @Nullable
    private Map<String, DataSetTemplate> dataSets;

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
     * High water marks
     */
    @Nonnull
    private final Map<String, String> highWaterMarks = new HashMap<>();

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
     * @param sparkContext     Spark context
     * @param dataSetProviders list of data set providers to search in-order
     */
    AbstractKyloCatalogClient(@Nonnull final SparkContext sparkContext, @Nonnull final List<DataSetProvider<T>> dataSetProviders) {
        hadoopConfiguration = sparkContext.hadoopConfiguration();
        this.dataSetProviders = dataSetProviders;
        resourceLoader = DataSourceResourceLoader.create(sparkContext);
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

    @Nonnull
    @Override
    public Map<String, String> getHighWaterMarks() {
        return Collections.unmodifiableMap(highWaterMarks);
    }

    /**
     * Gets the data source with the specified name.
     *
     * @param shortName data source name, case insensitive
     * @return the data source, if found
     * @throws IllegalArgumentException if multiple data sources match the name
     */
    @Nonnull
    public Option<DataSourceRegister> getDataSource(@Nonnull final String shortName) {
        final Optional<DataSourceRegister> dataSource = resourceLoader.getDataSource(shortName);
        return dataSource.isPresent() ? Option.apply(dataSource.get()) : Option.<DataSourceRegister>empty();
    }

    @Override
    public boolean isClosed() {
        if (!isClosed && isSparkStopped()) {
            try {
                close();
            } catch (final Exception e) {
                log.warn("Failed to close KyloCatalogClient[{}]: {}", this, e, e);
            }
        }
        return isClosed;
    }

    @Nonnull
    @Override
    public KyloCatalogReader<T> read() {
        return new DefaultKyloCatalogReader<>(this, hadoopConfiguration, resourceLoader);
    }

    @Nonnull
    @Override
    public KyloCatalogReader<T> read(@Nonnull final String id) {
        final DataSetTemplate dataSet = (dataSets != null) ? dataSets.get(id) : null;
        if (dataSet != null) {
            final DefaultKyloCatalogReader<T> reader = new DefaultKyloCatalogReader<>(this, hadoopConfiguration, resourceLoader);
            reader.dataSet(dataSet);
            return reader;
        } else {
            throw new KyloCatalogException("Data set does not exist: " + id);
        }
    }

    /**
     * Sets the pre-defined data sets for this client.
     *
     * @see #read(String)
     * @see #write(Object, String)
     */
    public void setDataSets(@Nonnull final Map<String, DataSetTemplate> dataSets) {
        this.dataSets = new HashMap<>(dataSets);
    }

    @Override
    public void setHighWaterMarks(@Nonnull final Map<String, String> highWaterMarks) {
        for (final Map.Entry<String, String> entry : highWaterMarks.entrySet()) {
            if (entry.getValue() != null) {
                this.highWaterMarks.put(entry.getKey(), entry.getValue());
            } else {
                this.highWaterMarks.remove(entry.getKey());
            }
        }
    }

    @Nonnull
    @Override
    public KyloCatalogWriter<T> write(@Nonnull final T df) {
        return new DefaultKyloCatalogWriter<>(this, hadoopConfiguration, resourceLoader, df);
    }

    @Nonnull
    @Override
    public KyloCatalogWriter<T> write(@Nonnull final T source, @Nonnull final String targetId) {
        final DataSetTemplate dataSet = (dataSets != null) ? dataSets.get(targetId) : null;
        if (dataSet != null) {
            final DefaultKyloCatalogWriter<T> writer = new DefaultKyloCatalogWriter<>(this, hadoopConfiguration, resourceLoader, source);
            writer.dataSet(dataSet);
            return writer;
        } else {
            throw new KyloCatalogException("Data set does not exist: " + targetId);
        }
    }

    /**
     * Indicates if the Spark session is stopped.
     */
    protected abstract boolean isSparkStopped();
}
