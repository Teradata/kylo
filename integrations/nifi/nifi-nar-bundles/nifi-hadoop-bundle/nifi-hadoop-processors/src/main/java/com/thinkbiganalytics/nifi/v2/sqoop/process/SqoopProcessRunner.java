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

import com.thinkbiganalytics.nifi.v2.sqoop.enums.SqoopLoadStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.security.KerberosConfig;

import org.apache.nifi.logging.ComponentLog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Run a sqoop job via a system process
 */
public class SqoopProcessRunner {

    private List<String> commands = new ArrayList<>();
    private KerberosConfig kerberosConfig = null;

    private ComponentLog logger = null;

    private SqoopLoadStrategy sourceLoadStrategy;

    private CountDownLatch latch;
    private String[] logLines;

    /**
     * Constructor
     *
     * @param kerberosConfig     kerberos configuration
     * @param commands           command list to run
     * @param logger             logger
     * @param sourceLoadStrategy load strategy (full/incremental)
     */
    public SqoopProcessRunner(KerberosConfig kerberosConfig,
                              final List<String> commands,
                              ComponentLog logger,
                              SqoopLoadStrategy sourceLoadStrategy) {
        if (commands == null) {
            throw new IllegalArgumentException("Unable to create SqoopProcessRunner with null arguments");
        }

        checkNotNull(kerberosConfig);
        checkNotNull(commands);
        checkNotNull(logger);
        checkNotNull(sourceLoadStrategy);

        this.kerberosConfig = kerberosConfig;
        this.commands = commands;
        this.logger = logger;
        this.sourceLoadStrategy = sourceLoadStrategy;

        if (sourceLoadStrategy == SqoopLoadStrategy.FULL_LOAD) {
            latch = new CountDownLatch(1);
            logLines = new String[1];
        } else if (sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_APPEND
                   || sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_LASTMODIFIED) {
            latch = new CountDownLatch(2);
            logLines = new String[2];
        } else {
            //this should not occur
            latch = new CountDownLatch(1);
            logLines = new String[1];
        }

        this.logger.info("SqoopProcessRunner initialized.");
    }

    /**
     * Execute the process
     *
     * @return result of execution as {@link SqoopProcessResult}
     */
    public SqoopProcessResult execute() {
        logger.info("Executing sqoop command");
        int exitValue = -1;

        try {
            ProcessBuilder pb = new ProcessBuilder(commands);

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

            SqoopThreadedStreamHandler inputStreamHandler = new SqoopThreadedStreamHandler(inputStream, logger, logLines, latch, sourceLoadStrategy);
            SqoopThreadedStreamHandler errorStreamHandler = new SqoopThreadedStreamHandler(errorStream, logger, logLines, latch, sourceLoadStrategy);

            inputStreamHandler.start();
            errorStreamHandler.start();

            logger.info("Waiting for sqoop job to complete");
            latch.await();
            exitValue = process.waitFor();

            inputStreamHandler.interrupt();
            errorStreamHandler.interrupt();

            inputStreamHandler.join();
            errorStreamHandler.join();

            logger.info("Sqoop job completed");

            return new SqoopProcessResult(exitValue, logLines);
        } catch (Exception e) {
            logger.error("Error running sqoop command [{}].", new Object[]{e.getMessage()});

            for (long i = 0; i < latch.getCount(); i++) {
                latch.countDown();
            }
            return new SqoopProcessResult(exitValue, logLines);
        }
    }

    /*
    Get the full command to be executed
     */
    @SuppressWarnings("unused")
    private String getFullCommand() {

        StringBuffer retVal = new StringBuffer();
        for (String c : commands) {
            retVal.append(c);
            retVal.append(" ");
        }
        return retVal.toString();
    }
}


