package com.thinkbiganalytics.kylo.catalog.spi;

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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogException;

import javax.annotation.Nonnull;

/**
 * Interface for data sources than can read or write Spark {@code DataFrame}s.
 *
 * @param <T> Spark {@code DataFrame} class
 */
public interface DataSetProvider<T> {

    /**
     * Reads in a data set using the specified options.
     *
     * @throws KyloCatalogException if the data set cannot be read
     */
    @Nonnull
    T read(@Nonnull KyloCatalogClient<T> client, @Nonnull DataSetOptions options);

    /**
     * Indicates if this provider supports the specified data source.
     */
    boolean supportsFormat(@Nonnull String source);

    /**
     * Writes out a data set using the specified options.
     *
     * @throws KyloCatalogException if the data cannot be written
     */
    void write(@Nonnull KyloCatalogClient<T> client, @Nonnull DataSetOptions options, @Nonnull T dataSet);
}
