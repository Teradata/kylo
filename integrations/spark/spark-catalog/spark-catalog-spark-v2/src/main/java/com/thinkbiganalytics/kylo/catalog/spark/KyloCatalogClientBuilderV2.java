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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Builder for {@link KyloCatalogClientV2}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class KyloCatalogClientBuilderV2 extends AbstractKyloCatalogClientBuilder<Dataset<Row>> {

    /**
     * Spark session
     */
    @Nonnull
    private final SparkSession sparkSession;

    /**
     * Constructs a {@code KyloCatalogClientBuilderV2} using the specified Spark session.
     */
    public KyloCatalogClientBuilderV2(@Nonnull final SparkSession sparkSession, @Nonnull final List<DataSetProvider<Dataset<Row>>> dataSetProviders) {
        super(dataSetProviders);
        this.sparkSession = sparkSession;
    }

    @Nonnull
    @Override
    protected KyloCatalogClient<Dataset<Row>> create(@Nonnull final List<DataSetProvider<Dataset<Row>>> dataSetProviders) {
        return new KyloCatalogClientV2(sparkSession, dataSetProviders);
    }
}
