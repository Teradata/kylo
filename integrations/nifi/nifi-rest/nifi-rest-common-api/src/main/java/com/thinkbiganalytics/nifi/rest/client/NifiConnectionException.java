package com.thinkbiganalytics.nifi.rest.client;

/**
 * Thrown to indicate that the connection to NiFi failed.
 */
public class NifiConnectionException extends NifiClientRuntimeException {

    public NifiConnectionException() {
        super();
    }

    public NifiConnectionException(String message) {
        super(message);
    }

    public NifiConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
