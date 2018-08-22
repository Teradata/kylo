package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base;

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
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.controllerservice.StandardTdchConnectionService;

import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link AbstractTdchProcessor} processor <br>
 */
public class AbstractTdchProcessorTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractTdchProcessorTest.class);

    @SuppressWarnings("EmptyMethod")
    @Before
    public void setUp() {
        //Nothing for now
    }

    @Test
    public void testControllerServiceConfiguration() throws InitializationException {
        final String CONNECTION_SERVICE_ID = "tdch-conn-service";

        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        final TdchConnectionService tdchConnectionService = new StandardTdchConnectionService();
        final Map<String, String> tdchConnectionServiceProperties = new HashMap<>();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService, tdchConnectionServiceProperties);
        runner.assertValid();

        String jdbcDriverClass = runner.getControllerService(CONNECTION_SERVICE_ID).getPropertyDescriptor(StandardTdchConnectionService.JDBC_DRIVER_CLASS_NAME.getName()).getDefaultValue();
        Assert.assertEquals("com.teradata.jdbc.TeraDriver", jdbcDriverClass);

        String jdbcConnectionUrl = runner.getControllerService(CONNECTION_SERVICE_ID).getPropertyDescriptor(StandardTdchConnectionService.JDBC_CONNECTION_URL.getName()).getDefaultValue();
        Assert.assertEquals("jdbc:teradata://localhost", jdbcConnectionUrl);

        String user = runner.getControllerService(CONNECTION_SERVICE_ID).getPropertyDescriptor(StandardTdchConnectionService.USERNAME.getName()).getDefaultValue();
        Assert.assertEquals("dbc", user);

        String password = runner.getControllerService(CONNECTION_SERVICE_ID).getPropertyDescriptor(StandardTdchConnectionService.PASSWORD.getName()).getDefaultValue();
        Assert.assertNull(password);

        String tdchJarPath = runner.getControllerService(CONNECTION_SERVICE_ID).getPropertyDescriptor(StandardTdchConnectionService.TDCH_JAR_PATH.getName()).getDefaultValue();
        Assert.assertEquals("/usr/lib/tdch/1.5/lib/teradata-connector-1.5.4.jar", tdchJarPath);

        String hiveConfPath = runner.getControllerService(CONNECTION_SERVICE_ID).getPropertyDescriptor(StandardTdchConnectionService.HIVE_CONF_PATH.getName()).getDefaultValue();
        Assert.assertEquals("/usr/hdp/current/hive-client/conf", hiveConfPath);

        String hiveLibPath = runner.getControllerService(CONNECTION_SERVICE_ID).getPropertyDescriptor(StandardTdchConnectionService.HIVE_LIB_PATH.getName()).getDefaultValue();
        Assert.assertEquals("/usr/hdp/current/hive-client/lib", hiveLibPath);
    }

    @Test
    public void testImportToolMethods() {
        String[] invalidImportToolMethods = new String[]{"", "invalid.value"};

        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.IMPORT_TOOL_METHOD_NAME).getDefaultValue();
        assertEquals("split.by.hash", defaultValue);

        Arrays.stream(TestAbstractTdchProcessor.validImportToolMethods).forEach(validImportToolMethod -> {
            runner.setProperty(TestAbstractTdchProcessor.IMPORT_TOOL_METHOD, validImportToolMethod);
            runner.assertValid();
        });

        Arrays.stream(invalidImportToolMethods).forEach(invalidImportToolMethod -> {
            runner.setProperty(TestAbstractTdchProcessor.IMPORT_TOOL_METHOD, invalidImportToolMethod);
            runner.assertNotValid();
        });
    }

    @Test
    public void testImportToolJobTypes() {
        String[] invalidImportToolJobTypes = new String[]{"", "invalid.value"};

        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.IMPORT_TOOL_JOB_TYPE_NAME).getDefaultValue();
        assertEquals("hive", defaultValue);

        Arrays.stream(TestAbstractTdchProcessor.validImportToolJobTypes).forEach(validImportToolJobType -> {
            runner.setProperty(TestAbstractTdchProcessor.IMPORT_TOOL_JOB_TYPE, validImportToolJobType);
            runner.assertValid();
        });

        Arrays.stream(invalidImportToolJobTypes).forEach(invalidImportToolJobType -> {
            runner.setProperty(TestAbstractTdchProcessor.IMPORT_TOOL_JOB_TYPE, invalidImportToolJobType);
            runner.assertNotValid();
        });
    }

    @Test
    public void testImportToolFileFormats() {
        String[] invalidImportToolFileFormats = new String[]{"", "invalid.value"};

        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.IMPORT_TOOL_FILEFORMAT_NAME).getDefaultValue();
        assertEquals("textfile", defaultValue);

        Arrays.stream(TestAbstractTdchProcessor.validImportToolFileFormats).forEach(validImportToolFileFormat -> {
            runner.setProperty(TestAbstractTdchProcessor.IMPORT_TOOL_FILEFORMAT, validImportToolFileFormat);
            runner.assertValid();
        });

        Arrays.stream(invalidImportToolFileFormats).forEach(invalidImportToolFileFormat -> {
            runner.setProperty(TestAbstractTdchProcessor.IMPORT_TOOL_FILEFORMAT, invalidImportToolFileFormat);
            runner.assertNotValid();
        });
    }

    @Test
    public void testExportToolMethods() {
        String[] invalidExportToolMethods = new String[]{"", "invalid.value"};

        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.EXPORT_TOOL_METHOD_NAME).getDefaultValue();
        assertEquals("batch.insert", defaultValue);

        Arrays.stream(TestAbstractTdchProcessor.validExportToolMethods).forEach(validExportToolMethod -> {
            runner.setProperty(TestAbstractTdchProcessor.EXPORT_TOOL_METHOD, validExportToolMethod);
            runner.assertValid();
        });

        Arrays.stream(invalidExportToolMethods).forEach(invalidExportToolMethod -> {
            runner.setProperty(TestAbstractTdchProcessor.EXPORT_TOOL_METHOD, invalidExportToolMethod);
            runner.assertNotValid();
        });
    }

    @Test
    public void testExportToolJobTypes() {
        String[] invalidExportToolJobTypes = new String[]{"", "invalid.value"};

        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.EXPORT_TOOL_JOB_TYPE_NAME).getDefaultValue();
        assertEquals("hive", defaultValue);

        Arrays.stream(TestAbstractTdchProcessor.validExportToolJobTypes).forEach(validExportToolJobType -> {
            runner.setProperty(TestAbstractTdchProcessor.EXPORT_TOOL_JOB_TYPE, validExportToolJobType);
            runner.assertValid();
        });

        Arrays.stream(invalidExportToolJobTypes).forEach(invalidExportToolJobType -> {
            runner.setProperty(TestAbstractTdchProcessor.EXPORT_TOOL_JOB_TYPE, invalidExportToolJobType);
            runner.assertNotValid();
        });
    }

    @Test
    public void testExportToolFileFormats() {
        String[] invalidExportToolFileFormats = new String[]{"", "invalid.value"};

        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.EXPORT_TOOL_FILEFORMAT_NAME).getDefaultValue();
        assertEquals("textfile", defaultValue);

        Arrays.stream(TestAbstractTdchProcessor.validExportToolFileFormats).forEach(validExportToolFileFormat -> {
            runner.setProperty(TestAbstractTdchProcessor.EXPORT_TOOL_FILEFORMAT, validExportToolFileFormat);
            runner.assertValid();
        });

        Arrays.stream(invalidExportToolFileFormats).forEach(invalidExportToolFileFormat -> {
            runner.setProperty(TestAbstractTdchProcessor.EXPORT_TOOL_FILEFORMAT, invalidExportToolFileFormat);
            runner.assertNotValid();
        });
    }

    @Test
    public void testNumberOfMappers() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.NUMBER_OF_MAPPERS_NAME).getDefaultValue();
        assertEquals(Integer.valueOf(2), Integer.valueOf(defaultValue));
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.NUMBER_OF_MAPPERS, "-1");
        runner.assertNotValid();

        runner.setProperty(TestAbstractTdchProcessor.NUMBER_OF_MAPPERS, "10");
        runner.assertValid();
    }

    @Test
    public void testThrottleMappersFlag() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.THROTTLE_MAPPERS_FLAG_NAME).getDefaultValue();
        assertFalse(Boolean.valueOf(defaultValue));
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.THROTTLE_MAPPERS_FLAG, "invalid.value");
        runner.assertNotValid();

        //need both to be set
        runner.setProperty(TestAbstractTdchProcessor.THROTTLE_MAPPERS_FLAG, "true");
        runner.assertNotValid();
        runner.setProperty(TestAbstractTdchProcessor.MINIMUM_MAPPERS, "10");
        runner.assertValid();
    }

    @Test
    public void testMinimumMappers() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.MINIMUM_MAPPERS_NAME).getDefaultValue();
        assertEquals(null, defaultValue);
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.MINIMUM_MAPPERS, "-1");
        runner.assertNotValid();

        //need both to be set
        runner.setProperty(TestAbstractTdchProcessor.MINIMUM_MAPPERS, "10");
        runner.assertNotValid();
        runner.setProperty(TestAbstractTdchProcessor.THROTTLE_MAPPERS_FLAG, "true");
        runner.assertValid();
    }

    @Test
    public void testOverrideOfNumberOfMappers() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String throttleMappersFlag = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.THROTTLE_MAPPERS_FLAG_NAME).getDefaultValue();
        assertFalse(Boolean.valueOf(throttleMappersFlag));

        String minimumMappers = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.MINIMUM_MAPPERS_NAME).getDefaultValue();
        assertEquals(null, minimumMappers);

        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.THROTTLE_MAPPERS_FLAG, "true");
        runner.assertNotValid();

        runner.setProperty(TestAbstractTdchProcessor.THROTTLE_MAPPERS_FLAG, "false");
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.THROTTLE_MAPPERS_FLAG, "true");
        runner.assertNotValid();

        runner.setProperty(TestAbstractTdchProcessor.MINIMUM_MAPPERS, "5");
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.THROTTLE_MAPPERS_FLAG, "false");
        runner.assertNotValid();

        runner.removeProperty(TestAbstractTdchProcessor.THROTTLE_MAPPERS_FLAG);
        runner.assertNotValid();

        runner.removeProperty(TestAbstractTdchProcessor.MINIMUM_MAPPERS);
        runner.assertValid();
    }

    @Test
    public void testSourceDateFormat() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.SOURCE_DATE_FORMAT_NAME).getDefaultValue();
        assertEquals("yyyy-MM-dd", defaultValue);
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.SOURCE_DATE_FORMAT, "");
        runner.assertNotValid();

        runner.removeProperty(TestAbstractTdchProcessor.SOURCE_DATE_FORMAT);
        runner.assertValid();
    }

    @Test
    public void testSourceTimeFormat() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.SOURCE_TIME_FORMAT_NAME).getDefaultValue();
        assertEquals("HH:mm:ss", defaultValue);
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.SOURCE_TIME_FORMAT, "");
        runner.assertNotValid();

        runner.removeProperty(TestAbstractTdchProcessor.SOURCE_TIME_FORMAT);
        runner.assertValid();
    }

    @Test
    public void testSourceTimestampFormat() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.SOURCE_TIMESTAMP_FORMAT_NAME).getDefaultValue();
        assertEquals("yyyy-MM-dd HH:mm:ss.SSS", defaultValue);
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.SOURCE_TIMESTAMP_FORMAT, "");
        runner.assertNotValid();

        runner.removeProperty(TestAbstractTdchProcessor.SOURCE_TIMESTAMP_FORMAT);
        runner.assertValid();
    }

    @Test
    public void testSourceTimezoneId() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.SOURCE_TIMEZONE_ID_NAME).getDefaultValue();
        assertEquals(null, defaultValue);
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.SOURCE_TIMEZONE_ID, "");
        runner.assertNotValid();

        runner.removeProperty(TestAbstractTdchProcessor.SOURCE_TIMEZONE_ID);
        runner.assertValid();
    }

    //
    @Test
    public void testTargetDateFormat() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.TARGET_DATE_FORMAT_NAME).getDefaultValue();
        assertEquals("yyyy-MM-dd", defaultValue);
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.TARGET_DATE_FORMAT, "");
        runner.assertNotValid();

        runner.removeProperty(TestAbstractTdchProcessor.TARGET_DATE_FORMAT);
        runner.assertValid();
    }

    @Test
    public void testTargetTimeFormat() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.TARGET_TIME_FORMAT_NAME).getDefaultValue();
        assertEquals("HH:mm:ss", defaultValue);
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.TARGET_TIME_FORMAT, "");
        runner.assertNotValid();

        runner.removeProperty(TestAbstractTdchProcessor.TARGET_TIME_FORMAT);
        runner.assertValid();
    }

    @Test
    public void testTargetTimestampFormat() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.TARGET_TIMESTAMP_FORMAT_NAME).getDefaultValue();
        assertEquals("yyyy-MM-dd HH:mm:ss.SSS", defaultValue);
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.TARGET_TIMESTAMP_FORMAT, "");
        runner.assertNotValid();

        runner.removeProperty(TestAbstractTdchProcessor.TARGET_TIMESTAMP_FORMAT);
        runner.assertValid();
    }

    @Test
    public void testTargetTimezoneId() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.TARGET_TIMEZONE_ID_NAME).getDefaultValue();
        assertEquals(null, defaultValue);
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.TARGET_TIMEZONE_ID, "");
        runner.assertNotValid();

        runner.removeProperty(TestAbstractTdchProcessor.TARGET_TIMEZONE_ID);
        runner.assertValid();
    }

    @Test
    public void testStringTruncateFlag() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.STRING_TRUNCATE_FLAG_NAME).getDefaultValue();
        assertTrue(Boolean.valueOf(defaultValue));
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.STRING_TRUNCATE_FLAG, "");
        runner.assertNotValid();

        runner.setProperty(TestAbstractTdchProcessor.STRING_TRUNCATE_FLAG, "invalid.value");
        runner.assertNotValid();

        runner.setProperty(TestAbstractTdchProcessor.STRING_TRUNCATE_FLAG, "false");
        runner.assertValid();
    }

    @Test
    public void testCharset() {
        final TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        String defaultValue = runner.getProcessor().getPropertyDescriptor(TestAbstractTdchProcessor.CHARSET_NAME).getDefaultValue();
        assertNull(defaultValue);
        runner.assertValid();

        runner.setProperty(TestAbstractTdchProcessor.CHARSET, "invalid-charset");
        runner.assertNotValid();

        runner.setProperty(TestAbstractTdchProcessor.CHARSET, "UTF8");
        runner.assertValid();
    }
}
