/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import org.junit.Test;

import java.io.IOException;


public class ColumnSpecTest {

    @Test
    public void testSpecs() throws IOException {
        ColumnSpec[] specs = ColumnSpec.createFromString("col1|string|my comment\ncol2|int\ncol3|string|foo description\ncol4|string");

        StringBuffer sb = new StringBuffer();
        int i = specs.length;
        for (ColumnSpec spec : specs) {
            sb.append(spec.toCreateSQL());
            if (i-- > 1) {
                sb.append(", ");
            }
        }
        System.out.println(sb.toString());
    }
}


