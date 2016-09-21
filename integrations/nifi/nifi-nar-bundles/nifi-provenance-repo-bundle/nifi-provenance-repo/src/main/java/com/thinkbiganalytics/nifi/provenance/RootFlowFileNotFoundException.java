package com.thinkbiganalytics.nifi.provenance;

/**
 * Created by sr186054 on 8/14/16.
 */
public class RootFlowFileNotFoundException extends RuntimeException {


    public RootFlowFileNotFoundException() {
        super();
    }

    public RootFlowFileNotFoundException(String message) {
        super(message);
    }

    public RootFlowFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RootFlowFileNotFoundException(Throwable cause) {
        super(cause);
    }

    protected RootFlowFileNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
