package com.thinkbiganalytics.nifi.v2.hdfs;

/*-
 * #%L
 * thinkbig-nifi-hadoop-processors
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

import com.google.common.collect.ImmutableMap;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.util.MockProcessContext;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RemoveHDFSFolderTest {

    /**
     * Mock file system
     */
    private final FileSystem fileSystem = Mockito.mock(FileSystem.class);

    /**
     * Test runner
     */
    private final TestRunner runner = TestRunners.newTestRunner(new TestableRemoveHDFSFolder());

    /**
     * Initialize instance variables
     */
    @Before
    public void setUp() throws Exception {
        // Setup mock file system
        Mockito.when(fileSystem.delete(Mockito.any(Path.class), Mockito.eq(true))).thenReturn(true);

        // Setup test runner
        runner.setValidateExpressionUsage(false);
    }

    /**
     * Verify property validators.
     */
    @Test
    public void testValidators() {
        // Test with no properties
        Collection<ValidationResult> results = validate(runner);
        Assert.assertEquals(1, results.size());
        results.forEach((ValidationResult result) -> Assert.assertEquals("'Directory' is invalid because Directory is required", result.toString()));

        // Test with empty directory
        runner.setProperty(RemoveHDFSFolder.DIRECTORY, "");
        results = validate(runner);
        Assert.assertEquals(1, results.size());
        results.forEach((ValidationResult result) -> Assert.assertEquals("'Directory' validated against '' is invalid because Directory cannot be empty", result.toString()));

        // Test with valid properties
        runner.setProperty(RemoveHDFSFolder.DIRECTORY, "/target");
        results = validate(runner);
        Assert.assertEquals(0, results.size());
    }

    /**
     * Verify removing folders.
     */
    @Test
    public void testRemoveFolder() throws Exception {
        // Test removing folders
        runner.setProperty(RemoveHDFSFolder.DIRECTORY, "/target/\n  /etl/${category}/${feed}/  \r\n");
        runner.enqueue(new byte[0], ImmutableMap.of("category", "movies", "feed", "actors"));
        runner.run();

        // Check relationships
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(RemoveHDFSFolder.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(RemoveHDFSFolder.REL_SUCCESS).size());

        // Check file system calls
        InOrder inOrder = Mockito.inOrder(fileSystem);
        inOrder.verify(fileSystem).delete(new Path("/target"), true);
        inOrder.verify(fileSystem).delete(new Path("/etl/movies/actors"), true);
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Verify failure for double slashes.
     */
    @Test
    public void testRemoveFolderWithDoubleSlash() throws Exception {
        runner.setProperty(RemoveHDFSFolder.DIRECTORY, "/target//path/");
        runner.enqueue(new byte[0]);
        runner.run();

        Assert.assertEquals(1, runner.getFlowFilesForRelationship(RemoveHDFSFolder.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(RemoveHDFSFolder.REL_SUCCESS).size());
        Mockito.verifyZeroInteractions(fileSystem);
    }

    /**
     * Verify failure for folders with a relative path.
     */
    @Test
    public void testRemoveFolderWithRelativePath() throws Exception {
        runner.setProperty(RemoveHDFSFolder.DIRECTORY, "target/");
        runner.enqueue(new byte[0]);
        runner.run();

        Assert.assertEquals(1, runner.getFlowFilesForRelationship(RemoveHDFSFolder.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(RemoveHDFSFolder.REL_SUCCESS).size());
        Mockito.verifyZeroInteractions(fileSystem);
    }

    /**
     * Verify failure for missing trailing slash.
     */
    @Test
    public void testRemoveFolderWithTrailingSlash() throws Exception {
        runner.setProperty(RemoveHDFSFolder.DIRECTORY, "/target");
        runner.enqueue(new byte[0]);
        runner.run();

        Assert.assertEquals(1, runner.getFlowFilesForRelationship(RemoveHDFSFolder.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(RemoveHDFSFolder.REL_SUCCESS).size());
        Mockito.verifyZeroInteractions(fileSystem);
    }

    /**
     * Enqueues a {@code FlowFile} and validates its properties.
     *
     * @param runner the test runner
     * @return the validation results
     */
    @Nonnull
    private Collection<ValidationResult> validate(@Nonnull final TestRunner runner) {
        runner.enqueue(new byte[0]);
        return ((MockProcessContext) runner.getProcessContext()).validate();
    }

    /**
     * A mock {@code RemoveHDFSFolder} for testing.
     */
    private class TestableRemoveHDFSFolder extends RemoveHDFSFolder {

        @Nullable
        @Override
        protected FileSystem getFileSystem(@Nonnull ProcessContext context) {
            return fileSystem;
        }

        @Override
        HdfsResources resetHDFSResources(String configResources, String dir, ProcessContext context) throws IOException {
            return null;
        }
    }
}
