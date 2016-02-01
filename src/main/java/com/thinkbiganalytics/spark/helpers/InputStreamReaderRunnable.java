package com.thinkbiganalytics.spark.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
 * This is a helper class
 * Used for reading and clearing Process buffers
 */

public class InputStreamReaderRunnable implements Runnable {

    private Logger logger = LoggerFactory.getLogger(InputStreamReaderRunnable.class);

    private BufferedReader reader;
    
    public InputStreamReaderRunnable(InputStream is) {
        this.reader = new BufferedReader(new InputStreamReader(is));
    }

    public void run() {
        try {
            String line = reader.readLine();
            while (line != null) {
                logger.info(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}