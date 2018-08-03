package com.thinkbiganalytics.kylo.catalog.spark.sources.spark;

/*-
 * #%L
 * Kylo Catalog Core
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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogException;
import com.thinkbiganalytics.kylo.catalog.spark.SparkUtil;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.spark.sql.sources.DataSourceRegister;
import org.apache.spark.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Option;

import static com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants.PATH_OPTION;

/**
 * Context for reading and writing Spark data sets.
 *
 * @param <T> Spark {@code DataFrame} class
 */
public class SparkDataSetContext<T> extends DataSetOptions {

    private static final Logger log = LoggerFactory.getLogger(SparkDataSetContext.class);

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

    /**
     * Kylo Catalog client
     */
    @Nonnull
    private final KyloCatalogClient<T> client;

    /**
     * Spark data set delegate
     */
    private SparkDataSetDelegate<T> delegate;

    /**
     * Cached indicator if the source is a file
     */
    private Boolean fileFormat;

    /**
     * Cached options map
     */
    private Map<String, String> options;

    /**
     * Cache list of paths
     */
    private Option<List<String>> paths;

    /**
     * Constructs a {@code SparkDataSetContext}.
     *
     * @param parent   data set options
     * @param client   Kylo Catalog client
     * @param delegate Spark data set delegate
     */
    public SparkDataSetContext(@Nonnull final DataSetOptions parent, @Nonnull final KyloCatalogClient<T> client, @Nonnull final SparkDataSetDelegate<T> delegate) {
        super(parent);
        this.client = client;
        this.delegate = delegate;
    }

    /**
     * Indicates if the specified options refers to a file data source.
     */
    public boolean isFileFormat() {
        if (fileFormat == null) {
            fileFormat = resolveFileFormat();
        }
        return fileFormat;
    }

    @Nonnull
    @Override
    public Option<String> getOption(@Nonnull final String key) {
        return Option.apply(getOptions().get(key));
    }

    @Nonnull
    @Override
    public Map<String, String> getOptions() {
        if (options == null) {
            // Allow options to be used before resolving file options
            options = super.getOptions();
            if (isFileFormat()) {
                options = resolveFileOptions();
            }
        }
        return options;
    }

    @Nullable
    @Override
    public List<String> getPaths() {
        if (paths == null) {
            paths = Option.apply(isFileFormat() ? resolveFilePaths() : super.getPaths());
        }
        return SparkUtil.getOrElse(paths, null);
    }

    /**
     * Determines if the source is a file.
     */
    private boolean resolveFileFormat() {
        final ClassLoader classLoader = Utils.getContextOrSparkClassLoader();

        // Resolve using Kylo Catalog class loader
        final String catalogClassName = SparkUtil.resolveDataSource(this, client);
        try {
            return delegate.isFileFormat(classLoader.loadClass(catalogClassName));
        } catch (final Exception e) {
            log.debug("Unable to resolve data source using Kylo Catalog: {}: {}", catalogClassName, e);
        }

        // Resolve using service loader
        final String format = getFormat();
        final ServiceLoader<DataSourceRegister> serviceLoader = ServiceLoader.load(DataSourceRegister.class, classLoader);

        for (final DataSourceRegister dataSource : serviceLoader) {
            if (dataSource.shortName().equalsIgnoreCase(format)) {
                return delegate.isFileFormat(dataSource.getClass());
            }
        }

        // Resolve using class name
        try {
            return delegate.isFileFormat(classLoader.loadClass(format));
        } catch (final Exception e) {
            log.debug("Unable to resolve data source using class name: {}: {}", format, e);
        }

        try {
            return delegate.isFileFormat(classLoader.loadClass(format + ".DefaultSource"));
        } catch (final Exception e) {
            log.debug("Unable to resolve data source using class name: {}.DefaultSource: {}", format, e);
        }

        // Unable to resolve; assume it's not a file format
        log.warn("Failed to resolve Spark data source: {}", format);
        return false;
    }

    /**
     * Determines the options map if the source is a file.
     */
    private Map<String, String> resolveFileOptions() {
        @SuppressWarnings({"squid:HiddenFieldCheck", "unchecked"}) final Map<String, String> options = new CaseInsensitiveMap(super.getOptions());
        options.remove(PATH_OPTION);
        return options;
    }

    /**
     * Determines the list of paths if the source is a file.
     */
    private List<String> resolveFilePaths() {
        // Determine paths from options
        final List<String> inputPaths = new ArrayList<>();
        if (super.getOption("path").isDefined()) {
            inputPaths.add(super.getOption("path").get());
        }
        if (super.getPaths() != null) {
            inputPaths.addAll(super.getPaths());
        }

        // Resolve paths
        if (getOption(HIGH_WATER_MARK_OPTION).isDefined() || getOption(HighWaterMarkInputFormat.HIGH_WATER_MARK).isDefined()) {
            try {
                return resolveHighWaterMarkPaths(inputPaths);
            } catch (final IOException e) {
                throw new KyloCatalogException("Unable to resolve paths: " + inputPaths, e);
            }
        } else {
            return inputPaths.isEmpty() ? super.getPaths() : inputPaths;
        }
    }

    /**
     * Resolves the specified URIs by removing files that have been previously read.
     *
     * @throws KyloCatalogException if a data set option is invalid
     * @throws IOException          if an I/O error occurs
     */
    @Nonnull
    @SuppressWarnings({"squid:HiddenFieldCheck", "squid:S1192"})
    private List<String> resolveHighWaterMarkPaths(@Nonnull final List<String> uris) throws IOException {
        // Get configuration
        final Configuration conf = delegate.getHadoopConfiguration(client);
        final String highWaterMarkName = SparkUtil.getOrElse(getOption(HighWaterMarkInputFormat.HIGH_WATER_MARK), SparkUtil.getOrElse(getOption(HIGH_WATER_MARK_OPTION), null));
        final Job job = Job.getInstance(conf);

        final String highWaterMarkValue = client.getHighWaterMarks().get(highWaterMarkName);
        if (highWaterMarkValue != null) {
            try {
                HighWaterMarkInputFormat.setHighWaterMark(job, Long.parseLong(highWaterMarkValue));
            } catch (final NumberFormatException e) {
                throw new KyloCatalogException("Invalid " + HIGH_WATER_MARK_OPTION + " value: " + highWaterMarkValue, e);
            }
        }

        final String maxFileAge = SparkUtil.getOrElse(getOption(HighWaterMarkInputFormat.MAX_FILE_AGE), SparkUtil.getOrElse(getOption(MAX_AGE_OPTION), null));
        if (maxFileAge != null) {
            try {
                HighWaterMarkInputFormat.setMaxFileAge(job, Long.parseLong(maxFileAge));
            } catch (final NumberFormatException e) {
                throw new KyloCatalogException("Invalid " + MAX_AGE_OPTION + " value: " + maxFileAge, e);
            }
        }

        final String minFileAge = SparkUtil.getOrElse(getOption(HighWaterMarkInputFormat.MIN_FILE_AGE), SparkUtil.getOrElse(getOption(MIN_AGE_OPTION), null));
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
}
