/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.util;

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
                //System.out.println(line);
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
