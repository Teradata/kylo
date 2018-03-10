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
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.SaveMode;

import javax.annotation.Nonnull;

import scala.collection.JavaConversions;

/**
 * Static support methods for interacting with the Spark SQL API.
 */
public class SparkSqlUtil {

    /**
     * Apply data set options to a data frame reader.
     */
    @Nonnull
    public static DataFrameReader prepareDataFrameReader(@Nonnull final DataFrameReader reader, @Nonnull final DataSetOptions options) {
        reader.options(options.getOptions());
        if (options.getFormat() != null) {
            reader.format(options.getFormat());
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
    public static DataFrameWriter prepareDataFrameWriter(@Nonnull final DataFrameWriter writer, @Nonnull final DataSetOptions options) {
        writer.options(options.getOptions());
        if (options.getFormat() != null) {
            writer.format(options.getFormat());
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
                throw new IllegalArgumentException("Unknown save mode: " + s + ". Accepted save modes are 'overwrite', 'append', 'ignore', 'error', 'errorifexists'.");
        }
    }

    /**
     * Instances of {@code SparkSqlUtil} should not be constructed.
     */
    private SparkSqlUtil() {
        throw new UnsupportedOperationException();
    }
}
