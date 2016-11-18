package com.thinkbiganalytics.feedmgr.service.feed;

/**
 * Created by sr186054 on 11/17/16.
 */
public class ImportFeedException extends RuntimeException {

    public ImportFeedException() {
        super();
    }

    public ImportFeedException(String message) {
        super(message);
    }

    public ImportFeedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImportFeedException(Throwable cause) {
        super(cause);
    }

    protected ImportFeedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
