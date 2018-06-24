package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.controllerservice;

/*-
 * #%L
 * nifi-teradata-tdch-core
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

import com.thinkbiganalytics.kylo.nifi.teradata.tdch.api.TdchConnectionService;

import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link StandardTdchConnectionService}
 */
public class StandardTdchConnectionService_Set1_Test {

    private static final Logger log = LoggerFactory.getLogger(StandardTdchConnectionService_Set1_Test.class);
    private static final String DATABASE_SPEC_IN_CONNECTION_URL = "/database=mydb";
    private static final String CONNECTION_SERVICE_ID = "std-tdch-conn-service";

    @Test
    public void testJdbcDriverClass() throws InitializationException {
        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);
        final TdchConnectionService standardTdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, standardTdchConnectionService);
        runner.assertValid(standardTdchConnectionService);

        //JDBC Driver Class
        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.JDBC_DRIVER_CLASS_NAME, "");
        Assert.assertEquals(StandardTdchConnectionService.JDBC_DRIVER_CLASS_NAME.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.JDBC_DRIVER_CLASS_NAME, StandardTdchConnectionService.DEFAULT_JDBC_DRIVER_CLASS);
        Assert.assertEquals(StandardTdchConnectionService.JDBC_DRIVER_CLASS_NAME.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);
    }

    @Test
    public void testJdbcConnectionUrl() throws InitializationException {
        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);
        final TdchConnectionService standardTdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, standardTdchConnectionService);
        runner.assertValid(standardTdchConnectionService);

        //JDBC Connection URL
        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.JDBC_CONNECTION_URL, "");
        Assert.assertEquals(StandardTdchConnectionService.JDBC_CONNECTION_URL.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        runner.assertNotValid(standardTdchConnectionService);

        validationResult =
            runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.JDBC_CONNECTION_URL,
                               StandardTdchConnectionService.DEFAULT_JDBC_CONNECTION_URL + DATABASE_SPEC_IN_CONNECTION_URL);
        Assert.assertEquals(StandardTdchConnectionService.JDBC_CONNECTION_URL.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.JDBC_CONNECTION_URL, StandardTdchConnectionService.DEFAULT_JDBC_CONNECTION_URL);
        Assert.assertEquals(StandardTdchConnectionService.JDBC_CONNECTION_URL.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);
    }

    @Test
    public void testUsername() throws InitializationException {
        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);
        final TdchConnectionService standardTdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, standardTdchConnectionService);
        runner.assertValid(standardTdchConnectionService);

        //Username
        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.USERNAME, "");
        Assert.assertEquals(StandardTdchConnectionService.USERNAME.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.USERNAME, StandardTdchConnectionService.DEFAULT_USERNAME);
        Assert.assertEquals(StandardTdchConnectionService.USERNAME.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);
    }

    @Test
    public void testPassword() throws InitializationException {
        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);
        final TdchConnectionService standardTdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, standardTdchConnectionService);
        runner.assertValid(standardTdchConnectionService);

        //Password
        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.PASSWORD, "");
        Assert.assertEquals(StandardTdchConnectionService.PASSWORD.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.PASSWORD, "mypwd");
        Assert.assertEquals(StandardTdchConnectionService.PASSWORD.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);

    }
}
