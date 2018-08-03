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
import org.apache.spark.Accumulator;
import org.apache.spark.scheduler.JobFailed$;
import org.apache.spark.scheduler.JobSucceeded$;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.annotation.Nonnull;

public class DeletePathsListenerTest {

    /**
     * Temporary folder
     */
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Temporary file
     */
    private File tempFile;

    /**
     * Set up tests.
     */
    @Before
    public void setUp() throws IOException {
        tempFile = tempFolder.newFile("file1.txt");
    }

    /**
     * Verify file exists if flag is false.
     */
    @Test
    public void testFlag() {
        final SparkListenerJobEnd event = new SparkListenerJobEnd(1, 0, JobSucceeded$.MODULE$);
        apply(false, event);
        Assert.assertTrue("Expected file to exist", tempFile.exists());
    }

    /**
     * Verify file exists after job failed.
     */
    @Test
    public void testJobFailed() {
        final SparkListenerJobEnd event = new SparkListenerJobEnd(1, 0, JobFailed$.MODULE$.apply(new RuntimeException()));
        apply(true, event);
        Assert.assertTrue("Expected file to exist", tempFile.exists());
    }

    /**
     * Verify file deleted after job succeeded.
     */
    @Test
    public void testJobSucceeded() throws IOException {
        // Test job succeeded
        final SparkListenerJobEnd event = new SparkListenerJobEnd(1, 0, JobSucceeded$.MODULE$);
        final DeletePathsListener function = apply(true, event);
        Assert.assertFalse("Expected file to be deleted", tempFile.exists());

        // Test recreated file is not deleted
        setUp();
        function.apply(event);
        Assert.assertTrue("Expected file to exist", tempFile.exists());
    }

    /**
     * Applies the delete paths function with the specified flag and event.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    private DeletePathsListener apply(final boolean flag, @Nonnull final SparkListenerJobEnd event) {
        // Mock accumulator
        final Accumulator<Boolean> accumulator = Mockito.mock(Accumulator.class);
        Mockito.when(accumulator.value()).thenReturn(flag);

        // Apply function
        final Configuration conf = new Configuration(false);
        final DeletePathsListener function = new DeletePathsListener(Collections.singletonList(tempFile.toURI().toString()), accumulator, conf);
        function.apply(event);
        return function;
    }
}
