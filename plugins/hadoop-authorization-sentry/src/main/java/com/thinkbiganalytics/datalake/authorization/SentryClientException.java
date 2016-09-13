package com.thinkbiganalytics.datalake.authorization;

/**
 * SentryClientException : Exception Supporting Class
 *
 * @author sv186029
 */

public class SentryClientException extends Exception {

    public SentryClientException() {
    }

    public SentryClientException(String message) {
        super(message);
    }

    public SentryClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public SentryClientException(Throwable cause) {
        super(cause);
    }

    public SentryClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}