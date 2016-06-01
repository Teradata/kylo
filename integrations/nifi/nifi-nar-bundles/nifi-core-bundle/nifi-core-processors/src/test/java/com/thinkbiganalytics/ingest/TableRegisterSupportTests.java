/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.ingest;

import com.google.common.collect.Sets;
import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.StandaloneHiveRunner;
import com.klarna.hiverunner.annotations.HiveProperties;
import com.klarna.hiverunner.annotations.HiveResource;
import com.klarna.hiverunner.annotations.HiveRunnerSetup;
import com.klarna.hiverunner.annotations.HiveSQL;
import com.klarna.hiverunner.annotations.HiveSetupScript;
import com.klarna.hiverunner.config.HiveRunnerConfig;
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.TableType;

import org.apache.commons.collections.MapUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

@RunWith(StandaloneHiveRunner.class)
public class TableRegisterSupportTests {


    /**
     * Explicit test class configuration of the HiveRunner runtime. See {@link HiveRunnerConfig} for further details.
     */
    @HiveRunnerSetup
    public final HiveRunnerConfig CONFIG = new HiveRunnerConfig() {{
        setHiveExecutionEngine("mr");
    }};

    /**
     * Cater for all the parameters in the script that we want to test. Note that the "hadoop.tmp.dir" is one of the dirs defined by the test harness
     */
    @HiveProperties
    public Map<String, String> hiveProperties = MapUtils.putAll(new HashMap(), new Object[]{
        "MY.HDFS.DIR", "${hadoop.tmp.dir}",
        "my.schema", "bar",
    });


    /**
     * In this example, the scripts under test expects a schema to be already present in hive so we do that with a setup script. <p/> There may be multiple setup scripts but the order of execution is
     * undefined.
     */
    @HiveSetupScript
    private String createSchemaScript = "create schema ${hiveconf:my.schema}";

    /**
     * Create some data in the target directory. Note that the 'targetFile' references the same dir as the create table statement in the script under test. <p/> This example is for defining the data
     * in line as a string.
     */
    @HiveResource(targetFile = "${hiveconf:MY.HDFS.DIR}/model.db/bar/employee/feed")
    private String dataFromString2 = "2,World\n3,!";

    @HiveResource(targetFile = "${hiveconf:MY.HDFS.DIR}/model.db/bar/employee/invalid")
    private String dataFromString3 = "2,World\n3,!";
    @HiveResource(targetFile = "${hiveconf:MY.HDFS.DIR}/model.db/bar/employee/valid")
    private String dataFromString4 = "2,World\n3,!";
    @HiveResource(targetFile = "${hiveconf:MY.HDFS.DIR}/app.warehouse/bar/employee")
    private String dataFromString5 = "2,World\n3,!";

    /**
     * Create some data in the target directory. Note that the 'targetFile' references the same dir as the create table statement in the script under test. <p/> This example is for defining the data
     * in line as a string.
     */
    @HiveResource(targetFile = "${hiveconf:MY.HDFS.DIR}/foo/data_from_string.csv")
    private String dataFromString = "2,World\n3,!";

    /**
     * Create some data in the target directory. Note that the 'targetFile' references the same dir as the create table statement in the script under test. <p/> This example is for defining the data
     * in in a resource file.
     */
    @HiveResource(targetFile = "${hiveconf:MY.HDFS.DIR}/foo/data_from_file.csv")
    private File dataFromFile =
        new File(ClassLoader.getSystemResource("hive-test-support/test_data.csv").getPath());


    /**
     * Define the script files under test. The files will be loaded in the given order. <p/> The HiveRunner instantiate and inject the HiveShell
     */
    @HiveSQL(files = {
        "hive-test-support/create_table.sql"
    }, encoding = "UTF-8")
    private HiveShell hiveShell;


    @Test
    public void testTablesCreated() {
        HashSet<String> expected = Sets.newHashSet("foo");
        HashSet<String> actual = Sets.newHashSet(hiveShell.executeQuery("show tables"));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testSelectFromFooWithCustomDelimiter() {
        HashSet<String> expected = Sets.newHashSet("3,!", "2,World", "1,Hello", "N/A,bar");
        HashSet<String> actual = Sets.newHashSet(hiveShell.executeQuery("select * from foo", ",", "N/A"));
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSelectFromFooWithTypeCheck() {

        List<Object[]> actual = hiveShell.executeStatement("select * from foo order by i");

        Assert.assertArrayEquals(new Object[]{null, "bar"}, actual.get(0));
        Assert.assertArrayEquals(new Object[]{1, "Hello"}, actual.get(1));
        Assert.assertArrayEquals(new Object[]{2, "World"}, actual.get(2));
        Assert.assertArrayEquals(new Object[]{3, "!"}, actual.get(3));
    }


    @Test
    public void testSelectFromCtas() {
        HashSet<String> expected = Sets.newHashSet("Hello", "World", "!");
        HashSet<String> actual = Sets.newHashSet(hiveShell
                                                     .executeQuery("select a.s from (select s, i from foo_prim order by i) a where a.i is not null"));
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testTableCreate() {
        ColumnSpec[] specs = ColumnSpec.createFromString("id|bigint|my comment\nname|string\ncompany|string|some description\nzip|string\nphone|string\nemail|string\ncountry|string\nhired|date");
        ColumnSpec[] parts = ColumnSpec.createFromString("year|int\ncountry|string");

        TableRegisterSupport support = new TableRegisterSupport();
        TableType[] tableTypes = new TableType[]{TableType.FEED, TableType.INVALID, TableType.VALID, TableType.MASTER};
        for (TableType tableType : tableTypes) {
            String ddl = support.createDDL("bar", "employee", specs, parts, "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'", "stored as orc", "tblproperties (\"orc"
                                                                                                                                                              + ".compress\"=\"SNAPPY\")",
                                           tableType);

            System.out.println(ddl);
            assertNotNull(ddl);

           // System.out.println(ddl);
 ddl = "CREATE EXTERNAL TABLE IF NOT EXISTS `bar.employee_feed` (`id` string COMMENT 'my comment', `name` string, `company` string COMMENT 'some description', `zip` string, `phone` string, "
         + "`email` string, `country` string, `hired` string)   PARTITIONED BY (`processing_dttm` string)  ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde' LOCATION '${hiveconf:MY.HDFS.DIR}/model"
             + ".db/bar/employee/feed'";
            //hiveShell.execute(ddl);
        }
    }
}
