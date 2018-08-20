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

import org.apache.spark.SparkContext;
import org.apache.spark.sql.DataFrameReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Common operations for accessing a data set.
 *
 * <p>Hadoop configuration properties can be set with {@code option()} by prefixing the key with "{@code spark.hadoop.}".</p>
 *
 * @param <R> builder type
 */
public interface KyloCatalogDataSetAccess<R extends KyloCatalogDataSetAccess> {

    /**
     * Adds a file to be downloaded with all Spark jobs.
     *
     * <p>NOTE: Local files cannot be used when Spark is running in yarn-cluster mode.</p>
     *
     * @param path can be either a local file, a file in HDFS (or other Hadoop-supported filesystem), or an HTTP/HTTPS/FTP URI
     * @see SparkContext#addFile(String)
     */
    @Nonnull
    R addFile(@Nullable String path);

    /**
     * Adds files to be downloaded with all Spark jobs.
     *
     * @see #addFile(String)
     */
    @Nonnull
    R addFiles(@Nullable java.util.List<String> paths);

    /**
     * (Scala-specific) Adds files to be downloaded with all Spark jobs.
     *
     * @see #addFile(String)
     */
    @Nonnull
    R addFiles(@Nullable scala.collection.Seq<String> paths);

    /**
     * Adds a JAR dependency containing the data source classes or its dependencies.
     *
     * <p>NOTE: Local jars cannot be used when Spark is running in yarn-cluster mode.</p>
     *
     * @param path can be either a local file, a file in HDFS (or other Hadoop-supported filesystem), an HTTP/HTTPS/FTP URI, or local:/path (for a file on every worker node)
     * @see SparkContext#addJar(String)
     */
    @Nonnull
    R addJar(@Nullable String path);

    /**
     * Adds JAR dependencies containing the data source classes and its dependencies.
     *
     * @see #addJar(String)
     */
    @Nonnull
    R addJars(@Nullable java.util.List<String> paths);

    /**
     * (Scala-specific) Adds JAR dependencies containing the data source classes and its dependencies.
     *
     * @see #addJar(String)
     */
    @Nonnull
    R addJars(@Nullable scala.collection.Seq<String> paths);

    /**
     * Specifies the input (or output) data source format.
     *
     * @see DataFrameReader#format(String)
     */
    @Nonnull
    R format(@Nonnull String source);

    /**
     * Adds an input option for the underlying data source.
     *
     * @see "org.apache.spark.sql.DataFrameReader#option(String, double)"
     */
    @Nonnull
    R option(@Nonnull String key, double value);

    /**
     * Adds an input option for the underlying data source.
     *
     * @see "org.apache.spark.sql.DataFrameReader#option(String, long)"
     */
    @Nonnull
    R option(@Nonnull String key, long value);

    /**
     * Adds an input option for the underlying data source.
     *
     * @see "org.apache.spark.sql.DataFrameReader#option(String, boolean)"
     */
    @Nonnull
    R option(@Nonnull String key, boolean value);

    /**
     * Adds an input option for the underlying data source.
     *
     * @see DataFrameReader#option(String, String)
     */
    @Nonnull
    R option(@Nonnull String key, @Nullable String value);

    /**
     * Adds input options for the underlying data source.
     *
     * @see DataFrameReader#options(java.util.Map)
     */
    @Nonnull
    R options(@Nullable java.util.Map<String, String> options);

    /**
     * (Scala-specific) Adds input options for the underlying data source.
     *
     * @see DataFrameReader#options(scala.collection.Map)
     */
    @Nonnull
    R options(@Nullable scala.collection.Map<String, String> options);
}
