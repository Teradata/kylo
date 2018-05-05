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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;

import org.apache.hadoop.conf.Configuration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultKyloCatalogReaderTest {

    /**
     * Verify jar is added only if not ignored
     */
    @Test
    @SuppressWarnings("unchecked")
    public void addJar() {
        // Mock resource loader
        final DataSourceResourceLoader resourceLoader = Mockito.mock(DataSourceResourceLoader.class);
        Mockito.when(resourceLoader.addJar(Mockito.anyString())).thenReturn(false, true);

        // Test adding jars
        final DefaultKyloCatalogReader reader = new DefaultKyloCatalogReader(Mockito.mock(KyloCatalogClient.class), new Configuration(false), resourceLoader);

        reader.addJar("file1.jar");
        reader.addJar("file2.jar");
        Assert.assertEquals(1, reader.getOptions().getJars().size());
        Assert.assertEquals("file2.jar", reader.getOptions().getJars().get(0));
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
        final DefaultKyloCatalogReader reader = new DefaultKyloCatalogReader(Mockito.mock(KyloCatalogClient.class), new Configuration(false), resourceLoader);

        reader.addJars(Arrays.asList("file1.jar", "file2.jar"));
        reader.addJars(Collections.singletonList("file2.jar"));
        reader.addJars(Arrays.asList("file1.jar", "file3.jar"));
        Assert.assertEquals(4, reader.getOptions().getJars().size());
        Assert.assertEquals("file1.jar", reader.getOptions().getJars().get(0));
        Assert.assertEquals("file2.jar", reader.getOptions().getJars().get(1));
        Assert.assertEquals("file1.jar", reader.getOptions().getJars().get(2));
        Assert.assertEquals("file3.jar", reader.getOptions().getJars().get(3));
    }

    /**
     * Verify setting Spark and Hadoop options.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void option() {
        // Test adding options
        final Configuration conf = new Configuration(false);
        final DefaultKyloCatalogReader reader = new DefaultKyloCatalogReader(Mockito.mock(KyloCatalogClient.class), conf, Mockito.mock(DataSourceResourceLoader.class));

        reader.option("spark.option", "1");
        reader.option("spark.hadoop.option", "2");
        Assert.assertEquals(2, reader.getOptions().getOptions().size());
        Assert.assertEquals("1", reader.getOptions().getOption("spark.option").get());
        Assert.assertEquals("2", reader.getOptions().getOption("spark.hadoop.option").get());
        Assert.assertEquals(1, conf.size());
        Assert.assertEquals("2", conf.get("option"));
    }
}
