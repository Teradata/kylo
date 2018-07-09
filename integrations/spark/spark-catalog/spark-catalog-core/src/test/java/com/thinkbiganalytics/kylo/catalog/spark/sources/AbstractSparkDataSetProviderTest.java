package com.thinkbiganalytics.kylo.catalog.spark.sources;

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
import com.thinkbiganalytics.kylo.catalog.spark.sources.spark.HighWaterMarkInputFormat;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.SQLContext;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.collection.Seq;

public class AbstractSparkDataSetProviderTest {

    /**
     * Temporary folder for listing files
     */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Hadoop configuration for {@link AbstractSparkDataSetProvider#getHadoopConfiguration(KyloCatalogClient)}
     */
    private final Configuration conf = new Configuration(false);

    /**
     * File format indicator for {@link AbstractSparkDataSetProvider#isFileFormat(Class)}
     */
    private boolean isFileFormat = false;

    /**
     * Verify resolving High Water Mark paths.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void resolveHighWaterMarkPaths() throws IOException {
        final long currentTime = System.currentTimeMillis();

        // Mock client
        final KyloCatalogClient client = Mockito.mock(KyloCatalogClient.class);
        Mockito.when(client.getHighWaterMarks()).thenReturn(Collections.singletonMap("water.mark", Long.toString(currentTime - 60000)));

        // Mock files
        final List<String> inputPaths = new ArrayList<>();

        final File file1 = tempFolder.newFile("file1");
        Assert.assertTrue(file1.setLastModified(currentTime - 60000));
        inputPaths.add(file1.getAbsolutePath());

        final File file2 = tempFolder.newFile("file2");
        Assert.assertTrue(file2.setLastModified(currentTime - 30000));
        inputPaths.add(file2.getAbsolutePath());

        final File file3 = tempFolder.newFile("file3");
        Assert.assertTrue(file3.setLastModified(currentTime));
        inputPaths.add(file3.getAbsolutePath());

        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("mock");
        options.setOption(HighWaterMarkInputFormat.HIGH_WATER_MARK, "water.mark");
        options.setOption(HighWaterMarkInputFormat.MAX_FILE_AGE, "300000");
        options.setOption(HighWaterMarkInputFormat.MIN_FILE_AGE, "15000");

        // Test resolving paths
        final MockSparkDataSetProvider provider = new MockSparkDataSetProvider();
        final List<String> paths = provider.resolveHighWaterMarkPaths(inputPaths, options, client);

        Assert.assertEquals(file2.toURI().toString(), paths.get(0));
        Assert.assertEquals(1, paths.size());
        Mockito.verify(client).setHighWaterMarks(Collections.singletonMap("water.mark", Long.toString(file2.lastModified())));
    }

    /**
     * Verify resolving file format paths.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void resolvePaths() throws IOException {
        isFileFormat = true;

        // Mock files
        final File file1 = tempFolder.newFile("file1");
        final File file2 = tempFolder.newFile("file2");

        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("mock");
        options.setOption("path", file1.getAbsolutePath());
        options.setPaths(Collections.singletonList(file2.getAbsolutePath()));

        // Test resolving paths
        final KyloCatalogClient client = Mockito.mock(KyloCatalogClient.class);
        final MockSparkDataSetProvider provider = new MockSparkDataSetProvider();
        provider.resolvePaths(options, client);

        final List<String> paths = options.getPaths();
        Assert.assertTrue("Expected path option to be empty", options.getOption("path").isEmpty());
        Assert.assertNotNull("Expected paths to be non-null", paths);
        Assert.assertEquals(file1.getAbsolutePath(), paths.get(0));
        Assert.assertEquals(file2.getAbsolutePath(), paths.get(1));
        Assert.assertEquals(2, paths.size());
    }

    /**
     * Verify resolving {@code null} paths.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void resolvePathsEmpty() {
        isFileFormat = true;

        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("mock");

        // Test resolving paths
        final KyloCatalogClient client = Mockito.mock(KyloCatalogClient.class);
        final MockSparkDataSetProvider provider = new MockSparkDataSetProvider();
        provider.resolvePaths(options, client);

        Assert.assertNull(options.getPaths());
    }

    /**
     * Verify resolving paths for non-file-format data sources.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void resolvePathsNotFileFormat() {
        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("mock");
        options.setOption("path", tempFolder.getRoot().getAbsolutePath());

        // Test resolving path for non-file-format data sources
        final KyloCatalogClient client = Mockito.mock(KyloCatalogClient.class);
        final MockSparkDataSetProvider provider = new MockSparkDataSetProvider();
        provider.resolvePaths(options, client);

        Assert.assertEquals(tempFolder.getRoot().getAbsolutePath(), options.getOption("path").get());
    }

    /**
     * Mock {@code AbstractSparkDataSetProvider} for testing.
     */
    private class MockSparkDataSetProvider extends AbstractSparkDataSetProvider<DataFrame> {

        @Nonnull
        @Override
        protected DataFrameReader getDataFrameReader(@Nonnull final KyloCatalogClient<DataFrame> client, @Nonnull final DataSetOptions options) {
            return new DataFrameReader(Mockito.mock(SQLContext.class));
        }

        @Nonnull
        @Override
        protected DataFrameWriter getDataFrameWriter(@Nonnull final DataFrame dataSet, @Nonnull final DataSetOptions options) {
            return new DataFrameWriter(Mockito.mock(DataFrame.class));
        }

        @Nonnull
        @Override
        protected Configuration getHadoopConfiguration(@Nonnull final KyloCatalogClient<DataFrame> client) {
            return conf;
        }

        @Override
        protected boolean isFileFormat(@Nonnull final Class<?> formatClass) {
            return isFileFormat;
        }

        @Nonnull
        @Override
        protected DataFrame load(@Nonnull final DataFrameReader reader, @Nullable final Seq<String> paths) {
            return Mockito.mock(DataFrame.class);
        }
    }
}
