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

import com.google.gson.Gson;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.MD5MD5CRC32FileChecksum;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MD5Hash;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.mockito.Matchers.any;

public class ComputeHDFSChecksumsTest {

    /**
     * Mock file system
     */
    private final FileSystem fileSystem = Mockito.mock(FileSystem.class);

    /**
     * Test runner
     */
    private final TestRunner runner = TestRunners.newTestRunner(new TestableComputeHDFSChecksums());

    private final String fileEntry = "{\n" +
                                     "\"name\": \"%s\",\n" +
                                     "\"size\": 131665,\n" +
                                     "\"checksum\": {\n" +
                                     "\"length\": 28,\n" +
                                     "\"value\": \"%s\",\n" +
                                     "\"algorithm\": \"MD5-of-0MD5-of-512CRC32C\"\n" +
                                     "}\n" +
                                     "}";

    /**
     * Initialize instance variables
     */
    @Before
    public void setUp() throws Exception {
        // Setup mock file system
        Mockito.when(fileSystem.delete(any(Path.class), Mockito.eq(true))).thenReturn(true);

        // Setup test runner
        runner.setValidateExpressionUsage(false);
    }

    /**
     * Verify required properties.
     */
    @Test
    public void testValidators() {
        String files = ComputeHDFSChecksums.FILES.getName();

        // Test with no properties
        Collection<ValidationResult> results = validate(runner);
        Assert.assertEquals(1, results.size());
        results.forEach((ValidationResult result) -> Assert.assertEquals(
            String.format("'%s' is invalid because %s is required", files, files), result.toString()));

        // Test with required properties present
        runner.setProperty(ComputeHDFSChecksums.FILES, "[]");
        results = validate(runner);
        Assert.assertEquals(0, results.size());

        // Test with additional property DIRECTORY set
        runner.setProperty(ComputeHDFSChecksums.DIRECTORY, "/dropzone");
        results = validate(runner);
        Assert.assertEquals(0, results.size());
    }

