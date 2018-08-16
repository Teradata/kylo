package com.thinkbiganalytics.kylo.catalog.api;

/*-
 * #%L
 * Kylo Catalog API
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import javax.annotation.Nonnull;

/**
 * Builder for pre-defining data sets.
 *
 * <p>Hadoop configuration properties can be set with {@code option()} by prefixing the key with "{@code spark.hadoop.}".</p>
 *
 * @param <T> Spark {@code DataFrame} class
 * @see KyloCatalogClientBuilder#addDataSetProvider(DataSetProvider)
 */
public interface KyloCatalogDataSetBuilder<T> extends KyloCatalogDataSetAccess<KyloCatalogDataSetBuilder<T>> {

    /**
     * Sets the paths to be loaded as input or written as output.
     */
    @Nonnull
    KyloCatalogDataSetBuilder<T> paths(@Nonnull String... paths);

    /**
     * Saves the properties of this data set.
     *
     * @return the parent Kylo Catalog client builder
     */
    @Nonnull
    KyloCatalogClientBuilder<T> save();
}
