/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.ingest;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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
        try (ByteArrayInputStream bis = new ByteArrayInputStream(sb.toString().getBytes()); ByteArrayOutputStream headerBos = new ByteArrayOutputStream(); ByteArrayOutputStream contentBos
            = new ByteArrayOutputStream()) {
            int rows = headerSupport.doStripHeader(1, bis, headerBos, contentBos);
            assertTrue("expecting 1 header row", rows == 1);
            assertTrue(headerBos.toString().equals("name,phone,zip\n"));
            assertEquals(4, contentBos.toString().split("\n").length);
            assertTrue(contentBos.toString().startsWith("Joe") && contentBos.toString().endsWith("94550\n"));
        }
    }

    @Test
    public void testDoStripNoHeader() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append("Joe,phone,95121\n");
        sb.append("Sally,phone,95121\n");
        sb.append("Sam,phone,95120\n");
        sb.append("Michael,phone,94550\n");
        try (ByteArrayInputStream bis = new ByteArrayInputStream(sb.toString().getBytes()); ByteArrayOutputStream headerBos = new ByteArrayOutputStream(); ByteArrayOutputStream contentBos
            = new ByteArrayOutputStream()) {
            int rows = headerSupport.doStripHeader(0, bis, headerBos, contentBos);
            assertTrue("expecting 0 header rows", rows == 0);
            assertEquals(4, contentBos.toString().split("\n").length);
            assertTrue(contentBos.toString().startsWith("Joe") && contentBos.toString().endsWith("94550\n"));
        }


    }
}