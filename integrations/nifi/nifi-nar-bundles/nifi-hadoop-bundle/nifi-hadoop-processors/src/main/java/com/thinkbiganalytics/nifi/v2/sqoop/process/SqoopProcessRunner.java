package com.thinkbiganalytics.nifi.v2.sqoop.process;


import com.thinkbiganalytics.nifi.v2.sqoop.security.KerberosConfig;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.SqoopLoadStrategy;

import org.apache.nifi.logging.ComponentLog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * @author jagrut sharma
 */
/*
Class to run a sqoop job via a system process
 */
public class SqoopProcessRunner {

    private List<String> commands = new ArrayList<>();
    private KerberosConfig kerberosConfig = null;
    private SqoopThreadedStreamHandler inputStreamHandler;
    private SqoopThreadedStreamHandler errorStreamHandler;

    private ComponentLog logger = null;

    private SqoopLoadStrategy sourceLoadStrategy;

    CountDownLatch latch;
    String[] logLines;

    /**
     * One argument constructor
     * @param commands Commands to run, as a list
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
        }
        else if (sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_APPEND
                 || sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_LASTMODIFIED) {
            latch = new CountDownLatch(2);
            logLines = new String[2];
        }
        else {
            //this should not occur
            latch = new CountDownLatch(1);
            logLines = new String[1];
        }

        this.logger.info("SqoopProcessRunner initialized.");
    }

    /**
     * Execute the process
     * @return exit code (0 indicates success, other codes indicate failure)
     */
    public SqoopProcessResult execute() {
        logger.info("Executing command: " + getFullCommand());
        int exitValue = -1;

        try {
            ProcessBuilder pb = new ProcessBuilder(commands);

            if (kerberosConfig.isKerberosConfigured()) {
                ProcessBuilder processBuilderKerberosInit = new ProcessBuilder(kerberosConfig.getKinitCommandAsList());
                Process processKerberosInit = processBuilderKerberosInit.start();
                int kerberosInitExitValue = processKerberosInit.waitFor();
                if (kerberosInitExitValue != 0) {
                    logger.info("Kerberos kinit failed ({})", new Object[] { kerberosConfig.getKinitCommandAsString() });
                    throw new Exception("Kerberos init failed");
                }
            }

            Process process = pb.start();

            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();

            inputStreamHandler = new SqoopThreadedStreamHandler(inputStream, logger, logLines, latch, sourceLoadStrategy);
            errorStreamHandler = new SqoopThreadedStreamHandler(errorStream, logger, logLines, latch, sourceLoadStrategy); // no latch countdown from this

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
        }
        catch (Exception e) {
            logger.error("Error running command: " + getFullCommand());

            for (long i = 0; i < latch.getCount(); i++) {
                latch.countDown();
            }
            return new SqoopProcessResult(exitValue, logLines);
        }
    }


    /**
     * Get the full command to be executed
     * @return command
     */
    public String getFullCommand() {

        StringBuffer retVal = new StringBuffer();
        for (String c: commands) {
            retVal.append(c);
            retVal.append(" ");
        }
        return retVal.toString();
    }
}


