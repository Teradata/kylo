package com.thinkbiganalytics.nifi.v2.sqoop.process;

/**
 * Class to store the result of sqoop command execution
 * @author jagrut sharma
 */
public class SqoopProcessResult {
    private int exitValue = -1;
    private String[] logLines = null;

    /**
     * Constructor
     * @param exitValue exit value of sqoop command execution
     * @param logLines log information with number of records, next high watermark
     */
    public SqoopProcessResult(int exitValue, String[] logLines) {
        this.exitValue = exitValue;
        this.logLines = logLines.clone();
    }

    /**
     * Get exit value of sqoop command execution (0 is success, any other value indicates failure)
     * @return exit value
     */
    public int getExitValue() {
        return exitValue;
    }

    /**
     * Get log information with number of records, next high watermark
     * @return log information as array of strings
     */
    public String[] getLogLines() {
        if (logLines != null) {
            return logLines.clone();
        }
        else {
            return new String[]{};
        }
    }
}
