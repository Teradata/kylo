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
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogException;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogReader;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.sql.types.StructType;

import java.util.Arrays;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Option;

/**
 * Loads a Spark {@code DataFrame} from an external system.
 *
 * @param <T> Spark {@code DataFrame} class
 * @see KyloCatalogClient#read()
 */
class DefaultKyloCatalogReader<T> extends AbstractDataSetOptionsAccess<KyloCatalogReader<T>> implements KyloCatalogReader<T> {

    /**
     * Kylo catalog client
     */
    @Nonnull
    private final KyloCatalogClient<T> client;

    /**
     * Constructs a {@code DefaultKyloCatalogReader} for the specified Kylo catalog client.
     */
    DefaultKyloCatalogReader(@Nonnull final KyloCatalogClient<T> client, @Nonnull final Configuration hadoopConfiguration, @Nonnull final DataSourceResourceLoader resourceLoader) {
        super(new DataSetOptions(), hadoopConfiguration, resourceLoader);
        this.client = client;
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
}
