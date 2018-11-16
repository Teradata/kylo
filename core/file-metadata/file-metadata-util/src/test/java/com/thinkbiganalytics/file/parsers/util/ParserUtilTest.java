package com.thinkbiganalytics.file.parsers.util;
/*-
 * #%L
 * kylo-file-metadata-util
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
import com.thinkbiganalytics.file.parsers.util.ParserUtil;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParserUtilTest {



    public void textExtract(String text, int numRows, int numExpected) throws Exception {
        try (InputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
            String value = ParserUtil.extractSampleLines(is, StandardCharsets.UTF_8, numRows);
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
        String text = StringUtils.leftPad("Z", ParserUtil.MAX_CHARS, "Z");
        try {
            textExtract(text, 100, 1);
            fail();
        } catch (IOException e) {
            // good
        }
    }

}
