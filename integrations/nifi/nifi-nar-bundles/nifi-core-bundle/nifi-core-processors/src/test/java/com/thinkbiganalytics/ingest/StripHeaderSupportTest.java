/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.ingest;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;


public class StripHeaderSupportTest {

    private StripHeaderSupport headerSupport = new StripHeaderSupport();

    @Test
    public void testDoStripHeader() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append("name,phone,zip\n");
        sb.append("Joe,phone,95121\n");
        sb.append("Sally,phone,95121\n");
        sb.append("Sam,phone,95120\n");
        sb.append("Michael,phone,94550\n");
        long bytes = headerSupport.findHeaderBoundary(1, new ByteArrayInputStream(sb.toString().getBytes()));
        assertEquals(15, bytes);
    }

}