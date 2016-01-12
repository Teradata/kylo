package com.thinkbiganalytics.spark.helpers;

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
    
    public InputStreamReaderRunnable(InputStream is) {
        this.reader = new BufferedReader(new InputStreamReader(is));
    }

    public void run() {
        try {
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}