/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.ingest;

import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.TableType;
import org.junit.Test;

public class TableRegisterSupportTests {

    @Test
    public void testTableCreate() {
        ColumnSpec[] specs = ColumnSpec.createFromString("id|bigint|my comment\nname|string\ncompany|string|some description\nzip|string\nphone|string\nemail|string\ncountry|string\nhired|date");
        ColumnSpec[] parts = ColumnSpec.createFromString("year|int\ncountry|string");

        TableRegisterSupport support = new TableRegisterSupport();
        TableType[] tableTypes = new TableType[]{TableType.FEED, TableType.INVALID, TableType.VALID, TableType.MASTER};
        for (TableType tableType : tableTypes) {
            System.out.println(support.createDDL("emp_sr3", "employee", specs, parts, "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'", tableType));
        }
    }
}