    @Test
    public void testFilesListAttributeNotJSON() {

        runner.setProperty(ComputeHDFSChecksums.FILES, "a");
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_SUCCESS).size());

        // Check file system calls
        InOrder inOrder = Mockito.inOrder(fileSystem);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFileListAttributeNotArray() {
        runner.setProperty(ComputeHDFSChecksums.FILES, "{}");
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_SUCCESS).size());

        // Check file system calls
        InOrder inOrder = Mockito.inOrder(fileSystem);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFileListAttributeEmtpy() {
        runner.setProperty(ComputeHDFSChecksums.FILES, "");
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_SUCCESS).size());

        // Check file system calls
        InOrder inOrder = Mockito.inOrder(fileSystem);
        inOrder.verifyNoMoreInteractions();
    }


    @Test
    public void testFileListAttributeEmptyArray() {
        runner.setProperty(ComputeHDFSChecksums.FILES, "[]");
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_SUCCESS).size());

        // Check file system calls
        InOrder inOrder = Mockito.inOrder(fileSystem);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testSingleFileInListDontFailOnWrongChecksum() throws Exception {
        String fileName = "000000_0";

        Mockito.doReturn(new MD5MD5CRC32FileChecksum(0, 512, new MD5Hash("112233445566778899aabbccddeeff00"))).
            when(fileSystem).getFileChecksum(any(Path.class));

        runner.setProperty(ComputeHDFSChecksums.FAIL_IF_INCORRECT_CHECKSUM, "False");
        runner.setProperty(ComputeHDFSChecksums.FILES, String.format("[" + fileEntry + "]", fileName,
                                                                     "AAACAAAAAAAAAAAArRnBpxcZ9ze14XqfLMB4yA=="));
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_SUCCESS).size());

        // Check whether checksum was passed correctly to attributes
        String filesJSON = runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_SUCCESS).get(0).
            getAttribute("files");
        Gson jsonParser = new Gson();
        ComputeHDFSChecksums.File[] files = jsonParser.fromJson(filesJSON, ComputeHDFSChecksums.File[].class);
        Assert.assertEquals(files[0].getComputedChecksum().getValue(), "AAAAAAAAAAAAAAIAESIzRFVmd4iZqrvM3e7/AAAAAAA=");

        // Check file system calls
        verifyGetFileChecksumCall(fileName);
    }

    @Test
    public void testSingleFileInListFailOnWrongChecksum() throws Exception {
        String fileName = "000000_0";

        Mockito.doReturn(new MD5MD5CRC32FileChecksum(0, 512, new MD5Hash("112233445566778899aabbccddeeff00"))).
            when(fileSystem).getFileChecksum(any(Path.class));

        runner.setProperty(ComputeHDFSChecksums.FAIL_IF_INCORRECT_CHECKSUM, "True");
        runner.setProperty(ComputeHDFSChecksums.FILES, String.format("[" + fileEntry + "]", fileName,
                                                                     "AAACAAAAAAAAAAAArRnBpxcZ9ze14XqfLMB4yA=="));
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_SUCCESS).size());

        // Check file system calls
        verifyGetFileChecksumCall(fileName);
    }

    @Test
    public void testSingleFileProperChecksum() throws Exception {
        String fileName = "000000_0";

        Mockito.doReturn(new MD5MD5CRC32FileChecksum(0, 512, new MD5Hash("112233445566778899aabbccddeeff00"))).
            when(fileSystem).getFileChecksum(any(Path.class));

        runner.setProperty(ComputeHDFSChecksums.FAIL_IF_INCORRECT_CHECKSUM, "True");
        runner.setProperty(ComputeHDFSChecksums.FILES, String.format("[" + fileEntry + "]", fileName,
                                                                     "AAAAAAAAAAAAAAIAESIzRFVmd4iZqrvM3e7/AAAAAAA="));
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_SUCCESS).size());

        // Check file system calls
        verifyGetFileChecksumCall(fileName);
    }

    @Test
    public void testMultipleFilesFailOnSingleWrongChecksum() throws Exception {
        String fileName = "000000_0";
        String fileName2 = "000000_1";
        String fileName3 = "000000_2";

        Mockito.when(fileSystem.getFileChecksum(any(Path.class))).
            thenReturn(new MD5MD5CRC32FileChecksum(0, 512, new MD5Hash("112233445566778899aabbccddeeff00")))
            .thenReturn(new MD5MD5CRC32FileChecksum(0, 512, new MD5Hash("112233445566778899aabbccddeeff01")))
            .thenReturn(new MD5MD5CRC32FileChecksum(0, 512, new MD5Hash("112233445566778899aabbccddeeff02")));

        runner.setProperty(ComputeHDFSChecksums.FAIL_IF_INCORRECT_CHECKSUM, "True");
        runner.setProperty(ComputeHDFSChecksums.FILES,
                           String.format("[" + fileEntry + "," + fileEntry + "," + fileEntry + "]",
                                         fileName, "AAAAAAAAAAAAAAIAESIzRFVmd4iZqrvM3e7/AAAAAAA=",
                                         fileName2, "AAAAAAAAAAAAAAIAESIzRFVmd4iZqrvM3e7/AAAAAAA=",
                                         fileName3, "AAAAAAAAAAAAAAIAESIzRFVmd4iZqrvM3e7/AgAAAAA="));
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_SUCCESS).size());

        // Check file system calls
        InOrder inOrder = Mockito.inOrder(fileSystem);
        inOrder.verify(fileSystem).getFileChecksum(new Path(fileName));
        inOrder.verify(fileSystem).getFileChecksum(new Path(fileName2));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testMultipleFilesWithDirectoryDefined() throws Exception {
        String fileName = "000000_0";
        String fileName2 = "000000_1";
        String fileName3 = "000000_2";
        String directory = "/dropzone";

        Mockito.when(fileSystem.getFileChecksum(any(Path.class))).
            thenReturn(new MD5MD5CRC32FileChecksum(0, 512, new MD5Hash("112233445566778899aabbccddeeff00")))
            .thenReturn(new MD5MD5CRC32FileChecksum(0, 512, new MD5Hash("112233445566778899aabbccddeeff01")))
            .thenReturn(new MD5MD5CRC32FileChecksum(0, 512, new MD5Hash("112233445566778899aabbccddeeff02")));

        runner.setProperty(ComputeHDFSChecksums.DIRECTORY, directory);
        runner.setProperty(ComputeHDFSChecksums.FAIL_IF_INCORRECT_CHECKSUM, "True");
        runner.setProperty(ComputeHDFSChecksums.FILES,
                           String.format("[" + fileEntry + "," + fileEntry + "," + fileEntry + "]",
                                         fileName, "AAAAAAAAAAAAAAIAESIzRFVmd4iZqrvM3e7/AAAAAAA=",
                                         fileName2, "AAAAAAAAAAAAAAIAESIzRFVmd4iZqrvM3e7/AQAAAAA=",
                                         fileName3, "AAAAAAAAAAAAAAIAESIzRFVmd4iZqrvM3e7/AgAAAAA="));
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_SUCCESS).size());

        // Check file system calls
        InOrder inOrder = Mockito.inOrder(fileSystem);
        inOrder.verify(fileSystem).getFileChecksum(new Path(directory, fileName));
        inOrder.verify(fileSystem).getFileChecksum(new Path(directory, fileName2));
        inOrder.verify(fileSystem).getFileChecksum(new Path(directory, fileName3));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testFileNotFoundException() throws Exception {
        String fileName = "000000_0";

        Mockito.doThrow(new FileNotFoundException()).when(fileSystem).getFileChecksum(any(Path.class));

        runner.setProperty(ComputeHDFSChecksums.FAIL_IF_INCORRECT_CHECKSUM, "True");
        runner.setProperty(ComputeHDFSChecksums.FILES, String.format("[" + fileEntry + "]", fileName,
                                                                     "AAAAAAAAAAAAAAIAESIzRFVmd4iZqrvM3e7/AAAAAAA="));
        runner.enqueue(new byte[0]);
        runner.run();

        // Check relationships
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(ComputeHDFSChecksums.REL_SUCCESS).size());

        // Check file system calls
        verifyGetFileChecksumCall(fileName);
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

    private void verifyGetFileChecksumCall(String fileName) throws Exception {
        InOrder inOrder = Mockito.inOrder(fileSystem);
        inOrder.verify(fileSystem).getFileChecksum(new Path(fileName));
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * A mock {@code ComputeHDFSChecksums} for testing.
     */
    private class TestableComputeHDFSChecksums extends ComputeHDFSChecksums {

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
