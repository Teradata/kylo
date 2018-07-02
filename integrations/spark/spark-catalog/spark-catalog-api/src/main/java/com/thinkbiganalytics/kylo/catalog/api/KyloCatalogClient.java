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

import java.io.Closeable;

import javax.annotation.Nonnull;

import scala.Option;

/**
 * Communicates with the Kylo Catalog service to read and write data sets.
 *
 * @param <T> Spark {@code DataFrame} class
 */
@SuppressWarnings("unused")
public interface KyloCatalogClient<T> extends AutoCloseable, Closeable {

    /**
     * Gets the provider that can access the specified data source.
     *
     * @param source the input or output data source
     * @return the provider, if found
     */
    @Nonnull
    Option<DataSetProvider<T>> getDataSetProvider(@Nonnull String source);

    /**
     * Indicates that this client has been closed and can no longer be used.
     */
    boolean isClosed();

    /**
     * Creates a reader for accessing non-streaming data as a Spark {@code DataFrame}.
     */
    @Nonnull
    KyloCatalogReader<T> read();

    /**
     * Creates a writer for saving the specified non-streaming Spark {@code DataFrame}.
     */
    @Nonnull
    KyloCatalogWriter<T> write(@Nonnull T dataSet);
}
