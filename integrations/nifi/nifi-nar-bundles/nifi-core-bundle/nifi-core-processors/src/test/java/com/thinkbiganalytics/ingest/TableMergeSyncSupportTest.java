package com.thinkbiganalytics.ingest;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.StandaloneHiveRunner;
import com.klarna.hiverunner.annotations.HiveProperties;
import com.klarna.hiverunner.annotations.HiveRunnerSetup;
import com.klarna.hiverunner.annotations.HiveSQL;
import com.klarna.hiverunner.config.HiveRunnerConfig;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.PartitionBatch;
import com.thinkbiganalytics.util.PartitionSpec;

import org.apache.commons.collections4.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(StandaloneHiveRunner.class)
public class TableMergeSyncSupportTest {

    /**
     * Explicit test class configuration of the HiveRunner runtime. See {@link HiveRunnerConfig} for further details.
     */
    @HiveRunnerSetup
    public final HiveRunnerConfig CONFIG = new HiveRunnerConfig() {{
        setHiveExecutionEngine("mr");
    }};
    private final String sourceSchema = "emp_sr";
    private final String sourceTable = "employee_valid";
    private final String targetSchema = "emp_sr";
    private final String targetTable = "employee";
    private final String targetTableNP = "employee_np";
    private final String processingPartition = "20160119074340";
    private final PartitionSpec spec = new PartitionSpec("country|string|country\nyear|int|year(hired)");
    private final PartitionSpec specNP = new PartitionSpec("");
    /**
     * Cater for all the parameters in the script that we want to test. Note that the "hadoop.tmp.dir" is one of the dirs defined by the test harness
     */
    @HiveProperties
    public Map<String, String> hiveProperties = MapUtils.putAll(new HashMap<String, String>(), new Object[]{
        "MY.HDFS.DIR", "${hadoop.tmp.dir}",
        "my.schema", "bar",
        });
    private TableMergeSyncSupport mergeSyncSupport;
    /**
     * Define the script files under test. The files will be loaded in the given order. <p/> The HiveRunner instantiate and inject the HiveShell
     */
    @HiveSQL(files = {
        "hive-test-support/create_table.sql"
    }, encoding = "UTF-8")
    private HiveShell hiveShell;

    @Before
    public void setupSupport() throws SQLException {
        this.mergeSyncSupport = new HiveShellTableMergeSyncSupport(hiveShell);
        mergeSyncSupport.enableDynamicPartitions();
    }

    @Test
    public void testPartitionBatches() {
        List<PartitionBatch> batches = fetchPartitionBatches();
        assertTrue(batches.size() == 4);
    }

    private List<PartitionBatch> fetchPartitionBatches() {
        List<PartitionBatch> vBatch = new Vector<>();
        String sql = spec.toDistinctSelectSQL(sourceSchema, sourceTable, processingPartition);
        List<Object[]> results = hiveShell.executeStatement(sql);
        for (Object[] vals : results) {
            vBatch.add(new PartitionBatch((Long) vals[2], spec, new String[]{vals[0].toString(), vals[1].toString()}));
        }
        return vBatch;
    }


    private List<String> fetchEmployees(String targetSchema, String targetTable) {
        return hiveShell.executeQuery("select * from " + HiveUtils.quoteIdentifier(targetSchema, targetTable));
    }

    private List<String> fetchEmployeesWithoutProcessingDttm(String targetSchema, String targetTable) {
        return hiveShell.executeQuery("select `id`, `timestamp`, `name`, `company`, `zip`, `phone`, `email`, `hired`, `country` from " + HiveUtils.quoteIdentifier(targetSchema, targetTable));
    }


    @Test
    /**
     * Tests the sync function
     */
    public void testSyncWithPartitions() throws Exception {
        doTestSync(targetSchema, targetTable, spec);
    }

    @Test
    /**
     * Tests the sync function
     */
    public void testSyncNonPartitioned() throws Exception {
        doTestSync(targetSchema, targetTableNP, specNP);
    }


