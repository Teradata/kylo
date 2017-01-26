package com.thinkbiganalytics.discovery.util;

/*-
 * #%L
 * thinkbig-schema-discovery-api
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

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.JDBCType;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParserHelperTest {


    @Before
    public void setUp() throws Exception {

    }

    public void textExtract(String text, int numRows, int numExpected) throws Exception {
        try (InputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
            String value = ParserHelper.extractSampleLines(is, StandardCharsets.UTF_8, numRows);
            assertEquals(numExpected, value.split("\n").length);
        }
    }


    @Test
    public void testExtractSample10Lines() throws Exception {
        String text = "col1,col2,col3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\n";
        textExtract(text, 10, 10);
    }

    @Test
    public void testExtractMaxSampleLines() throws Exception {
        String text = "col1,col2,col3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\nv1,v2,v3\n";
        textExtract(text, 100, 12);
    }

    @Test
    public void testInvalidFile() throws Exception {
        String text = StringUtils.leftPad("Z", ParserHelper.MAX_CHARS, "Z");
        try {
            textExtract(text, 100, 1);
            fail();
        } catch (IOException e) {
            // good
        }
    }


    @Test
    public void testDeriveJDBCDataType() throws Exception {
        assertEquals("DOUBLE", ParserHelper.deriveJDBCDataType(Arrays.asList("1.0", "20000", "-64.2001")).getName());
        assertEquals("INTEGER", ParserHelper.deriveJDBCDataType(Arrays.asList("1", "20000", "64")).getName());
        assertEquals("VARCHAR", ParserHelper.deriveJDBCDataType(Arrays.asList("1L", "200,00", "64")).getName());
        assertEquals("VARCHAR", ParserHelper.deriveJDBCDataType(Arrays.asList("BOB", "20", "64")).getName());
        assertEquals("VARCHAR", ParserHelper.deriveJDBCDataType(null).getName());
    }

    @Test

    public void testSqlTypeToHiveType() throws Exception {
        assertEquals(ParserHelper.sqlTypeToHiveType(JDBCType.DOUBLE), "double");
    }

    @Test
    public void testDeriveDataTypes() throws Exception {
        TestField f1 = new TestField();
        //f1.setNativeDataType("");
        f1.setSampleValues(Arrays.asList("10", "20", "30"));

        TestField f2 = new TestField();
        f2.setNativeDataType("");
        f2.setSampleValues(Arrays.asList("10.2", "20.3", "30.4"));

        TestField f3 = new TestField();
        f3.setNativeDataType("");
        f3.setSampleValues(Arrays.asList("BOB", "20.3", "30.4"));

        TestField f4 = new TestField();
        f4.setNativeDataType("BIGINT");
        f4.setSampleValues(Arrays.asList("2015", "203", "304"));

        TestField f5 = new TestField();
        f5.setNativeDataType("INVALIDTYPE");
        f5.setSampleValues(Arrays.asList("BOB", "20.3", "30.4"));

        TestField f6 = new TestField();
        f6.setSampleValues(null);

        ParserHelper.deriveDataTypes(TableSchemaType.HIVE, Arrays.asList(f1, f2, f3, f4, f5, f6));
        assertEquals("int", f1.getDerivedDataType());
        assertEquals("double", f2.getDerivedDataType());
        assertEquals("string", f3.getDerivedDataType());
        assertEquals("bigint", f4.getDerivedDataType());
        assertEquals("string", f5.getDerivedDataType());
        assertEquals("string", f6.getDerivedDataType());

    }
}
