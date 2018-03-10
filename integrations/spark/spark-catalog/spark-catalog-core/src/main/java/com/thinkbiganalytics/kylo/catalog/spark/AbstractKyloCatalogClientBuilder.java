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
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClientBuilder;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Base implementation of a {@link KyloCatalogClientBuilder} backed by a Spark engine.
 *
 * @param <T> Spark {@code DataFrame} class
 * @see AbstractKyloCatalogClient
 */
abstract class AbstractKyloCatalogClientBuilder<T> implements KyloCatalogClientBuilder<T> {

    /**
     * Active Kylo catalog client
     */
    private static final ThreadLocal<KyloCatalogClient> activeClient = new InheritableThreadLocal<>();

    /**
     * List of data set providers
     */
    @Nonnull
    private final List<DataSetProvider<T>> dataSetProviders;

    /**
     * Constructs an {@code AbstractKyloCatalogClientBuilder} with the specified default data set providers.
     */
    protected AbstractKyloCatalogClientBuilder(@Nonnull final List<DataSetProvider<T>> defaultDataSetProviders) {
        dataSetProviders = defaultDataSetProviders;
    }

    @Nonnull
    @Override
    public KyloCatalogClientBuilder<T> addDataSetProvider(@Nonnull final DataSetProvider<T> provider) {
        dataSetProviders.add(provider);
        return this;
    }

    @Nonnull
    @Override
    public KyloCatalogClientBuilder<T> setDataSetProviders(@Nonnull final List<DataSetProvider<T>> dataSetProviders) {
        this.dataSetProviders.clear();
        this.dataSetProviders.addAll(dataSetProviders);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public final KyloCatalogClient<T> build() {
        KyloCatalogClient<T> client = activeClient.get();
        if (client == null || client.isClosed()) {
            synchronized (activeClient) {
                if (activeClient.get() == null || activeClient.get().isClosed()) {
                    activeClient.set(create(dataSetProviders));
                }
                client = activeClient.get();
            }
        }
        return client;
    }

    /**
     * Creates a new Kylo catalog client.
     */
    @Nonnull
    protected abstract KyloCatalogClient<T> create(@Nonnull final List<DataSetProvider<T>> dataSetProviders);
}
