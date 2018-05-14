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
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link StandardTdchConnectionService}
 */
public class StandardTdchConnectionServiceTest {

    private static final Logger log = LoggerFactory.getLogger(StandardTdchConnectionServiceTest.class);
    private static final String TDCH_JAR_BASE = "src/test/resources/usr/lib/tdch/1.5/lib";
    private static final String TDCH_JAR_PATH = TDCH_JAR_BASE + "/valid/teradata-connector-dummy-v1.txt";
    private static final String COMMON_HDP_CDH_HIVE_CLIENT_CONF_DIR = "src/test/resources/usr/hdp/current_v_2_5/hive-client/conf";

    private static final String HDP_HIVE_CLIENT_LIB_BASE = "src/test/resources/usr/hdp/current_v_2_5/hive-client/lib";
    private static final String HDP_HIVE_CLIENT_LIB_DIR = HDP_HIVE_CLIENT_LIB_BASE + "/all";

    private static final String CDH_HIVE_CLIENT_LIB_BASE = "src/test/resources/cdh/lib/hive/lib";
    private static final String CDH_HIVE_CLIENT_LIB_DIR = CDH_HIVE_CLIENT_LIB_BASE + "/all";

    private static final String DATABASE_SPEC_IN_CONNECTION_URL = "/database=mydb";

    private static final String CONNECTION_SERVICE_ID = "std-tdch-conn-service";


