/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.v2.ingest;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertTrue;


public class StripHeaderTest {

    final String originalFilename = "stripheader.txt";
    final Path dataPath = Paths.get("src/test/resources/");
    final Path file = dataPath.resolve(originalFilename);
    final Path emptyFile = dataPath.resolve("empty.txt");
    final Path headerFile = dataPath.resolve("header.txt");

    @Test
    public void testStandardRoutes() throws IOException {
        final TestRunner runner = TestRunners.newTestRunner(new StripHeader());
        runner.setProperty(StripHeader.HEADER_LINE_COUNT, "1");
        runner.setProperty(StripHeader.ENABLED, "true");

        runner.enqueue(file);
        runner.run();
        runner.assertTransferCount(StripHeader.REL_CONTENT, 1);
        runner.assertTransferCount(StripHeader.REL_HEADER, 1);
        runner.assertTransferCount(StripHeader.REL_ORIGINAL, 1);
        List<MockFlowFile> headerFlows = runner.getFlowFilesForRelationship(StripHeader.REL_HEADER);
        headerFlows.get(0).assertContentEquals("name,phone,zip\n");

        List<MockFlowFile> contentFlows = runner.getFlowFilesForRelationship(StripHeader.REL_CONTENT);
        String content = new String(contentFlows.get(0).toByteArray());
        assertTrue(content.startsWith("Joe") && content.endsWith("94550\n"));


        List<MockFlowFile> originalFlows = runner.getFlowFilesForRelationship(StripHeader.REL_ORIGINAL);
        String originalContent = new String(originalFlows.get(0).toByteArray());
        assertTrue(originalContent.startsWith("name") && originalContent.endsWith("94550\n"));
    }

    @Test
    public void testSplit2RowsInHeader() throws IOException {
        final TestRunner runner = TestRunners.newTestRunner(new StripHeader());
        runner.setProperty(StripHeader.HEADER_LINE_COUNT, "2");
        runner.setProperty(StripHeader.ENABLED, "true");

        runner.enqueue(file);
        runner.run();
        runner.assertTransferCount(StripHeader.REL_CONTENT, 1);
        runner.assertTransferCount(StripHeader.REL_HEADER, 1);
        runner.assertTransferCount(StripHeader.REL_ORIGINAL, 1);
        List<MockFlowFile> headerFlows = runner.getFlowFilesForRelationship(StripHeader.REL_HEADER);
        headerFlows.get(0).assertContentEquals("name,phone,zip\nJoe,phone,95121\n");
        List<MockFlowFile> contentFlows = runner.getFlowFilesForRelationship(StripHeader.REL_CONTENT);
        String content = new String(contentFlows.get(0).toByteArray());
        assertTrue(content.startsWith("Sally") && content.endsWith("94550\n"));

        List<MockFlowFile> originalFlows = runner.getFlowFilesForRelationship(StripHeader.REL_ORIGINAL);
        String originalContent = new String(originalFlows.get(0).toByteArray());
        assertTrue(originalContent.startsWith("name") && originalContent.endsWith("94550\n"));

    }

    @Test
    public void testFileWithOnlyHeader() throws IOException {
        final TestRunner runner = TestRunners.newTestRunner(new StripHeader());
        runner.setProperty(StripHeader.HEADER_LINE_COUNT, "1");
        runner.setProperty(StripHeader.ENABLED, "true");

        runner.enqueue(headerFile);
        runner.run();
        runner.assertTransferCount(StripHeader.REL_CONTENT, 1);
        runner.assertTransferCount(StripHeader.REL_HEADER, 1);
        runner.assertTransferCount(StripHeader.REL_ORIGINAL, 1);
        List<MockFlowFile> headerFlows = runner.getFlowFilesForRelationship(StripHeader.REL_HEADER);
        String s = new String(headerFlows.get(0).toByteArray());
        headerFlows.get(0).assertContentEquals("name,phone,zip");
    }

    @Test
    public void testEmptyFile() throws IOException {
        final TestRunner runner = TestRunners.newTestRunner(new StripHeader());
        runner.setProperty(StripHeader.HEADER_LINE_COUNT, "1");
        runner.setProperty(StripHeader.ENABLED, "true");

        runner.enqueue(emptyFile);
        runner.run();
        runner.assertTransferCount(StripHeader.REL_CONTENT, 1);
        runner.assertTransferCount(StripHeader.REL_HEADER, 0);
        runner.assertTransferCount(StripHeader.REL_ORIGINAL, 1);
    }

    @Test
    public void testRoutesToFailureIfHeaderLinesNotAllPresent() throws IOException {
        final TestRunner runner = TestRunners.newTestRunner(new StripHeader());
        runner.setProperty(StripHeader.HEADER_LINE_COUNT, "100");
        runner.setProperty(StripHeader.ENABLED, "true");

        runner.enqueue(file);
        runner.run();
        runner.assertAllFlowFilesTransferred(StripHeader.REL_FAILURE, 1);
    }

    @Test
    public void testBypass() throws IOException {
        final TestRunner runner = TestRunners.newTestRunner(new StripHeader());
        runner.setProperty(StripHeader.HEADER_LINE_COUNT, "1");
        runner.setProperty(StripHeader.ENABLED, "false");

        runner.enqueue(file);
        runner.run();
        runner.assertTransferCount(StripHeader.REL_CONTENT, 1);
        runner.assertTransferCount(StripHeader.REL_HEADER, 0);
        runner.assertTransferCount(StripHeader.REL_ORIGINAL, 1);

        List<MockFlowFile> originalFlows = runner.getFlowFilesForRelationship(StripHeader.REL_ORIGINAL);
        String originalContent = new String(originalFlows.get(0).toByteArray());
        assertTrue(originalContent.startsWith("name") && originalContent.endsWith("94550\n"));
    }


}
