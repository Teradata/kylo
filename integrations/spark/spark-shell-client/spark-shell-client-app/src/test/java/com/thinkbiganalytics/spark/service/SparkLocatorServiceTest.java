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
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import javax.annotation.Nonnull;

public class SparkLocatorServiceTest {

    /**
     * Exclude a data source by class name.
     */
    @Test
    public void excludeDataSourcesByClassName() {
        final SparkLocatorService sparkLocatorService = new SparkLocatorService();
        Assert.assertTrue("Precondition failed: missing text data source", containsDataSource(sparkLocatorService.getDataSources(), "text"));

        sparkLocatorService.excludeDataSources(Collections.singletonList("org.apache.spark.sql.execution.datasources.text.DefaultSource"));
        Assert.assertFalse("Expected excluded text data source, but found text data source", containsDataSource(sparkLocatorService.getDataSources(), "text"));
    }

    /**
     * Exclude a data source by package name.
     */
    @Test
    public void excludeDataSourcesByPackageName() {
        final SparkLocatorService sparkLocatorService = new SparkLocatorService();
        Assert.assertTrue("Precondition failed: missing text data source", containsDataSource(sparkLocatorService.getDataSources(), "text"));

        sparkLocatorService.excludeDataSources(Collections.singletonList("org.apache.spark.sql.execution.datasources.text"));
        Assert.assertFalse("Expected excluded text data source, but found text data source", containsDataSource(sparkLocatorService.getDataSources(), "text"));
    }

    /**
     * Exclude a data source by its short name.
     */
    @Test
    public void excludeDataSourcesByShortName() {
        final SparkLocatorService sparkLocatorService = new SparkLocatorService();
        Assert.assertTrue("Precondition failed: missing text data source", containsDataSource(sparkLocatorService.getDataSources(), "text"));

        sparkLocatorService.excludeDataSources(Collections.singletonList("text"));
        Assert.assertFalse("Expected excluded text data source, but found text data source", containsDataSource(sparkLocatorService.getDataSources(), "text"));
    }

    /**
     * Include a data source by class name.
     */
    @Test
    public void includeDataSourcesByClassName() {
        final SparkLocatorService sparkLocatorService = new SparkLocatorService();
        Assert.assertFalse("Precondition failed: found kylo-test data source", containsDataSource(sparkLocatorService.getDataSources(), DefaultSource.NAME));

        sparkLocatorService.includeDataSources(Collections.singletonList(DefaultSource.class.getName()));
        Assert.assertTrue("Expected included kylo-test data source, but missing kylo-test data source", containsDataSource(sparkLocatorService.getDataSources(), DefaultSource.NAME));
    }

    /**
     * Include a data source by package name.
     */
    @Test
    public void includeDataSourcesByPackageName() {
        final SparkLocatorService sparkLocatorService = new SparkLocatorService();
        Assert.assertFalse("Precondition failed: found kylo-test data source", containsDataSource(sparkLocatorService.getDataSources(), DefaultSource.NAME));

        sparkLocatorService.includeDataSources(Collections.singletonList(DefaultSource.class.getPackage().getName()));
        Assert.assertTrue("Expected included kylo-test data source, but missing kylo-test data source", containsDataSource(sparkLocatorService.getDataSources(), DefaultSource.NAME));
    }

    /**
     * Indicates if the specified data source is contained in the iterable.
     */
    private boolean containsDataSource(@Nonnull final Iterable<DataSourceRegister> dataSources, @Nonnull final String shortName) {
        for (final DataSourceRegister dataSource : dataSources) {
            if (dataSource.shortName().equals(shortName)) {
                return true;
            }
        }
        return false;
    }
}
