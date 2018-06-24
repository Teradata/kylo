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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.spark.SparkUtil;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.spark.sql.DataFrameWriter;

import java.net.URL;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Base implementation of a data set provider that can read from and write to Hive tables.
 *
 * @param <T> Spark {@code DataFrame} class
 */
abstract class AbstractHiveDataSetProvider<T> implements DataSetProvider<T> {

    @Override
    public final boolean supportsFormat(@Nonnull final String format) {
        return KyloCatalogConstants.HIVE_FORMAT.equalsIgnoreCase(format);
    }

    @Nonnull
    @Override
    public final T read(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options) {
        final String tableName = DataSetUtil.getOptionOrThrow(options, KyloCatalogConstants.PATH_OPTION, "Table name must be defined as path");
        addJars(client, options.getJars());
        return loadFromTable(client, tableName);
    }

    @Override
    public final void write(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options, @Nonnull final T dataSet) {
        final String tableName = DataSetUtil.getOptionOrThrow(options, KyloCatalogConstants.PATH_OPTION, "Table name must be defined as path");
        addJars(client, options.getJars());

        // CDH-33639 Tables saved with the Spark SQL DataFrame.saveAsTable method are not compatible with Hive
        // (https://www.cloudera.com/documentation/enterprise/release-notes/topics/cdh_rn_spark_ki.html#ki_sparksql_dataframe_saveastable)
        //
        // Using DataFrameWriter.insertInto is a workaround but requires the DataFrame's fields to be in the same order as the target table's columns. The only way to achieve the same functionality as
        // saveAsTable is to save the DataFrame as a temp table (DataFrame.registerTempTable), execute a Hive query to create the target table, re-order the columns to match the target table, then
        // execute a Hive query to insert into the table.
        final DataFrameWriter writer = SparkUtil.prepareDataFrameWriter(getDataFrameWriter(dataSet, options), options, null);
        writer.insertInto(tableName);
    }

    /**
     * Gets a writer for the specified data set.
     *
     * <p>The options, format, mode, and partitioning will be applied to the writer before saving.</p>
     */
    @Nonnull
    protected abstract DataFrameWriter getDataFrameWriter(@Nonnull T dataSet, @Nonnull DataSetOptions options);

    /**
     * Reads in the specified Hive table.
     */
    @Nonnull
    protected abstract T loadFromTable(@Nonnull KyloCatalogClient<T> client, @Nonnull String tableName);

    /**
     * Executes the specified SQL query.
     */
    protected abstract void sql(@Nonnull KyloCatalogClient<T> client, @Nonnull String query);

    /**
     * Adds the specified jars to the Hive isolated client loader.
     */
    private void addJars(@Nonnull final KyloCatalogClient<T> client, @Nonnull final List<String> jars) {
        for (final String jar : jars) {
            final URL url = SparkUtil.parseUrl(jar);
            final String path = "hadoop".equals(url.getProtocol()) ? url.getPath() : url.toString();
            sql(client, "ADD JAR " + path);
        }
    }
}
