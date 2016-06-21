package com.thinkbiganalytics.nifi.rest.client;

/**
 * Created by sr186054 on 6/20/16.
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

    public boolean is409Error() {
        if (this.getCause() != null && this.getCause().getMessage().contains("409")) {
            return true;
        }
        return false;
    }
}
