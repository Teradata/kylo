package com.thinkbiganalytics.kylo.catalog;

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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClientBuilder;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogReader;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogWriter;
import com.thinkbiganalytics.kylo.catalog.spark.KyloCatalogClientBuilderV2;
import com.thinkbiganalytics.kylo.catalog.spark.sources.HiveDataSetProviderV2;
import com.thinkbiganalytics.kylo.catalog.spark.sources.JdbcDataSetProviderV2;
import com.thinkbiganalytics.kylo.catalog.spark.sources.SparkDataSetProviderV2;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Helper methods for accessing the Kylo catalog.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class KyloCatalog {

    /**
     * List of default data set providers.
     */
    private static List<DataSetProvider<Dataset<Row>>> defaultDataSetProviders;

    /**
     * Creates a builder for constructing a {@link KyloCatalogClient}.
     */
    @Nonnull
    public static KyloCatalogClientBuilder<Dataset<Row>> builder() {
        return builder(SparkSession.builder().getOrCreate());
    }

    /**
     * Creates a builder for constructing a {@link KyloCatalogClient}.
     *
     * @param sparkSession the Spark session that should be used
     */
    @Nonnull
    public static KyloCatalogClientBuilder<Dataset<Row>> builder(@Nonnull final SparkSession sparkSession) {
        return new KyloCatalogClientBuilderV2(sparkSession, getDefaultDataSetProviders());
    }

    /**
     * Creates a builder for constructing a {@link KyloCatalogClient}.
     *
     * @param sqlContext the Spark SQL context that should be used
     */
    @Nonnull
    public static KyloCatalogClientBuilder<Dataset<Row>> builder(@Nonnull final SQLContext sqlContext) {
        return builder(sqlContext.sparkSession());
    }

    /**
     * Gets the default data set providers.
     *
     * @return the data set providers in the order that they should be tried
     */
    @Nonnull
    public static List<DataSetProvider<Dataset<Row>>> getDefaultDataSetProviders() {
        if (defaultDataSetProviders == null) {
            loadDefaultDataSetProviders();
        }
        return defaultDataSetProviders;
    }

    /**
     * Creates a reader for accessing non-streaming data as a Spark {@code DataFrame}.
     */
    @Nonnull
    public static KyloCatalogReader<Dataset<Row>> read() {
        return client().read();
    }

    /**
     * Creates a reader for specified non-streaming data set as a Spark {@code DataFrame}.
     *
     * <p>Use the reader to override properties of the data set. Then call {@link KyloCatalogReader#load() load()} to retrieve the data set.</p>
     *
     * @param id identifier of the pre-defined data set
     * @return a reader pre-configured to access the data set
     */
    @Nonnull
    public static KyloCatalogReader<Dataset<Row>> read(@Nonnull final String id) {
        return client().read(id);
    }

    /**
     * Sets the default data set providers.
     *
     * @param dataSetProviders the data set providers in the order that they should be tried
     */
    public static void setDefaultDataSetProviders(@Nonnull final List<DataSetProvider<Dataset<Row>>> dataSetProviders) {
        defaultDataSetProviders = dataSetProviders;
    }

    /**
     * Creates a writer for saving the specified non-streaming Spark {@code DataFrame}.
     */
    @Nonnull
    public static KyloCatalogWriter<Dataset<Row>> write(@Nonnull final Dataset<Row> df) {
        return client().write(df);
    }

    /**
     * Creates a writer for saving the specified non-streaming Spark {@code DataFrame} to the specified data set.
     *
     * <p>Use the writer to override properties of the data set. Then call {@link KyloCatalogWriter#save() save()} to update the data set.</p>
     *
     * @param source   the source data set
     * @param targetId identifier of the pre-defined target data set
     * @return a write pre-configured to access the target data set
     */
    @Nonnull
    public static KyloCatalogWriter<Dataset<Row>> write(@Nonnull final Dataset<Row> source, @Nonnull final String targetId) {
        return client().write(source, targetId);
    }

    /**
     * Gets the current Kylo Catalog client.
     */
    @Nonnull
    private static KyloCatalogClient<Dataset<Row>> client() {
        return builder().build();
    }

    /**
     * Loads the default data set providers.
     */
    private static synchronized void loadDefaultDataSetProviders() {
        if (defaultDataSetProviders == null) {
            defaultDataSetProviders = Arrays.asList(new HiveDataSetProviderV2(), new JdbcDataSetProviderV2(), new SparkDataSetProviderV2());
        }
    }

    /**
     * Instances of {@code KyloCatalog} should not be constructed.
     */
    private KyloCatalog() {
        throw new UnsupportedOperationException();
    }
}
