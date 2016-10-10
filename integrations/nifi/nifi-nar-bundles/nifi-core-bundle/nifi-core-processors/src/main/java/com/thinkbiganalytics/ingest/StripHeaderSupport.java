/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.ingest;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.stream.io.BufferedOutputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by matthutton on 10/10/16.
 */
public class StripHeaderSupport {

    private static final int BUFFER_SIZE = 8192;

    /**
     * Writes header and content to different output streams and returns the number of header rows found
     *
     * @param headerCount numer of lines to include in header
     * @param rawIn       the file input
     * @param headerOut   the header portion
     * @param contentOut  the content portion
     * @return number of header rows found
     */
    public int doStripHeader(int headerCount, InputStream rawIn, OutputStream headerOut, OutputStream contentOut) throws IOException {

        int rows = 0;
        try (final BufferedReader contentReader = new BufferedReader(new InputStreamReader(rawIn, "UTF-8")); final BufferedOutputStream bufferedHeaderOut = new BufferedOutputStream(headerOut); final
        BufferedOutputStream bufferedContentOut = new BufferedOutputStream(contentOut)) {

            while (rows < headerCount && contentReader.ready()) {
                String next = contentReader.readLine()+"\n";
                rows++;
                bufferedHeaderOut.write(next.getBytes());
            }

            if (rows < headerCount) {
                //errorMessage.set("Header Line Count is set to " + headerCount + " but file had only " + rows + " lines");
                return rows;
            }
            // Copy remaining
            IOUtils.copy(contentReader, bufferedContentOut, "UTF-8");
        }
        return rows;

    }

}
