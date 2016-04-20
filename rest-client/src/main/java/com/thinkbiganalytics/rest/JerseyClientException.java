package com.thinkbiganalytics.rest;

/**
 * Created by sr186054 on 10/16/15.
 */
public class JerseyClientException extends Throwable{

    public JerseyClientException() {
    }

    public JerseyClientException(String message) {
        super(message);
    }

    public JerseyClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public JerseyClientException(Throwable cause) {
        super(cause);
    }

    public JerseyClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
