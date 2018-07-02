package com.thinkbiganalytics.nifi.v2.sqoop;

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

import com.thinkbiganalytics.nifi.v2.sqoop.core.ImportSqoop;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.MockProcessContext;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

public class ImportSqoopTest {


    /**
     * Test runner
     */
    private final TestRunner runner = TestRunners.newTestRunner(ImportSqoop.class);

    @Before
    public void setUp() throws Exception {
        MockSqoopConnectionService mockSqoopConnectionService = new MockSqoopConnectionService();
        // Setup test runner
        runner.setValidateExpressionUsage(false);
        runner.addControllerService("sqoop-connection-service", mockSqoopConnectionService);

        runner.enableControllerService(mockSqoopConnectionService);

    }

    @Test
    public void testImportSqoopCommand() {
        String systemProperties = "-Dsystem.prop1=prop1 -Dsystem.prop2=prop2 ";
        String additionalProperties = "--arg1=test1 --arg=test2";
        runner.setProperty(ImportSqoop.SQOOP_SYSTEM_PROPERTIES, systemProperties);
        runner.setProperty(ImportSqoop.TARGET_COMPRESSION_ALGORITHM, "SNAPPY");
        runner.setProperty(ImportSqoop.SQOOP_CONNECTION_SERVICE, "sqoop-connection-service");
        runner.setProperty(ImportSqoop.SQOOP_ADDITIONAL_ARGUMENTS, additionalProperties);
        runner.setProperty(ImportSqoop.SOURCE_TABLE_WHERE_CLAUSE, "where clause");
        runner.setProperty(ImportSqoop.SOURCE_LOAD_STRATEGY, "FULL_LOAD");
        runner.enqueue(new byte[0]);
        ProcessContext ctx = runner.getProcessContext();
        Collection<ValidationResult> validationResults = validate(runner);
        runner.run();

        //look at the failure results since we dont have sqoop running
        List<MockFlowFile> failResults = runner.getFlowFilesForRelationship(ImportSqoop.REL_FAILURE);

        MockFlowFile result = failResults.get(0);
        String sqoopCommand = result.getAttribute("sqoop.command.text");
        Assert.assertTrue(StringUtils.startsWithIgnoreCase(sqoopCommand, "sqoop import " + systemProperties));
        Assert.assertTrue(StringUtils.endsWithIgnoreCase(sqoopCommand.trim(), additionalProperties));

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


    private static class MockSqoopConnectionService extends AbstractControllerService implements SqoopConnectionService {

        @Override
        public String getConnectionString() {
            return "jdbc:connection.url";
        }

        @Override
        public String getUserName() {
            return "username";
        }

        @Override
        public PasswordMode getPasswordMode() {
            return PasswordMode.CLEAR_TEXT_ENTRY;
        }

        @Override
        public String getPasswordHdfsFile() {
            return null;
        }

        @Override
        public String getPasswordPassphrase() {
            return null;
        }

        @Override
        public String getEnteredPassword() {
            return "password";
        }

        @Override
        public String getConnectionManager() {
            return "connectionMgr";
        }

        @Override
        public String getDriver() {
            return "com.sqoop.driver";
        }
    }

    /**
     * Provides a stubbed processor instance for testing
     */
    public static class MockImportSqoop extends ImportSqoop {

        @Override
        public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
            // nothing to do
        }


    }


}
