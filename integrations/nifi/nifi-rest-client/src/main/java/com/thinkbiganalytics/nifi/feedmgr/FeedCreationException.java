package com.thinkbiganalytics.nifi.feedmgr;

/**
 * Created by sr186054 on 6/15/16.
 */
public class FeedCreationException extends RuntimeException {


    /**
     * @param message
     */
    public FeedCreationException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public FeedCreationException(String message, Throwable cause) {
        super(cause);
    }

}
