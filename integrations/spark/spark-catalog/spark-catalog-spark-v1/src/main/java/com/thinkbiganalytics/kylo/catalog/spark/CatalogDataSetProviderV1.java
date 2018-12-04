package com.thinkbiganalytics.kylo.catalog.spark;

/*-
 * #%L
 * Kylo Commons Spark Shell for Spark 1
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import com.thinkbiganalytics.kylo.catalog.KyloCatalog;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.spark.shell.AbstractCatalogDataSetProvider;

import org.apache.spark.sql.DataFrame;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * A data source provider for Spark 1.
 */
public class CatalogDataSetProviderV1 extends AbstractCatalogDataSetProvider<DataFrame> {

    /**
     * Constructs a {@code DatasourceProviderV1}.
     *
     * @param dataSets the data sets
     */
    CatalogDataSetProviderV1(@Nonnull final Collection<DataSet> dataSets) {
        super(dataSets);
    }

    @Override
    protected KyloCatalogClient<DataFrame> getClient() {
        return KyloCatalog.builder().build();
    }
}
