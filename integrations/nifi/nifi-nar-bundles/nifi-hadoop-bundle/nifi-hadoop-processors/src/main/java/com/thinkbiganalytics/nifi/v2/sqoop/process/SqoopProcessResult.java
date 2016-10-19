package com.thinkbiganalytics.nifi.v2.sqoop.process;

/**
 * @author jagrut sharma
 */

/*
Class to store the exit value and log output (containing number of records and next high watermark) after a sqoop run
 */
public class SqoopProcessResult {
    private int exitValue = -1;
    private String[] logLines = null;

    public SqoopProcessResult(int exitValue, String[] logLines) {
        this.exitValue = exitValue;
        this.logLines = logLines;
    }

    public int getExitValue() {
        return exitValue;
    }

    public String[] getLogLines() {
        return logLines;
    }
}
