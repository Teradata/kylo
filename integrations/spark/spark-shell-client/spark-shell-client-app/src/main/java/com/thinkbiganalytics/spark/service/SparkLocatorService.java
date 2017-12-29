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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.apache.spark.sql.sources.DataSourceRegister;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Locates Spark resources.
 */
public class SparkLocatorService {

    private static final XLogger log = XLoggerFactory.getXLogger(SparkLocatorService.class);

    /**
     * Class name of the default data source for a package.
     */
    private static final String DEFAULT_SOURCE = ".DefaultSource";

    /**
     * Loads Spark data sources.
     */
    @Nullable
    private Iterable<DataSourceRegister> dataSources;

    /**
     * Spark class loader.
     */
    @Nonnull
    private ClassLoader sparkClassLoader = Thread.currentThread().getContextClassLoader();

    /**
     * Excludes the specified list of data source short names, class names, or package names from {@link #getDataSources()}.
     */
    public void excludeDataSources(@Nonnull final List<String> excluded) {
        log.entry(excluded);

        dataSources = Iterables.filter(getDataSources(), new Predicate<DataSourceRegister>() {
            @Override
            public boolean apply(@Nullable final DataSourceRegister input) {
                // Check for null
                if (input == null) {
                    return false;
                }

                // Check for match in excluded
                final String className = input.getClass().getName();
                final String packageName = className.endsWith(DEFAULT_SOURCE) ? className.substring(0, className.length() - DEFAULT_SOURCE.length()) : null;
                final String shortName = input.shortName();
                return !excluded.contains(className) && !excluded.contains(shortName) && (packageName == null || !excluded.contains(packageName));
            }
        });

        log.exit();
    }

    /**
     * Gets the registered Spark data sources.
     */
    @Nonnull
    public Iterable<DataSourceRegister> getDataSources() {
        log.entry();

        if (dataSources == null) {
            dataSources = ServiceLoader.load(DataSourceRegister.class, sparkClassLoader);
        }

        return log.exit(dataSources);
    }

    /**
     * Includes the specified list of data source class names or package names in {@link #getDataSources()}.
     */
    public void includeDataSources(@Nonnull final List<String> included) {
        log.entry(included);
        final List<DataSourceRegister> includedDataSources = new ArrayList<>(included.size());

        for (final String name : included) {
            final DataSourceRegister dataSource = loadDataSource(name);
            if (dataSource != null) {
                includedDataSources.add(dataSource);
            } else {
                log.warn("Unable to find DataSourceRegister class: {}", name);
            }
        }

        dataSources = Iterables.concat(getDataSources(), includedDataSources);
        log.exit();
    }

    /**
     * Sets the class loader used by Spark.
     */
    public void setSparkClassLoader(@Nonnull final ClassLoader classLoader) {
        log.entry(classLoader);
        sparkClassLoader = classLoader;
        log.exit();
    }

    /**
     * Attempts to create in instance of the specified class.
     */
    @Nullable
    private DataSourceRegister createDataSourceRegister(@Nonnull final String className) {
        try {
            final Class<?> clazz = sparkClassLoader.loadClass(className);
            if (DataSourceRegister.class.isAssignableFrom(clazz)) {
                return (DataSourceRegister) clazz.newInstance();
            }
        } catch (final ClassNotFoundException e) {
            // ignored
        } catch (final IllegalAccessException e) {
            log.info("Unable to access DataSourceRegister: {}", className, e);
        } catch (final InstantiationException e) {
            log.info("Unable to instantiate DataSourceRegister: {}", className, e);
        }
        return null;
    }

    /**
     * Creates a new instance of the data source specified by class name or package name.
     */
    @Nullable
    private DataSourceRegister loadDataSource(@Nonnull final String name) {
        // Try exact class name
        DataSourceRegister dataSource = createDataSourceRegister(name);

        // Try package with DefaultSource class
        if (dataSource == null) {
            dataSource = createDataSourceRegister(name + DEFAULT_SOURCE);
        }

        // Return data source
        return dataSource;
    }
}
