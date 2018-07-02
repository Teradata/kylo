package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.utils;

/*-
 * #%L
 * kylo-nifi-teradata-tdch-core
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

import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TdchOperationType;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TdchProcessResult;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TestAbstractTdchProcessor;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.utils.TdchUtils;

import org.apache.nifi.util.LogMessage;
import org.apache.nifi.util.MockComponentLog;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests for class {@link TdchUtils}
 */
public class TdchUtilsTest {

    @Test
    public void testMaskTdchCredentials_MaskPassword() {
        TdchUtils tdchUtils = new TdchUtils();

        String tdchCommand = "This is a command -password password_value remaining parameters";
        String maskedTdchCommand = tdchUtils.maskTdchCredentials(tdchCommand);
        Assert.assertEquals("This is a command -password ***** remaining parameters",
                            maskedTdchCommand);
    }

    @Test
    public void testMaskTdchCredentials_MaskingNotApplicable() {
        TdchUtils tdchUtils = new TdchUtils();

        String tdchCommand = "This is a command with no -my_key my_value parameters to mask";
        String maskedTdchCommand = tdchUtils.maskTdchCredentials(tdchCommand);
        Assert.assertEquals("This is a command with no -my_key my_value parameters to mask",
                            maskedTdchCommand);
    }

    @Test
    public void testGetExportHiveToTeradataInputRecordsCount_ValidCountAndSuccessExitCode() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataInputRecordsCount(getTdchProcessResultWithInputRecordCountAndSuccessExitCodeInfo(), componentLog);
        Assert.assertEquals(6, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(0, warnMessages.size());
    }

    @Test
    public void testGetExportHiveToTeradataInputRecordsCount_ValidCountAndFailureExitCode() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataInputRecordsCount(getTdchProcessResultWithInputRecordCountAndFailureExitCodeInfo(), componentLog);
        Assert.assertEquals(-1, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve number of input records"));
    }

    @Test
    public void testGetExportHiveToTeradataInputRecordsCount_NoRecordCount() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataInputRecordsCount(getTdchProcessResultWithNoInputRecordCountInfo(), componentLog);
        Assert.assertEquals(-1, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve number of input records"));
    }

    @Test
    public void testGetExportHiveToTeradataInputRecordsCount_EmptyRecordCount() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataInputRecordsCount(getTdchProcessResultWithEmptyInputRecordCountInfo(), componentLog);
        Assert.assertEquals(-1, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve number of input records"));
    }

