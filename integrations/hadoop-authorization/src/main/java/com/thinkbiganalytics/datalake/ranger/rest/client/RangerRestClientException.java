package com.thinkbiganalytics.datalake.ranger.rest.client;

/**
 * RangerRestClientException : Exception Supporting class
 * @author sv186029
 *
 */

public class RangerRestClientException extends Exception {

    public RangerRestClientException() {
    }

    public RangerRestClientException(String message) {
        super(message);
    }

    public RangerRestClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public RangerRestClientException(Throwable cause) {
        super(cause);
    }

    public RangerRestClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}