package com.thinkbiganalytics.nifi.provenance;

/**
 * If when processing the system cannot find the parent FeedFlowFile it will throw this exception
 */
public class FeedFlowFileNotFoundException extends RuntimeException {


    public FeedFlowFileNotFoundException() {
        super();
    }

    public FeedFlowFileNotFoundException(String message) {
        super(message);
    }

    public FeedFlowFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeedFlowFileNotFoundException(Throwable cause) {
        super(cause);
    }

    protected FeedFlowFileNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
