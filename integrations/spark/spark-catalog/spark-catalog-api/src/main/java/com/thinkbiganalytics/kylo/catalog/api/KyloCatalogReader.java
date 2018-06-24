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

import org.apache.spark.SparkContext;
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
public interface KyloCatalogReader<T> {

    /**
     * Adds a file to be downloaded with all Spark jobs.
     *
     * <p>NOTE: Local files cannot be used when Spark is running in yarn-cluster mode.</p>
     *
     * @param path can be either a local file, a file in HDFS (or other Hadoop-supported filesystem), or an HTTP/HTTPS/FTP URI
     * @see SparkContext#addFile(String)
     */
    @Nonnull
    KyloCatalogReader<T> addFile(@Nullable String path);

    /**
     * Adds files to be downloaded with all Spark jobs.
     *
     * @see #addFile(String)
     */
    @Nonnull
    KyloCatalogReader<T> addFiles(@Nullable java.util.List<String> paths);

    /**
     * (Scala-specific) Adds files to be downloaded with all Spark jobs.
     *
     * @see #addFile(String)
     */
    @Nonnull
    KyloCatalogReader<T> addFiles(@Nullable scala.collection.Seq<String> paths);

    /**
     * Adds a JAR dependency containing the data source classes or its dependencies.
     *
     * <p>NOTE: Local jars cannot be used when Spark is running in yarn-cluster mode.</p>
     *
     * @param path can be either a local file, a file in HDFS (or other Hadoop-supported filesystem), an HTTP/HTTPS/FTP URI, or local:/path (for a file on every worker node)
     * @see SparkContext#addJar(String)
     */
    @Nonnull
    KyloCatalogReader<T> addJar(@Nullable String path);

    /**
     * Adds JAR dependencies containing the data source classes and its dependencies.
     *
     * @see #addJar(String)
     */
    @Nonnull
    KyloCatalogReader<T> addJars(@Nullable java.util.List<String> paths);

    /**
     * (Scala-specific) Adds JAR dependencies containing the data source classes and its dependencies.
     *
     * @see #addJar(String)
     */
    @Nonnull
    KyloCatalogReader<T> addJars(@Nullable scala.collection.Seq<String> paths);

    /**
     * Specifies the input data source format.
     *
     * @see DataFrameReader#format(String)
     */
    @Nonnull
    KyloCatalogReader<T> format(@Nonnull String source);

    /**
     * Adds an input option for the underlying data source.
     *
     * @see "org.apache.spark.sql.DataFrameReader#option(String, double)"
     */
    @Nonnull
    KyloCatalogReader<T> option(@Nonnull String key, double value);

    /**
     * Adds an input option for the underlying data source.
     *
     * @see "org.apache.spark.sql.DataFrameReader#option(String, long)"
     */
    @Nonnull
    KyloCatalogReader<T> option(@Nonnull String key, long value);

    /**
     * Adds an input option for the underlying data source.
     *
     * @see "org.apache.spark.sql.DataFrameReader#option(String, boolean)"
     */
    @Nonnull
    KyloCatalogReader<T> option(@Nonnull String key, boolean value);

    /**
     * Adds an input option for the underlying data source.
     *
     * @see DataFrameReader#option(String, String)
     */
    @Nonnull
    KyloCatalogReader<T> option(@Nonnull String key, @Nullable String value);

    /**
     * Adds input options for the underlying data source.
     *
     * @see DataFrameReader#options(java.util.Map)
     */
    @Nonnull
    KyloCatalogReader<T> options(@Nullable java.util.Map<String, String> options);

    /**
     * (Scala-specific) Adds input options for the underlying data source.
     *
     * @see DataFrameReader#options(scala.collection.Map)
     */
    @Nonnull
    KyloCatalogReader<T> options(@Nullable scala.collection.Map<String, String> options);

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
