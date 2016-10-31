/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.schema;

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

/**
 * Provides utility methods useful for writing parsers
 * Note: This will eventually be replaced by the new discovery api
 */
public class ParserHelper2 {

    private static final Logger log = LoggerFactory.getLogger(ParserHelper2.class);

    /**
     * Maximum number of characters to sample from a file protecting from memory
     */
    private static int MAX_CHARS = 64000;

    private static int MAX_ROWS = 1000;

    /**
     * Extracts the given number of rows from the file and returns a new reader for the sample.  This method protects memory in the case where a large file can be submitted with no delimiters.
     */
    public static String extractSampleLines(InputStream is, Charset charset, int rows) throws IOException {

        StringWriter sw = new StringWriter();
        Validate.notNull(is, "empty input stream");
        Validate.notNull(charset, "charset cannot be null");
        Validate.exclusiveBetween(1, MAX_ROWS, rows, "invalid number of sample rows");

        // Sample the file in case there are no newlines we don't want to
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
            if (linesRead <= 1 && sw.toString().length() >= MAX_CHARS) throw new IOException("Failed to detect newlines for sample file.");
        }

        return sw.toString();
    }




}
