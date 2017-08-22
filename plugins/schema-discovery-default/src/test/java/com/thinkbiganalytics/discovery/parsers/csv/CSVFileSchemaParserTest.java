package com.thinkbiganalytics.discovery.parsers.csv;

/*-
 * #%L
 * thinkbig-schema-discovery-default
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
import static org.junit.Assert.fail;

/**
 */
public class CSVFileSchemaParserTest {

    private CSVFileSchemaParser parser = new CSVFileSchemaParser();

    private InputStream toInputStream(String text) {
        return new ByteArrayInputStream(text.getBytes());
    }

    @org.junit.Test
    public void testEmbeddedCommas() throws Exception {
        validateSchema1("col1,col2,col3\n\"Edoceo, Inc.\",Seattle,WA\nfoo,bar,fee");
    }

    @org.junit.Test
    public void testEmbeddedCommasNoAutodetect() throws Exception {
        parser.setAutoDetect(false);
        validateSchema1("col1,col2,col3\n\"Edoceo, Inc.\",Seattle,WA\nfoo,bar,fee");
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
    public void testTildeCSVWithEscapeParse() throws Exception {
        parser.setSeparatorChar("~");
        validateSchema1("col1~col2~col3\nr1v1~r1v2~r1v3\nr2v1~r2v2~r2v3\n");
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
    public void testNoHeaderFirstRowDuplicateValues() throws Exception {
        parser.setHeaderRow(false);
        firstRowDuplicateValues();
    }

    @org.junit.Test (expected=IllegalArgumentException.class)
    public void testHeaderFirstRowDuplicateValues() throws Exception {
        parser.setHeaderRow(true);
        firstRowDuplicateValues();

    }

    private void firstRowDuplicateValues() throws Exception {
        try (InputStream is = toInputStream("r1v1,r1v1,r1v3\nr2v1,r2v2,r2v3\n")) {
            HiveTableSchema schema = toHiveTableSchema(is);
            List<? extends Field> fields = schema.getFields();
            assertTrue(fields.size() == 3);

            IntStream.range(0, fields.size()).forEach(idx -> {
                assertEquals(fields.get(idx).getName(), "Col_" + (idx + 1));
                assertEquals(fields.get(idx).getSampleValues().size(), 1);
            });
        }
    }

    @org.junit.Test
    public void testNoHeaderFirstRowNoDuplicateValues() throws Exception {
        parser.setHeaderRow(false);
        try (InputStream is = toInputStream("HEAD_1,HEAD_2,HEAD_3\nr2v1,r2v2,r2v3\n")) {
            HiveTableSchema schema = toHiveTableSchema(is);
            List<? extends Field> fields = schema.getFields();
            assertTrue(fields.size() == 3);

            IntStream.range(0, fields.size()).forEach(idx -> {
                assertEquals(fields.get(idx).getName(), "Col_" + (idx + 1));
                assertEquals(fields.get(idx).getSampleValues().size(), 1);
            });
        }
    }

    @org.junit.Test
    public void testHeaderFirstRowNoDuplicateValues() throws Exception {
        parser.setHeaderRow(true);
        try (InputStream is = toInputStream("HEAD_1,HEAD_2,HEAD_3\nr2v1,r2v2,r2v3\n")) {
            HiveTableSchema schema = toHiveTableSchema(is);
            List<? extends Field> fields = schema.getFields();
            assertTrue(fields.size() == 3);

            IntStream.range(0, fields.size()).forEach(idx -> {
                assertEquals(fields.get(idx).getName(), "HEAD_" + (idx + 1));
                assertEquals(fields.get(idx).getSampleValues().size(), 1);
            });
        }
    }

    @org.junit.Test
    public void testSparse() throws Exception {
        // Test extra columns
        parser.setSeparatorChar("\t");
        try {
            validateSchema1("col1\tcol2\tcol3\nr1v1\tr1v2\tr1v3\tr1v4\nr2v1\tr2v2\tr2v3\n");
            fail("Expecting unrecognized format");
        } catch (IOException e) {
            checkInvalidFormatException(e);
        }
    }

    @org.junit.Test
    public void testCSVUnixFile() throws Exception {
        parser.setAutoDetect(true);
        validateSchema2("MOCK_DATA.csv_unix.txt");
        assertTrue("Expecting csv delim", ",".equals(parser.getSeparatorChar()));
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
        assertTrue("Expecting tab delim", "\t".equals(parser.getSeparatorChar()));
    }

    @org.junit.Test
    public void testExcel() throws Exception {
        parser.setAutoDetect(true);
        validateSchema2("MOCK_DATA.csv_excel.txt");
    }

    @org.junit.Test
    public void testCustom() throws Exception {
        parser.setAutoDetect(false);
        parser.setSeparatorChar("*");
        validateSchema2("MOCK_DATA.custom.txt");

    }

    @org.junit.Test
    public void testPipeDelim() throws Exception {
        parser.setAutoDetect(true);
        validateSchema2("MOCK_DATA.pipe.txt");
        assertTrue("Expecting pipe delim", "|".equals(parser.getSeparatorChar()));
    }

    @org.junit.Test
    public void testSingleQuotedDelim() throws Exception {
        parser.setAutoDetect(true);
        validateSchema2("MOCK_DATA_csv_singlequote.txt");
        assertTrue("Expecting comma delim", ",".equals(parser.getSeparatorChar()));
        assertTrue("Expecting single quote char", "'".equals(parser.getQuoteChar()));
    }

    @org.junit.Test
    public void testSingleQuoted() throws Exception {
        // Test single quoted string with embedded quote "
        parser.setAutoDetect(true);
        validateSchema1("col1,col2,col3\n'\"Edoceo, Inc.',Seattle,WA\nfoo,bar,fee");
        assertTrue("Expecting comma delim", ",".equals(parser.getSeparatorChar()));
        assertTrue("Expecting single quote char", "'".equals(parser.getQuoteChar()));
    }

    @org.junit.Test
    public void testEmptyStream() throws Exception {
        parser.setAutoDetect(true);
        try {
            validateSchema2("missingfile.txt");
            fail("Expecting error for missing file or empty stream");
        } catch (NullPointerException e) {
            // ok
        }
    }

    @org.junit.Test
    public void testNoDelimFound() throws Exception {
        // Should return defaults and not error
        parser.setAutoDetect(true);
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("junk.txt")) {
            try {
                HiveTableSchema schema = toHiveTableSchema(is);
                fail("Expecting unrecognized format");
            } catch (IOException e) {
                checkInvalidFormatException(e);
            }
        }
    }

    @org.junit.Test
    public void testUTF16() throws Exception {
        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream("MOCK_DATA_utf16_encoded.txt")) {
            parser.setAutoDetect(false);
            parser.setSeparatorChar(",");
            parser.setQuoteChar("\t");
            HiveTableSchema schema = toHiveTableSchema(is, Charset.forName("UTF-16LE"));
            assertEquals("UTF-16LE", schema.getCharset());
            List<? extends Field> fields = schema.getFields();
            assertTrue(fields.size() == 4);

            IntStream.range(0, fields.size()).forEach(idx -> {
                assertEquals(fields.get(idx).getName(), "col" + (idx + 1));
                assertEquals(fields.get(idx).getSampleValues().size(), 4);
            });
        }
    }

    private void checkInvalidFormatException(IOException e) {
        assertTrue("Expecting unrecognized format exception", e.getLocalizedMessage().contains("Unrecognized format"));
    }

    private HiveTableSchema toHiveTableSchema(InputStream is) throws IOException {
        return toHiveTableSchema(is, Charset.defaultCharset());
    }

    private HiveTableSchema toHiveTableSchema(InputStream is, Charset cs) throws IOException {
        Schema schema = parser.parse(is, cs, TableSchemaType.HIVE);
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
            //System.out.println(schema.getHiveFormat());
            return schema;
        }
    }


    private HiveTableSchema validateSchema2(String filename) throws IOException {

        try (InputStream is = CSVFileSchemaParserTest.class.getClassLoader().getResourceAsStream(filename)) {
            HiveTableSchema schema = toHiveTableSchema(is);
            List<? extends Field> fields = schema.getFields();
            assertTrue("Expecting 9 fields", fields.size() == 9);

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


}
