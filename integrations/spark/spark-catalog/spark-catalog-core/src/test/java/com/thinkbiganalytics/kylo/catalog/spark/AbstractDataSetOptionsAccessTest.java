package com.thinkbiganalytics.kylo.catalog.spark;

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

import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.hadoop.conf.Configuration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class AbstractDataSetOptionsAccessTest {

    /**
     * Verify jar is added only if not ignored
     */
    @Test
    public void addJar() {
        // Mock resource loader
        final DataSourceResourceLoader resourceLoader = Mockito.mock(DataSourceResourceLoader.class);
        Mockito.when(resourceLoader.addJar(Mockito.anyString())).thenReturn(false, true);

        // Test adding jars
        final MockDataSetOptionsAccess mock = new MockDataSetOptionsAccess(resourceLoader);

        mock.addJar("file1.jar");
        mock.addJar("file2.jar");
        Assert.assertEquals(1, mock.getOptions().getJars().size());
        Assert.assertEquals("file2.jar", mock.getOptions().getJars().get(0));
    }

    /**
     * Verify jars are only added if not ignored.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void addJars() {
        // Mock resource loader
        final DataSourceResourceLoader resourceLoader = Mockito.mock(DataSourceResourceLoader.class);
        Mockito.when(resourceLoader.addJars(Mockito.any(List.class))).thenReturn(true, false, true);

        // Test adding jars
        final MockDataSetOptionsAccess mock = new MockDataSetOptionsAccess(resourceLoader);

        mock.addJars(Arrays.asList("file1.jar", "file2.jar"));
        mock.addJars(Collections.singletonList("file2.jar"));
        mock.addJars(Arrays.asList("file1.jar", "file3.jar"));
        Assert.assertEquals(4, mock.getOptions().getJars().size());
        Assert.assertEquals("file1.jar", mock.getOptions().getJars().get(0));
        Assert.assertEquals("file2.jar", mock.getOptions().getJars().get(1));
        Assert.assertEquals("file1.jar", mock.getOptions().getJars().get(2));
        Assert.assertEquals("file3.jar", mock.getOptions().getJars().get(3));
    }

    /**
     * Verify setting Spark and Hadoop options.
     */
    @Test
    public void option() {
        // Test adding options
        final MockDataSetOptionsAccess mock = new MockDataSetOptionsAccess(Mockito.mock(DataSourceResourceLoader.class));

        mock.option("spark.option", "1");
        mock.option("spark.hadoop.option", "2");
        Assert.assertEquals(2, mock.getOptions().getOptions().size());
        Assert.assertEquals("1", mock.getOptions().getOption("spark.option").get());
        Assert.assertEquals("2", mock.getOptions().getOption("spark.hadoop.option").get());
        Assert.assertEquals(1, mock.hadoopConfiguration.size());
        Assert.assertEquals("2", mock.hadoopConfiguration.get("option"));
    }

    private static class MockDataSetOptionsAccess extends AbstractDataSetOptionsAccess<MockDataSetOptionsAccess> {

        MockDataSetOptionsAccess(@Nonnull final DataSourceResourceLoader resourceLoader) {
            super(new DataSetOptions(), new Configuration(false), resourceLoader);
        }
    }
}
