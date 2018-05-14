package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base;

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

import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.process.TdchExportHiveToTeradataThreadedStreamHandler;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.common.KerberosConfig;

import org.apache.nifi.logging.ComponentLog;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Run a TDCH job via a system process
 */
public class TdchProcessRunner {

    private List<String> commands;
    private KerberosConfig kerberosConfig;
    private ComponentLog logger;
    private TdchOperationType tdchOperationType;
    private CountDownLatch latch;
    private String[] logLines;
    private Map<String, String> tdchEnvironmentVariables;

    public TdchProcessRunner(KerberosConfig kerberosConfig,
                             final List<String> commands,
                             ComponentLog logger,
                             TdchOperationType tdchOperationType,
                             Map<String, String> tdchEnvironmentVariables) {
        if (commands == null) {
            throw new IllegalArgumentException("Unable to create TdchProcessRunner with null arguments");
        }

        checkNotNull(kerberosConfig);
        checkNotNull(commands);
        checkNotNull(logger);
        checkNotNull(tdchOperationType);
        checkNotNull(tdchEnvironmentVariables);

        this.kerberosConfig = kerberosConfig;
        this.commands = commands;
        this.logger = logger;
        this.tdchOperationType = tdchOperationType;
        this.tdchEnvironmentVariables = tdchEnvironmentVariables;

        switch (tdchOperationType) {
            case TDCH_EXPORT:
                int NUMBER_OF_LINES_TO_DETECT = 4;
                latch = new CountDownLatch(NUMBER_OF_LINES_TO_DETECT);
                logLines = new String[NUMBER_OF_LINES_TO_DETECT];
                break;
            case TDCH_IMPORT:
                throw new UnsupportedOperationException("TDCH Import not yet implemented");
            default:
                throw new IllegalArgumentException("Unsupported operation type: " + tdchOperationType.toString());
        }

        this.logger.info("TdchProcessRunner initialized ({})", new Object[]{tdchOperationType.toString()});
    }

    public TdchProcessResult execute() {
        logger.info("Executing TDCH command ({})", new Object[]{tdchOperationType.toString()});
        int exitValue = -1;

        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            if ((tdchEnvironmentVariables) != null && (tdchEnvironmentVariables.size() > 0)) {
                pb.environment().putAll(tdchEnvironmentVariables);
            }

            if (kerberosConfig.isKerberosConfigured()) {
                logger.info("Kerberos service principal and keytab are provided.");
                ProcessBuilder processBuilderKerberosInit = new ProcessBuilder(kerberosConfig.getKinitCommandAsList());
                Process processKerberosInit = processBuilderKerberosInit.start();
                int kerberosInitExitValue = processKerberosInit.waitFor();
                if (kerberosInitExitValue != 0) {
                    logger.error("Kerberos kinit failed ({})", new Object[]{kerberosConfig.getKinitCommandAsString()});
                    throw new Exception("Kerberos kinit failed");
                } else {
                    logger.info("Kerberos kinit succeeded");
                }
            }

            Process process = pb.start();

            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();

            TdchExportHiveToTeradataThreadedStreamHandler inputStreamHandler = new TdchExportHiveToTeradataThreadedStreamHandler(inputStream, logger, logLines, latch, tdchOperationType);
            TdchExportHiveToTeradataThreadedStreamHandler errorStreamHandler = new TdchExportHiveToTeradataThreadedStreamHandler(errorStream, logger, logLines, latch, tdchOperationType);

            inputStreamHandler.start();
            errorStreamHandler.start();

            logger.info("Waiting for TDCH job to complete");
            latch.await();
            exitValue = process.waitFor();

            inputStreamHandler.interrupt();
            errorStreamHandler.interrupt();

            inputStreamHandler.join();
            errorStreamHandler.join();

            logger.info("TDCH job completed");

            return new TdchProcessResult(exitValue, logLines, tdchOperationType);
        } catch (Exception e) {
            logger.error("Error running TDCH command [{}].", new Object[]{e.getMessage()});

            while (latch.getCount() != 0) {
                latch.countDown();
            }

            return new TdchProcessResult(exitValue, logLines, tdchOperationType);
        }
    }

    /*
    Get the full command to be executed
     */
    private String getFullCommand() {

        StringBuilder retVal = new StringBuilder();
        for (String c : commands) {
            retVal.append(c);
            retVal.append(" ");
        }
        return retVal.toString();
    }
}
