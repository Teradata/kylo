package com.thinkbiganalytics.kylo.hadoop;

/*-
 * #%L
 * kylo-commons-hadoop
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

import com.thinkbiganalytics.kylo.protocol.hadoop.Handler;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.RawLocalFileSystem;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;

public class FileSystemUtilTest {

    @BeforeClass
    public static void beforeClass() {
        Handler.register();
    }

    /**
     * Verify detecting if an HDFS file exists.
     */
    @Test
    public void fileExistsHdfs() throws IOException {
        final Configuration conf = new Configuration(false);
        conf.setClass("fs.hdfs.impl", MockFileSystem.class, FileSystem.class);

        final File file = File.createTempFile("kylo-", ".tmp");
        try {
            Assert.assertTrue("Expected HDFS file to exist", FileSystemUtil.fileExists(URI.create("hdfs:" + file.getAbsolutePath()), conf));
            Assert.assertTrue("Expected HDFS file to exist", FileSystemUtil.fileExists(URI.create("hadoop:hdfs:" + file.getAbsolutePath()), conf));
        } finally {
            Files.delete(file.toPath());
        }
    }

    /**
     * Verify detecting if a local file exists.
     */
    @Test
    public void fileExistsLocal() throws IOException {
        final File file = File.createTempFile("kylo-", ".tmp");
        try {
            Assert.assertTrue("Expected local file to exist", FileSystemUtil.fileExists(file.toURI(), new Configuration(false)));
        } finally {
            Files.delete(file.toPath());
        }
    }

    /**
     * Verify response for unsupported file systems.
     */
    @Test(expected = IOException.class)
    public void fileExistsOther() throws IOException {
        final Configuration conf = new Configuration(false);
        conf.setClass("fs.mock.impl", MockFileSystem.class, FileSystem.class);
        FileSystemUtil.fileExists(URI.create("mock:/tmp/" + UUID.randomUUID().toString()), conf);
    }

    /**
     * Verify parsing URLs from strings.
     */
    @Test
    public void parseUrl() throws MalformedURLException {
        final Configuration conf = new Configuration(false);
        conf.set("fs.defaultFS", "mock:///");

        Assert.assertEquals(new URL("file:/path/file.ext"), FileSystemUtil.parseUrl("/path/file.ext", null));
        Assert.assertEquals(new File("file.ext").toURI().toURL(), FileSystemUtil.parseUrl("file.ext", null));
        Assert.assertEquals(new URL("hadoop:mock:/path/file.ext"), FileSystemUtil.parseUrl("/path/file.ext", conf));
        Assert.assertEquals(new URL("http://localhost/path/file.ext"), FileSystemUtil.parseUrl("http://localhost/path/file.ext", null));
        Assert.assertEquals(new URL("hadoop:hdfs://localhost:8020/path/file.ext"), FileSystemUtil.parseUrl("hdfs://localhost:8020/path/file.ext", null));
    }

    /**
     * Verify adding file systems to Hadoop configuration.
     */
    @Test
    public void registerFileSystems() {
        final Configuration conf = new Configuration(false);
        FileSystemUtil.registerFileSystems(Arrays.asList(new LocalFileSystem(), new MockFileSystem(), new RawLocalFileSystem()), conf);
        Assert.assertEquals(LocalFileSystem.class.getName(), conf.get("fs.file.impl"));
        Assert.assertEquals(MockFileSystem.class.getName(), conf.get("fs.mock.impl"));
    }
}
