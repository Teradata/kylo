package com.thinkbiganalytics.spark.shell;

/*-
 * #%L
 * Kylo Commons Spark Shell for Spark 2
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

import com.thinkbiganalytics.spark.rest.model.Datasource;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.util.Collection;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * A data source provider for Spark 2.
 */
public class DatasourceProviderV2 extends AbstractDatasourceProvider<Dataset<Row>> {

    /**
     * Constructs a {@code DatasourceProviderV2}.
     *
     * @param datasources the data sources
     */
    DatasourceProviderV2(@Nonnull final Collection<Datasource> datasources) {
        super(datasources);
    }

    @Nonnull
    @Override
    protected Dataset<Row> readJdbcTable(@Nonnull final String url, @Nonnull final String table, @Nonnull final Properties properties, @Nonnull final SQLContext sqlContext) {
        return sqlContext.read().jdbc(url, table, properties);
    }
}
