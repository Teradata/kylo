package com.thinkbiganalytics.nifi.rest.client;

/**
 * Created by sr186054 on 6/18/16.
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
