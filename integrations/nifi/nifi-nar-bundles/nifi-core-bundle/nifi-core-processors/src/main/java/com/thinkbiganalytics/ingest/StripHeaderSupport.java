/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.ingest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;


public class StripHeaderSupport {

    /**
     * Identify the byte boundary of the end of the header
     *
     * @param headerRows the number of header rows
     * @param is         the input steram
     * @return the bytes
     */
    public long findHeaderBoundary(int headerRows, InputStream is) throws IOException {
        int rows = 0;
        long bytes = 0;
        boolean eof = false;
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            while (rows < headerRows && !eof) {
                Boundary boundary = nextLineBoundary(bis);
                eof = boundary.isEOF;
                bytes += boundary.bytes;
                rows++;
            }
        }
        if (rows < headerRows) {
            return -1L;
        }
        return bytes;
    }


    protected Boundary nextLineBoundary(BufferedInputStream bis) throws IOException {

        int lastByte = -1;
        long numBytesRead = 0L;
        Boundary boundary = new Boundary();

        while (true) {
            bis.mark(1);
            final int thisByte = bis.read();

            if (thisByte == -1) {
                boundary.isEOF = true;
                boundary.bytes = numBytesRead;
                return boundary;
            }

            numBytesRead++;

            if (thisByte == '\n') {
                boundary.bytes = numBytesRead;
                return boundary;

            } else if (lastByte == '\r') {
                bis.reset();
                numBytesRead--;
                boundary.bytes = numBytesRead;
                return boundary;

            }
            lastByte = thisByte;
        }

    }

    static class Boundary {

        long bytes;
        boolean isEOF;
    }
}