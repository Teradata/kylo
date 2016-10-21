package com.thinkbiganalytics.nifi.v2.sqoop.process;

/**
 * @author jagrut sharma
 */
import com.thinkbiganalytics.nifi.v2.sqoop.enums.SqoopLoadStrategy;

import org.apache.nifi.logging.ComponentLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/*
Class to handle a stream. Clear it out to avoid buffer overflow.
 */
public class SqoopThreadedStreamHandler extends Thread {

    /* Class variables */
    private InputStream inputStream;
    private Boolean logLineWithRetrievedRecordsFound = false;
    private final String SEARCH_STRING_FOR_RETRIEVED_RECORDS_FOUND = "INFO mapreduce.ImportJobBase: Retrieved";
    private final String SEARCH_STRING_FOR_NO_NEW_RECORDS_FOUND = "No new rows detected since last import";
    private Boolean logLineWithNewHighWaterMarkFound = false;
    private final String SEARCH_STRING_FOR_NEW_HIGH_WATERMARK_FOUND = "INFO tool.ImportTool:   --last-value";

    /* Logger */
    private ComponentLog logger = null;

    SqoopLoadStrategy sourceLoadStrategy;

    String[] logLines;
    CountDownLatch latch;

    /**
     * one argument Constructor
     * @param inputStream InputStream to be handled by this thread
     */
    public SqoopThreadedStreamHandler(InputStream inputStream,
                                      ComponentLog logger,
                                      String[] logLines,
                                      CountDownLatch latch,
                                      SqoopLoadStrategy sourceLoadStrategy) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream has invalid value of null");
        }
        checkNotNull(inputStream);
        checkNotNull(logger);
        checkNotNull(logLines);
        checkNotNull(latch);
        checkNotNull(sourceLoadStrategy);
        this.inputStream = inputStream;
        this.logger = logger;
        this.logLines = logLines;
        this.latch = latch;
        this.sourceLoadStrategy = sourceLoadStrategy;

        this.logger.info("Input stream initialized for type: " + inputStream.getClass().toString());
    }


    /**
     * Run the thread
     */
    public void run() {
        BufferedReader bufferedReader = null;
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;

        /* Print output to console. This will clear the buffer. */
        try {
            while ((line = bufferedReader.readLine()) != null) {

                if ((!logLineWithRetrievedRecordsFound)
                    &&
                    (line.contains(SEARCH_STRING_FOR_RETRIEVED_RECORDS_FOUND)
                     || line.contains(SEARCH_STRING_FOR_NO_NEW_RECORDS_FOUND))) {
                    logLineWithRetrievedRecordsFound = true;
                    logLines[0] = line;
                    latch.countDown();
                }

                if (this.sourceLoadStrategy != SqoopLoadStrategy.FULL_LOAD) {
                    if ((!logLineWithNewHighWaterMarkFound)
                        && (line.contains(SEARCH_STRING_FOR_NEW_HIGH_WATERMARK_FOUND))) {
                        logLineWithNewHighWaterMarkFound = true;
                        logLines[1] = line;
                        latch.countDown();
                    }
                }

                logger.info(line);
            }
        }
        catch (IOException ioe) {
            logger.error("Error handling stream");
            ioe.printStackTrace();
        }
        catch(Throwable t) {
            logger.error("Error handling stream");
            t.printStackTrace();
        }
        finally {
            for (long i = 0; i < latch.getCount(); i++) {
                latch.countDown();
            }
            try {
                bufferedReader.close();
            }
            catch(IOException ioe) {
                logger.warn("Error closing buffered reader for stream");
                //ioe.printStackTrace();
            }
        }
    }
}

