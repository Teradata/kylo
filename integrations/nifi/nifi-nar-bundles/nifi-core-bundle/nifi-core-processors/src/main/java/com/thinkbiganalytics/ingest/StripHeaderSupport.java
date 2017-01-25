package com.thinkbiganalytics.ingest;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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
