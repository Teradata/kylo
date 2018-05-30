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
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.common.TdchValidations;

import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import static org.mockito.Matchers.any;

/**
 * Tests for {@link StandardTdchConnectionService}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TdchValidations.class,
                 StandardTdchConnectionService.class,
                 StandardValidators.FileExistsValidator.class,
                 StandardValidators.DirectoryExistsValidator.class,
                 Path.class})
public class StandardTdchConnectionService_Set2_Test {

    private static final Logger log = LoggerFactory.getLogger(StandardTdchConnectionService_Set2_Test.class);
    private static final String DATABASE_SPEC_IN_CONNECTION_URL = "/database=mydb";
    private static final String CONNECTION_SERVICE_ID = "std-tdch-conn-service";
    private static final String MOCK_DIR = "/mock-dir/";

    private final String expectedHdpTdchLibraryJarsPath =
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

    private String expectedHdpTdchHadoopClassPath =
        "/avro-1.7.5.test.dummy.jar:"
        + "/antlr-2.7.7.test.dummy.jar:"
        + "/antlr-runtime-3.4.test.dummy.jar:"
        + "/commons-dbcp-1.4.test.dummy.jar:"
        + "/commons-pool-1.5.4.test.dummy.jar:"
        + "/datanucleus-api-jdo-4.2.1.test.dummy.jar:"
        + "/datanucleus-core-4.1.6.test.dummy.jar:"
        + "/datanucleus-rdbms-4.1.7.test.dummy.jar:"
        + "/hive-cli-1.2.1000.2.5.3.0-37.test.dummy.jar:"
        + "/hive-exec-1.2.1000.2.5.3.0-37.test.dummy.jar:"
        + "/hive-jdbc-1.2.1000.2.5.3.0-37-standalone.test.dummy.jar:"
        + "/hive-metastore-1.2.1000.2.5.3.0-37.test.dummy.jar:"
        + "/jdo-api-3.0.1.test.dummy.jar:"
        + "/libfb303-0.9.3.test.dummy.jar:"
        + "/libthrift-0.9.3.test.dummy.jar:"
        + MOCK_DIR;

    private final String expectedCdhTdchLibraryJarsPath =
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

    String expectedCdhTdchHadoopClassPath =
        "/avro.test.dummy.jar:"
        + "/antlr-2.7.7.test.dummy.jar:"
        + "/antlr-runtime-3.4.test.dummy.jar:"
        + "/commons-dbcp-1.4.test.dummy.jar:"
        + "/commons-pool-1.5.4.test.dummy.jar:"
        + "/datanucleus-api-jdo-3.2.6.test.dummy.jar:"
        + "/datanucleus-core-3.2.10.test.dummy.jar:"
        + "/datanucleus-rdbms-3.2.9.test.dummy.jar:"
        + "/hive-cli-1.1.0-cdh5.14.0.test.dummy.jar:"
        + "/hive-exec-1.1.0-cdh5.14.0.test.dummy.jar:"
        + "/hive-jdbc-1.1.0-cdh5.14.0-standalone.test.dummy.jar:"
        + "/hive-metastore-1.1.0-cdh5.14.0.test.dummy.jar:"
        + "/jdo-api-3.0.1.test.dummy.jar:"
        + "/libfb303-0.9.3.test.dummy.jar:"
        + "/libthrift-0.9.3.test.dummy.jar:"
        + MOCK_DIR;

    public class MockPath implements Path {

        private final String str;

        public MockPath(String str) {
            this.str = str;
        }

        @Override
        public FileSystem getFileSystem() {
            return null;
        }

        @Override
        public boolean isAbsolute() {
            return false;
        }

        @Override
        public Path getRoot() {
            return null;
        }

        @Override
        public Path getFileName() {
            return null;
        }

        @Override
        public Path getParent() {
            return null;
        }

        @Override
        public int getNameCount() {
            return 0;
        }

        @Override
        public Path getName(int index) {
            return null;
        }

        @Override
        public Path subpath(int beginIndex, int endIndex) {
            return null;
        }

        @Override
        public boolean startsWith(Path other) {
            return false;
        }

        @Override
        public boolean startsWith(String other) {
            return false;
        }

        @Override
        public boolean endsWith(Path other) {
            return false;
        }

        @Override
        public boolean endsWith(String other) {
            return false;
        }

        @Override
        public Path normalize() {
            return null;
        }

        @Override
        public Path resolve(Path other) {
            return null;
        }

        @Override
        public Path resolve(String other) {
            return null;
        }

        @Override
        public Path resolveSibling(Path other) {
            return null;
        }

        @Override
        public Path resolveSibling(String other) {
            return null;
        }

        @Override
        public Path relativize(Path other) {
            return null;
        }

        @Override
        public URI toUri() {
            return null;
        }

        @Override
        public Path toAbsolutePath() {
            return null;
        }

        @Override
        public Path toRealPath(LinkOption... options) throws IOException {
            return null;
        }

        @Override
        public File toFile() {
            return null;
        }

        @Override
        public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
            return null;
        }

        @Override
        public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
            return null;
        }

        @Override
        public Iterator<Path> iterator() {
            return null;
        }

        @Override
        public int compareTo(Path other) {
            return 0;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    public static BasicFileAttributes mockBasicFileAttributes() {
        return new BasicFileAttributes() {
            @Override
            public FileTime lastModifiedTime() {
                return null;
            }

            @Override
            public FileTime lastAccessTime() {
                return null;
            }

            @Override
            public FileTime creationTime() {
                return null;
            }

            @Override
            public boolean isRegularFile() {
                return false;
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public boolean isSymbolicLink() {
                return false;
            }

            @Override
            public boolean isOther() {
                return false;
            }

            @Override
            public long size() {
                return 0;
            }

            @Override
            public Object fileKey() {
                return null;
            }
        };
    }

    public static class MockFileExistsValidatorReturnsTrue extends StandardValidators.FileExistsValidator {

        public MockFileExistsValidatorReturnsTrue(boolean allowExpressionLanguage) {
            super(allowExpressionLanguage);
        }

        public ValidationResult validate(String subject, String value, ValidationContext context) {
            return (new ValidationResult.Builder()).subject(subject).input(value).valid(true).explanation("Exists").build();
        }
    }


    public static class MockDirectoryExistsValidatorReturnsTrue extends StandardValidators.DirectoryExistsValidator {

        public MockDirectoryExistsValidatorReturnsTrue(boolean allowExpressionLanguage, boolean create) {
            super(allowExpressionLanguage, create);
        }

        public ValidationResult validate(String subject, String value, ValidationContext context) {
            return (new ValidationResult.Builder()).subject(subject).input(value).valid(true).explanation("Exists").build();
        }
    }

    private void setupMockPaths(List<Path> paths) throws Exception {
        PowerMockito.when(Files.find(any(Path.class), Mockito.anyInt(), any(BiPredicate.class))).thenAnswer(new Answer<Stream>() {
            @Override
            public Stream answer(InvocationOnMock invocationOnMock) throws Throwable {
                Path path = invocationOnMock.getArgumentAt(0, Path.class);
                BiPredicate predicate = invocationOnMock.getArgumentAt(2, BiPredicate.class);
                return new ArrayList<>(paths).stream().filter(p -> predicate.test(p, mockBasicFileAttributes()));
            }
        });
    }

    //checked
    @Test
    public void testStandardTdchConnectionServiceConfiguration() throws Exception {
        PowerMockito.whenNew(StandardValidators.DirectoryExistsValidator.class).withArguments(true, false).thenReturn(new MockDirectoryExistsValidatorReturnsTrue(true, false));
        PowerMockito.whenNew(StandardValidators.FileExistsValidator.class).withAnyArguments().thenReturn(new MockFileExistsValidatorReturnsTrue(true));

        List<Path> paths = Arrays.stream(expectedHdpTdchLibraryJarsPath.split(",")).map(s -> new MockPath(s.trim())).collect(Collectors.toList());

        StandardTdchConnectionService spiedStandardTdchConnectionService = Mockito.spy(StandardTdchConnectionService.class);
        PowerMockito.when(spiedStandardTdchConnectionService.getIdentifier()).thenReturn(CONNECTION_SERVICE_ID);

        PowerMockito.mockStatic(Files.class);
        PowerMockito.mockStatic(Paths.class);
        PowerMockito.when(Paths.get(Mockito.anyString())).thenReturn(new MockPath(MOCK_DIR));

        setupMockPaths(paths);
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);

        runner.addControllerService(CONNECTION_SERVICE_ID, spiedStandardTdchConnectionService);
        runner.assertNotValid(spiedStandardTdchConnectionService);
        runner.setProperty(spiedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, MOCK_DIR + "teradata-connector-1.5.jar");
        runner.assertNotValid(spiedStandardTdchConnectionService);
        runner.setProperty(spiedStandardTdchConnectionService, StandardTdchConnectionService.HIVE_CONF_PATH, MOCK_DIR);
        runner.assertNotValid(spiedStandardTdchConnectionService);
        runner.setProperty(spiedStandardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, MOCK_DIR);
        runner.assertNotValid(spiedStandardTdchConnectionService);
        runner.setProperty(spiedStandardTdchConnectionService, StandardTdchConnectionService.PASSWORD, "mydbpwd");
        runner.assertValid(spiedStandardTdchConnectionService);
    }

    //checked
    @Test
    public void testHdpCdhLibraryJarsPathAndHadoopClasspathGeneration() throws Exception {
        String hiveLibPath = "/hive-lib-path";
        String hiveConfPath = "/hive-conf-path";

        PowerMockito.mockStatic(Files.class);
        PowerMockito.mockStatic(Paths.class);
        PowerMockito.when(Paths.get(Mockito.anyString())).thenReturn(new MockPath(hiveLibPath));
        String jars = hiveLibPath + "/hive-dep-001.jar,"
                      + hiveLibPath + "/hive-dep-002.jar,"
                      + hiveLibPath + "/hive-dep-003.jar";
        List<Path> paths = Arrays.stream(jars.split(",")).map(s -> new MockPath(s.trim())).collect(Collectors.toList());
        setupMockPaths(paths);

        StandardTdchConnectionService standardTdchConnectionService = new StandardTdchConnectionService();

        Set<String> hiveDependencies = new LinkedHashSet<>();
        hiveDependencies.add("hive-dep-001.jar");
        hiveDependencies.add("hive-dep-002.jar");
        hiveDependencies.add("hive-dep-003.jar");

        String expectedPath = hiveLibPath + "/hive-dep-001.jar,"
                              + hiveLibPath + "/hive-dep-002.jar,"
                              + hiveLibPath + "/hive-dep-003.jar,"
                              + hiveConfPath;
        String generatedPath = standardTdchConnectionService.generatePath(hiveLibPath, hiveConfPath, hiveDependencies, standardTdchConnectionService.getTdchLibraryJarsPathDelimiter());
        Assert.assertEquals(expectedPath, generatedPath);

        expectedPath = hiveLibPath + "/hive-dep-001.jar:"
                       + hiveLibPath + "/hive-dep-002.jar:"
                       + hiveLibPath + "/hive-dep-003.jar:"
                       + hiveConfPath;
        generatedPath = standardTdchConnectionService.generatePath(hiveLibPath, hiveConfPath, hiveDependencies, standardTdchConnectionService.getTdchHadoopClassPathDelimiter());
        Assert.assertEquals(expectedPath, generatedPath);
    }

    //checked
    @Test
    public void testHdpHiveLib() throws Exception {
        testHiveLib(expectedHdpTdchLibraryJarsPath);
    }

    //checked
    @Test
    public void testCdhHiveLib() throws Exception {
        testHiveLib(expectedCdhTdchLibraryJarsPath);
    }

    //checked
    private void testHiveLib(String expectedTdchLibraryJarsPath) throws Exception {
        PowerMockito.whenNew(StandardValidators.DirectoryExistsValidator.class).withArguments(true, false).thenReturn(new MockDirectoryExistsValidatorReturnsTrue(true, false));

        List<Path> paths = new ArrayList<>();
        String[] pathsStringArray = expectedTdchLibraryJarsPath.split(",");
        for (int i = 0; i < pathsStringArray.length; i++) {
            paths.add(i, new MockPath(pathsStringArray[i].trim()));
        }

        StandardTdchConnectionService mockedStandardTdchConnectionService = PowerMockito.mock(StandardTdchConnectionService.class);
        PowerMockito.when(mockedStandardTdchConnectionService.getIdentifier()).thenReturn(CONNECTION_SERVICE_ID);

        PowerMockito.mockStatic(Files.class);
        PowerMockito.mockStatic(Paths.class);
        PowerMockito.when(Paths.get(Mockito.anyString())).thenReturn(new MockPath(MOCK_DIR));

        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);

        runner.addControllerService(CONNECTION_SERVICE_ID, mockedStandardTdchConnectionService);
        runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.PASSWORD, "mydbpwd");

        for (int i = 0; i < paths.size(); i++) {
            setupMockPaths(paths.subList(0, i));
            validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.HIVE_LIB_PATH, MOCK_DIR);
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
        //Not testing NiFi's FileExistsValidator
        PowerMockito.whenNew(StandardValidators.FileExistsValidator.class).withAnyArguments().thenReturn(new MockFileExistsValidatorReturnsTrue(true));

        StandardTdchConnectionService mockedStandardTdchConnectionService = PowerMockito.mock(StandardTdchConnectionService.class);
        PowerMockito.when(mockedStandardTdchConnectionService.getIdentifier()).thenReturn(CONNECTION_SERVICE_ID);

        ValidationResult validationResult;
        final TestRunner runner = TestRunners.newTestRunner(TestTdchProcessorForTestingTdchConnectionService.class);

        runner.addControllerService(CONNECTION_SERVICE_ID, mockedStandardTdchConnectionService);
        runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.PASSWORD, "mydbpwd");

        validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, MOCK_DIR + "teradata-fail.jar");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());

        validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, MOCK_DIR + "teradata-connector-1.4.jar");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());

        validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, MOCK_DIR + "");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertFalse(validationResult.isValid());

        validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, MOCK_DIR + "teradata-connector-1.5.jar");
        Assert.assertEquals(StandardTdchConnectionService.TDCH_JAR_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());

        validationResult = runner.setProperty(mockedStandardTdchConnectionService, StandardTdchConnectionService.TDCH_JAR_PATH, MOCK_DIR + "my-teradata-connector.jar");
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

        PowerMockito
            .whenNew(StandardValidators.DirectoryExistsValidator.class)
            .withArguments(true, false)
            .thenReturn(new MockDirectoryExistsValidatorReturnsTrue(true, false));

        validationResult = runner.setProperty(standardTdchConnectionService, StandardTdchConnectionService.HIVE_CONF_PATH, MOCK_DIR + "-exists");
        Assert.assertEquals(StandardTdchConnectionService.HIVE_CONF_PATH.getDisplayName(), validationResult.getSubject());
        Assert.assertTrue(validationResult.isValid());
        runner.assertValid(standardTdchConnectionService);
    }
}