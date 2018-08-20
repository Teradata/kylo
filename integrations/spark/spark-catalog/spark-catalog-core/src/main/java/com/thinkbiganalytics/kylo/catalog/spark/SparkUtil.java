package com.thinkbiganalytics.kylo.catalog.spark;

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

import com.google.common.base.Preconditions;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;
import com.thinkbiganalytics.kylo.hadoop.FileSystemUtil;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.sources.DataSourceRegister;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Option;
import scala.collection.JavaConversions;

/**
 * Static support methods for interacting with the Spark SQL API.
 */
@SuppressWarnings("WeakerAccess")
public class SparkUtil {

    private static final Logger log = LoggerFactory.getLogger(SparkUtil.class);

    /**
     * Returns the option's value if the option is non-empty, otherwise returns the default value.
     */
    @Contract("_, null -> _; _, !null -> !null")
    @Nullable
    public static <T> T getOrElse(@Nullable final Option<T> option, @Nullable final T defaultValue) {
        return (option == null || option.isEmpty()) ? defaultValue : option.get();
    }

    /**
     * Parses and modifies the specified string to produce a {@link URL} that can be used with {@link URLClassLoader}.
     *
     * @param s string to parse as a URL
     * @return parsed URL
     */
    @Nonnull
    public static URL parseUrl(@Nonnull final String s) {
        final URI uri = URI.create(s);

        if (uri.getScheme() == null || "local".equals(uri.getScheme())) {
            try {
                return new URL("file:" + uri.getSchemeSpecificPart());
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException();
            }
        } else {
            return FileSystemUtil.parseUrl(s, null);
        }
    }

    /**
     * Apply data set options to a data frame reader.
     */
    @Nonnull
    public static DataFrameReader prepareDataFrameReader(@Nonnull final DataFrameReader reader, @Nonnull final DataSetOptions options, @Nullable final KyloCatalogClient client) {
        reader.options(options.getOptions());
        if (options.getFormat() != null) {
            reader.format(resolveDataSource(options, client));
        }
        if (options.getSchema() != null) {
            reader.schema(options.getSchema());
        }
        return reader;
    }

    /**
     * Apply data set options to a data frame writer.
     */
    @Nonnull
    public static DataFrameWriter prepareDataFrameWriter(@Nonnull final DataFrameWriter writer, @Nonnull final DataSetOptions options, @Nullable final KyloCatalogClient client) {
        writer.options(options.getOptions());
        if (options.getFormat() != null) {
            writer.format(resolveDataSource(options, client));
        }
        if (options.getMode() != null) {
            writer.mode(options.getMode());
        }
        if (options.getPartitioningColumns() != null) {
            writer.partitionBy(JavaConversions.asScalaBuffer(options.getPartitioningColumns()));
        }
        return writer;
    }

    /**
     * Determines the format (or data source) to use for the specified data set.
     */
    public static String resolveDataSource(@Nonnull final DataSetOptions options, @Nullable final KyloCatalogClient client) {
        Preconditions.checkNotNull(options.getFormat(), "Format must be defined");

        // Determine format
        String format = getOrElse(options.getOption(KyloCatalogConstants.FORMAT_OPTION2), null);
        if (format == null) {
            format = getOrElse(options.getOption(KyloCatalogConstants.FORMAT_OPTION), null);
        }
        if (format == null) {
            format = options.getFormat();
        }

        // Resolve format using resource loader
        if (client instanceof AbstractKyloCatalogClient) {
            Option<DataSourceRegister> dataSource = ((AbstractKyloCatalogClient<?>) client).getDataSource(format);
            if (dataSource.isEmpty()) {
                dataSource = ((AbstractKyloCatalogClient<?>) client).getDataSource(format + ".DefaultSource");
            }
            if (dataSource.isDefined()) {
                format = dataSource.get().getClass().getName();
            }
        }

        return format;
    }

    /**
     * Returns the save mode with the specified name.
     *
     * @throws IllegalArgumentException if no save mode has the specified name
     */
    @Nonnull
    public static SaveMode toSaveMode(final String s) {
        Preconditions.checkNotNull(s, "Save mode cannot be null");
        switch (s.toLowerCase()) {
            case "overwrite":
                return SaveMode.Overwrite;

            case "append":
                return SaveMode.Append;

            case "ignore":
                return SaveMode.Ignore;

            case "error":
            case "errorifexists":
            case "default":
                return SaveMode.ErrorIfExists;

            default:
                log.debug("Unknown save mode: {}", s);
                throw new IllegalArgumentException("Unknown save mode: " + s + ". Accepted save modes are 'overwrite', 'append', 'ignore', 'error', 'errorifexists'.");
        }
    }

    /**
     * Instances of {@code SparkUtil} should not be constructed.
     */
    private SparkUtil() {
        throw new UnsupportedOperationException();
    }
}
