package com.thinkbiganalytics.kylo.catalog.file;

/*-
 * #%L
 * kylo-catalog-core
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

import com.google.common.io.Files;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DefaultDataSetTemplate;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CatalogFileManagerTest {

    /**
     * Folder for dataset uploads
     */
    @Rule
    public TemporaryFolder datasetsFolder = new TemporaryFolder();

    /**
     * Verify uploading a file.
     */
    @Test
    public void createUpload() throws IOException {
        final DataSet dataSet = createDataSet();
        final String src = "Hello world!";

        // Test uploading a file
        final CatalogFileManager fileManager = new MockCatalogFileManager();
        final DataSetFile upload = fileManager.createUpload(dataSet, "file-upload.txt", new ByteArrayInputStream(src.getBytes(StandardCharsets.UTF_8)));

        final File file = datasetsFolder.getRoot().toPath().resolve(dataSet.getId()).resolve("file-upload.txt").toFile();
        Assert.assertFalse("Expected uploaded file to not be a directory", upload.isDirectory());
        Assert.assertEquals(src.length(), upload.getLength().longValue());
        Assert.assertEquals("file-upload.txt", upload.getName());
        Assert.assertEquals(file.toURI(), URI.create(upload.getPath()));
        Assert.assertEquals(src, Files.toString(file, StandardCharsets.UTF_8));
    }

    /**
     * Verify deleting an uploaded file.
     */
    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteUpload() throws IOException {
        // Create data set including files
        final DataSet dataSet = createDataSet();

        final File file = new File(datasetsFolder.newFolder(dataSet.getId()), "test-file.txt");
        file.createNewFile();
        Assert.assertTrue("Expected file to exist", file.exists());

        // Test deleting a file
        final CatalogFileManager fileManager = new MockCatalogFileManager();
        fileManager.deleteUpload(dataSet, "test-file.txt");
        Assert.assertFalse("Expected file to be deleted", file.exists());
    }

    /**
     * Verify listing uploaded files.
     */
    @Test
    public void listUploads() throws IOException {
        // Create data set including files
        final DataSet dataSet = createDataSet();
        final File dataSetFolder = datasetsFolder.newFolder(dataSet.getId());

        Files.write("data1", new File(dataSetFolder, "file1.txt"), StandardCharsets.UTF_8);
        Files.write("data2", new File(dataSetFolder, "file2.txt"), StandardCharsets.UTF_8);
        Files.write("data3", new File(dataSetFolder, "file3.txt"), StandardCharsets.UTF_8);

        // Test listing files
        final CatalogFileManager fileManager = new MockCatalogFileManager();
        final List<DataSetFile> files = fileManager.listUploads(dataSet);
        Assert.assertThat(files, CoreMatchers.hasItem(equalTo("file1.txt", new Path(dataSetFolder.toPath().resolve("file1.txt").toUri()).toString(), false, 5, "data1")));
        Assert.assertThat(files, CoreMatchers.hasItem(equalTo("file2.txt", new Path(dataSetFolder.toPath().resolve("file2.txt").toUri()).toString(), false, 5, "data2")));
        Assert.assertThat(files, CoreMatchers.hasItem(equalTo("file3.txt", new Path(dataSetFolder.toPath().resolve("file3.txt").toUri()).toString(), false, 5, "data3")));
        Assert.assertEquals(3, files.size());
    }

    /**
     * Creates a new data set.
     */
    @Nonnull
    private DataSet createDataSet() {
        final DefaultDataSetTemplate dataSourceTemplate = new DefaultDataSetTemplate();
        dataSourceTemplate.setPaths(Collections.singletonList(datasetsFolder.getRoot().toURI().toString()));

        final DataSource dataSource = new DataSource();
        dataSource.setId(UUID.randomUUID().toString());
        dataSource.setTemplate(dataSourceTemplate);

        final DataSet dataSet = new DataSet();
        dataSet.setId(UUID.randomUUID().toString());
        dataSet.setDataSource(dataSource);
        return dataSet;
    }

    /**
     * Matches data set files with the specified attributes.
     */
    @Nonnull
    @SuppressWarnings("SameParameterValue")
    private static Matcher<DataSetFile> equalTo(@Nonnull final String name, @Nonnull final String path, final boolean isDirectory, final long size, @Nonnull final String content) {
        return new BaseMatcher<DataSetFile>() {
            @Override
            public boolean matches(@Nullable final Object item) {
                if (item instanceof DataSetFile) {
                    final DataSetFile file = (DataSetFile) item;
                    try {
                        return Objects.equals(name, file.getName())
                               && Objects.equals(path, file.getPath())
                               && Objects.equals(isDirectory, file.isDirectory())
                               && Objects.equals(size, file.getLength())
                               && Objects.equals(content, Files.toString(new File(URI.create(file.getPath())), StandardCharsets.UTF_8));
                    } catch (final IOException e) {
                        System.err.println("error: unable to read data set file: " + file.getPath() + ": " + e);
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public void describeTo(@Nonnull final Description description) {
                description.appendText("equal to DataSetFile[name=" + name + " path=" + path + " directory=" + isDirectory + " size=" + size + " content=" + content + "]");
            }
        };
    }

    /**
     * Mock {@link CatalogFileManager} for testing.
     */
    private class MockCatalogFileManager extends DefaultCatalogFileManager {

        /**
         * Construct a {@code MockCatalogFileManager}.
         */
        MockCatalogFileManager() {
            super(new PathValidator());
        }

        @Override
        protected <R> R isolatedFunction(@Nonnull final DataSetTemplate template, @Nonnull final Path path, @Nonnull final FileSystemFunction<R> function) throws IOException {
            return function.apply(FileSystem.getLocal(new Configuration(false)));
        }
    }
}