    @Test
    public void testGetExportHiveToTeradataInputRecordsCount_InvalidRecordCountShort() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataInputRecordsCount(getTdchProcessResultWithInvalidInputRecordCountShortLineInfo(), componentLog);
        Assert.assertEquals(-1, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().contains("Skipping attempt to retrieve number of input records"));
    }

    @Test
    public void testGetExportHiveToTeradataInputRecordsCount_InvalidRecordCountLong() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataInputRecordsCount(getTdchProcessResultWithInvalidInputRecordCountLongLineInfo(), componentLog);
        Assert.assertEquals(-1, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().contains("Unable to parse number of input records processed"));
    }

    @Test
    public void testGetExportHiveToTeradataOutputRecordsCount_ValidCountAndSuccessExitCode() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataOutputRecordsCount(getTdchProcessResultWithOutputRecordCountAndSuccessExitCodeInfo(), componentLog);
        Assert.assertEquals(5, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(0, warnMessages.size());
    }

    @Test
    public void testGetExportHiveToTeradataOutputRecordsCount_ValidCountAndFailureExitCode() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataOutputRecordsCount(getTdchProcessResultWithOutputRecordCountAndFailureExitCodeInfo(), componentLog);
        Assert.assertEquals(-1, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve number of output records"));
    }

    @Test
    public void testGetExportHiveToTeradataOutputRecordsCount_NoRecordCount() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataOutputRecordsCount(getTdchProcessResultWithNoOutputRecordCountInfo(), componentLog);
        Assert.assertEquals(-1, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve number of output records"));
    }

    @Test
    public void testGetExportHiveToTeradataOutputRecordsCount_EmptyRecordCount() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataOutputRecordsCount(getTdchProcessResultWithEmptyOutputRecordCountInfo(), componentLog);
        Assert.assertEquals(-1, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve number of output records"));
    }

    @Test
    public void testGetExportHiveToTeradataOutputRecordsCount_InvalidRecordCountShort() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataOutputRecordsCount(getTdchProcessResultWithInvalidOutputRecordCountShortLineInfo(), componentLog);
        Assert.assertEquals(-1, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().contains("Skipping attempt to retrieve number of output records"));
    }

    @Test
    public void testGetExportHiveToTeradataOutputRecordsCount_InvalidRecordCountLong() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long count = tdchUtils.getExportHiveToTeradataOutputRecordsCount(getTdchProcessResultWithInvalidOutputRecordCountLongLineInfo(), componentLog);
        Assert.assertEquals(-1, count);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().contains("Unable to parse number of output records processed"));
    }

    @Test
    public void testGetExportHiveToTeradataJobExitCode_SuccessCode() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long code = tdchUtils.getExportHiveToTeradataJobExitCode(getTdchProcessResultWithSuccessExitCode(), componentLog);
        Assert.assertEquals(0, code);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(0, warnMessages.size());
    }

    @Test
    public void testGetExportHiveToTeradataJobExitCode_FailureCode() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long code = tdchUtils.getExportHiveToTeradataJobExitCode(getTdchProcessResultWithFailureExitCode(), componentLog);
        Assert.assertEquals(-1, code);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve TDCH exit code"));
    }

    @Test
    public void testGetExportHiveToTeradataJobExitCode_NoCode() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long code = tdchUtils.getExportHiveToTeradataJobExitCode(getTdchProcessResultWithNoExitCode(), componentLog);
        Assert.assertEquals(-1, code);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve TDCH exit code"));
    }

    @Test
    public void testGetExportHiveToTeradataJobExitCode_EmptyCode() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long code = tdchUtils.getExportHiveToTeradataJobExitCode(getTdchProcessResultWithEmptyExitCode(), componentLog);
        Assert.assertEquals(-1, code);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve TDCH exit code"));
    }

    @Test
    public void testGetExportHiveToTeradataJobExitCode_InvalidCodeShort() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long code = tdchUtils.getExportHiveToTeradataJobExitCode(getTdchProcessResultWithShortInvalidExitCode(), componentLog);
        Assert.assertEquals(-1, code);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve TDCH exit code"));
    }

    @Test
    public void testGetExportHiveToTeradataJobExitCode_InvalidCodeLong() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        long code = tdchUtils.getExportHiveToTeradataJobExitCode(getTdchProcessResultWithLongInvalidExitCode(), componentLog);
        Assert.assertEquals(-1, code);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().contains("Unable to parse TDCH exit code"));
    }

    @Test
    public void testGetExportHiveToTeradataJobTimeTaken_ValidTimeAndSuccessExitCode() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        String timeTaken = tdchUtils.getExportHiveToTeradataJobTimeTaken(getTdchProcessResultWithTimeTakenAndSuccessExitCode(), componentLog);
        Assert.assertEquals("33s", timeTaken);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(0, warnMessages.size());
    }

    @Test
    public void testGetExportHiveToTeradataJobTimeTaken_ValidTimeAndFailureExitCode() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        String timeTaken = tdchUtils.getExportHiveToTeradataJobTimeTaken(getTdchProcessResultWithTimeTakenAndFailureExitCode(), componentLog);
        Assert.assertEquals("Unable to determine time taken", timeTaken);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve time taken by TDCH job"));
    }

    @Test
    public void testGetExportHiveToTeradataJobTimeTaken_NoTimeTaken() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        String timeTaken = tdchUtils.getExportHiveToTeradataJobTimeTaken(getTdchProcessResultWithNoTimeTaken(), componentLog);
        Assert.assertEquals("Unable to determine time taken", timeTaken);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve time taken by TDCH job"));
    }

    @Test
    public void testGetExportHiveToTeradataJobTimeTaken_EmptyTimeTaken() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        String timeTaken = tdchUtils.getExportHiveToTeradataJobTimeTaken(getTdchProcessResultWithEmptyTimeTaken(), componentLog);
        Assert.assertEquals("Unable to determine time taken", timeTaken);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve time taken by TDCH job"));
    }

    @Test
    public void testGetExportHiveToTeradataJobTimeTaken_InvalidTimeTakenShort() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        String timeTaken = tdchUtils.getExportHiveToTeradataJobTimeTaken(getTdchProcessResultWithShortInvalidTimeTaken(), componentLog);
        Assert.assertEquals("Unable to determine time taken", timeTaken);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Skipping attempt to retrieve time taken by TDCH job"));
    }

    @Test
    public void testGetExportHiveToTeradataJobTimeTaken_InvalidTimeTakenLong() {
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        TdchUtils tdchUtils = new TdchUtils();
        String timeTaken = tdchUtils.getExportHiveToTeradataJobTimeTaken(getTdchProcessResultWithLongInvalidTimeTaken(), componentLog);
        Assert.assertEquals("Unable to determine time taken", timeTaken);
        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("Unable to get valid value for time taken by TDCH job"));
    }

    private static TdchProcessResult getTdchProcessResultWithInputRecordCountAndSuccessExitCodeInfo() {
        return getTdchProcessResultWithSuccessExitCode();
    }

    private static TdchProcessResult getTdchProcessResultWithInputRecordCountAndFailureExitCodeInfo() {
        return getTdchProcessResultWithFailureExitCode();
    }

    private static TdchProcessResult getTdchProcessResultWithNoInputRecordCountInfo() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = null;
        logLines[1] = "     Map output records=5";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithEmptyInputRecordCountInfo() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "";
        logLines[1] = "     Map output records=5";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithInvalidInputRecordCountShortLineInfo() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "No input rec";
        logLines[1] = "     Map output records=5";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithInvalidInputRecordCountLongLineInfo() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "No input records found in log";
        logLines[1] = "     Map output records=5";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithOutputRecordCountAndSuccessExitCodeInfo() {
        return getTdchProcessResultWithSuccessExitCode();
    }

    private static TdchProcessResult getTdchProcessResultWithOutputRecordCountAndFailureExitCodeInfo() {
        return getTdchProcessResultWithFailureExitCode();
    }

    private static TdchProcessResult getTdchProcessResultWithNoOutputRecordCountInfo() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = null;
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithEmptyOutputRecordCountInfo() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithInvalidOutputRecordCountShortLineInfo() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "No output rec";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithInvalidOutputRecordCountLongLineInfo() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "No output records found in log";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithSuccessExitCode() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "     Map output records=5";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithFailureExitCode() {
        int exitValue = -1;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "     Map output records=5";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithNoExitCode() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "     Map output records=5";
        logLines[2] = null;
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithEmptyExitCode() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "     Map output records=5";
        logLines[2] = "";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithShortInvalidExitCode() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "     Map output records=5";
        logLines[2] = "Not valid exit code";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithLongInvalidExitCode() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "     Map output records=5";
        logLines[2] = "This log line does not contain a valid exit code from the run of the TDCH job";
        logLines[3] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: ConnectorExportTool time is 33s";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithTimeTakenAndSuccessExitCode() {
        return getTdchProcessResultWithSuccessExitCode();
    }

    private static TdchProcessResult getTdchProcessResultWithTimeTakenAndFailureExitCode() {
        return getTdchProcessResultWithFailureExitCode();
    }

    private static TdchProcessResult getTdchProcessResultWithNoTimeTaken() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "     Map output records=5";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = null;
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithEmptyTimeTaken() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "     Map output records=5";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithShortInvalidTimeTaken() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "     Map output records=5";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "Not valid time taken";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    private static TdchProcessResult getTdchProcessResultWithLongInvalidTimeTaken() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[4];
        logLines[0] = "     Map input records=6";
        logLines[1] = "     Map output records=5";
        logLines[2] = "18/03/21 05:43:24 INFO tool.ConnectorExportTool: job completed with exit code 0";
        logLines[3] = "This log line does not contain a valid time taken from the run of the TDCH job";
        return new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }
}
