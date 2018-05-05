package com.thinkbiganalytics.kylo.catalog.spark;

/*-
 * #%L
 * Kylo Catalog for Spark 1
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

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Builder for {@link KyloCatalogClientV1}.
 */
public class KyloCatalogClientBuilderV1 extends AbstractKyloCatalogClientBuilder<DataFrame> {

    /**
     * Spark SQL context
     */
    @Nonnull
    private final SQLContext sqlContext;

    /**
     * Constructs a {@code KyloCatalogClientBuilderV1} using the specified Spark SQL context.
     */
    public KyloCatalogClientBuilderV1(@Nonnull final SQLContext sqlContext, @Nonnull final List<DataSetProvider<DataFrame>> dataSetProviders) {
        super(dataSetProviders);
        this.sqlContext = sqlContext;
    }

    @Nonnull
    @Override
    protected KyloCatalogClient<DataFrame> create(@Nonnull final List<DataSetProvider<DataFrame>> dataSetProviders) {
        return new KyloCatalogClientV1(sqlContext, dataSetProviders);
    }
}
