package com.thinkbiganalytics.nifi.feedmgr;

/**
 * Created by sr186054 on 6/20/16.
 */
public class FeedRollbackException extends RuntimeException {

    public FeedRollbackException() {
    }

    public FeedRollbackException(String message) {
        super(message);
    }

    public FeedRollbackException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeedRollbackException(Throwable cause) {
        super(cause);
    }

    public FeedRollbackException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
