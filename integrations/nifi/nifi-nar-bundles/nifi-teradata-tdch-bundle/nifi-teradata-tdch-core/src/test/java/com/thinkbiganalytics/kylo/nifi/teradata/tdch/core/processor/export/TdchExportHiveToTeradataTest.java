package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export;

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
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.controllerservice.DevTdchConnectionService;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.controllerservice.DummyTdchConnectionService;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.main.TdchExportHiveToTeradata;

import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link TdchExportHiveToTeradata} processor
 */
public class TdchExportHiveToTeradataTest {

    private static final Logger log = LoggerFactory.getLogger(TdchExportHiveToTeradataTest.class);
    private static final String CONNECTION_SERVICE_ID = "tdch-conn-service";

    @SuppressWarnings("EmptyMethod")
    @Before
    public void setUp() {
        //Nothing for now
    }

    @Test
    public void testTdchConnectionService() throws InitializationException {

        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);
        runner.assertNotValid();

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertNotValid();

        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.assertNotValid();

        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();
    }

    @Test
    public void testHiveConfigurationFileHdfsPath() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertNull(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_CONFIGURATION_FILE_HDFS_PATH_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_CONFIGURATION_FILE_HDFS_PATH_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_CONFIGURATION_FILE_HDFS_PATH_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.HIVE_CONFIGURATION_FILE_HDFS_PATH, "/abc/xyz/hive-site-invalid.xml");
        Assert.assertFalse(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.HIVE_CONFIGURATION_FILE_HDFS_PATH, "/abc/xyz/invalid-hive-site.xml");
        Assert.assertFalse(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.HIVE_CONFIGURATION_FILE_HDFS_PATH, "/abc/xyz/hive-site.xml");
        Assert.assertTrue(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.HIVE_CONFIGURATION_FILE_HDFS_PATH);
        runner.assertValid();
    }

    @Test
    public void testHiveDatabase() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals(null, runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_DATABASE_NAME).getDefaultValue());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_DATABASE_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_DATABASE_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "source_hive_db");
        Assert.assertTrue(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        Assert.assertTrue(result.isValid());
        runner.assertValid();

        runner.removeProperty(TdchExportHiveToTeradata.HIVE_DATABASE);
        runner.assertNotValid();
    }

    @Test
    public void testHiveTable() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals(null, runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_TABLE_NAME).getDefaultValue());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_TABLE_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_TABLE_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "source_hive_table");
        Assert.assertTrue(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        Assert.assertTrue(result.isValid());
        runner.assertValid();

        runner.removeProperty(TdchExportHiveToTeradata.HIVE_TABLE);
        runner.assertNotValid();
    }

    @Test
    public void testHiveFieldNames() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertNull(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_FIELD_NAMES_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_FIELD_NAMES_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_FIELD_NAMES_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_NAMES, "hive_field1,hive_field2,hive_field3");
        Assert.assertTrue(result.isValid());
        runner.assertNotValid();

        ValidationResult result2 = runner.setProperty(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES, "td_field1,td_field2");
        Assert.assertTrue(result2.isValid());
        runner.assertNotValid();

        result2 = runner.setProperty(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES, "td_field1,td_field2,td_field3");
        Assert.assertTrue(result2.isValid());
        runner.assertValid();

        result = runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_NAMES, "");
        Assert.assertFalse(result.isValid());
        runner.assertNotValid();

        runner.removeProperty(TdchExportHiveToTeradata.HIVE_FIELD_NAMES);
        runner.assertNotValid();
        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES);
        runner.assertValid();
    }

    @Test
    public void testHiveFieldSeparator() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals(null, runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_FIELD_SEPARATOR_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_FIELD_SEPARATOR_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_FIELD_SEPARATOR_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_SEPARATOR, "|");
        Assert.assertTrue(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_SEPARATOR, "");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.HIVE_FIELD_SEPARATOR);
        runner.assertValid();
    }

    @Test
    public void testHiveLineSeparator() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals(null, runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_LINE_SEPARATOR_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_LINE_SEPARATOR_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.HIVE_LINE_SEPARATOR_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.HIVE_LINE_SEPARATOR, "/\\r");
        Assert.assertTrue(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.HIVE_LINE_SEPARATOR, "");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.HIVE_LINE_SEPARATOR);
        runner.assertValid();
    }

    @Test
    public void testTeradataDatabaseTable() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals(null,
                            runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE_NAME).getDefaultValue());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "database");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "table");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "database.");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, ".table");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "database.table");
        Assert.assertTrue(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        Assert.assertTrue(result.isValid());
        runner.assertValid();

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE);
        runner.assertNotValid();
    }

    @Test
    public void testTeradataFieldNames() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertNull(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES, "td_field1,td_field2,td_field3");
        Assert.assertTrue(result.isValid());
        runner.assertNotValid();

        ValidationResult result2 = runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_NAMES, "hv_field1,hv_field2");
        Assert.assertTrue(result2.isValid());
        runner.assertNotValid();

        result2 = runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_NAMES, "hv_field1,hv_field2,hv_field3");
        Assert.assertTrue(result2.isValid());
        runner.assertValid();

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES, "");
        Assert.assertFalse(result.isValid());
        runner.assertNotValid();

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES);
        runner.assertNotValid();
        runner.removeProperty(TdchExportHiveToTeradata.HIVE_FIELD_NAMES);
        runner.assertValid();
    }

    @Test
    public void testTeradataTruncate() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals("false", runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_TRUNCATE_TABLE_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_TRUNCATE_TABLE_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_TRUNCATE_TABLE_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_TRUNCATE_TABLE, "true");
        Assert.assertTrue(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_TRUNCATE_TABLE, "");
        Assert.assertFalse(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_TRUNCATE_TABLE, "not-boolean-value");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_TRUNCATE_TABLE);
        runner.assertValid();
    }

    @Test
    public void testTeradataUseXviews() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals("false", runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_USE_XVIEWS_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_USE_XVIEWS_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_USE_XVIEWS_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_USE_XVIEWS, "true");
        Assert.assertTrue(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_USE_XVIEWS, "");
        Assert.assertFalse(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_USE_XVIEWS, "not-boolean-value");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_USE_XVIEWS);
        runner.assertValid();
    }

    @Test
    public void testTeradataUseQueryBand() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertFalse(Boolean.valueOf(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_QUERY_BAND_NAME).getDefaultValue()));
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_QUERY_BAND_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_QUERY_BAND_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_QUERY_BAND, "key;");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_QUERY_BAND, "=key;");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_QUERY_BAND, "key=;");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_QUERY_BAND, "key=value=;");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_QUERY_BAND, ";");
        Assert.assertFalse(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_QUERY_BAND, "key=value;");
        Assert.assertTrue(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_QUERY_BAND, "");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_QUERY_BAND);
        runner.assertValid();
    }

    @Test
    public void testTeradataBatchSize() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals("10000", runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE, "10000");
        Assert.assertTrue(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE, "");
        Assert.assertFalse(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE, "-1");
        Assert.assertFalse(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE, "not-an-integer");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE);
        runner.assertValid();
    }


    @Test
    public void testTeradataStagingDatabase() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals(null, runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_STAGING_DATABASE_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_STAGING_DATABASE_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_STAGING_DATABASE_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_STAGING_DATABASE, "td_staging_db");
        Assert.assertTrue(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_STAGING_DATABASE, "");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_STAGING_DATABASE);
        runner.assertValid();
    }

    @Test
    public void testTeradataStagingTable() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals(null, runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_STAGING_TABLE_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_STAGING_TABLE_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_STAGING_TABLE_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_STAGING_TABLE, "td_staging_table");
        Assert.assertTrue(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_STAGING_TABLE, "");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_STAGING_TABLE);
        runner.assertValid();
    }

    @Test
    public void testTeradataForceStage() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals("false", runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FORCE_STAGE_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FORCE_STAGE_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FORCE_STAGE_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_FORCE_STAGE, "true");
        Assert.assertTrue(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_FORCE_STAGE, "");
        Assert.assertFalse(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_FORCE_STAGE, "not-boolean-value");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_FORCE_STAGE);
        runner.assertValid();
    }

    @Test
    public void testTeradataKeepStage() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals("false", runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_KEEP_STAGE_TABLE_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_KEEP_STAGE_TABLE_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_KEEP_STAGE_TABLE_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_KEEP_STAGE_TABLE, "true");
        Assert.assertTrue(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_KEEP_STAGE_TABLE, "");
        Assert.assertFalse(result.isValid());
        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_KEEP_STAGE_TABLE, "not-boolean-value");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_KEEP_STAGE_TABLE);
        runner.assertValid();
    }

    @Test
    public void testTeradataFastLoadErrorDatabase() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals(null, runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_DATABASE_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_DATABASE_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_DATABASE_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_DATABASE, "td_error_db");
        Assert.assertTrue(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_DATABASE, "");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_DATABASE);
        runner.assertValid();
    }

    @Test
    public void testTeradataFastLoadErrorTable() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);

        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        Assert.assertEquals(null, runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_TABLE_NAME).getDefaultValue());
        Assert.assertFalse(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_TABLE_NAME).isRequired());
        Assert.assertTrue(runner.getProcessor().getPropertyDescriptor(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_TABLE_NAME).isExpressionLanguageSupported());

        ValidationResult result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_TABLE, "td_error_table");
        Assert.assertTrue(result.isValid());

        result = runner.setProperty(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_TABLE, "");
        Assert.assertFalse(result.isValid());

        runner.removeProperty(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_TABLE);
        runner.assertValid();
    }


    @Test
    public void testFullExport() throws InitializationException {
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);
        TdchConnectionService tdchConnectionService = new DummyTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "hive_db");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "hive_table");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "teradata_db.teradata_table");
        runner.assertValid();

        runner.enqueue();
        runner.run(1);
        List<MockFlowFile> failedFlowFiles = runner.getFlowFilesForRelationship(TdchExportHiveToTeradata.REL_FAILURE);
        Assert.assertEquals(1, failedFlowFiles.size());
        runner.assertQueueEmpty();

        String
            expectedCommand =
            "hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=false -classname \"jdbc.driver.class.name\" -url \"jdbc.connection.url/database=teradata_db\" -username \"user.name\" -password ***** -method \"batch.insert\" -jobtype \"hive\" -fileformat \"textfile\" -nummappers \"2\" -throttlemappers \"false\" -sourcedateformat \"yyyy-MM-dd\" -sourcetimeformat \"HH:mm:ss\" -sourcetimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\" -targetdateformat \"yyyy-MM-dd\" -targettimeformat \"HH:mm:ss\" -targettimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\" -stringtruncate \"true\" -sourcedatabase \"hive_db\" -sourcetable \"hive_table\" -targettable \"teradata_db.teradata_table\" -usexviews \"false\" -batchsize \"10000\" -forcestage \"false\" -keepstagetable \"false\" ";

        MockFlowFile failedFlowFile = failedFlowFiles.get(0);
        Assert.assertEquals(expectedCommand, failedFlowFile.getAttribute("tdch.export.hive.to.teradata.command"));
        Assert.assertEquals("127", failedFlowFile.getAttribute("tdch.export.hive.to.teradata.kylo.result.code"));
        Assert.assertEquals("-1", failedFlowFile.getAttribute("tdch.export.hive.to.teradata.input.record.count"));
        Assert.assertEquals("-1", failedFlowFile.getAttribute("tdch.export.hive.to.teradata.output.record.count"));
        Assert.assertEquals("-1", failedFlowFile.getAttribute("tdch.export.hive.to.teradata.tdch.exit.code"));
        Assert.assertEquals("Unable to determine time taken", failedFlowFile.getAttribute("tdch.export.hive.to.teradata.tdch.time.taken"));
    }

    @Test
    public void testExport_HiveTextToTeradataBatchInsert_5_6() throws InitializationException {
        /*
        Key: 'tdch.export.hive.to.teradata.command'
	Value: 'hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=false -classname "com.teradata.jdbc.TeraDriver" -url "jdbc:teradata://localhost/database=finance" -username "dbc" -password ***** -method "batch.insert" -jobtype "hive" -fileformat "textfile" -nummappers "2" -throttlemappers "false" -sourcedateformat "yyyy-MM-dd" -sourcetimeformat "HH:mm:ss" -sourcetimestampformat "yyyy-MM-dd HH:mm:ss.SSS" -targetdateformat "yyyy-MM-dd" -targettimeformat "HH:mm:ss" -targettimestampformat "yyyy-MM-dd HH:mm:ss.SSS" -stringtruncate "true" -sourcedatabase "tdch" -sourcetable "example5_hive" -targettable "finance.example5_td" -usexviews "false" -batchsize "10000" -forcestage "false" -keepstagetable "false" '
        */

        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);
        TdchConnectionService tdchConnectionService = new DevTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);

        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "tdch");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "example5_hive");
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "finance.example5_td");
        runner.assertValid();

        runner.enqueue();
        runner.run(1);
        List<MockFlowFile> failedFlowFiles = runner.getFlowFilesForRelationship(TdchExportHiveToTeradata.REL_FAILURE);
        Assert.assertEquals(1, failedFlowFiles.size());
        runner.assertQueueEmpty();

        String
            expectedCommand =
            "hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=false -classname \"com.teradata.jdbc.TeraDriver\" -url \"jdbc:teradata://localhost/database=finance\" -username \"dbc\" -password ***** -method \"batch.insert\" -jobtype \"hive\" -fileformat \"textfile\" -nummappers \"2\" -throttlemappers \"false\" -sourcedateformat \"yyyy-MM-dd\" -sourcetimeformat \"HH:mm:ss\" -sourcetimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\" -targetdateformat \"yyyy-MM-dd\" -targettimeformat \"HH:mm:ss\" -targettimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\" -stringtruncate \"true\" -sourcedatabase \"tdch\" -sourcetable \"example5_hive\" -targettable \"finance.example5_td\" -usexviews \"false\" -batchsize \"10000\" -forcestage \"false\" -keepstagetable \"false\" ";
        MockFlowFile failedFlowFile = failedFlowFiles.get(0);
        Assert.assertEquals(expectedCommand, failedFlowFile.getAttribute("tdch.export.hive.to.teradata.command"));
    }

    @Test
    public void testExport_HiveTextToTeradataBatchInsert_5_6_WithExpressions() throws InitializationException {
        /*
        From actual run:
        Key: 'tdch.export.hive.to.teradata.command'
	Value: 'hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=false -classname "com.teradata.jdbc.TeraDriver" -url "jdbc:teradata://localhost/database=finance" -username "dbc" -password ***** -method "batch.insert" -jobtype "hive" -fileformat "textfile" -nummappers "2" -throttlemappers "false" -sourcedateformat "yyyy-MM-dd" -sourcetimeformat "HH:mm:ss" -sourcetimestampformat "yyyy-MM-dd HH:mm:ss.SSS" -targetdateformat "yyyy-MM-dd" -targettimeformat "HH:mm:ss" -targettimestampformat "yyyy-MM-dd HH:mm:ss.SSS" -stringtruncate "true" -sourcedatabase "tdch" -sourcetable "example5_hive" -targettable "finance.example5_td" -usexviews "false" -batchsize "10000" -forcestage "false" -keepstagetable "false" '
        */

        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);
        TdchConnectionService tdchConnectionService = new DevTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);

        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "${hive_db}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "${hive_table}");
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "${teradata_db}.${teradata_table}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${my.custom.var.export.tool.job.type}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${tdch.export.tool.job.type}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${my.custom.var.export.tool.file.format}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${tdch.export.tool.file.format}");
        runner.assertValid();

        MockFlowFile mockFlowFile = new MockFlowFile(1L);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("tdch.export.tool.job.type", "hive");
        attributes.put("tdch.export.tool.file.format", "textfile");
        attributes.put("hive_db", "tdch");
        attributes.put("hive_table", "example5_hive");
        attributes.put("teradata_db", "finance");
        attributes.put("teradata_table", "example5_td");
        mockFlowFile.putAttributes(attributes);

        runner.enqueue(mockFlowFile);
        runner.run(1);
        List<MockFlowFile> failedFlowFiles = runner.getFlowFilesForRelationship(TdchExportHiveToTeradata.REL_FAILURE);
        Assert.assertEquals(1, failedFlowFiles.size());
        runner.assertQueueEmpty();

        String
            expectedCommand =
            "hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=false -classname \"com.teradata.jdbc.TeraDriver\" -url \"jdbc:teradata://localhost/database=finance\" -username \"dbc\" -password ***** -method \"batch.insert\" -jobtype \"hive\" -fileformat \"textfile\" -nummappers \"2\" -throttlemappers \"false\" -sourcedateformat \"yyyy-MM-dd\" -sourcetimeformat \"HH:mm:ss\" -sourcetimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\" -targetdateformat \"yyyy-MM-dd\" -targettimeformat \"HH:mm:ss\" -targettimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\" -stringtruncate \"true\" -sourcedatabase \"tdch\" -sourcetable \"example5_hive\" -targettable \"finance.example5_td\" -usexviews \"false\" -batchsize \"10000\" -forcestage \"false\" -keepstagetable \"false\" ";
        MockFlowFile failedFlowFile = failedFlowFiles.get(0);
        Assert.assertEquals(expectedCommand, failedFlowFile.getAttribute("tdch.export.hive.to.teradata.command"));
    }

    @Test
    public void testExport_HiveRcFileToTeradataBatchInsert_5_8() throws InitializationException {
        /*
        From actual run:
        Key: 'tdch.export.hive.to.teradata.command'
	Value: 'hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=false -classname "com.teradata.jdbc.TeraDriver" -url "jdbc:teradata://localhost/database=finance" -username "dbc" -password ***** -method "batch.insert" -jobtype "hive" -fileformat "rcfile" -nummappers "2" -throttlemappers "false" -sourcedateformat "yyyy-MM-dd" -sourcetimeformat "HH:mm:ss" -sourcetimestampformat "yyyy-MM-dd HH:mm:ss.SSS" -targetdateformat "yyyy-MM-dd" -targettimeformat "HH:mm:ss" -targettimestampformat "yyyy-MM-dd HH:mm:ss.SSS" -stringtruncate "true" -sourcedatabase "tdch" -sourcetable "example7_hive" -sourcefieldnames "h1,h2" -targettable "finance.example7_td" -targetfieldnames "c1,c2" -usexviews "false" -batchsize "10000" -forcestage "false" -keepstagetable "false" '
        */

        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);
        TdchConnectionService tdchConnectionService = new DevTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);

        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "${hive_db}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "${hive_table}");
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "${teradata_db}.${teradata_table}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_NAMES, "${hive_field_names}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES, "${teradata_field_names}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${my.custom.var.export.tool.job.type}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${tdch.export.tool.job.type}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${my.custom.var.export.tool.file.format}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${tdch.export.tool.file.format}");
        runner.assertValid();

        MockFlowFile mockFlowFile = new MockFlowFile(1L);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("tdch.export.tool.job.type", "hive");
        attributes.put("tdch.export.tool.file.format", "rcfile");
        attributes.put("hive_db", "tdch");
        attributes.put("hive_table", "example7_hive");
        attributes.put("teradata_db", "finance");
        attributes.put("teradata_table", "example7_td");
        attributes.put("hive_field_names", "h1,h2");
        attributes.put("teradata_field_names", "c1, c2");
        mockFlowFile.putAttributes(attributes);

        runner.enqueue(mockFlowFile);
        runner.run(1);
        List<MockFlowFile> failedFlowFiles = runner.getFlowFilesForRelationship(TdchExportHiveToTeradata.REL_FAILURE);
        Assert.assertEquals(1, failedFlowFiles.size());
        runner.assertQueueEmpty();

        String
            expectedCommand =
            "hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=false -classname \"com.teradata.jdbc.TeraDriver\" -url \"jdbc:teradata://localhost/database=finance\" -username \"dbc\" -password ***** -method \"batch.insert\" -jobtype \"hive\" -fileformat \"rcfile\" -nummappers \"2\" -throttlemappers \"false\" -sourcedateformat \"yyyy-MM-dd\" -sourcetimeformat \"HH:mm:ss\" -sourcetimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\" -targetdateformat \"yyyy-MM-dd\" -targettimeformat \"HH:mm:ss\" -targettimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\" -stringtruncate \"true\" -sourcedatabase \"tdch\" -sourcetable \"example7_hive\" -sourcefieldnames \"h1,h2\" -targettable \"finance.example7_td\" -targetfieldnames \"c1,c2\" -usexviews \"false\" -batchsize \"10000\" -forcestage \"false\" -keepstagetable \"false\" ";
        MockFlowFile failedFlowFile = failedFlowFiles.get(0);
        Assert.assertEquals(expectedCommand, failedFlowFile.getAttribute("tdch.export.hive.to.teradata.command"));
    }

    @Test
    public void testExport_HiveRcFileToTeradataBatchInsert_5_8_SetRequiredAndDefaultPropertiesViaExpressionLanguageToValidValues() throws InitializationException {
        /*
        From actual run:
        Key: 'tdch.export.hive.to.teradata.command'
	Value: 'hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=true -classname "com.teradata.jdbc.TeraDriver" -url "jdbc:teradata://localhost/database=finance" -username "dbc" -password ***** -method "batch.insert" -jobtype "hive" -fileformat "rcfile" -nummappers "5" -throttlemappers "true" -sourcedateformat "yy-MM-dd" -sourcetimeformat "HH:mm" -sourcetimestampformat "yyyy-MM-dd HH:mm:ss" -targetdateformat "yy-MM-dd" -targettimeformat "HH:mm" -targettimestampformat "yyyy-MM-dd HH:mm:ss" -stringtruncate "false" -sourcedatabase "tdch" -sourcetable "example7_hive" -sourcefieldnames "h1,h2" -targettable "finance.example7_td" -targetfieldnames "c1,c2" -usexviews "true" -batchsize "500" -forcestage "true" -keepstagetable "true" '
         */
        //This test covers assigning valid values to all properties that are either required or get a default value. It ensures that all of them can be set via expression variables.

        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);
        TdchConnectionService tdchConnectionService = new DevTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);

        //These are required, and support arbitrary expression variable
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "${required_config_hive_db}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "${required_config_hive_table}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "${required_config_teradata_db}.${required_config_teradata_table}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TRUNCATE_TABLE, "${config_teradata_truncate_table}");

        //These are optional (but get defaults when processor is instantiated), and support arbitrary expression variable
        runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_NAMES, "${config_hive_field_names}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES, "${config_teradata_field_names}");
        runner.setProperty(TdchExportHiveToTeradata.NUMBER_OF_MAPPERS, "${config_num_mappers}");
        runner.setProperty(TdchExportHiveToTeradata.THROTTLE_MAPPERS_FLAG, "${config_throttle_mappers_flag}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_DATE_FORMAT, "${config_hive_source_date_format}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_TIME_FORMAT, "${config_hive_source_time_format}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_TIMESTAMP_FORMAT, "${config_hive_source_timestamp_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_DATE_FORMAT, "${config_teradata_target_date_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_TIME_FORMAT, "${config_teradata_target_time_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_TIMESTAMP_FORMAT, "${config_teradata_target_timestamp_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_STRING_TRUNCATE_FLAG, "${config_teradata_string_truncate_flag}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_USE_XVIEWS, "${config_teradata_use_xviews}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE, "${config_teradata_batch_size}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_FORCE_STAGE, "${config_teradata_force_stage}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_KEEP_STAGE_TABLE, "${config_teradata_keep_stage_table}");
        runner.assertValid();

        //These need the specific expression language variable since processor checks them. The test verifies this.
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_EXPORT_TOOL_METHOD, "${my.custom.var.export.tool.method}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_EXPORT_TOOL_METHOD, "${tdch.export.tool.method}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${my.custom.var.export.tool.job.type}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${tdch.export.tool.job.type}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${my.custom.var.export.tool.file.format}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${tdch.export.tool.file.format}");
        runner.assertValid();

        //Assign values to the expression variables upstream in flowfile
        MockFlowFile mockFlowFile = new MockFlowFile(1L);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("required_config_hive_db", "tdch");
        attributes.put("required_config_hive_table", "example7_hive");
        attributes.put("required_config_teradata_db", "finance");
        attributes.put("required_config_teradata_table", "example7_td");
        attributes.put("config_teradata_truncate_table", "true");
        attributes.put("config_hive_field_names", "h1,h2");
        attributes.put("config_teradata_field_names", "c1, c2");
        attributes.put("config_num_mappers", "5");
        attributes.put("config_throttle_mappers_flag", "true");
        attributes.put("config_hive_source_date_format", "yy-MM-dd");
        attributes.put("config_hive_source_time_format", "HH:mm");
        attributes.put("config_hive_source_timestamp_format", "yyyy-MM-dd HH:mm:ss");
        attributes.put("config_teradata_target_date_format", "yy-MM-dd");
        attributes.put("config_teradata_target_time_format", "HH:mm");
        attributes.put("config_teradata_target_timestamp_format", "yyyy-MM-dd HH:mm:ss");
        attributes.put("config_teradata_string_truncate_flag", "false");
        attributes.put("config_teradata_use_xviews", "true");
        attributes.put("config_teradata_batch_size", "500");
        attributes.put("config_teradata_force_stage", "true");
        attributes.put("config_teradata_keep_stage_table", "true");
        attributes.put("tdch.export.tool.method", "batch.insert");
        attributes.put("tdch.export.tool.job.type", "hive");
        attributes.put("tdch.export.tool.file.format", "rcfile");

        mockFlowFile.putAttributes(attributes);

        runner.enqueue(mockFlowFile);
        runner.run(1);
        List<MockFlowFile> failedFlowFiles = runner.getFlowFilesForRelationship(TdchExportHiveToTeradata.REL_FAILURE);
        Assert.assertEquals(1, failedFlowFiles.size());
        runner.assertQueueEmpty();

        String
            expectedCommand =
            "hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=true -classname \"com.teradata.jdbc.TeraDriver\" -url \"jdbc:teradata://localhost/database=finance\" -username \"dbc\" -password ***** -method \"batch.insert\" -jobtype \"hive\" -fileformat \"rcfile\" -nummappers \"5\" -throttlemappers \"true\" -sourcedateformat \"yy-MM-dd\" -sourcetimeformat \"HH:mm\" -sourcetimestampformat \"yyyy-MM-dd HH:mm:ss\" -targetdateformat \"yy-MM-dd\" -targettimeformat \"HH:mm\" -targettimestampformat \"yyyy-MM-dd HH:mm:ss\" -stringtruncate \"false\" -sourcedatabase \"tdch\" -sourcetable \"example7_hive\" -sourcefieldnames \"h1,h2\" -targettable \"finance.example7_td\" -targetfieldnames \"c1,c2\" -usexviews \"true\" -batchsize \"500\" -forcestage \"true\" -keepstagetable \"true\" ";
        MockFlowFile failedFlowFile = failedFlowFiles.get(0);
        Assert.assertEquals(expectedCommand, failedFlowFile.getAttribute("tdch.export.hive.to.teradata.command"));
    }

    @Test
    public void testExport_HiveRcFileToTeradataBatchInsert_5_8_SetRequiredAndDefaultPropertiesViaExpressionLanguageToEmptyValues() throws InitializationException {
        /*
        From actual run:
        Key: 'tdch.export.hive.to.teradata.command'
	Value: 'hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=false -classname "com.teradata.jdbc.TeraDriver" -url "jdbc:teradata://localhost/database=finance" -username "dbc" -password ***** -method "batch.insert" -jobtype "hive" -fileformat "rcfile" -nummappers "2" -throttlemappers "false" -stringtruncate "true" -sourcedatabase "tdch" -sourcetable "example7_hive" -targettable "finance.example7_td" -usexviews "false" -batchsize "10000" -forcestage "false" -keepstagetable "false" '
         */
        //This test covers assigning empty values to all properties that are either required or get a default value. It ensures that all of them can be set via expression variables.

        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);
        TdchConnectionService tdchConnectionService = new DevTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);

        //These are required, and support arbitrary expression variable
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "${required_config_hive_db}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "${required_config_hive_table}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "${required_config_teradata_db}.${required_config_teradata_table}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TRUNCATE_TABLE, "${config_teradata_truncate_table}");
        runner.assertValid();

        //These are optional (but get defaults when processor is instantiated), and support arbitrary expression variable
        runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_NAMES, "${config_hive_field_names}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES, "${config_teradata_field_names}");
        runner.setProperty(TdchExportHiveToTeradata.NUMBER_OF_MAPPERS, "${config_num_mappers}");
        runner.setProperty(TdchExportHiveToTeradata.THROTTLE_MAPPERS_FLAG, "${config_throttle_mappers_flag}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_DATE_FORMAT, "${config_hive_source_date_format}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_TIME_FORMAT, "${config_hive_source_time_format}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_TIMESTAMP_FORMAT, "${config_hive_source_timestamp_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_DATE_FORMAT, "${config_teradata_target_date_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_TIME_FORMAT, "${config_teradata_target_time_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_TIMESTAMP_FORMAT, "${config_teradata_target_timestamp_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_STRING_TRUNCATE_FLAG, "${config_teradata_string_truncate_flag}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_USE_XVIEWS, "${config_teradata_use_xviews}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE, "${config_teradata_batch_size}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_FORCE_STAGE, "${config_teradata_force_stage}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_KEEP_STAGE_TABLE, "${config_teradata_keep_stage_table}");
        runner.assertValid();

        //These need the specific expression language variable since processor checks them. The test verifies this.
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_EXPORT_TOOL_METHOD, "${my.custom.var.export.tool.method}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_EXPORT_TOOL_METHOD, "${tdch.export.tool.method}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${my.custom.var.export.tool.job.type}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${tdch.export.tool.job.type}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${my.custom.var.export.tool.file.format}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${tdch.export.tool.file.format}");
        runner.assertValid();

        //Assign values to the expression variables upstream in flowfile
        MockFlowFile mockFlowFile = new MockFlowFile(1L);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("required_config_hive_db", "tdch");
        attributes.put("required_config_hive_table", "example7_hive");
        attributes.put("required_config_teradata_db", "finance");
        attributes.put("required_config_teradata_table", "example7_td");
        attributes.put("config_teradata_truncate_table", "");
        attributes.put("config_hive_field_names", "");
        attributes.put("config_teradata_field_names", "");
        attributes.put("config_num_mappers", "");
        attributes.put("config_throttle_mappers_flag", "");
        attributes.put("config_hive_source_date_format", "");
        attributes.put("config_hive_source_time_format", "");
        attributes.put("config_hive_source_timestamp_format", "");
        attributes.put("config_teradata_target_date_format", "");
        attributes.put("config_teradata_target_time_format", "");
        attributes.put("config_teradata_target_timestamp_format", "");
        attributes.put("config_teradata_string_truncate_flag", "");
        attributes.put("config_teradata_use_xviews", "");
        attributes.put("config_teradata_batch_size", "");
        attributes.put("config_teradata_force_stage", "");
        attributes.put("config_teradata_keep_stage_table", "");
        attributes.put("tdch.export.tool.method", "");
        attributes.put("tdch.export.tool.job.type", "");
        attributes.put("tdch.export.tool.file.format", "rcfile");   //need this since Hive table is in rcfile format.

        mockFlowFile.putAttributes(attributes);

        runner.enqueue(mockFlowFile);
        runner.run(1);
        List<MockFlowFile> failedFlowFiles = runner.getFlowFilesForRelationship(TdchExportHiveToTeradata.REL_FAILURE);
        Assert.assertEquals(1, failedFlowFiles.size());
        runner.assertQueueEmpty();

        String
            expectedCommand =
            "hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=false -classname \"com.teradata.jdbc.TeraDriver\" -url \"jdbc:teradata://localhost/database=finance\" -username \"dbc\" -password ***** -method \"batch.insert\" -jobtype \"hive\" -fileformat \"rcfile\" -nummappers \"2\" -throttlemappers \"false\" -stringtruncate \"true\" -sourcedatabase \"tdch\" -sourcetable \"example7_hive\" -targettable \"finance.example7_td\" -usexviews \"false\" -batchsize \"10000\" -forcestage \"false\" -keepstagetable \"false\" ";
        MockFlowFile failedFlowFile = failedFlowFiles.get(0);
        Assert.assertEquals(expectedCommand, failedFlowFile.getAttribute("tdch.export.hive.to.teradata.command"));
    }

    @Test
    public void testExport_HiveTextToTeradataBatchInsert_5_6_SetRequiredAndDefaultPropertiesViaExpressionLanguageToEmptyValues() throws InitializationException {
        /*
        From actual run:
        Key: 'tdch.export.hive.to.teradata.command'
	Value: 'hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=false -classname "com.teradata.jdbc.TeraDriver" -url "jdbc:teradata://localhost/database=finance" -username "dbc" -password ***** -method "batch.insert" -jobtype "hive" -fileformat "textfile" -nummappers "2" -throttlemappers "false" -stringtruncate "true" -sourcedatabase "tdch" -sourcetable "example5_hive" -targettable "finance.example5_td" -usexviews "false" -batchsize "10000" -forcestage "false" -keepstagetable "false" '
	*/
        //This test is similar to testExport_HiveRcFileToTeradataBatchInsert_5_8_SetRequiredAndDefaultPropertiesViaExpressionLanguageToEmptyValues()
        //It differs in terms of: hive format is textfile, hive format is set to empty via expression language.
        //This test covers assigning empty values to all properties that are either required or get a default value. It ensures that all of them can be set via expression variables.

        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);
        TdchConnectionService tdchConnectionService = new DevTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);

        //These are required, and support arbitrary expression variable
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "${required_config_hive_db}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "${required_config_hive_table}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "${required_config_teradata_db}.${required_config_teradata_table}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TRUNCATE_TABLE, "${config_teradata_truncate_table}");

        //These are optional (but get defaults when processor is instantiated), and support arbitrary expression variable
        runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_NAMES, "${config_hive_field_names}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES, "${config_teradata_field_names}");
        runner.setProperty(TdchExportHiveToTeradata.NUMBER_OF_MAPPERS, "${config_num_mappers}");
        runner.setProperty(TdchExportHiveToTeradata.THROTTLE_MAPPERS_FLAG, "${config_throttle_mappers_flag}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_DATE_FORMAT, "${config_hive_source_date_format}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_TIME_FORMAT, "${config_hive_source_time_format}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_TIMESTAMP_FORMAT, "${config_hive_source_timestamp_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_DATE_FORMAT, "${config_teradata_target_date_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_TIME_FORMAT, "${config_teradata_target_time_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_TIMESTAMP_FORMAT, "${config_teradata_target_timestamp_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_STRING_TRUNCATE_FLAG, "${config_teradata_string_truncate_flag}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_USE_XVIEWS, "${config_teradata_use_xviews}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE, "${config_teradata_batch_size}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_FORCE_STAGE, "${config_teradata_force_stage}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_KEEP_STAGE_TABLE, "${config_teradata_keep_stage_table}");
        runner.assertValid();

        //These need the specific expression language variable since processor checks them. The test verifies this.
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_EXPORT_TOOL_METHOD, "${my.custom.var.export.tool.method}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_EXPORT_TOOL_METHOD, "${tdch.export.tool.method}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${my.custom.var.export.tool.job.type}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${tdch.export.tool.job.type}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${my.custom.var.export.tool.file.format}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${tdch.export.tool.file.format}");
        runner.assertValid();

        //Assign values to the expression variables upstream in flowfile
        MockFlowFile mockFlowFile = new MockFlowFile(1L);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("required_config_hive_db", "tdch");
        attributes.put("required_config_hive_table", "example5_hive");
        attributes.put("required_config_teradata_db", "finance");
        attributes.put("required_config_teradata_table", "example5_td");
        attributes.put("config_teradata_truncate_table", "");
        attributes.put("config_hive_field_names", "");
        attributes.put("config_teradata_field_names", "");
        attributes.put("config_num_mappers", "");
        attributes.put("config_throttle_mappers_flag", "");
        attributes.put("config_hive_source_date_format", "");
        attributes.put("config_hive_source_time_format", "");
        attributes.put("config_hive_source_timestamp_format", "");
        attributes.put("config_teradata_target_date_format", "");
        attributes.put("config_teradata_target_time_format", "");
        attributes.put("config_teradata_target_timestamp_format", "");
        attributes.put("config_teradata_string_truncate_flag", "");
        attributes.put("config_teradata_use_xviews", "");
        attributes.put("config_teradata_batch_size", "");
        attributes.put("config_teradata_force_stage", "");
        attributes.put("config_teradata_keep_stage_table", "");
        attributes.put("tdch.export.tool.method", "");
        attributes.put("tdch.export.tool.job.type", "");
        attributes.put("tdch.export.tool.file.format", "");

        mockFlowFile.putAttributes(attributes);

        runner.enqueue(mockFlowFile);
        runner.run(1);
        List<MockFlowFile> failedFlowFiles = runner.getFlowFilesForRelationship(TdchExportHiveToTeradata.REL_FAILURE);
        Assert.assertEquals(1, failedFlowFiles.size());
        runner.assertQueueEmpty();

        String
            expectedCommand =
            "hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=false -classname \"com.teradata.jdbc.TeraDriver\" -url \"jdbc:teradata://localhost/database=finance\" -username \"dbc\" -password ***** -method \"batch.insert\" -jobtype \"hive\" -fileformat \"textfile\" -nummappers \"2\" -throttlemappers \"false\" -stringtruncate \"true\" -sourcedatabase \"tdch\" -sourcetable \"example5_hive\" -targettable \"finance.example5_td\" -usexviews \"false\" -batchsize \"10000\" -forcestage \"false\" -keepstagetable \"false\" ";
        MockFlowFile failedFlowFile = failedFlowFiles.get(0);
        Assert.assertEquals(expectedCommand, failedFlowFile.getAttribute("tdch.export.hive.to.teradata.command"));
    }

    @Test
    public void testExport_HiveTextToTeradataInternalFastload_5_6_SetAllPropertiesViaExpressionLanguageToValidValues() throws InitializationException {
        /*
        From actual run:
        Key: 'tdch.export.hive.to.teradata.command'
	Value: 'hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=true -classname "com.teradata.jdbc.TeraDriver" -url "jdbc:teradata://localhost/database=perf" -username "dbc" -password ***** -method "internal.fastload" -jobtype "hive" -fileformat "textfile" -nummappers "2" -throttlemappers "true" -minmappers "1" -sourcedateformat "yyyy-MM-dd" -sourcetimeformat "HH:mm:ss" -sourcetimestampformat "yyyy-MM-dd HH:mm:ss.SSS" -sourcetimezoneid "UTC" -targetdateformat "yyyy-MM-dd" -targettimeformat "HH:mm:ss" -targettimestampformat "yyyy-MM-dd HH:mm:ss.SSS" -targettimezoneid "PST" -stringtruncate "true" -hiveconf "/tdch/hive-config/hive-site.xml" -sourcedatabase "tdch" -sourcetable "tdch.perf_hive_text_10k" -sourcefieldnames "yelp_text,yelp_date,yelp_likes,yelp_business_id,user_id" -separator , -lineseparator \\n -targettable "perf.perf_td_allstr_10_to_1000k" -targetfieldnames "yelp_text,yelp_date,yelp_likes,yelp_business_id,user_id" -usexviews "false" -queryband "org=finance;" -batchsize "10000" -stagedatabase "finance_scratchpad" -stagetablename "yelp_stg" -forcestage "true" -keepstagetable "true" -errortabledatabase "finance_scratchpad" -errortablename "yelp_err" '
         */
        //This test covers assigning all values (34) via expression language. It ensures all of them can be set via expression variables.
        final TestRunner runner = TestRunners.newTestRunner(TdchExportHiveToTeradata.class);
        TdchConnectionService tdchConnectionService = new DevTdchConnectionService();
        runner.addControllerService(CONNECTION_SERVICE_ID, tdchConnectionService);
        runner.assertValid(tdchConnectionService);
        runner.enableControllerService(tdchConnectionService);
        runner.setProperty(TdchExportHiveToTeradata.TDCH_CONNECTION_SERVICE, CONNECTION_SERVICE_ID);

        //These are required, and support arbitrary expression variable
        runner.setProperty(TdchExportHiveToTeradata.HIVE_DATABASE, "${required_config_hive_db}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_TABLE, "${required_config_hive_table}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_DATABASE_TABLE, "${required_config_teradata_db}.${required_config_teradata_table}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TRUNCATE_TABLE, "${config_teradata_truncate_table}");
        runner.assertValid();

        //These are optional (but get defaults when processor is instantiated), and support arbitrary expression variable
        runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_NAMES, "${config_hive_field_names}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_FIELD_NAMES, "${config_teradata_field_names}");
        runner.setProperty(TdchExportHiveToTeradata.NUMBER_OF_MAPPERS, "${config_num_mappers}");
        runner.setProperty(TdchExportHiveToTeradata.THROTTLE_MAPPERS_FLAG, "${config_throttle_mappers_flag}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_DATE_FORMAT, "${config_hive_source_date_format}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_TIME_FORMAT, "${config_hive_source_time_format}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_TIMESTAMP_FORMAT, "${config_hive_source_timestamp_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_DATE_FORMAT, "${config_teradata_target_date_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_TIME_FORMAT, "${config_teradata_target_time_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_TIMESTAMP_FORMAT, "${config_teradata_target_timestamp_format}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_STRING_TRUNCATE_FLAG, "${config_teradata_string_truncate_flag}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_USE_XVIEWS, "${config_teradata_use_xviews}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_BATCH_SIZE, "${config_teradata_batch_size}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_FORCE_STAGE, "${config_teradata_force_stage}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_KEEP_STAGE_TABLE, "${config_teradata_keep_stage_table}");
        runner.assertValid();

        //These need the specific expression language variable since processor checks them. The test verifies this.
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_EXPORT_TOOL_METHOD, "${my.custom.var.export.tool.method}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_EXPORT_TOOL_METHOD, "${tdch.export.tool.method}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${my.custom.var.export.tool.job.type}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_JOB_TYPE, "${tdch.export.tool.job.type}");
        runner.assertValid();

        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${my.custom.var.export.tool.file.format}");
        runner.assertNotValid();
        runner.setProperty(TdchExportHiveToTeradata.HIVE_EXPORT_TOOL_FILEFORMAT, "${tdch.export.tool.file.format}");
        runner.assertValid();

        //These are optional (do not get defaults when processor is instantiated), and support arbitrary expression variable
        runner.setProperty(TdchExportHiveToTeradata.HIVE_SOURCE_TIMEZONE_ID, "${config_hive_source_timezone_id}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_CONFIGURATION_FILE_HDFS_PATH, "${config_hive_configuration_file_hdfs_path}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_FIELD_SEPARATOR, "${config_hive_field_separator}");
        runner.setProperty(TdchExportHiveToTeradata.HIVE_LINE_SEPARATOR, "${config_hive_line_separator}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_TARGET_TIMEZONE_ID, "${config_teradata_target_timezone_id}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_QUERY_BAND, "${config_teradata_query_band}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_STAGING_DATABASE, "${config_teradata_staging_database}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_STAGING_TABLE, "${config_teradata_staging_table}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_DATABASE, "${config_teradata_fast_load_error_database}");
        runner.setProperty(TdchExportHiveToTeradata.TERADATA_FAST_LOAD_ERROR_TABLE, "${config_teradata_fast_load_error_table}");
        runner.setProperty(TdchExportHiveToTeradata.MINIMUM_MAPPERS, "${config_minimum_mappers}");

        //Assign values to the expression variables upstream in flowfile
        MockFlowFile mockFlowFile = new MockFlowFile(1L);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("required_config_hive_db", "tdch");
        attributes.put("required_config_hive_table", "tdch.perf_hive_text_10k");
        attributes.put("required_config_teradata_db", "perf");
        attributes.put("required_config_teradata_table", "perf_td_allstr_10_to_1000k");
        attributes.put("config_teradata_truncate_table", "true");
        attributes.put("config_hive_field_names", "yelp_text,yelp_date,yelp_likes,yelp_business_id,user_id");
        attributes.put("config_teradata_field_names", "yelp_text,yelp_date,yelp_likes, yelp_business_id,user_id");
        attributes.put("config_num_mappers", "2");
        attributes.put("config_throttle_mappers_flag", "true");
        attributes.put("config_hive_source_date_format", "yyyy-MM-dd");
        attributes.put("config_hive_source_time_format", "HH:mm:ss");
        attributes.put("config_hive_source_timestamp_format", "yyyy-MM-dd HH:mm:ss.SSS");
        attributes.put("config_teradata_target_date_format", "yyyy-MM-dd");
        attributes.put("config_teradata_target_time_format", "HH:mm:ss");
        attributes.put("config_teradata_target_timestamp_format", "yyyy-MM-dd HH:mm:ss.SSS");
        attributes.put("config_teradata_string_truncate_flag", "true");
        attributes.put("config_teradata_use_xviews", "false");
        attributes.put("config_teradata_batch_size", "10000");
        attributes.put("config_teradata_force_stage", "true");
        attributes.put("config_teradata_keep_stage_table", "true");
        attributes.put("tdch.export.tool.method", "internal.fastload");
        attributes.put("tdch.export.tool.job.type", "hive");
        attributes.put("tdch.export.tool.file.format", "textfile");
        attributes.put("config_hive_source_timezone_id", "UTC");
        attributes.put("config_hive_configuration_file_hdfs_path", "/tdch/hive-config/hive-site.xml");
        attributes.put("config_hive_field_separator", ",");
        attributes.put("config_hive_line_separator", "\\n");
        attributes.put("config_teradata_target_timezone_id", "PST");
        attributes.put("config_teradata_query_band", "org=finance;");
        attributes.put("config_teradata_staging_database", "finance_scratchpad");
        attributes.put("config_teradata_staging_table", "yelp_stg");
        attributes.put("config_teradata_fast_load_error_database", "finance_scratchpad");
        attributes.put("config_teradata_fast_load_error_table", "yelp_err");
        attributes.put("config_minimum_mappers", "1");

        mockFlowFile.putAttributes(attributes);

        runner.enqueue(mockFlowFile);
        runner.run(1);
        List<MockFlowFile> failedFlowFiles = runner.getFlowFilesForRelationship(TdchExportHiveToTeradata.REL_FAILURE);
        Assert.assertEquals(1, failedFlowFiles.size());
        runner.assertQueueEmpty();

        String
            expectedCommand =
            "hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIB_JARS -Dtdch.output.teradata.truncate=true -classname \"com.teradata.jdbc.TeraDriver\" -url \"jdbc:teradata://localhost/database=perf\" -username \"dbc\" -password ***** -method \"internal.fastload\" -jobtype \"hive\" -fileformat \"textfile\" -nummappers \"2\" -throttlemappers \"true\" -minmappers \"1\" -sourcedateformat \"yyyy-MM-dd\" -sourcetimeformat \"HH:mm:ss\" -sourcetimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\" -sourcetimezoneid \"UTC\" -targetdateformat \"yyyy-MM-dd\" -targettimeformat \"HH:mm:ss\" -targettimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\" -targettimezoneid \"PST\" -stringtruncate \"true\" -hiveconf \"/tdch/hive-config/hive-site.xml\" -sourcedatabase \"tdch\" -sourcetable \"tdch.perf_hive_text_10k\" -sourcefieldnames \"yelp_text,yelp_date,yelp_likes,yelp_business_id,user_id\" -separator , -lineseparator \\\\n -targettable \"perf.perf_td_allstr_10_to_1000k\" -targetfieldnames \"yelp_text,yelp_date,yelp_likes,yelp_business_id,user_id\" -usexviews \"false\" -queryband \"org=finance;\" -batchsize \"10000\" -stagedatabase \"finance_scratchpad\" -stagetablename \"yelp_stg\" -forcestage \"true\" -keepstagetable \"true\" -errortabledatabase \"finance_scratchpad\" -errortablename \"yelp_err\" ";
        MockFlowFile failedFlowFile = failedFlowFiles.get(0);
        Assert.assertEquals(expectedCommand, failedFlowFile.getAttribute("tdch.export.hive.to.teradata.command"));
    }
}