    private void doTestSync(String targetSchema, String targetTable, PartitionSpec spec) throws SQLException {
        mergeSyncSupport.doSync(sourceSchema, sourceTable, targetSchema, targetTable, spec, processingPartition);
        List<String> results = fetchEmployees(targetSchema, targetTable);
        assertEquals(4, results.size());

        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074340') (  `id`,  `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values "
                          + "(100,'1',"
                          + "'Bruce',"
                          + "'ABC',"
                          + "'94550','555-1212','bruce@acme.org','2016-01-01','Canada');");

        mergeSyncSupport.doSync(sourceSchema, sourceTable, targetSchema, targetTable, spec, processingPartition);
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(5, results.size());
    }

    @Test
    /**
     * Tests the merge with empty target table
     */
    public void testMergePKWithEmptyTargetTable() throws Exception {

        List<String> results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(0, results.size());

        ColumnSpec columnSpec1 = new ColumnSpec("id", "String", "", true, false, false);
        ColumnSpec columnSpec2 = new ColumnSpec("name", "String", "", false, false, false);
        ColumnSpec[] columnSpecs = Arrays.asList(columnSpec1, columnSpec2).toArray(new ColumnSpec[0]);

        // Call merge
        mergeSyncSupport.doPKMerge(sourceSchema, sourceTable, targetSchema, targetTableNP, new PartitionSpec(), processingPartition, columnSpecs);

        // We should have 4 records
        results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(4, results.size());

        // Merge with same source should leave us with 4 records
        mergeSyncSupport.doPKMerge(sourceSchema, sourceTable, targetSchema, targetTableNP, new PartitionSpec(), processingPartition, columnSpecs);

        // We should have 4 records
        results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(4, results.size());

        // Should update 1 and add 1
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074350') (  `id`,  `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values "
                          + "(1,'1',"
                          + "'NEW VALUE',"
                          + "'ABC',"
                          + "'94550','555-1212','bruce@acme.org','2016-01-01','Canada');");

        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074350') (  `id`,  `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values "
                          + "(10010,'1',"
                          + "'Bruce',"
                          + "'ABC',"
                          + "'94550','555-1212','bruce@acme.org','2016-01-01','Canada');");

        // Call merge
        mergeSyncSupport.doPKMerge(sourceSchema, sourceTable, targetSchema, targetTableNP, new PartitionSpec(), "20160119074350", columnSpecs);

        // We should have 4 records
        results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(5, results.size());

    }

    @Test
    /**
     * Tests the merge partition with empty target table
     */
    public void testMergePartitionPKWithEmptyTargetTable() throws Exception {
        List<String> results = fetchEmployees(targetSchema, targetTable);
        assertEquals(0, results.size());

        ColumnSpec columnSpec1 = new ColumnSpec("id", "String", "", true, false, false);
        ColumnSpec columnSpec2 = new ColumnSpec("name", "String", "", false, false, false);
        ColumnSpec[] columnSpecs = Arrays.asList(columnSpec1, columnSpec2).toArray(new ColumnSpec[0]);
        // Call merge
        mergeSyncSupport.doPKMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, processingPartition, columnSpecs);

        // We should have 4 records
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(4, results.size());
    }

    @Test
    /**
     * Tests the merge partition without dedupe and the merge partition with dedupe
     */
    public void testMergePartitionPK() throws Exception {
        // Insert one record to start
        hiveShell.execute(
            "insert into emp_sr.employee partition(country='USA',year=2015) (  `id`,  `timestamp`,`name`,`company`,`zip`,`phone`,`email`,  `hired`)  values (1,'1','Sally','OLD VALUE','94550',"
            + "'555-1212',"
            + "'sally@acme.org','2015-01-01');");

        doTestMergePK(targetSchema, targetTable, spec);
    }

    @Test
    /**
     * Tests the merge partition without dedupe and the merge partition with dedupe
     */
    public void testMergePartitionMovingPartitionPK() throws Exception {
        doTestMergePKWithDifferentPartitions(targetSchema, targetTable, spec);
    }

    @Test
    /**
     * Tests the merge partition without dedupe and the merge partition with dedupe
     */
    public void testMergePartition() throws Exception {

        // Insert one record to start
        hiveShell.execute(
            "insert into emp_sr.employee partition(country='USA',year=2015) (`id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`, `processing_dttm`)  values (60,'1','Billy',"
            + "'ABC','94550',"
            + "'555-1212',"
            + "'billy@acme.org','2015-01-01','20150119974340');");

        // Validate one record initial test condition
        List<String> results = fetchEmployees(targetSchema, targetTable);
        assertEquals(1, results.size());

        // Call merge
        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, processingPartition, false);

        // We should have 5 records 4 from the sourceTable and 1 existing
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(5, results.size());

        // Now create a duplicate record and ensure we don't see it twice the final table
        hiveShell.execute("insert into emp_sr.employee partition(country='Canada',year=2016) (`id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`,`processing_dttm`)  "
                          + "values (100, '1', 'Bruce','ABC','94550','555-1212','bruce@acme.org','2016-01-01','20150119974340');");

        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, "20160119974350", true);

        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(7, results.size());
        verifyUnique(results);
    }

    @Test
    /**
     * Tests the merge partition without dedupe and the merge partition with dedupe
     */
    public void testMergePartitionNoProcessingDttm() throws Exception {
        String targetTable = "employeepd";

        // Insert one record to start
        hiveShell.execute(
            "insert into emp_sr.employeepd partition(country='USA',year=2015) (`id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`)  values (60,'1','Billy',"
            + "'ABC','94550',"
            + "'555-1212',"
            + "'billy@acme.org','2015-01-01');");

        // Validate one record initial test condition
        List<String> results = fetchEmployees(targetSchema, targetTable);
        assertEquals(1, results.size());

        // Call merge
        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, processingPartition, false);

        // We should have 5 records 4 from the sourceTable and 1 existing
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(5, results.size());

        // Now create a duplicate record and ensure we don't see it twice the final table
        hiveShell.execute("insert into emp_sr.employeepd partition(country='Canada',year=2016) (`id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`)  "
                          + "values (100, '1', 'Bruce','ABC','94550','555-1212','bruce@acme.org','2016-01-01');");

        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, "20160119974350", true);

        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(7, results.size());
        verifyUnique(results);
    }


    // Verify no duplicates exist in the table
    private void verifyUnique(List<String> results) {
        HashSet<String> existing = new HashSet<>();
        for (String r : results) {
            assertFalse(existing.contains(r));
            existing.add(r);
        }
    }

    @Test
    /**
     * Tests the merge partition without dedupe and the merge partition with dedupe
     */
    public void testMergeNonPartitioned() throws Exception {
        // Insert one record to start
        hiveShell.execute(
            "insert into emp_sr.employee_np (`id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`, `country`)  values (60, '1', 'Billy',"
            + "'ABC',"
            + "'94550',"
            + "'555-1212',"
            + "'billy@acme.org','2015-01-01', 'USA');");

        List<String> results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(1, results.size());

        // Call merge without dedupe
        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTableNP, specNP, processingPartition, false);

        // We should have 5 records 4 from the sourceTable and 1 existing
        results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(5, results.size());

        // Now create a duplicate record and ensure we don't see it in the final table
        hiveShell.execute("insert into emp_sr.employee_np (`id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`, `country`)  "
                          + "values (100, '1', 'Bruce','ABC','94550','555-1212','bruce@acme.org','2016-01-01', 'Canada');");

        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTableNP, specNP, "20160119974350", true);

        results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(7, results.size());
        verifyUnique(results);
    }

    @Test
    /**
     * Tests the merge partition without dedupe and the merge partition with dedupe
     */
    public void testMergeNonPartitionedWithProcessingDttm() throws Exception {

        String targetTableNP = "employeepd_np";
        // Insert one record to start
        hiveShell.execute(
            "insert into emp_sr.employeepd_np (`id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`, `country`, `processing_dttm`)  values (60, '1', 'Billy',"
            + "'ABC',"
            + "'94550',"
            + "'555-1212',"
            + "'billy@acme.org','2015-01-01', 'USA', '20150119974350');");

        List<String> results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(1, results.size());

        // Call merge without dedupe
        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTableNP, specNP, processingPartition, false);

        // We should have 5 records 4 from the sourceTable and 1 existing
        results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(5, results.size());

        // Now create a duplicate record and ensure we don't see it in the final table
        hiveShell.execute("insert into emp_sr.employeepd_np (`id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`, `country`, `processing_dttm`)  "
                          + "values (100, '1', 'Bruce','ABC','94550','555-1212','bruce@acme.org','2016-01-01', 'Canada', '20150119974350');");

        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTableNP, specNP, "20160119974350", true);

        results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(7, results.size());
        verifyUnique(results);
    }

    @Test
    /**
     * Tests that merge with dedupe works correctly even when merges are processed out of order
     */
    public void testMergeNonPartitionedWithProcessingDttmOutOfOrder() throws Exception {
        String targetTableNP = "employeepd_np";
        // Insert one record to start
        hiveShell.execute(
                "insert into emp_sr.employeepd_np (`id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`, `country`, `processing_dttm`)  values (60, '1', 'Billy',"
                        + "'ABC',"
                        + "'94550',"
                        + "'555-1212',"
                        + "'billy@acme.org','2015-01-01', 'USA', '20150119974350');");

        List<String> results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(1, results.size());

        // Call merge without dedupe
        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTableNP, specNP, processingPartition, false);

        // We should have 5 records 4 from the sourceTable and 1 existing
        results = fetchEmployees(targetSchema, targetTableNP);
        assertEquals(5, results.size());

        // Now create a new record that is duplicated in two successive processing_dttms
        hiveShell.execute("insert into emp_sr.employee_valid partition (processing_dttm='20150119974360') "
                + "values (200, '1', 'Helen', 'ABC', '94611', '555-1212', 'wendy@acme.org', '2016-01-02', 'USA');");

        hiveShell.execute("insert into emp_sr.employee_valid partition (processing_dttm='20150119974370') "
                + "values (200, '1', 'Helen', 'ABC', '94611', '555-1212', 'wendy@acme.org', '2016-01-02', 'USA');");

        // Ensure that only a single record is inserted, even if the processing_dttms are merged out of order
        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTableNP, specNP, "20150119974370", true);
        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTableNP, specNP, "20150119974360", true);

        // we should now have 6 records and no duplicates
        results = fetchEmployeesWithoutProcessingDttm(targetSchema, targetTableNP);
        verifyUnique(results);
        assertEquals(6, results.size());
    }

    @Test
    /**
     * Test Rolling Sync.
     */
    public void testRollingSync() throws Exception {

        List<String> results = fetchEmployees(targetSchema, targetTable);
        assertEquals(0, results.size());

        doTestRollingSyncMerge(processingPartition);

        //Target table is empty.  All 4 records should be inserted.
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(4, results.size());

        //update existing partition
        String job1 = "20110119074340";
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='" + job1 + "') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (101,'Harry',"
                          + "'ABC',"
                          + "'94550','555-1212','harry@acme.org','2016-01-01','Canada');");

        doTestRollingSyncMerge(job1);

        //Target table should still have 4 records.  Partition Canada/2016 had one record before merge.  It should now have 1 updated record.
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(4, results.size());

        //Record Jen is gone and replaced by Hary
        assertFalse(results.stream().anyMatch(x -> x.contains("Jen")));
        assertTrue(results.stream().anyMatch(x -> x.contains("Harry")));

        //add new existing partition
        String job2 = "20120119074340";
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='" + job2 + "') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (101,'Flora',"
                          + "'ABC',"
                          + "'94550','555-1212','harry@acme.org','2017-01-01','France');");

        doTestRollingSyncMerge(job2);

        //Target table should now have 5 records.  Partition France/2017 has new data.  No other partitions are disturbed.
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(5, results.size());

    }

    private void doTestRollingSyncMerge(String processingPartition) throws SQLException {
        mergeSyncSupport.doRollingSync(sourceSchema, sourceTable, targetSchema, targetTable, spec, processingPartition);
    }


    private void doTestMergeNoProcessingDttm(String targetTable, PartitionSpec spec) {

        List<String> results = fetchEmployees(targetSchema, targetTable);
        assertEquals(1, results.size());

        // Call merge
        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, processingPartition, false);

        // We should have 5 records 4 from the sourceTable and 1 existing
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(5, results.size());

        // Run merge with dedupe and should get the following two additional results. The result should not include any duplicates in the target table.
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119974340') (  `id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) "
                          + "values (100, '1', 'Bruce','ABC','94550','555-1212','bruce@acme.org','2016-01-01','Canada');");
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119974340') (  `id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) "
                          + "values (101, '1','Harry','ABC','94550','555-1212','harry@acme.org','2016-01-01','Canada');");

        hiveShell.execute("insert into emp_sr.employee partition(country='Canada',year=2016) (`id`, `timestamp`, `name`,`company`,`zip`,`phone`,`email`,  `hired`,`processing_dttm`)  "
                          + "values (100, '1', 'Bruce','ABC','94550','555-1212','bruce@acme.org','2016-01-01','20150119974340');");

        mergeSyncSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, "20160119974340", true);

        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(7, results.size());
        // Verify no duplicates exist in the table
        HashSet<String> existing = new HashSet<>();
        for (String r : results) {
            assertFalse(existing.contains(r));
            existing.add(r);
        }

    }


    private void doTestMergePK(String targetSchema, String targetTable, PartitionSpec spec) {

        List<String> results = fetchEmployees(targetSchema, targetTable);
        assertEquals(1, results.size());

        ColumnSpec columnSpec1 = new ColumnSpec("id", "String", "", true, false, false);
        ColumnSpec columnSpec2 = new ColumnSpec("name", "String", "", false, false, false);
        ColumnSpec[] columnSpecs = Arrays.asList(columnSpec1, columnSpec2).toArray(new ColumnSpec[0]);
        // Call merge
        mergeSyncSupport.doPKMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, processingPartition, columnSpecs);

        // We should have 4 records
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(4, results.size());
        assertFalse("Should not have old valur", results.stream().anyMatch(s -> s.contains("OLD")));

        // Run merge with dedupe and should get the following two additional results. The result should not include any duplicates in the target table.
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074340') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (100,'Bruce',"
                          + "'OLD',"
                          + "'94550','555-1212','bruce@acme.org','2016-01-01','Canada');");
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074340') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (101,'Harry',"
                          + "'OLD',"
                          + "'94550','555-1212','harry@acme.org','2016-01-01','Canada');");

        mergeSyncSupport.doPKMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, processingPartition, columnSpecs);

        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(6, results.size());
        // Verify no duplicates exist in the table
        HashSet<String> existing = new HashSet<>();
        for (String r : results) {
            assertFalse(existing.contains(r));
            existing.add(r);
        }

        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074540') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (100,'Bruce',"
                          + "'ABC',"
                          + "'94550','555-1212','bruce@acme.org','2016-01-01','Canada');");
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074540') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (101,'Harry',"
                          + "'ABC',"
                          + "'94550','555-1212','harry@acme.org','2016-01-01','Canada');");
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074540') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (102,'Buddy',"
                          + "'ABC',"
                          + "'94550','555-1212','buddy@acme.org','2016-01-01','Canada');");

        mergeSyncSupport.doPKMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, "20160119074540", columnSpecs);
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(7, results.size());
        existing = new HashSet<>();
        for (String r : results) {
            assertFalse(existing.contains(r));
            existing.add(r);
        }

        assertFalse("Should not have old valur", results.stream().anyMatch(s -> s.contains("OLD")));

    }

    /*
    Test ability to strip records that match the ID but are in a different partition than the newer record
     */
    private void doTestMergePKWithDifferentPartitions(String targetSchema, String targetTable, PartitionSpec spec) {

        // Insert one record to start
        hiveShell.execute(
            "insert into emp_sr.employee partition(country='USA',year=2012) (  `id`,  `timestamp`,`name`,`company`,`zip`,`phone`,`email`,  `hired`)  values (1,'1','Sally','OLD VALUE','94550',"
            + "'555-1212',"
            + "'sally@acme.org','2012-01-01');");
        hiveShell.execute(
            "insert into emp_sr.employee partition(country='USA',year=2012) (  `id`,  `timestamp`,`name`,`company`,`zip`,`phone`,`email`,  `hired`)  values (1002,'1','Jimbo','VALUE','94550',"
            + "'555-1212',"
            + "'sally@acme.org','2012-01-01');");

        hiveShell.execute(
            "insert into emp_sr.employee partition(country='USA',year=2015) (  `id`,  `timestamp`,`name`,`company`,`zip`,`phone`,`email`,  `hired`)  values (1000,'1','Jill','ORIG','94550',"
            + "'555-1212',"
            + "'sally@acme.org','2015-01-01');");
        hiveShell.execute(
            "insert into emp_sr.employee partition(country='USA',year=2013) (  `id`,  `timestamp`,`name`,`company`,`zip`,`phone`,`email`,  `hired`)  values (2,'1','Bill','OLD VALUE','94550',"
            + "'555-1212',"
            + "'sally@acme.org','2013-01-01');");
        hiveShell.execute(
            "insert into emp_sr.employee partition(country='USA',year=2013) (  `id`,  `timestamp`,`name`,`company`,`zip`,`phone`,`email`,  `hired`)  values (3,'1','Ray','OLD VALUE','94550',"
            + "'555-1212',"
            + "'sally@acme.org','2013-01-01');");
        hiveShell.execute(
            "insert into emp_sr.employee partition(country='USA',year=2013) (  `id`,  `timestamp`,`name`,`company`,`zip`,`phone`,`email`,  `hired`)  values (1001,'1','Fred','VALUE','94550',"
            + "'555-1212',"
            + "'sally@acme.org','2013-01-01');");

        List<String> results = fetchEmployees(targetSchema, targetTable);
        assertEquals(6, results.size());

        ColumnSpec columnSpec1 = new ColumnSpec("id", "String", "", true, false, false);
        ColumnSpec columnSpec2 = new ColumnSpec("name", "String", "", false, false, false);
        ColumnSpec[] columnSpecs = Arrays.asList(columnSpec1, columnSpec2).toArray(new ColumnSpec[0]);
        // Call merge
        mergeSyncSupport.doPKMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, processingPartition, columnSpecs);

        // We should have 6 records
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(6, results.size());
        assertFalse("Should not have old value", results.stream().anyMatch(s -> s.contains("OLD")));

        // Run merge with dedupe and should get the following two additional results. The result should not include any duplicates in the target table.
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074340') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (100,'Bruce',"
                          + "'OLD',"
                          + "'94550','555-1212','bruce@acme.org','2016-01-01','Canada');");
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074340') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (101,'Harry',"
                          + "'OLD',"
                          + "'94550','555-1212','harry@acme.org','2016-01-01','Canada');");

        mergeSyncSupport.doPKMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, processingPartition, columnSpecs);

        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(8, results.size());
        // Verify no duplicates exist in the table
        HashSet<String> existing = new HashSet<>();
        for (String r : results) {
            assertFalse(existing.contains(r));
            existing.add(r);
        }

        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074540') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (100,'Bruce',"
                          + "'ABC',"
                          + "'94550','555-1212','bruce@acme.org','2016-01-01','Canada');");
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074540') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (101,'Harry',"
                          + "'ABC',"
                          + "'94550','555-1212','harry@acme.org','2016-01-01','Canada');");
        hiveShell.execute("insert into emp_sr.employee_valid partition(processing_dttm='20160119074540') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`) values (102,'Buddy',"
                          + "'ABC',"
                          + "'94550','555-1212','buddy@acme.org','2016-01-01','Canada');");

        mergeSyncSupport.doPKMerge(sourceSchema, sourceTable, targetSchema, targetTable, spec, "20160119074540", columnSpecs);
        results = fetchEmployees(targetSchema, targetTable);
        assertEquals(9, results.size());
        existing = new HashSet<>();
        for (String r : results) {
            assertFalse(existing.contains(r));
            existing.add(r);
        }

        assertFalse("Should not have old value", results.stream().anyMatch(s -> s.contains("OLD")));

    }


}
