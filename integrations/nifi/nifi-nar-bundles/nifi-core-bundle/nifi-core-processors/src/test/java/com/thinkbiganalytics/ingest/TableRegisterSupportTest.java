/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.ingest;

import com.google.common.collect.ImmutableSet;
import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.StandaloneHiveRunner;
import com.klarna.hiverunner.annotations.HiveProperties;
import com.klarna.hiverunner.annotations.HiveRunnerSetup;
import com.klarna.hiverunner.annotations.HiveSQL;
import com.klarna.hiverunner.config.HiveRunnerConfig;
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.TableType;

import org.apache.commons.collections.MapUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(StandaloneHiveRunner.class)
public class TableRegisterSupportTest {

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
     * Define the script files under test. The files will be loaded in the given order. <p/> The HiveRunner instantiate and inject the HiveShell
     */
    @HiveSQL(files = {
        "hive-test-support/create_table.sql"
    }, encoding = "UTF-8")
    private HiveShell hiveShell;

    @Test
    public void testShowLocation() {

        List<Object[]> values = hiveShell.executeStatement("show table extended like foo");
        for (Object[] o : values) {
            String value = o[0].toString();
            if (value.startsWith("location:")) {
                System.out.println(value.substring(9));
            }
        }

    }

    @Test
    public void testSplit() {

        // Extract schema vs table
        String[] schemaPart = "foo.bar".split("\\.");
        assertEquals(schemaPart.length, 2);
        String schema = schemaPart[0];
        String targetTable = schemaPart[1];
        assertNotNull(schema);
        assertNotNull(targetTable);

    }


    @Test
    public void testTableCreate() {
        ColumnSpec[] specs = ColumnSpec.createFromString("id|bigint|my comment\nname|string\ncompany|string|some description\nzip|string\nphone|string\nemail|string\ncountry|string\nhired|date");
        ColumnSpec[] parts = ColumnSpec.createFromString("year|int\ncountry|string");

        TableRegisterSupport support = new TableRegisterSupport();
        TableType[] tableTypes = new TableType[]{TableType.FEED, TableType.INVALID, TableType.VALID, TableType.MASTER};
        for (TableType tableType : tableTypes) {
            String
                ddl =
                support.createDDL("bar", "employee", specs, parts, "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'", "stored as orc", "tblproperties (\"orc.compress\"=\"SNAPPY\")",
                                  tableType);
            // Hack to make a legal file root
            ddl = ddl.replace("LOCATION '", "LOCATION '${hiveconf:MY.HDFS.DIR}");
            hiveShell.execute(ddl);
        }
    }

    /** Verify dropping a table. */
    @Test
    public void testDropTable() throws Exception {
        // Mock SQL objects
        final Statement statement = Mockito.mock(Statement.class);
        Mockito.when(statement.execute(Mockito.anyString())).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(final InvocationOnMock invocation) throws Throwable {
                final String sql = (String)invocation.getArguments()[0];
                if (sql.equals("DROP TABLE IF EXISTS invalid")) {
                    throw new SQLException();
                }
                return true;
            }
        });

        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);

        // Test dropping table with success
        final TableRegisterSupport support = new TableRegisterSupport(connection);
        Assert.assertTrue(support.dropTable("feed"));
        Mockito.verify(statement).execute("DROP TABLE IF EXISTS feed");

        // Test dropping table with exception
        Assert.assertFalse(support.dropTable("invalid"));
    }

    /** Verify exception if the connection is null. */
    @Test(expected = NullPointerException.class)
    public void testDropTableWithNullConnection() {
        new TableRegisterSupport().dropTable("invalid");
    }

    /** Verify dropping multiple tables. */
    @Test
    public void testDropTables() throws Exception {
        // Mock SQL objects
        final Statement statement = Mockito.mock(Statement.class);
        Mockito.when(statement.execute(Mockito.anyString())).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                final String sql = (String)invocation.getArguments()[0];
                if (sql.startsWith("DROP TABLE IF EXISTS invalid")) {
                    throw new SQLException();
                }
                return true;
            }
        });

        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);

        // Test dropping tables with success
        TableRegisterSupport support = new TableRegisterSupport(connection);
        Assert.assertTrue(support.dropTables("cat", "feed", EnumSet.of(TableType.MASTER, TableType.VALID, TableType.INVALID), ImmutableSet.of("backup.feed")));
        Mockito.verify(statement).execute("DROP TABLE IF EXISTS cat.feed");
        Mockito.verify(statement).execute("DROP TABLE IF EXISTS cat.feed_valid");
        Mockito.verify(statement).execute("DROP TABLE IF EXISTS cat.feed_invalid");
        Mockito.verify(statement).execute("DROP TABLE IF EXISTS backup.feed");

        // Test dropping tables with exception
        Assert.assertFalse(support.dropTables("invalid", "feed", EnumSet.allOf(TableType.class), ImmutableSet.of()));
        Assert.assertFalse(support.dropTables("cat", "feed", ImmutableSet.of(), ImmutableSet.of("invalid")));
    }
}
