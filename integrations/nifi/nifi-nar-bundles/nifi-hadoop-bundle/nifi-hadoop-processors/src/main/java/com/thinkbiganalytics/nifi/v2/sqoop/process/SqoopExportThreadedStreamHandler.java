package com.thinkbiganalytics.nifi.v2.sqoop.process;

/*-
 * #%L
 * thinkbig-nifi-hadoop-processors
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

import org.apache.nifi.logging.ComponentLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Class to handle a sqoop export stream, and clear it to avoid filling up buffer
 */
class SqoopExportThreadedStreamHandler extends Thread {

    private InputStream inputStream;
    private Boolean logLineWithExportedRecordsFound = false;

    private ComponentLog logger = null;

    private String[] logLines;
    private CountDownLatch latch;

    public SqoopExportThreadedStreamHandler(InputStream inputStream,
                                            ComponentLog logger,
                                            String[] logLines,
                                            CountDownLatch latch) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream has invalid value of null");
        }

        checkNotNull(inputStream);
        checkNotNull(logger);
        checkNotNull(logLines);
        checkNotNull(latch);

        this.inputStream = inputStream;
        this.logger = logger;
        this.logLines = logLines;
        this.latch = latch;

        this.logger.info("Input stream initialized for type: " + inputStream.getClass().toString());
    }

    /**
     * Run the thread
     */
    public void run() {
        BufferedReader bufferedReader;
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;

        /* Clear the buffer */
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String SEARCH_STRING_FOR_EXPORTED_RECORDS_FOUND = "INFO mapreduce.ExportJobBase: Exported";

                if ((!logLineWithExportedRecordsFound) && (line.contains(SEARCH_STRING_FOR_EXPORTED_RECORDS_FOUND))) {
                    logLineWithExportedRecordsFound = true;
                    logLines[0] = line;
                    latch.countDown();
                }

                logger.info(line);
            }
        } catch (IOException ioe) {
            logger.warn("I/O error occurred while handling stream. [{}]", new Object[]{ioe.getMessage()});
        } catch (Throwable t) {
            logger.warn("An error occurred handling stream. [{}]", new Object[]{t.getMessage()});
        } finally {
            for (long i = 0; i < latch.getCount(); i++) {
                latch.countDown();
            }
            try {
                bufferedReader.close();
            } catch (IOException ioe) {
                logger.warn("I/O error closing buffered reader for stream");
            }
        }
    }
}
