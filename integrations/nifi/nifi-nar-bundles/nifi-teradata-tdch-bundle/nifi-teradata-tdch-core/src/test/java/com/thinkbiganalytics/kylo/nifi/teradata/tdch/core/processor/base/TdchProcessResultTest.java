package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base;

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

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for class {@link TdchProcessResult}
 */
public class TdchProcessResultTest {

    @Test
    public void testTdchProcessResult_NormalParameters() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = new String[2];
        logLines[0] = "This is the first log line";
        logLines[1] = "Here comes the second line of log";

        TdchProcessResult tdchProcessResult = new TdchProcessResult(exitValue, logLines, tdchOperationType);

        Assert.assertEquals(0, tdchProcessResult.getExitValue());
        Assert.assertEquals("TDCH_EXPORT", tdchProcessResult.getTdchOperationType().toString());

        String[] retrievedLogLines = tdchProcessResult.getLogLines();
        Assert.assertEquals(2, retrievedLogLines.length);

        retrievedLogLines[0] = "Changed first log line";

        Assert.assertEquals("This is the first log line", logLines[0]);
        Assert.assertEquals("Changed first log line", retrievedLogLines[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testTdchProcessResult_NullLogLines() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        String[] logLines = null;
        TdchProcessResult tdchProcessResult = new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }

    @Test(expected = NullPointerException.class)
    public void testTdchProcessResult_NullOperationType() {
        int exitValue = 0;
        TdchOperationType tdchOperationType = null;
        String[] logLines = new String[2];
        logLines[0] = "log line 0";
        logLines[1] = "log line 1";
        TdchProcessResult tdchProcessResult = new TdchProcessResult(exitValue, logLines, tdchOperationType);
    }
}
