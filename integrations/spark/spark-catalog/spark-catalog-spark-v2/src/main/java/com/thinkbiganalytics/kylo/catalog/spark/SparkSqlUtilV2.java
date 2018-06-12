package com.thinkbiganalytics.kylo.catalog.spark;

/*-
 * #%L
 * Kylo Catalog for Spark 2
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

import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.spark.sql.DataFrameWriter;

import java.util.List;

import javax.annotation.Nonnull;

import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Static support methods for interacting with the Spark SQL v2 API.
 */
public class SparkSqlUtilV2 {

    /**
     * Apply data set options to a Spark 2 data frame writer.
     */
    @Nonnull
    public static DataFrameWriter prepareDataFrameWriter(@Nonnull final DataFrameWriter writer, @Nonnull final DataSetOptions options) {
        if (options.getBucketColumnNames() != null) {
            final List<String> bucketColumnNames = options.getBucketColumnNames();
            final Seq<String> colNames = JavaConversions.asScalaBuffer(bucketColumnNames.subList(1, bucketColumnNames.size()));
            writer.bucketBy(options.getNumBuckets(), bucketColumnNames.get(0), colNames);
        }
        if (options.getSortColumnNames() != null) {
            final List<String> sortColumnNames = options.getSortColumnNames();
            final Seq<String> colNames = JavaConversions.asScalaBuffer(sortColumnNames.subList(1, sortColumnNames.size()));
            writer.sortBy(sortColumnNames.get(0), colNames);
        }
        return writer;
    }

    /**
     * Instances of {@code SparkSqlUtilV2} should not be constructed.
     */
    private SparkSqlUtilV2() {
        throw new UnsupportedOperationException();
    }
}
