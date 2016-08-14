package com.thinkbiganalytics.nifi.provenance;

/**
 * Created by sr186054 on 8/14/16.
 */
public class ProvenanceEventProcessingException extends RuntimeException {


    public ProvenanceEventProcessingException() {
        super();
    }

    public ProvenanceEventProcessingException(String message) {
        super(message);
    }

    public ProvenanceEventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProvenanceEventProcessingException(Throwable cause) {
        super(cause);
    }

    protected ProvenanceEventProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
