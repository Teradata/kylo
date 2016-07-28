package com.thinkbiganalytics.feedmgr.service;

/**
 * Thrown to indicate that a feed's cleanup flow failed to completed within the allotted time.
 */
public class FeedCleanupTimeoutException extends RuntimeException {

    private static final long serialVersionUID = 5504910662840560644L;

    /**
     * Constructs a {@code FeedCleanupTimeoutException} with the specified detail message.
     *
     * @param message the detail message
     */
    public FeedCleanupTimeoutException(final String message) {
        super(message);
    }
}
