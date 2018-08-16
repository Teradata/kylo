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
import java.util.Map;

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
     * Gets the values for all high water marks.
     */
    @Nonnull
    Map<String, String> getHighWaterMarks();

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
     * Creates a reader for specified non-streaming data set as a Spark {@code DataFrame}.
     *
     * <p>Use the reader to override properties of the data set. Then call {@link KyloCatalogReader#load() load()} to retrieve the data set.</p>
     *
     * @param id identifier of the pre-defined data set
     * @return a reader pre-configured to access the data set
     */
    @Nonnull
    KyloCatalogReader<T> read(@Nonnull String id);

    /**
     * Sets the values of the specified high water marks.
     *
     * <p>A {@code null} value will delete the high water mark.</p>
     *
     * @param highWaterMarks map of high water mark names to values that should be set
     */
    void setHighWaterMarks(@Nonnull Map<String, String> highWaterMarks);

    /**
     * Creates a writer for saving the specified non-streaming Spark {@code DataFrame}.
     */
    @Nonnull
    KyloCatalogWriter<T> write(@Nonnull T dataSet);

    /**
     * Creates a writer for saving the specified non-streaming Spark {@code DataFrame} to the specified data set.
     *
     * <p>Use the writer to override properties of the data set. Then call {@link KyloCatalogWriter#save() save()} to update the data set.</p>
     *
     * @param source   the source data set
     * @param targetId identifier of the pre-defined target data set
     * @return a write pre-configured to access the target data set
     */
    @Nonnull
    KyloCatalogWriter<T> write(@Nonnull T source, @Nonnull String targetId);
}
