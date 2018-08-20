package com.thinkbiganalytics.kylo.catalog.spark.sources.spark;

/*-
 * #%L
 * Kylo Catalog Core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import org.apache.hadoop.conf.Configuration;

import javax.annotation.Nonnull;

/**
 * Methods for interacting with Spark data sets.
 *
 * @param <T> Spark {@code DataFrame} class
 */
public interface SparkDataSetDelegate<T> {

    /**
     * Gets the Hadoop configuration from the specified client.
     */
    @Nonnull
    Configuration getHadoopConfiguration(@Nonnull KyloCatalogClient<T> client);

    /**
     * Indicate if the specified class is a {@code FileFormat}.
     */
    boolean isFileFormat(@Nonnull Class<?> formatClass);
}
