package com.thinkbiganalytics.spark.service;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import org.apache.spark.sql.sources.DataSourceRegister;

import java.util.ServiceLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Locates Spark resources.
 */
public class SparkLocatorService {

    /**
     * Loads Spark data sources.
     */
    @Nullable
    private ServiceLoader<DataSourceRegister> dataSources;

    /**
     * Spark class loader.
     */
    @Nonnull
    private ClassLoader sparkClassLoader = Thread.currentThread().getContextClassLoader();

    /**
     * Gets the registered Spark data sources.
     */
    @Nonnull
    public Iterable<DataSourceRegister> getDataSources() {
        if (dataSources == null) {
            dataSources = ServiceLoader.load(DataSourceRegister.class, sparkClassLoader);
        }
        return dataSources;
    }

    /**
     * Sets the class loader used by Spark.
     */
    public void setSparkClassLoader(@Nonnull final ClassLoader classLoader) {
        sparkClassLoader = classLoader;
    }
}
