package com.thinkbiganalytics.kylo.catalog.api;

/*-
 * #%L
 * Kylo Catalog API
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

import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Builder for {@link KyloCatalogClient}.
 *
 * @param <T> Spark {@code DataFrame} class
 */
@SuppressWarnings("unused")
public interface KyloCatalogClientBuilder<T> {

    /**
     * Creates a builder for adding a data set with the specified identifier.
     *
     * @param id data set identifier
     * @return a data set builder
     */
    @Nonnull
    KyloCatalogDataSetBuilder<T> addDataSet(@Nonnull String id);

    /**
     * Adds the specified provider for reading and writing data sets.
     */
    @Nonnull
    KyloCatalogClientBuilder<T> addDataSetProvider(@Nonnull DataSetProvider<T> provider);

    /**
     * Sets the providers for reading and writing data sets to the specified providers.
     *
     * <p>NOTE: The default providers will be removed.</p>
     */
    @Nonnull
    KyloCatalogClientBuilder<T> setDataSetProviders(@Nonnull List<DataSetProvider<T>> providers);

    /**
     * Sets the specified high water mark value.
     *
     * <p>A {@code null} value will delete the high water mark.</p>
     *
     * @param name  high water mark name
     * @param value high water mark value
     */
    @Nonnull
    KyloCatalogClientBuilder<T> setHighWaterMark(@Nonnull String name, @Nullable String value);

    /**
     * Builds a {@link KyloCatalogClient}.
     *
     * @throws KyloCatalogException if the client cannot be created
     */
    @Nonnull
    KyloCatalogClient<T> build();
}
