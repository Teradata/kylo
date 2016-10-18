/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ColumnSpecTest {


    @Test
    public void testFromString() throws IOException {
        ColumnSpec[] specs = ColumnSpec.createFromString("col1|string|my comment\ncol2|int\ncol3|string|foo description\ncol4|string");
        assertTrue("Expecting 4 parts", specs.length==4);
        assertTrue(specs[0].getComment().equals("my comment"));

        ColumnSpec[] specs2 = ColumnSpec.createFromString("col1|string|my comment|1|0|0\ncol2|int\ncol3|string|foo description\ncol4|string||BAD|1|1");
        assertTrue("Expecting 4 parts", specs.length==4);
        assertTrue(specs2[0].isPk());
        assertFalse(specs2[0].isCreateDt());
        assertFalse(specs2[0].isModifiedDt());

        assertFalse(specs2[1].isPk());
        assertFalse(specs2[1].isCreateDt());
        assertFalse(specs2[1].isModifiedDt());

        assertFalse(specs2[2].isPk());
        assertFalse(specs2[2].isCreateDt());
        assertFalse(specs2[2].isModifiedDt());

        assertFalse(specs2[3].isPk());
        assertTrue(specs2[3].isCreateDt());
        assertTrue(specs2[3].isModifiedDt());
    }


    @Test
    public void testSpecs() throws IOException {
        ColumnSpec[]
            specs =
            ColumnSpec.createFromString("col1|string|my comment\ncol2|int\ncol3|string|foo description\ncol4|string");

        StringBuffer sb = new StringBuffer();
        int i = specs.length;
        for (ColumnSpec spec : specs) {
            sb.append(spec.toCreateSQL());
            if (i-- > 1) {
                sb.append(", ");
            }
        }
        System.out.println(sb.toString());
        assertNotNull(sb.toString());
    }

    @Test
    public void testPartitionKeyAndQuery() {
        PartitionKey key1 = new PartitionKey("country", "string", "country");
        PartitionKey key2 = new PartitionKey("year", "int", "year(hired_date)");
        PartitionKey key3 = new PartitionKey("reg date month", "int", "month(reg date)");
        PartitionKey key4 = new PartitionKey("`reg date day`", "int", "day(`reg date`)");

        PartitionSpec spec = new PartitionSpec(key1, key2, key3, key4);

        String sql = spec.toDistinctSelectSQL("sourceTable", "11111111");
        System.out.println(sql);
        String
            expected =
            "select country,year(hired_date),month(`reg date`),day(`reg date`) , count(0) as tb_cnt from sourceTable where processing_dttm = '11111111' group by country,year(hired_date),month(`reg date`),day(`reg date`)";
        assertEquals(expected, sql);

        assertEquals(key1.getKeyForSql(), "country");
        assertEquals(key2.getKeyForSql(), "year");
        assertEquals(key3.getKeyForSql(), "`reg date month`");
        assertEquals(key4.getKeyForSql(), "`reg date day`");

    }




}


