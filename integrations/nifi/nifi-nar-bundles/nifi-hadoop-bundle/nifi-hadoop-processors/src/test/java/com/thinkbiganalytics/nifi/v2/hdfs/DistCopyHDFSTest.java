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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.tools.DistCp;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DistCopyHDFSTest {

    /**
     * Mock file system and configuration
     */
    private final FileSystem fileSystem = Mockito.mock(FileSystem.class);
    private final Configuration configuration = Mockito.mock(Configuration.class);
    private final DistCp distCp = Mockito.mock(DistCp.class);
    private final Job job = Mockito.mock(Job.class);

    /**
     * Test runner
     */
    private final TestRunner runner = TestRunners.newTestRunner(new TestableDistCopyHDFS());

    private final String fileEntry = "{\n" +
                                     "\"name\": \"%s\"\n" +
                                     "}";

    /**
     * Initialize instance variables
     */
    @Before
    public void setUp() throws Exception {
        // Setup test runner
        runner.setValidateExpressionUsage(false);

        Mockito.when(distCp.execute()).thenReturn(job);
    }

    /**
     * Verify required properties.
     */
    @Test
    public void testValidators() {
        String destination = DistCopyHDFS.DESTINATION.getName();

        // Test with no properties
        Collection<ValidationResult> results = validate(runner);
        Assert.assertEquals(1, results.size());
        results.forEach((ValidationResult result) -> Assert.assertEquals(
            String.format("'%s' is invalid because %s is required", destination, destination), result.toString()));

        // Test with required properties present
        runner.setProperty(DistCopyHDFS.DESTINATION, "/dropzone");
        results = validate(runner);
        Assert.assertEquals(0, results.size());

        // Test with additional property SOURCE set
        runner.setProperty(DistCopyHDFS.SOURCE, "/var");
        results = validate(runner);
        Assert.assertEquals(0, results.size());
    }

    @Test
    public void testNoFilesNorSource() {

        runner.setProperty(DistCopyHDFS.DESTINATION, "/dropzone");
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(DistCopyHDFS.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(DistCopyHDFS.REL_SUCCESS).size());

        // Check distCp call
        InOrder inOrder = Mockito.inOrder(distCp);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testSourceSet() throws Exception {
        Path source = new Path("/var");
        Path destination = new Path("/dropzone");

        runner.setProperty(DistCopyHDFS.DESTINATION, destination.toString());
        runner.setProperty(DistCopyHDFS.SOURCE, source.toString());
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(DistCopyHDFS.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(DistCopyHDFS.REL_SUCCESS).size());

        // Check distCp call
        InOrder inOrder = Mockito.inOrder(distCp);
        inOrder.verify(distCp).execute();
        inOrder.verifyNoMoreInteractions();

        // Check final paths provided to DistCp
        TestableDistCopyHDFS proc = (TestableDistCopyHDFS) runner.getProcessor();
        Assert.assertEquals(proc.TEST_destination, destination);
        Assert.assertEquals(proc.TEST_pathsList, new ArrayList<>(Arrays.asList(source)));
    }

    @Test
    public void testSourceAndFilesSet() throws Exception {
        String source = "/var";
        Path destination = new Path("/dropzone");
        String file1 = "001";
        String file2 = "002";
        String files = String.format("[" + fileEntry + "," + fileEntry + "]", file1, file2);

        runner.setProperty(DistCopyHDFS.DESTINATION, destination.toString());
        runner.setProperty(DistCopyHDFS.SOURCE, source);
        runner.setProperty(DistCopyHDFS.FILES, files);
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(DistCopyHDFS.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(DistCopyHDFS.REL_SUCCESS).size());

        // Check distCp call
        InOrder inOrder = Mockito.inOrder(distCp);
        inOrder.verify(distCp).execute();
        inOrder.verifyNoMoreInteractions();

        // Check final paths provided to DistCp
        TestableDistCopyHDFS proc = (TestableDistCopyHDFS) runner.getProcessor();
        Assert.assertEquals(proc.TEST_destination, destination);
        Assert.assertEquals(proc.TEST_pathsList, new ArrayList<>(Arrays.asList(
            new Path(source, file1), new Path(source, file2))));
    }

    @Test
    public void testFilesSet() throws Exception {
        Path destination = new Path("/dropzone");
        String file1 = "/001";
        String file2 = "/002";
        String files = String.format("[" + fileEntry + "," + fileEntry + "]", file1, file2);

        runner.setProperty(DistCopyHDFS.DESTINATION, destination.toString());
        runner.setProperty(DistCopyHDFS.FILES, files);
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(DistCopyHDFS.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(DistCopyHDFS.REL_SUCCESS).size());

        // Check distCp call
        InOrder inOrder = Mockito.inOrder(distCp);
        inOrder.verify(distCp).execute();
        inOrder.verifyNoMoreInteractions();

        // Check final paths provided to DistCp
        TestableDistCopyHDFS proc = (TestableDistCopyHDFS) runner.getProcessor();
        Assert.assertEquals(proc.TEST_destination, destination);
        Assert.assertEquals(proc.TEST_pathsList, new ArrayList<>(Arrays.asList(
            new Path(file1), new Path(file2))));
    }

    @Test
    public void testFilesListAttributeNotJSON() {

        runner.setProperty(DistCopyHDFS.DESTINATION, "/dropzone");
        runner.setProperty(DistCopyHDFS.FILES, "a");
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(DistCopyHDFS.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(DistCopyHDFS.REL_SUCCESS).size());

        // Check file system calls
        InOrder inOrder = Mockito.inOrder(fileSystem);
        inOrder.verifyNoMoreInteractions();
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
     * A mock {@code DistCopyHDFS} for testing.
     */
    private class TestableDistCopyHDFS extends DistCopyHDFS {

        private List<Path> TEST_pathsList;
        private Path TEST_destination;

        @Nullable
        @Override
        protected FileSystem getFileSystem(@Nonnull ProcessContext context) {
            return fileSystem;
        }

        @Nullable
        @Override
        protected Configuration getConfiguration() {
            return configuration;
        }

        @Override
        protected DistCp getDistCp(List<Path> pathsList, Path destination) {
            TEST_pathsList = pathsList;
            TEST_destination = destination;
            return distCp;
        }

        @Override
        HdfsResources resetHDFSResources(String configResources, String dir, ProcessContext context) throws IOException {
            return null;
        }
    }
}
