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
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.hadoop.conf.Configuration;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.sources.HadoopFsRelationProvider;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class SparkDataSetContextTest {

    /**
     * Temporary folder
     */
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Verify retrieving options.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getOptions() {
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("text");
        options.setOption(KyloCatalogConstants.PATH_OPTION, "/mock/path/file.txt");

        final SparkDataSetContext<DataFrame> context = new SparkDataSetContext<>(options, Mockito.mock(KyloCatalogClient.class), Mockito.mock(SparkDataSetDelegate.class));
        Assert.assertEquals("/mock/path/file.txt", context.getOption(KyloCatalogConstants.PATH_OPTION).get());
    }

    /**
     * Verify retrieving options for file data sources.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getOptionsFileFormat() {
        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("text");
        options.setOption(KyloCatalogConstants.PATH_OPTION, "/mock/path/file.txt");

        // Mock delegate
        final SparkDataSetDelegate<DataFrame> delegate = Mockito.mock(SparkDataSetDelegate.class);
        Mockito.when(delegate.isFileFormat(Mockito.any(Class.class))).thenReturn(true);

        // Test getting path
        final SparkDataSetContext<DataFrame> context = new SparkDataSetContext<>(options, Mockito.mock(KyloCatalogClient.class), delegate);
        Assert.assertTrue("Expected path option to be empty", context.getOption(KyloCatalogConstants.PATH_OPTION).isEmpty());
    }

    /**
     * Verify retrieving paths.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getPaths() {
        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("mock");
        options.setOption(KyloCatalogConstants.PATH_OPTION, "/mock/path/file1.txt");
        options.setPaths(Collections.singletonList("/mock/path/file2.txt"));

        // Test getting path for non-file-format data sources
        final SparkDataSetContext<DataFrame> context = new SparkDataSetContext<>(options, Mockito.mock(KyloCatalogClient.class), Mockito.mock(SparkDataSetDelegate.class));
        Assert.assertEquals("/mock/path/file1.txt", context.getOption("path").get());
        Assert.assertEquals(Collections.singletonList("/mock/path/file2.txt"), context.getPaths());
    }

    /**
     * Verify retrieving file format paths.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getPathsFileFormat() {
        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("text");
        options.setOption(KyloCatalogConstants.PATH_OPTION, "/mock/path/file1.txt");
        options.setPaths(Collections.singletonList("/mock/path/file2.txt"));

        // Mock delegate
        final SparkDataSetDelegate<DataFrame> delegate = Mockito.mock(SparkDataSetDelegate.class);
        Mockito.when(delegate.isFileFormat(Mockito.any(Class.class))).thenReturn(true);

        // Test retrieving paths
        final SparkDataSetContext<DataFrame> context = new SparkDataSetContext<>(options, Mockito.mock(KyloCatalogClient.class), delegate);
        final List<String> paths = context.getPaths();
        Assert.assertTrue("Expected path option to be empty", context.getOption("path").isEmpty());
        Assert.assertNotNull("Expected paths to be non-null", paths);
        Assert.assertEquals("/mock/path/file1.txt", paths.get(0));
        Assert.assertEquals("/mock/path/file2.txt", paths.get(1));
        Assert.assertEquals(2, paths.size());
    }

    /**
     * Verify retrieving {@code null} paths.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getPathsFileFormatEmpty() {
        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("text");

        // Mock delegate
        final SparkDataSetDelegate<DataFrame> delegate = Mockito.mock(SparkDataSetDelegate.class);
        Mockito.when(delegate.isFileFormat(Mockito.any(Class.class))).thenReturn(true);

        // Test getting paths
        final SparkDataSetContext<DataFrame> context = new SparkDataSetContext<>(options, Mockito.mock(KyloCatalogClient.class), delegate);
        Assert.assertNull(context.getPaths());
    }

    /**
     * Verify retrieving High Water Mark paths.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getPathsHighWaterMark() throws IOException {
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
        options.setPaths(inputPaths);

        // Mock delegate
        final SparkDataSetDelegate<DataFrame> delegate = Mockito.mock(SparkDataSetDelegate.class);
        Mockito.when(delegate.getHadoopConfiguration(Mockito.any(KyloCatalogClient.class))).thenReturn(new Configuration(false));
        Mockito.when(delegate.isFileFormat(Mockito.any(Class.class))).thenReturn(true);

        // Test resolving paths
        final SparkDataSetContext<DataFrame> context = new SparkDataSetContext<>(options, client, delegate);
        final List<String> paths = context.getPaths();
        Assert.assertNotNull("Expected paths to be non-null", paths);
        Assert.assertEquals(file2.toURI().toString(), paths.get(0));
        Assert.assertEquals(1, paths.size());
        Mockito.verify(client).setHighWaterMarks(Collections.singletonMap("water.mark", Long.toString(file2.lastModified())));
    }

    /**
     * Verify retrieving High Water Mark paths when no files match.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getPathsHighWaterMarkEmpty() throws IOException {
        final long currentTime = System.currentTimeMillis();

        // Mock client
        final KyloCatalogClient client = Mockito.mock(KyloCatalogClient.class);
        Mockito.when(client.getHighWaterMarks()).thenReturn(Collections.singletonMap("water.mark", Long.toString(currentTime)));

        // Mock file
        final File file = tempFolder.newFile("file.txt");
        Assert.assertTrue(file.setLastModified(currentTime - 1000));

        // Mock options
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("mock");
        options.setOption(HighWaterMarkInputFormat.HIGH_WATER_MARK, "water.mark");
        options.setPaths(Collections.singletonList(file.getAbsolutePath()));

        // Mock delegate
        final SparkDataSetDelegate<DataFrame> delegate = Mockito.mock(SparkDataSetDelegate.class);
        Mockito.when(delegate.getHadoopConfiguration(Mockito.any(KyloCatalogClient.class))).thenReturn(new Configuration(false));
        Mockito.when(delegate.isFileFormat(Mockito.any(Class.class))).thenReturn(true);

        // Test resolving paths
        final SparkDataSetContext<DataFrame> context = new SparkDataSetContext<>(options, client, delegate);
        final List<String> paths = context.getPaths();
        Assert.assertNotNull("Expected paths to be non-null", paths);
        Assert.assertEquals("file:/dev/null", paths.get(0));
        Assert.assertEquals(1, paths.size());
        Mockito.verify(client).setHighWaterMarks(Collections.singletonMap("water.mark", Long.toString(currentTime)));
    }

    /**
     * Verify determining if a format reads files.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void isFileFormat() {
        // Mock delegate
        final SparkDataSetDelegate<DataFrame> delegate = Mockito.mock(SparkDataSetDelegate.class);
        Mockito.when(delegate.isFileFormat(Mockito.any(Class.class))).then(new Answer<Boolean>() {
            @Nonnull
            @Override
            public Boolean answer(@Nonnull final InvocationOnMock invocation) {
                final Class<?> clazz = invocation.getArgumentAt(0, Class.class);
                return HadoopFsRelationProvider.class.isAssignableFrom(clazz);
            }
        });

        // Test invalid format
        final DataSetOptions options = new DataSetOptions();
        options.setFormat("invalid");

        final KyloCatalogClient<DataFrame> client = Mockito.mock(KyloCatalogClient.class);
        SparkDataSetContext<DataFrame> context = new SparkDataSetContext<>(options, client, delegate);
        Assert.assertFalse("Expected 'invalid' to not be a file format", context.isFileFormat());

        // Test non-file format
        options.setFormat("jdbc");
        context = new SparkDataSetContext<>(options, client, delegate);
        Assert.assertFalse("Expected 'jdbc' to not be a file format", context.isFileFormat());

        // Test short name
        options.setFormat("text");
        context = new SparkDataSetContext<>(options, client, delegate);
        Assert.assertTrue("Expected 'text' to be a file format", context.isFileFormat());

        // Test class name
        options.setFormat("org.apache.spark.sql.execution.datasources.text.DefaultSource");
        context = new SparkDataSetContext<>(options, client, delegate);
        Assert.assertTrue("Expected 'org.apache.spark.sql.execution.datasources.text.DefaultSource' to be a file format", context.isFileFormat());

        // Test package name
        options.setFormat("org.apache.spark.sql.execution.datasources.text");
        context = new SparkDataSetContext<>(options, client, delegate);
        Assert.assertTrue("Expected 'org.apache.spark.sql.execution.datasources.text' to be a file format", context.isFileFormat());
    }
}
