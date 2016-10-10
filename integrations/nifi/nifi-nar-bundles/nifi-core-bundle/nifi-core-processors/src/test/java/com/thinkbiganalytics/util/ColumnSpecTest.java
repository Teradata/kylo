/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

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
}


