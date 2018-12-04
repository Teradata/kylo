package com.thinkbiganalytics.spark.shell;

/*-
 * #%L
 * Spark Shell Core
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

import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;

/**
 * Writes to a {@link Logger}.
 */
public class LoggerOutputStream extends ByteArrayOutputStream {

    /**
     * Destination logger
     */
    @Nonnull
    private final Logger logger;

    /**
     * Constructs a {@code LoggerOutputStream} that writes to the specified logger
     * @param logger the destination logger
     */
    public LoggerOutputStream(@Nonnull final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void write(final int b) {
        if (logger.isInfoEnabled()) {
            super.write(b);
            extractMessages();
        }
    }

    @Override
    public void write(final byte[] b, final int off, final int len) {
        if (logger.isInfoEnabled()) {
            super.write(b, off, len);
            extractMessages();
        }
    }

    /**
     * Extracts messages from the byte buffer and writes them to the logger.
     */
    private synchronized void extractMessages() {
        boolean more = true;

        while (more) {
            // Look for a message to log
            boolean found = false;
            int i;

            for (i = 0; i < count && !found; ++i) {
                found = (buf[i] == '\n');
            }

            // Log the message or exit loop
            if (found) {
                if (logger.isInfoEnabled()) {
                    logger.info(new String(buf, 0, i + 1));
                }
                count -= i + 1;
                System.arraycopy(buf, i + 1, buf, 0, count);
            } else {
                more = false;
            }
        }
    }
}
