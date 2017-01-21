package com.thinkbiganalytics.nifi.rest.client;

/**
 * Thrown to indicate an exception when communicating with the NiFi REST API.
 */
public class NifiClientRuntimeException extends RuntimeException {


    public NifiClientRuntimeException() {
    }

    public NifiClientRuntimeException(String message) {
        super(message);
    }

    public NifiClientRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NifiClientRuntimeException(Throwable cause) {
        super(cause);
    }

    public NifiClientRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
