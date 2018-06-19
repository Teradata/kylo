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
import com.thinkbiganalytics.kylo.catalog.spark.SparkUtil;
import com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc.JdbcRelationProvider;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;

import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * Base implementation of a data set provider that can read from and write to JDBC tables.
 *
 * @param <T> Spark {@code DataFrame} class
 */
public abstract class AbstractJdbcDataSetProvider<T> implements DataSetProvider<T> {

    @Override
    public boolean supportsFormat(@Nonnull final String source) {
        return "jdbc".equalsIgnoreCase(source)
               || "org.apache.spark.sql.jdbc".equals(source)
               || "org.apache.spark.sql.jdbc.DefaultSource".equals(source)
               || "org.apache.spark.sql.execution.datasources.jdbc".equals(source)
               || "org.apache.spark.sql.execution.datasources.jdbc.DefaultSource".equals(source)
               || "org.apache.spark.sql.execution.datasources.jdbc.JdbcRelationProvider".equals(source);
    }

    @Nonnull
    @Override
    public final T read(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options) {
        final DataFrameReader reader = SparkUtil.prepareDataFrameReader(getDataFrameReader(client, options), options, null);
        reader.format(JdbcRelationProvider.class.getName());
        return load(reader);
    }

    @Override
    public final void write(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options, @Nonnull final T dataSet) {
        // Extract JDBC options
        final String url = DataSetUtil.getOptionOrThrow(options, "url", "Option 'url' is required");
        final String table = DataSetUtil.getOptionOrThrow(options, "dbtable", "Option 'dbtable' is required");

        final Properties properties = new Properties();
        properties.putAll(options.getOptions());

        // Write to JDBC table
        final DataFrameWriter writer = SparkUtil.prepareDataFrameWriter(getDataFrameWriter(dataSet, options), options, null);
        writer.jdbc(url, table, properties);
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
     * Loads a data set using the specified reader.
     */
    @Nonnull
    protected abstract T load(@Nonnull DataFrameReader reader);
}
