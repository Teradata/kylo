/*
 * Copyright (c) 2015. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.logging.LogLevel;
import org.apache.nifi.logging.ProcessorLog;

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
    private ProcessorLog logger;
    private LogLevel level;

    public InputStreamReaderRunnable(LogLevel level, ProcessorLog logger, InputStream is) {
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