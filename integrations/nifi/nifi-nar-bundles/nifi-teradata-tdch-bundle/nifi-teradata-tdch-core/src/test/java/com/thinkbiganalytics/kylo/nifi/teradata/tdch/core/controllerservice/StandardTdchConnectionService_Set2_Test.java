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
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests for {@link StandardTdchConnectionService}
 */
public class StandardTdchConnectionService_Set2_Test {

    private static final String CONNECTION_SERVICE_ID = "std-tdch-conn-service";
    private static final String MOCK_DIR = "/mock-dir/";

    private static final String EXPECTED_HDP_TDCH_LIBRARY_JARS_PATH =
        "/avro-1.7.5.test.dummy.jar,"
        + "/antlr-2.7.7.test.dummy.jar,"
        + "/antlr-runtime-3.4.test.dummy.jar,"
        + "/commons-dbcp-1.4.test.dummy.jar,"
        + "/commons-pool-1.5.4.test.dummy.jar,"
        + "/datanucleus-api-jdo-4.2.1.test.dummy.jar,"
        + "/datanucleus-core-4.1.6.test.dummy.jar,"
        + "/datanucleus-rdbms-4.1.7.test.dummy.jar,"
        + "/hive-cli-1.2.1000.2.5.3.0-37.test.dummy.jar,"
        + "/hive-exec-1.2.1000.2.5.3.0-37.test.dummy.jar,"
        + "/hive-jdbc-1.2.1000.2.5.3.0-37-standalone.test.dummy.jar,"
        + "/hive-metastore-1.2.1000.2.5.3.0-37.test.dummy.jar,"
        + "/jdo-api-3.0.1.test.dummy.jar,"
        + "/libfb303-0.9.3.test.dummy.jar,"
        + "/libthrift-0.9.3.test.dummy.jar,"
        + MOCK_DIR;

    private static final String EXPECTED_CDH_TDCH_LIBRARY_JARS_PATH =
        "/avro.test.dummy.jar,"
        + "/antlr-2.7.7.test.dummy.jar,"
        + "/antlr-runtime-3.4.test.dummy.jar,"
        + "/commons-dbcp-1.4.test.dummy.jar,"
        + "/commons-pool-1.5.4.test.dummy.jar,"
        + "/datanucleus-api-jdo-3.2.6.test.dummy.jar,"
        + "/datanucleus-core-3.2.10.test.dummy.jar,"
        + "/datanucleus-rdbms-3.2.9.test.dummy.jar,"
        + "/hive-cli-1.1.0-cdh5.14.0.test.dummy.jar,"
        + "/hive-exec-1.1.0-cdh5.14.0.test.dummy.jar,"
        + "/hive-jdbc-1.1.0-cdh5.14.0-standalone.test.dummy.jar,"
        + "/hive-metastore-1.1.0-cdh5.14.0.test.dummy.jar,"
        + "/jdo-api-3.0.1.test.dummy.jar,"
        + "/libfb303-0.9.3.test.dummy.jar,"
        + "/libthrift-0.9.3.test.dummy.jar,"
        + MOCK_DIR;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private void setupMockPaths(List<String> paths, File folder) {
        paths.forEach(path -> {
            try {
                if (!new File(folder, path).createNewFile()) {
                    throw new AssertionError("Failed to create file: " + path);
                }
            } catch (final IOException e) {
                throw new AssertionError("Failed to create file: " + path, e);
            }
        });
    }

    //checked
    @Test
    public void testStandardTdchConnectionServiceConfiguration() throws Exception {
        List<String> paths = Arrays.stream(EXPECTED_HDP_TDCH_LIBRARY_JARS_PATH.split(",")).map(String::trim).collect(Collectors.toList());

        StandardTdchConnectionService spiedStandardTdchConnectionService = Mockito.spy(StandardTdchConnectionService.class);
        Mockito.when(spiedStandardTdchConnectionService.getIdentifier()).thenReturn(CONNECTION_SERVICE_ID);

        setupMockPaths(paths, tempFolder.getRoot());
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);

