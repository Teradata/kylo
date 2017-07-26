package com.thinkbiganalytics.spark.shell;

/*-
 * #%L
 * Kylo Commons Spark Shell
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

import org.apache.spark.sql.SQLContext;

import javax.annotation.Nonnull;

/**
 * Provides instances of {@link Datasource}.
 */
public interface DatasourceProvider<T> {

    /**
     * Gets the data source with the specified id.
     *
     * @param id the data source id
     * @return the data source, if found
     */
    @Nonnull
    Datasource findById(@Nonnull String id);

    /**
     * Gets the specified table from the specified data source.
     *
     * @param table      the table name
     * @param datasource the data source
     * @param sqlContext the Spark SQL context
     * @return the table dataset
     * @throws IllegalArgumentException if the data source does not provide tables
     */
    @Nonnull
    T getTableFromDatasource(@Nonnull String table, @Nonnull Datasource datasource, @Nonnull SQLContext sqlContext);

    /**
     * Gets the specified table from the specified data source.
     *
     * @param table        the table name
     * @param datasourceId the data source id
     * @param sqlContext   the Spark SQL context
     * @return the table dataset
     * @throws IllegalArgumentException if the data source does not exist or does not provide tables
     */
    @Nonnull
    // @formatter:off
    @SuppressWarnings("unused")  // method is used by generated Scala code
    // @formatter:on
    T getTableFromDatasource(@Nonnull String table, @Nonnull String datasourceId, @Nonnull SQLContext sqlContext);
}
