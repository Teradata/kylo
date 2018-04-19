/**
 * 
 */
package com.thinkbiganalytics.spark.multiexec;

/**
 *
 */
public class SparkAppExecException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SparkAppExecException(String message, Throwable cause) {
        super(message, cause);
    }

    public SparkAppExecException(String message) {
        super(message);
    }
}
