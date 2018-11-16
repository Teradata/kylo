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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class ParserUtil {

    private static final Logger log = LoggerFactory.getLogger(ParserUtil.class);

    /**
     * Maximum number of characters to sample from a file protecting from memory
     */
    protected static int MAX_CHARS = 128000;

    protected static int MAX_ROWS = 1000;

    /**
     * Extracts the given number of rows from the file and returns a new reader for the sample.
     * This method protects memory in the case where a large file can be submitted with no delimiters.
     */
    public static String extractSampleLines(InputStream is, Charset charset, int rows) throws IOException {

        StringWriter sw = new StringWriter();
        Validate.notNull(is, "empty input stream");
        Validate.notNull(charset, "charset cannot be null");
        Validate.exclusiveBetween(1, MAX_ROWS, rows, "invalid number of sample rows");

        // Sample the file in case there are no newlines
        StringWriter swBlock = new StringWriter();
        IOUtils.copyLarge(new InputStreamReader(is, charset), swBlock, -1, MAX_CHARS);
        try (BufferedReader br = new BufferedReader(new StringReader(swBlock.toString()))) {
            IOUtils.closeQuietly(swBlock);
            String line = br.readLine();
            int linesRead = 0;
            for (int i = 1; i <= rows && line != null; i++) {
                sw.write(line);
                sw.write("\n");
                linesRead++;
                line = br.readLine();
            }
            if (linesRead <= 1 && sw.toString().length() >= MAX_CHARS) {
                throw new IOException("Failed to detect newlines for sample file.");
            }
        }

        return sw.toString();
    }
}
