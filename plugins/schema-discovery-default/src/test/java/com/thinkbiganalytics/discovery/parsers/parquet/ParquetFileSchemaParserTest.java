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
    public void testMe() throws IOException {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("users.parquet")) {
            LegacyParquetFileSchemaParser parser = new LegacyParquetFileSchemaParser();
            parser.parse(is, Charset.defaultCharset(), TableSchemaType.HIVE);
        }
    }


}