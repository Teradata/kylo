/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.parsers.parquet;

import com.thinkbiganalytics.discovery.parsers.csv.CSVFileSchemaParserTest;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by matthutton on 11/28/16.
 */
public class ParquetFileSchemaParserTest {

    @Test
    public void testMe1() throws IOException {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("users.parquet")) {
            doIt(is);
        }
    }

    @Test
    public void testMe2() throws IOException {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("AvroPrimitiveInList.parquet")) {
            doIt(is);
        }
    }

    @Test
    public void testMe3() throws IOException {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("AvroSingleFieldGroupInList.parquet")) {
            doIt(is);
        }
    }

    @Test
    public void testMe4() throws IOException {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("HiveGroup.parquet")) {
            doIt(is);
        }
    }

    @Test
    public void testMe5() throws IOException {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("HiveRequiredGroupInList.parquet")) {
            doIt(is);
        }
    }

    @Test
    public void testMe6() throws IOException {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("MultiFieldGroupInList.parquet")) {
            doIt(is);
        }
    }

    @Test
    public void testMe() throws IOException {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("NestedMap.parquet")) {
            doIt(is);
        }
    }

    @Test
    public void testMe7() throws IOException {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("NewOptionalGroupInList.parquet")) {
            doIt(is);
        }
    }

    @Test
    public void testMe8() throws IOException {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("NewRequiredGroupInList.parquet")) {
            doIt(is);
        }
    }

    @Test
    public void testMe9() throws IOException {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("SingleFieldGroupInList.parquet")) {
            doIt(is);
        }
    }

    private void doIt(InputStream is) throws IOException {
        LegacyParquetFileSchemaParser parser = new LegacyParquetFileSchemaParser();
        parser.parse(is, Charset.defaultCharset(), TableSchemaType.HIVE);
    }

}