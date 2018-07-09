package com.thinkbiganalytics.kylo.catalog.spark.sources;

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

import com.google.common.annotations.VisibleForTesting;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogException;
import com.thinkbiganalytics.kylo.catalog.spark.SparkUtil;
import com.thinkbiganalytics.kylo.catalog.spark.sources.spark.HighWaterMarkInputFormat;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.sources.DataSourceRegister;
import org.apache.spark.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Base implementation of a data set provider that can read from and write to any Spark data source.
 *
 * <p>A high water mark is supported for file formats to ensure that files are only ingested once. When the {@code highwatermark} option is given, the paths are listed and filtered by their
 * modification time for changes occurring after the previous use of the data set. The high water mark is then updated and written to {@link KyloCatalogClient}.</p>
 *
 * @param <T> Spark {@code DataFrame} class
 */
abstract class AbstractSparkDataSetProvider<T> implements DataSetProvider<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractSparkDataSetProvider.class);

    /**
     * Option key to specify the high water mark name for reading files
     */
    private static final String HIGH_WATER_MARK_OPTION = "highwatermark";

    /**
     * Option key to specify the minimum age for reading files
     */
    private static final String MIN_AGE_OPTION = "min-file-age";

    /**
     * Option key to specify the maximum age for reading files
     */
    private static final String MAX_AGE_OPTION = "max-file-age";

    @Override
    public final boolean supportsFormat(@Nonnull final String source) {
        return true;  // supports any format supported by Spark
    }

    @Nonnull
    @Override
    public final T read(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options) {
        final DataSetOptions resolvedOptions = resolvePaths(options, client);
        final DataFrameReader reader = SparkUtil.prepareDataFrameReader(getDataFrameReader(client, resolvedOptions), resolvedOptions, client);
        final Seq<String> paths = (resolvedOptions.getPaths() != null) ? JavaConversions.asScalaBuffer(resolvedOptions.getPaths()) : null;
        return load(reader, paths);
    }

    @Override
    public final void write(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options, @Nonnull final T dataSet) {
        final DataFrameWriter writer = SparkUtil.prepareDataFrameWriter(getDataFrameWriter(dataSet, options), options, client);
        writer.save();
    }

    /**
     * Gets a reader from the specified client.
     *
     * <p>The options, format, and scheme will be applied to the reader before loading.</p>
     */
    @Nonnull
    protected abstract DataFrameReader getDataFrameReader(@Nonnull KyloCatalogClient<T> client, @Nonnull DataSetOptions options);

    /**
     * Gets a writer for the specified data set.
     *
     * <p>The options, format, mode, and partitioning will be applied to the writer before saving.</p>
     */
    @Nonnull
    protected abstract DataFrameWriter getDataFrameWriter(@Nonnull T dataSet, @Nonnull DataSetOptions options);

    /**
     * Gets the Hadoop configuration from the specified client.
     */
    @Nonnull
    protected abstract Configuration getHadoopConfiguration(@Nonnull KyloCatalogClient<T> client);

    /**
     * Indicate if the specified class is a {@code FileFormat}.
     */
    protected abstract boolean isFileFormat(@Nonnull final Class<?> formatClass);

    /**
     * Loads a data set using the specified reader and paths.
     */
    @Nonnull
    protected abstract T load(@Nonnull DataFrameReader reader, @Nullable Seq<String> paths);

    /**
     * Resolves the specified URIs by removing files that have been previously read.
     *
     * @throws KyloCatalogException if a data set option is invalid
     * @throws IOException          if an I/O error occurs
     */
    @Nonnull
    @SuppressWarnings("squid:S1192")
    @VisibleForTesting
    List<String> resolveHighWaterMarkPaths(@Nonnull final List<String> uris, @Nonnull final DataSetOptions options, @Nonnull final KyloCatalogClient<T> client) throws IOException {
        // Get configuration
        final Configuration conf = getHadoopConfiguration(client);
        final String highWaterMarkName = SparkUtil.getOrElse(options.getOption(HighWaterMarkInputFormat.HIGH_WATER_MARK), SparkUtil.getOrElse(options.getOption(HIGH_WATER_MARK_OPTION), null));
        final Job job = Job.getInstance(conf);

        final String highWaterMarkValue = client.getHighWaterMarks().get(highWaterMarkName);
        if (highWaterMarkValue != null) {
            try {
                HighWaterMarkInputFormat.setHighWaterMark(job, Long.parseLong(highWaterMarkValue));
            } catch (final NumberFormatException e) {
                throw new KyloCatalogException("Invalid " + HIGH_WATER_MARK_OPTION + " value: " + highWaterMarkValue, e);
            }
        }

        final String maxFileAge = SparkUtil.getOrElse(options.getOption(HighWaterMarkInputFormat.MAX_FILE_AGE), SparkUtil.getOrElse(options.getOption(MAX_AGE_OPTION), null));
        if (maxFileAge != null) {
            try {
                HighWaterMarkInputFormat.setMaxFileAge(job, Long.parseLong(maxFileAge));
            } catch (final NumberFormatException e) {
                throw new KyloCatalogException("Invalid " + MAX_AGE_OPTION + " value: " + maxFileAge, e);
            }
        }

        final String minFileAge = SparkUtil.getOrElse(options.getOption(HighWaterMarkInputFormat.MIN_FILE_AGE), SparkUtil.getOrElse(options.getOption(MIN_AGE_OPTION), null));
        if (minFileAge != null) {
            try {
                HighWaterMarkInputFormat.setMinFileAge(job, Long.parseLong(minFileAge));
            } catch (final NumberFormatException e) {
                throw new KyloCatalogException("Invalid " + MIN_AGE_OPTION + " value: " + minFileAge, e);
            }
        }

        // Convert URIs to Paths
        final Path[] paths = new Path[uris.size()];

        for (int i = 0; i < uris.size(); ++i) {
            final Path path = new Path(uris.get(i));
            final FileSystem fs = path.getFileSystem(conf);
            paths[i] = path.makeQualified(fs.getUri(), fs.getWorkingDirectory());
        }

        HighWaterMarkInputFormat.setInputPaths(job, paths);

        // Get high water mark paths
        final HighWaterMarkInputFormat inputFormat = new HighWaterMarkInputFormat();
        final List<FileStatus> files = inputFormat.listStatus(job);
        client.setHighWaterMarks(Collections.singletonMap(highWaterMarkName, Long.toString(inputFormat.getLastHighWaterMark())));

        // Return resolved paths
        final List<String> resolvedPaths = new ArrayList<>(files.size());

        if (files.isEmpty()) {
            resolvedPaths.add("file:/dev/null");
        } else {
            for (final FileStatus file : files) {
                resolvedPaths.add(file.getPath().toString());
            }
        }

        return resolvedPaths;
    }

    @Nonnull
    @VisibleForTesting
    DataSetOptions resolvePaths(@Nonnull final DataSetOptions options, @Nonnull final KyloCatalogClient<T> client) {
        // Determine paths from options
        final List<String> inputPaths = new ArrayList<>();
        if (options.getOption("path").isDefined()) {
            inputPaths.add(options.getOption("path").get());
        }
        if (options.getPaths() != null) {
            inputPaths.addAll(options.getPaths());
        }

        // Resolve paths
        if (!inputPaths.isEmpty() && isFileFormat(options, client)) {
            // Remove 'path' option; will use setPaths() instead
            options.getOptions().remove("path");

            // Determine resolve method
            final List<String> resolvedPaths;

            if (options.getOption(HIGH_WATER_MARK_OPTION).isDefined() || options.getOption(HighWaterMarkInputFormat.HIGH_WATER_MARK).isDefined()) {
                try {
                    resolvedPaths = resolveHighWaterMarkPaths(inputPaths, options, client);
                } catch (final IOException e) {
                    throw new KyloCatalogException("Unable to resolve paths: " + inputPaths, e);
                }
            } else {
                resolvedPaths = inputPaths;
            }

            options.setPaths(resolvedPaths);
        }

        return options;
    }

    /**
     * Indicates if the specified options refers to a file data source.
     */
    private boolean isFileFormat(@Nonnull final DataSetOptions options, @Nonnull final KyloCatalogClient<T> client) {
        final ClassLoader classLoader = Utils.getContextOrSparkClassLoader();

        // Resolve using Kylo Catalog class loader
        final String catalogClassName = SparkUtil.resolveDataSource(options, client);
        try {
            return isFileFormat(classLoader.loadClass(catalogClassName));
        } catch (final Exception e) {
            log.debug("Unable to resolve data source using Kylo Catalog: {}: {}", catalogClassName, e);
        }

        // Resolve using service loader
        final String format = options.getFormat();
        final ServiceLoader<DataSourceRegister> serviceLoader = ServiceLoader.load(DataSourceRegister.class, classLoader);

        for (final DataSourceRegister dataSource : serviceLoader) {
            if (dataSource.shortName().equalsIgnoreCase(format)) {
                return isFileFormat(dataSource.getClass());
            }
        }

        // Resolve using class name
        try {
            return isFileFormat(classLoader.loadClass(format));
        } catch (final Exception e) {
            log.debug("Unable to resolve data source using class name: {}: {}", format, e);
        }

        try {
            return isFileFormat(classLoader.loadClass(format + ".DefaultSource"));
        } catch (final Exception e) {
            log.debug("Unable to resolve data source using class name: {}.DefaultSource: {}", format, e);
        }

        // Unable to resolve; assume it's not a file format
        log.warn("Failed to resolve Spark data source: {}", format);
        return false;
    }
}
