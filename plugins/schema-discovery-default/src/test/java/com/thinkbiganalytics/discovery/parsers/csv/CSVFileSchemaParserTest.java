/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.parsers.csv;

import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.HiveTableSchema;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class CSVFileSchemaParserTest {

    private CSVFileSchemaParser parser = new CSVFileSchemaParser();

    private InputStream toInputStream(String text) {
        return new ByteArrayInputStream(text.getBytes());
    }

    @org.junit.Test
    public void testDefaultCSVParse() throws Exception {
        validateSchema1("col1,col2,col3\nr1v1,r1v2,r1v3\nr2v1,r2v2,r2v3\n");
    }

    @org.junit.Test
    public void testSemiColonCSVWithEscapeParse() throws Exception {
        parser.setSeparatorChar(";");
        validateSchema1("col1;col2;col3\nr1v1;r1v2;r1v3\nr2v1;r2v2;r2v3\n");
    }

    @org.junit.Test
    public void testSemiColonTSVWithEscapeParse() throws Exception {
        parser.setSeparatorChar("\t");
        validateSchema1("col1\tcol2\tcol3\nr1v1\tr1v2\tr1v3\nr2v1\tr2v2\tr2v3\n");
    }

    @org.junit.Test
    public void testNoHeader() throws Exception {
        parser.setHeaderRow(false);
        try (InputStream is = toInputStream("r1v1,r1v2,r1v3\nr2v1,r2v2,r2v3\n")) {
            HiveTableSchema schema = toHiveTableSchema(is);
            List<? extends Field> fields = schema.getFields();
            assertTrue(fields.size() == 3);

            IntStream.range(0, fields.size()).forEach(idx -> {
                assertEquals(fields.get(idx).getName(), "Col_" + (idx + 1));
                // Note: only loads one
                assertEquals(fields.get(idx).getSampleValues().size(), 1);
            });
        }
    }

    @org.junit.Test
    public void testSparse() throws Exception {
        // Test extra columns
        parser.setSeparatorChar("\t");
        validateSchema1("col1\tcol2\tcol3\nr1v1\tr1v2\tr1v3\tr1v4\nr2v1\tr2v2\tr2v3\n");
    }

    @org.junit.Test
    public void testCSVUnixFile() throws Exception {
        parser.setAutoDetect(true);
        validateSchema2("MOCK_DATA.csv_unix.txt");
    }

    @org.junit.Test
    public void testCSVWinFile() throws Exception {
        parser.setAutoDetect(true);
        validateSchema2("MOCK_DATA.csv_win.txt");
    }

    @org.junit.Test
    public void testTABUnixFile() throws Exception {
        parser.setAutoDetect(true);
        validateSchema2("MOCK_DATA.tab_unix.txt");
    }

    @org.junit.Test
    public void testExcel() throws Exception {
        parser.setAutoDetect(true);
        validateSchema2("MOCK_DATA.csv_excel.txt");
    }

    @org.junit.Test
    public void testPipeDelim() throws Exception {
        parser.setAutoDetect(true);
        validateSchema2("MOCK_DATA.pipe.txt");
    }

    private HiveTableSchema toHiveTableSchema(InputStream is) throws IOException {
        Schema schema = parser.parse(is, Charset.defaultCharset(), TableSchemaType.HIVE);
        assertTrue(schema != null);
        assertTrue(schema instanceof HiveTableSchema);
        return (HiveTableSchema) schema;
    }

    private HiveTableSchema validateSchema1(String text) throws IOException {
        try (InputStream is = toInputStream(text)) {
            HiveTableSchema schema = toHiveTableSchema(is);
            List<? extends Field> fields = schema.getFields();
            assertTrue(fields.size() == 3);

            IntStream.range(0, fields.size()).forEach(idx -> {
                assertEquals(fields.get(idx).getName(), "col" + (idx + 1));
                assertEquals(fields.get(idx).getSampleValues().size(), 2);
            });
            return schema;
        }
    }


    private HiveTableSchema validateSchema2(String filename) throws IOException {

        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream(filename)) {
            HiveTableSchema schema = toHiveTableSchema(is);
            List<? extends Field> fields = schema.getFields();
            assertTrue(fields.size() == 9);

            IntStream.range(0, fields.size()).forEach(idx -> {
                assertEquals("Expecting 9 samples values", 9, fields.get(idx).getSampleValues().size());
                switch (idx) {
                    case 0:
                        assertEquals(fields.get(idx).getName(), "id");
                        break;
                    case 1:
                        assertEquals(fields.get(idx).getName(), "first_name");
                        break;
                    case 2:
                        assertEquals(fields.get(idx).getName(), "last name");
                        break;
                    case 3:
                        assertEquals(fields.get(idx).getName(), "url");
                        break;
                    case 4:
                        assertEquals(fields.get(idx).getName(), "gender");
                        break;
                    case 5:
                        assertEquals(fields.get(idx).getName(), "ip_address");
                        break;
                    case 6:
                        assertEquals(fields.get(idx).getName(), "timezone");
                        break;
                    case 7:
                        assertEquals(fields.get(idx).getName(), "desc");
                        break;
                    case 8:
                        assertEquals(fields.get(idx).getName(), "comment");
                        break;
                }
            });
            return schema;
        }
    }

    @org.junit.Test
    public void testDeriveHiveRecordFormat() throws Exception {

    }
}