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

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.types.StructType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Loads a Spark {@code DataFrame} from an external system.
 *
 * <p>Hadoop configuration properties can be set with {@code option()} by prefixing the key with "{@code spark.hadoop.}".</p>
 *
 * @param <T> Spark {@code DataFrame} class
 * @see KyloCatalogClient#read()
 */
@SuppressWarnings("unused")
public interface KyloCatalogReader<T> extends KyloCatalogDataSetAccess<KyloCatalogReader<T>> {

    /**
     * Specifies the input schema.
     *
     * @see DataFrameReader#schema(StructType)
     */
    @Nonnull
    KyloCatalogReader<T> schema(@Nullable StructType schema);

    /**
     * Loads input in as a {@code DataFrame}, for data sources that support multiple paths. Only works if the source is a {@code HadoopFsRelationProvider}.
     *
     * @throws KyloCatalogException if the data source cannot be loaded
     * @see DataFrameReader#load(String...)
     */
    @Nonnull
    T load(@Nonnull String... paths);

    /**
     * Loads input in as a {@code DataFrame}, for data sources that require a path (e.g. data backed by a local or distributed file system).
     *
     * @throws KyloCatalogException if the data source cannot be loaded
     * @see DataFrameReader#load(String)
     */
    @Nonnull
    T load(@Nonnull String path);

    /**
     * Loads input in as a {@code DataFrame}, for data sources that don't require a path (e.g. external key-value stores).
     *
     * @throws KyloCatalogException if the data source cannot be loaded
     * @see DataFrameReader#load()
     */
    @Nonnull
    T load();
}
