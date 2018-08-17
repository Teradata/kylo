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
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogDataSetBuilder;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogException;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base implementation of a {@link KyloCatalogClientBuilder} backed by a Spark engine.
 *
 * @param <T> Spark {@code DataFrame} class
 * @see AbstractKyloCatalogClient
 */
@SuppressWarnings("WeakerAccess")
abstract class AbstractKyloCatalogClientBuilder<T> implements KyloCatalogClientBuilder<T> {

    /**
     * Active Kylo catalog client
     */
    private static final ThreadLocal<KyloCatalogClient> activeClient = new InheritableThreadLocal<>();

    /**
     * Map of data set identifier to data set
     */
    @Nonnull
    private final Map<String, DataSetTemplate> dataSets = new HashMap<>();

    /**
     * List of data set providers
     */
    @Nonnull
    private final List<DataSetProvider<T>> dataSetProviders;

    /**
     * High water marks
     */
    @Nonnull
    private final Map<String, String> highWaterMarks = new HashMap<>();

    /**
     * Constructs an {@code AbstractKyloCatalogClientBuilder} with the specified default data set providers.
     */
    protected AbstractKyloCatalogClientBuilder(@Nonnull final List<DataSetProvider<T>> defaultDataSetProviders) {
        dataSetProviders = new ArrayList<>(defaultDataSetProviders);
    }

    @Nonnull
    @Override
    public KyloCatalogDataSetBuilder<T> addDataSet(@Nonnull final String id) {
        return new DefaultKyloCatalogDataSetBuilder<>(id, this);
    }

    /**
     * Adds a pre-defined data set.
     *
     * @param id      data set identifier
     * @param dataSet data set
     */
    @Nonnull
    public KyloCatalogClientBuilder<T> addDataSet(@Nonnull final String id, @Nonnull final DataSetTemplate dataSet) {
        dataSets.put(id, dataSet);
        return this;
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
    public KyloCatalogClientBuilder<T> setHighWaterMark(@Nonnull final String name, @Nullable final String value) {
        highWaterMarks.put(name, value);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public final KyloCatalogClient<T> build() {
        KyloCatalogClient<T> client = activeClient.get();
        if (client == null || client.isClosed()) {
            if (dataSetProviders.isEmpty()) {
                throw new KyloCatalogException("At least one DataSetProvider is required");
            }

            synchronized (activeClient) {
                if (activeClient.get() == null || activeClient.get().isClosed()) {
                    client = create(dataSetProviders);
                    client.setHighWaterMarks(highWaterMarks);
                    if (client instanceof AbstractKyloCatalogClient) {
                        ((AbstractKyloCatalogClient) client).setDataSets(dataSets);
                    }
                    activeClient.set(client);
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
