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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

public class HighWaterMarkInputFormatTest {

    /**
     * Temporary folder for listing files
     */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Mock current time
     */
    private long currentTimeMillis = (System.currentTimeMillis() / 1000) * 1000;  // seconds with milliseconds precision; for compatibility across file systems

    /**
     * Verify listing statuses without any configuration.
     */
    @Test
    public void listStatus() throws IOException {
        // Create temp files
        final File file1 = tempFolder.newFile("file1");
        Assert.assertTrue(file1.setLastModified(currentTimeMillis));

        final File folder1 = tempFolder.newFolder("folder1");
        final File file2 = tempFolder.newFile("folder1/file2");
        Assert.assertTrue(file2.setLastModified(currentTimeMillis));

        tempFolder.newFolder("folder1", "folder2");
        final File file3 = tempFolder.newFile("folder1/folder2/file3");
        Assert.assertTrue(file3.setLastModified(currentTimeMillis));

        // Test listing files
        final Job job = Job.getInstance(new Configuration(false));
        HighWaterMarkInputFormat.setInputPaths(job, file1.getAbsolutePath() + "," + folder1.getAbsolutePath());

        final HighWaterMarkInputFormat inputFormat = new MockHighWaterMarkInputFormat();
        final List<FileStatus> files = inputFormat.listStatus(job);
        Collections.sort(files, new FileStatusComparator());
        Assert.assertEquals(new Path(file1.toURI()), files.get(0).getPath());
        Assert.assertEquals(new Path(file2.toURI()), files.get(1).getPath());
        Assert.assertEquals(2, files.size());
        Assert.assertEquals(currentTimeMillis, inputFormat.getLastHighWaterMark());

        // Verify files are not listed again
        Assert.assertEquals(0, inputFormat.listStatus(job).size());
    }

    /**
     * Verify listing files with a high water mark.
     */
    @Test
    public void listStatusHighWaterMark() throws IOException {
        // Create temp file
        final File file1 = tempFolder.newFile("file1");
        Assert.assertTrue(file1.setLastModified(currentTimeMillis));

        final File file2 = tempFolder.newFile("file2");
        Assert.assertTrue(file2.setLastModified(currentTimeMillis + 1000));

        // Test listing files with high water mark
        final Job job = Job.getInstance(new Configuration(false));
        HighWaterMarkInputFormat.setHighWaterMark(job, currentTimeMillis);
        HighWaterMarkInputFormat.setInputPaths(job, tempFolder.getRoot().getAbsolutePath());

        final HighWaterMarkInputFormat inputFormat = new MockHighWaterMarkInputFormat();
        final List<FileStatus> files = inputFormat.listStatus(job);
        Collections.sort(files, new FileStatusComparator());
        Assert.assertEquals(new Path(file2.toURI()), files.get(0).getPath());
        Assert.assertEquals(1, files.size());
        Assert.assertEquals(currentTimeMillis + 1000, inputFormat.getLastHighWaterMark());
    }

    /**
     * Verify listing files before a maximum age.
     */
    @Test
    public void listStatusMaxFileAge() throws IOException {
        // Create temp file
        final File file1 = tempFolder.newFile("file1");
        Assert.assertTrue(file1.setLastModified(currentTimeMillis));

        final File file2 = tempFolder.newFile("file2");
        Assert.assertTrue(file2.setLastModified(currentTimeMillis - 2000));

        final File file3 = tempFolder.newFile("file3");
        Assert.assertTrue(file3.setLastModified(currentTimeMillis - 1000));

        // Test listing files with high water mark
        final Job job = Job.getInstance(new Configuration(false));
        HighWaterMarkInputFormat.setInputPaths(job, tempFolder.getRoot().getAbsolutePath());
        HighWaterMarkInputFormat.setMaxFileAge(job, 1000);

        final HighWaterMarkInputFormat inputFormat = new MockHighWaterMarkInputFormat();
        final List<FileStatus> files = inputFormat.listStatus(job);
        Collections.sort(files, new FileStatusComparator());
        Assert.assertEquals(new Path(file1.toURI()), files.get(0).getPath());
        Assert.assertEquals(new Path(file3.toURI()), files.get(1).getPath());
        Assert.assertEquals(2, files.size());
    }

    /**
     * Verify listing files after a minimum age.
     */
    @Test
    public void listStatusMinFileAge() throws IOException {
        // Create temp file
        final File file1 = tempFolder.newFile("file1");
        Assert.assertTrue(file1.setLastModified(currentTimeMillis));

        final File file2 = tempFolder.newFile("file2");
        Assert.assertTrue(file2.setLastModified(currentTimeMillis - 2000));

        final File file3 = tempFolder.newFile("file3");
        Assert.assertTrue(file3.setLastModified(currentTimeMillis - 1000));

        // Test listing files with high water mark
        final Job job = Job.getInstance(new Configuration(false));
        HighWaterMarkInputFormat.setInputPaths(job, tempFolder.getRoot().getAbsolutePath());
        HighWaterMarkInputFormat.setMinFileAge(job, 1000);

        final HighWaterMarkInputFormat inputFormat = new MockHighWaterMarkInputFormat();
        final List<FileStatus> files = inputFormat.listStatus(job);
        Collections.sort(files, new FileStatusComparator());
        Assert.assertEquals(new Path(file2.toURI()), files.get(0).getPath());
        Assert.assertEquals(new Path(file3.toURI()), files.get(1).getPath());
        Assert.assertEquals(2, files.size());
    }

    @Test(expected = IOException.class)
    public void listStatusMinAfterMax() throws IOException {
        final Job job = Job.getInstance(new Configuration(false));
        HighWaterMarkInputFormat.setMaxFileAge(job, 0);
        HighWaterMarkInputFormat.setMinFileAge(job, 1);

        final HighWaterMarkInputFormat inputFormat = new MockHighWaterMarkInputFormat();
        inputFormat.listStatus(job);
    }

    /**
     * Orders {@code FileStatus} objects by URI.
     */
    public static class FileStatusComparator implements Comparator<FileStatus> {

        @Override
        public int compare(@Nonnull final FileStatus file1, @Nonnull final FileStatus file2) {
            return file1.getPath().compareTo(file2.getPath());
        }

        @Override
        public boolean equals(final Object obj) {
            return false;
        }
    }

    /**
     * Mock {@code HighWaterMarkInputFormat} for testing.
     */
    private class MockHighWaterMarkInputFormat extends HighWaterMarkInputFormat {

        @Override
        long currentTimeMillis() {
            return currentTimeMillis;
        }
    }
}
