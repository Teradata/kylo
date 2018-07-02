package com.thinkbiganalytics.kylo.catalog.spark;

/*-
 * #%L
 * Kylo Catalog Core
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

import com.google.common.base.Optional;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.sources.DataSourceRegister;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DataSourceResourceLoaderTest {

    /**
     * Verify adding a file to the Spark context.
     */
    @Test
    public void addFile() {
        // Mock Spark context
        final SparkContext sparkContext = Mockito.mock(SparkContext.class);
        Mockito.when(sparkContext.hadoopConfiguration()).thenReturn(new Configuration(false));

        // Test adding null file
        final DataSourceResourceLoader loader = DataSourceResourceLoader.create(sparkContext);
        loader.addFile(null);
        Mockito.verify(sparkContext, Mockito.never()).addFile(Mockito.anyString());

        // Test adding missing file
        loader.addFile("file:/tmp/" + UUID.randomUUID().toString());
        Mockito.verify(sparkContext, Mockito.never()).addFile(Mockito.anyString());

        // Test adding new file
        final String filePath = getClass().getResource("DataSourceResourceLoaderTest.class").toString();
        loader.addFile(filePath);
        loader.addFile(filePath);  // de-duplication check
        Mockito.verify(sparkContext, Mockito.times(1)).addFile(filePath);
    }

    /**
     * Verify adding a jar to the Spark context.
     */
    @Test
    public void addJar() {
        // Mock Spark Context
        final SparkContext sparkContext = Mockito.mock(SparkContext.class);
        Mockito.when(sparkContext.hadoopConfiguration()).thenReturn(new Configuration(false));

        // Test adding local:/ jar
        final DataSourceResourceLoader loader = DataSourceResourceLoader.create(sparkContext);

        final String jarUrl = getClass().getResource("./").toString();
        loader.addJar(jarUrl.replace("file:", "local:"));
        Mockito.verify(sparkContext, Mockito.times(1)).addJar(jarUrl);
        Assert.assertNotNull(loader.getResource("DataSourceResourceLoaderTest.class"));
    }

    /**
     * Verify finding data source services.
     */
    @Test
    public void getDataSource() {
        // Mock Spark context
        final SparkContext sparkContext = Mockito.mock(SparkContext.class);
        Mockito.when(sparkContext.hadoopConfiguration()).thenReturn(new Configuration(false));

        // Test with no jars
        final DataSourceResourceLoader loader = DataSourceResourceLoader.create(sparkContext);

        Optional<DataSourceRegister> dataSource = loader.getDataSource("mock");
        Assert.assertThat(dataSource, isAbsent());

        // Test with service jar
        loader.addJar(getClass().getResource("/").toString());
        dataSource = loader.getDataSource("mock");
        Assert.assertThat(dataSource, isPresentAndThat(dataSourceShortName(CoreMatchers.equalTo("mock"))));

        // Test case-insensitive match (as Spark does)
        dataSource = loader.getDataSource("MOCK");
        Assert.assertThat(dataSource, isPresentAndThat(dataSourceShortName(CoreMatchers.equalTo("mock"))));
    }

    /**
     * Matches {@link DataSourceRegister} instances that have a {@code shortName} matching {@code matcher}.
     */
    @Nonnull
    private Matcher<DataSourceRegister> dataSourceShortName(@Nonnull final Matcher<String> matcher) {
        return new BaseMatcher<DataSourceRegister>() {
            @Override
            public boolean matches(@Nullable final Object item) {
                return (item instanceof DataSourceRegister && matcher.matches(((DataSourceRegister) item).shortName()));
            }

            @Override
            public void describeTo(@Nonnull final Description description) {
                description.appendText("a DataSourceRegister with shortName ");
                description.appendDescriptionOf(matcher);
            }
        };
    }

    /**
     * Matches {@link Optional} instances that do not contain a reference.
     */
    @Nonnull
    private Matcher<Optional> isAbsent() {
        return new BaseMatcher<Optional>() {
            @Override
            public boolean matches(@Nullable final Object item) {
                return (item instanceof Optional && !((Optional) item).isPresent());
            }

            @Override
            public void describeTo(@Nonnull final Description description) {
                description.appendValue(Optional.absent());
            }
        };
    }

    /**
     * Matches {@link Optional} instances that contain a reference matching {@code matcher}.
     */
    @Nonnull
    private <T> Matcher<Optional<T>> isPresentAndThat(@Nonnull final Matcher<T> matcher) {
        return new BaseMatcher<Optional<T>>() {
            @Override
            public boolean matches(@Nullable final Object item) {
                if (item instanceof Optional) {
                    final Optional<?> optional = (Optional) item;
                    return optional.isPresent() && matcher.matches(optional.get());
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(@Nonnull final Description description) {
                description.appendText("an optional containing ");
                description.appendDescriptionOf(matcher);
            }
        };
    }
}
