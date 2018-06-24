package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.process;

/*-
 * #%L
 * nifi-teradata-tdch-core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TdchOperationType;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.utils.TdchUtils;

import org.apache.nifi.logging.ComponentLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handle a stream from TDCH export Hive to Teradata, and clear it to avoid filling up buffer
 */
public class TdchExportHiveToTeradataThreadedStreamHandler extends Thread {

    private InputStream inputStream;
    private Boolean logLineWithInputRecordsFound = false;
    private Boolean logLineWithOutputRecordsFound = false;
    private Boolean logLineWithTdchExitCodeFound = false;
    private Boolean logLineWithTimeTakenFound = false;
    private ComponentLog logger;
    private TdchOperationType tdchOperationType;
    private String[] logLines;
    private CountDownLatch latch;

    public TdchExportHiveToTeradataThreadedStreamHandler(InputStream inputStream,
                                                         ComponentLog logger,
                                                         String[] logLines,
                                                         CountDownLatch latch,
                                                         TdchOperationType tdchOperationType) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream has invalid value of null");
        }
        checkNotNull(inputStream);
        checkNotNull(logger);
        checkNotNull(logLines);
        checkNotNull(latch);
        checkNotNull(tdchOperationType);
        this.inputStream = inputStream;
        this.logger = logger;
        this.logLines = logLines;
        this.latch = latch;
        this.tdchOperationType = tdchOperationType;

        this.logger.info("Input stream initialized for type: {} [{}]", new Object[]{inputStream.getClass().toString(), tdchOperationType});
    }

    public void run() {
        BufferedReader bufferedReader;
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                if ((!logLineWithInputRecordsFound) && (line.contains(TdchUtils.TDCH_EXPORT_HIVE_TO_TERADATA_INPUT_RECORDS_IDENTIFIER))) {
                    logLineWithInputRecordsFound = true;
                    logLines[0] = line;
                    latch.countDown();
                }

                if ((!logLineWithOutputRecordsFound) && (line.contains(TdchUtils.TDCH_EXPORT_HIVE_TO_TERADATA_OUTPUT_RECORDS_IDENTIFIER))) {
                    logLineWithOutputRecordsFound = true;
                    logLines[1] = line;
                    latch.countDown();
                }

                if ((!logLineWithTdchExitCodeFound) && (line.contains(TdchUtils.TDCH_EXPORT_HIVE_TO_TERADATA_EXIT_CODE_IDENTIFIER))) {
                    logLineWithTdchExitCodeFound = true;
                    logLines[2] = line;
                    latch.countDown();
                }

                if ((!logLineWithTimeTakenFound) && (line.contains(TdchUtils.TDCH_EXPORT_HIVE_TO_TERADATA_TIME_TAKEN_IDENTIFIER))) {
                    logLineWithTimeTakenFound = true;
                    logLines[3] = line;
                    latch.countDown();
                }

                logger.info(line);
            }
        } catch (IOException ioe) {
            logger.warn("I/O error occurred while handling stream. [{}] [{}]", new Object[]{tdchOperationType, ioe.getMessage()});
            ioe.printStackTrace();
        } catch (Throwable t) {
            logger.warn("An error occurred handling stream. [{}] [{}]", new Object[]{tdchOperationType, t.getMessage()});
            t.printStackTrace();
        } finally {
            while (latch.getCount() != 0) {
                latch.countDown();
            }
            try {
                bufferedReader.close();
            } catch (IOException ioe) {
                logger.warn("I/O error closing buffered reader for stream. [{}] [{}]", new Object[]{tdchOperationType, ioe.getMessage()});
                ioe.printStackTrace();
            }
        }
    }

}