        runner.addControllerService(CONNECTION_SERVICE_ID, spiedStandardTdchConnectionService);
        runner.assertNotValid(spiedStandardTdchConnectionService);
        runner.setProperty(spiedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, tempFolder.newFile("teradata-connector-1.5.jar").getAbsolutePath());
        runner.assertNotValid(spiedStandardTdchConnectionService);
        runner.setProperty(spiedStandardTdchConnectionService, StandardTdchConnectionService.HIVE_CONF_PATH, tempFolder.getRoot().getAbsolutePath());
        runner.assertNotValid(spiedStandardTdchConnectionService);
        runner.setProperty(spiedStandardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, tempFolder.getRoot().getAbsolutePath());
        runner.assertNotValid(spiedStandardTdchConnectionService);
        runner.setProperty(spiedStandardTdchConnectionService, StandardTdchConnectionService.PASSWORD, "mydbpwd");
        runner.assertValid(spiedStandardTdchConnectionService);
    }

    //checked
    @Test
    public void testHdpCdhLibraryJarsPathAndHadoopClasspathGeneration() throws Exception {
        File hiveLibPath = tempFolder.newFolder("hive-lib-path");
        File hiveConfPath = tempFolder.newFolder("hive-conf-path");

        setupMockPaths(Arrays.asList("hive-dep-001.jar", "hive-dep-002.jar", "hive-dep-003.jar"), hiveLibPath);

        StandardTdchConnectionService standardTdchConnectionService = new StandardTdchConnectionService();

        Set<String> hiveDependencies = new LinkedHashSet<>();
        hiveDependencies.add("hive-dep-001.jar");
        hiveDependencies.add("hive-dep-002.jar");
        hiveDependencies.add("hive-dep-003.jar");

        String expectedPath = hiveLibPath + "/hive-dep-001.jar,"
                              + hiveLibPath + "/hive-dep-002.jar,"
                              + hiveLibPath + "/hive-dep-003.jar,"
                              + hiveConfPath;
        String generatedPath = standardTdchConnectionService.generatePath(hiveLibPath.getAbsolutePath(), hiveConfPath.getAbsolutePath(), hiveDependencies,
                                                                          standardTdchConnectionService.getTdchLibraryJarsPathDelimiter());
        Assert.assertEquals(expectedPath, generatedPath);

        expectedPath = hiveLibPath + "/hive-dep-001.jar:"
                       + hiveLibPath + "/hive-dep-002.jar:"
                       + hiveLibPath + "/hive-dep-003.jar:"
                       + hiveConfPath;
        generatedPath = standardTdchConnectionService.generatePath(hiveLibPath.getAbsolutePath(), hiveConfPath.getAbsolutePath(), hiveDependencies,
                                                                   standardTdchConnectionService.getTdchHadoopClassPathDelimiter());
        Assert.assertEquals(expectedPath, generatedPath);
    }

    //checked
    @Test
    public void testHdpHiveLib() throws Exception {
        testHiveLib(EXPECTED_HDP_TDCH_LIBRARY_JARS_PATH);
    }

    //checked
    @Test
    public void testCdhHiveLib() throws Exception {
        testHiveLib(EXPECTED_CDH_TDCH_LIBRARY_JARS_PATH);
    }

    //checked
    private void testHiveLib(String expectedTdchLibraryJarsPath) throws Exception {
        List<String> paths = Arrays.stream(expectedTdchLibraryJarsPath.split(",")).map(String::trim).collect(Collectors.toList());

        StandardTdchConnectionService mockedStandardTdchConnectionService = Mockito.mock(StandardTdchConnectionService.class);
        Mockito.when(mockedStandardTdchConnectionService.getIdentifier()).thenReturn(CONNECTION_SERVICE_ID);

        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);

        runner.addControllerService(CONNECTION_SERVICE_ID, mockedStandardTdchConnectionService);
        runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.PASSWORD, "mydbpwd");

        for (int i = 0; i < paths.size(); i++) {
            final File currentFolder = tempFolder.newFolder(Integer.toString(i));
            setupMockPaths(paths.subList(0, i), currentFolder);
            validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, currentFolder.getAbsolutePath());
            if (i < paths.size() - 1) {
                Assert.assertFalse(validationResult.isValid());
                switch (i) {
                    case 0:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_AVRO_IDENTIFIER));
                        break;

                    case 1:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_ANTLR_IDENTIFIER));
                        break;

                    case 2:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_ANTLR_RUNTIME_IDENTIFIER));
                        break;

                    case 3:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_COMMONS_DBCP_IDENTIFIER));
                        break;

                    case 4:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_COMMONS_POOL_IDENTIFIER));
                        break;

                    case 5:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_DATANUCLEUS_API_JDO_IDENTIFIER));
                        break;

                    case 6:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_DATANUCLEUS_CORE_IDENTIFIER));
                        break;

                    case 7:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_DATANUCLEUS_RDBMS_IDENTIFIER));
                        break;

                    case 8:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_HIVE_CLI_IDENTIFIER));
                        break;

                    case 9:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_HIVE_EXEC_IDENTIFIER));
                        break;

                    case 10:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_STANDALONE_IDENTIFIER));
                        break;

                    case 11:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_HIVE_METASTORE_IDENTIFIER));
                        break;

                    case 12:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_JDO_API_IDENTIFIER));
                        break;

                    case 13:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_LIBFB303_IDENTIFIER));
                        break;

                    case 14:
                        Assert.assertTrue(validationResult.getExplanation().contains(StandardTdchConnectionService.HIVE_DEPENDENCY_LIBTHRIFT_IDENTIFIER));
                        break;

                    default:
                        Assert.fail("More paths than expected");
                        break;
                }
            } else {
                Assert.assertEquals(15, i);
                Assert.assertTrue(validationResult.isValid());
            }
        }
    }

    //checked
    @Test
    public void testTdchJarPath() throws Exception {
        StandardTdchConnectionService mockedStandardTdchConnectionService = Mockito.mock(StandardTdchConnectionService.class);
        Mockito.when(mockedStandardTdchConnectionService.getIdentifier()).thenReturn(CONNECTION_SERVICE_ID);

        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);

        runner.addControllerService(CONNECTION_SERVICE_ID, mockedStandardTdchConnectionService);
        runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.PASSWORD, "mydbpwd");

        validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, tempFolder.newFile("teradata-fail.jar").getAbsolutePath());
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());

        validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH,
                                              tempFolder.newFile("teradata-connector-1.4.jar").getAbsolutePath());
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());

        validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, tempFolder.getRoot().getAbsolutePath());
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());

        validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH,
                                              tempFolder.newFile("teradata-connector-1.5.jar").getAbsolutePath());
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());

        validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, tempFolder.newFile("my-teradata-connector.jar").getAbsolutePath());
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());
    }

    //checked
    @Test
    public void testHdpHiveConf_WithValidDirectory() throws Exception {
        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);
        final TdchConnectionService standardTdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, standardTdchConnectionService);
        runner.assertValid(standardTdchConnectionService);

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_CONF_PATH, tempFolder.getRoot().getAbsolutePath());
        Assert.assertEquals(StandardTdchConnectionService.HIVE_CONF_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);
    }
}