    @Ignore
    @Test
    public void testStandardTdchConnectionServiceConfiguration() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);
        final StandardTdchConnectionService standardTdchConnectionService = new StandardTdchConnectionService();

        //Set connection service in valid state
        runner.addControllerService(CONNECTION_SERVICE_ID, standardTdchConnectionService);
        runner.assertNotValid(standardTdchConnectionService);
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.PASSWORD, "mydbpwd");
        runner.assertNotValid(standardTdchConnectionService);
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, TDCH_JAR_PATH);
        runner.assertNotValid(standardTdchConnectionService);
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_CONF_PATH, COMMON_HDP_CDH_HIVE_CLIENT_CONF_DIR);
        runner.assertNotValid(standardTdchConnectionService);
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_DIR);
        runner.assertValid(standardTdchConnectionService);
    }

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

    @Ignore
    @Test
    public void testTdchJarPath() throws InitializationException {
        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);
        final TdchConnectionService standardTdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, standardTdchConnectionService);
        runner.assertValid(standardTdchConnectionService);

        //TDCH Jar Path
        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, TDCH_JAR_BASE + "/invalid/some-connector.txt");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, TDCH_JAR_BASE + "/valid/teradata-connector-dummy-v2.txt");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, TDCH_JAR_BASE + "/invalid/teradata-jdbc-con-dummy-1.5.2.txt");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, TDCH_JAR_BASE + "/valid/teradata-connector-dummy-v1.txt");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, TDCH_JAR_BASE + "/invalid/teradata-connector-v3.not-exist.txt");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, TDCH_JAR_BASE + "/valid/teradata-connector-dummy-v2.txt");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, "");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, TDCH_JAR_BASE + "/valid/teradata-connector-dummy-v2.txt");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);

    }

    @Ignore
    @Test
    public void testHdpHiveConf() throws InitializationException {
        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);
        final TdchConnectionService standardTdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, standardTdchConnectionService);
        runner.assertValid(standardTdchConnectionService);

        //Validating Hive Conf - HDP
        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_CONF_PATH, COMMON_HDP_CDH_HIVE_CLIENT_CONF_DIR + "-not-exist");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_CONF_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_CONF_PATH, COMMON_HDP_CDH_HIVE_CLIENT_CONF_DIR);
        Assert.assertEquals(StandardTdchConnectionService.HIVE_CONF_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_CONF_PATH, "");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_CONF_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_CONF_PATH, COMMON_HDP_CDH_HIVE_CLIENT_CONF_DIR);
        Assert.assertEquals(StandardTdchConnectionService.HIVE_CONF_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);
    }

    @Ignore
    @Test
    public void testHdpHiveLib() throws InitializationException {
        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);
        final StandardTdchConnectionService standardTdchConnectionService = new StandardTdchConnectionService();

        //Set connection service in valid state
        runner.addControllerService(CONNECTION_SERVICE_ID, standardTdchConnectionService);
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.PASSWORD, "mydbpwd");
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, TDCH_JAR_PATH);
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_CONF_PATH, COMMON_HDP_CDH_HIVE_CLIENT_CONF_DIR);
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_DIR);
        runner.assertValid(standardTdchConnectionService);

        //Validating Hive Lib - HDP
        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/0");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_AVRO_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/1");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_ANTLR_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/2");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_ANTLR_RUNTIME_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/3");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_COMMONS_DBCP_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/4");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_COMMONS_POOL_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/5");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_DATANUCLEUS_API_JDO_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/6");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_DATANUCLEUS_CORE_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/7");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_DATANUCLEUS_RDBMS_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/8");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_HIVE_CLI_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/9");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_HIVE_EXEC_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/10");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_STANDALONE_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/11");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_HIVE_METASTORE_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/12");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_JDO_API_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/13");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_LIBFB303_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_BASE + "/14");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_LIBTHRIFT_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_DIR);
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);

        runner.enableControllerService(standardTdchConnectionService);
        runner.assertValid(standardTdchConnectionService);

        String expectedHdpTdchLibraryJarsPath =
            HDP_HIVE_CLIENT_LIB_DIR + "/avro-1.7.5.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/antlr-2.7.7.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/antlr-runtime-3.4.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/antlr-runtime-3.4.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/commons-dbcp-1.4.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/commons-pool-1.5.4.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/datanucleus-api-jdo-4.2.1.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/datanucleus-core-4.1.6.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/datanucleus-rdbms-4.1.7.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/hive-cli-1.2.1000.2.5.3.0-37.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/hive-exec-1.2.1000.2.5.3.0-37.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/hive-jdbc-1.2.1000.2.5.3.0-37-standalone.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/hive-metastore-1.2.1000.2.5.3.0-37.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/jdo-api-3.0.1.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/libfb303-0.9.3.test.dummy.jar,"
            + HDP_HIVE_CLIENT_LIB_DIR + "/libthrift-0.9.3.test.dummy.jar,"
            + COMMON_HDP_CDH_HIVE_CLIENT_CONF_DIR;
        Assert.assertEquals(expectedHdpTdchLibraryJarsPath, standardTdchConnectionService.getTdchLibraryJarsPath());

        String expectedHdpTdchHadoopClassPath =
            HDP_HIVE_CLIENT_LIB_DIR + "/avro-1.7.5.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/antlr-2.7.7.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/antlr-runtime-3.4.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/antlr-runtime-3.4.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/commons-dbcp-1.4.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/commons-pool-1.5.4.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/datanucleus-api-jdo-4.2.1.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/datanucleus-core-4.1.6.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/datanucleus-rdbms-4.1.7.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/hive-cli-1.2.1000.2.5.3.0-37.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/hive-exec-1.2.1000.2.5.3.0-37.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/hive-jdbc-1.2.1000.2.5.3.0-37-standalone.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/hive-metastore-1.2.1000.2.5.3.0-37.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/jdo-api-3.0.1.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/libfb303-0.9.3.test.dummy.jar:"
            + HDP_HIVE_CLIENT_LIB_DIR + "/libthrift-0.9.3.test.dummy.jar:"
            + COMMON_HDP_CDH_HIVE_CLIENT_CONF_DIR;
        Assert.assertEquals(expectedHdpTdchHadoopClassPath, standardTdchConnectionService.getTdchHadoopClassPath());

        runner.disableControllerService(standardTdchConnectionService);
        runner.assertValid(standardTdchConnectionService);
    }

    @Ignore
    @Test
    public void testCdhHiveLib() throws InitializationException {
        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);
        final StandardTdchConnectionService standardTdchConnectionService = new StandardTdchConnectionService();

        //Set connection service in valid state
        runner.addControllerService(CONNECTION_SERVICE_ID, standardTdchConnectionService);
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.PASSWORD, "mydbpwd");
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, TDCH_JAR_PATH);
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_CONF_PATH, COMMON_HDP_CDH_HIVE_CLIENT_CONF_DIR);
        runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, HDP_HIVE_CLIENT_LIB_DIR);
        runner.assertValid(standardTdchConnectionService);

        //Validating Hive Lib - CDH
        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/0");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_AVRO_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/1");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_ANTLR_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/2");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_ANTLR_RUNTIME_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/3");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_COMMONS_DBCP_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/4");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_COMMONS_POOL_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/5");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_DATANUCLEUS_API_JDO_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/6");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_DATANUCLEUS_CORE_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/7");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_DATANUCLEUS_RDBMS_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/8");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_HIVE_CLI_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/9");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_HIVE_EXEC_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/10");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_STANDALONE_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/11");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_HIVE_METASTORE_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/12");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_JDO_API_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/13");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_LIBFB303_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_BASE + "/14");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_LIBTHRIFT_IDENTIFIER));
        runner.assertNotValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, CDH_HIVE_CLIENT_LIB_DIR);
        Assert.assertEquals(StandardTdchConnectionService.HIVE_LIB_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);

        runner.enableControllerService(standardTdchConnectionService);
        runner.assertValid(standardTdchConnectionService);

        String expectedCdhTdchLibraryJarsPath =
            CDH_HIVE_CLIENT_LIB_DIR + "/avro.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/antlr-2.7.7.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/antlr-runtime-3.4.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/antlr-runtime-3.4.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/commons-dbcp-1.4.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/commons-pool-1.5.4.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/datanucleus-api-jdo-3.2.6.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/datanucleus-core-3.2.10.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/datanucleus-rdbms-3.2.9.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/hive-cli-1.1.0-cdh5.14.0.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/hive-exec-1.1.0-cdh5.14.0.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/hive-jdbc-1.1.0-cdh5.14.0-standalone.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/hive-metastore-1.1.0-cdh5.14.0.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/jdo-api-3.0.1.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/libfb303-0.9.3.test.dummy.jar,"
            + CDH_HIVE_CLIENT_LIB_DIR + "/libthrift-0.9.3.test.dummy.jar,"
            + COMMON_HDP_CDH_HIVE_CLIENT_CONF_DIR;
        Assert.assertEquals(expectedCdhTdchLibraryJarsPath, standardTdchConnectionService.getTdchLibraryJarsPath());

        String expectedCdhTdchHadoopClassPath =
            CDH_HIVE_CLIENT_LIB_DIR + "/avro.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/antlr-2.7.7.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/antlr-runtime-3.4.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/antlr-runtime-3.4.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/commons-dbcp-1.4.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/commons-pool-1.5.4.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/datanucleus-api-jdo-3.2.6.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/datanucleus-core-3.2.10.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/datanucleus-rdbms-3.2.9.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/hive-cli-1.1.0-cdh5.14.0.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/hive-exec-1.1.0-cdh5.14.0.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/hive-jdbc-1.1.0-cdh5.14.0-standalone.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/hive-metastore-1.1.0-cdh5.14.0.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/jdo-api-3.0.1.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/libfb303-0.9.3.test.dummy.jar:"
            + CDH_HIVE_CLIENT_LIB_DIR + "/libthrift-0.9.3.test.dummy.jar:"
            + COMMON_HDP_CDH_HIVE_CLIENT_CONF_DIR;
        Assert.assertEquals(expectedCdhTdchHadoopClassPath, standardTdchConnectionService.getTdchHadoopClassPath());
    }
}
