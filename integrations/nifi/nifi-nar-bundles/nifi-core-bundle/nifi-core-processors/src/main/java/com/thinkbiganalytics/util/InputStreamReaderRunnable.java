package com.thinkbiganalytics.util;

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

import org.apache.commons.io.IOUtils;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.logging.LogLevel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
 * This is a helper class
 * Used for reading and clearing Process buffers
 */

public class InputStreamReaderRunnable implements Runnable {

    private BufferedReader reader;
    private ComponentLog logger;
    private LogLevel level;

    public InputStreamReaderRunnable(LogLevel level, ComponentLog logger, InputStream is) {
        this.level = level;
        this.logger = logger;
        this.reader = new BufferedReader(new InputStreamReader(is));
    }

    public void run() {
        try {
            String line = reader.readLine();
            while (line != null) {
                if (level == LogLevel.DEBUG) {
                    logger.debug(line);
                } else if (level == LogLevel.INFO) {
                    logger.info(line);
                } else if (level == LogLevel.WARN) {
                    logger.warn(line);
                } else if (level == LogLevel.ERROR) {
                    logger.error(line);
                }
                line = reader.readLine();
            }
            IOUtils.closeQuietly(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
