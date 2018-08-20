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

import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.SaveMode;

import javax.annotation.Nonnull;

/**
 * Saves a Spark {@code DataFrame} to an external system.
 *
 * <p>Hadoop configuration properties can be set with {@code option()} by prefixing the key with "{@code spark.hadoop.}".</p>
 *
 * @param <T> Spark {@code DataFrame} class
 * @see KyloCatalogClient#write(Object)
 */
@SuppressWarnings("unused")
public interface KyloCatalogWriter<T> extends KyloCatalogDataSetAccess<KyloCatalogWriter<T>> {

    /**
     * Buckets the output by the given columns. If specified, the output is laid out on the file system similar to Hive's bucketing scheme.
     *
     * <p>Requires Spark 2.0+</p>
     *
     * @see "org.apache.spark.sql.DataFrameWriter#bucketBy(int, String, String...)"
     */
    @Nonnull
    KyloCatalogWriter<T> bucketBy(int numBuckets, @Nonnull String colName, String... colNames);

    /**
     * Specifies the behavior when data or table already exists.
     *
     * @see DataFrameWriter#mode(String)
     */
    @Nonnull
    KyloCatalogWriter<T> mode(@Nonnull String saveMode);

    /**
     * Specifies the behavior when data or table already exists.
     *
     * @see DataFrameWriter#mode(String)
     */
    @Nonnull
    KyloCatalogWriter<T> mode(@Nonnull SaveMode saveMode);

    /**
     * Partitions the output by the given columns on the file system. If specified, the output is laid out on the file system similar to Hive's partitioning scheme.
     *
     * @see DataFrameWriter#partitionBy(String...)
     */
    @Nonnull
    KyloCatalogWriter<T> partitionBy(@Nonnull String... colNames);

    /**
     * Saves the content of the {@code DataFrame} as the specified table.
     *
     * @throws KyloCatalogException if the data cannot be saved
     * @see DataFrameWriter#save()
     */
    void save();

    /**
     * Saves the content of the {@code DataFrame} at the specified path.
     *
     * @throws KyloCatalogException if the data cannot be saved
     * @see DataFrameWriter#save(String)
     */
    void save(@Nonnull String path);

    /**
     * Sorts the output in each bucket by the given columns.
     *
     * <p>Requires Spark 2.0+</p>
     *
     * @see "DataFrameWriter#org.apache.spark.sql.DataFrameWriter(String, String...)"
     */
    @Nonnull
    KyloCatalogWriter<T> sortBy(@Nonnull String colName, String... colNames);
}
