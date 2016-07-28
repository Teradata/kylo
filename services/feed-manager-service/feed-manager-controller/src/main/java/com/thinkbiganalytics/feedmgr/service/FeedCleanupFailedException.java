package com.thinkbiganalytics.feedmgr.service;

/**
 * Thrown to indicate that a feed's cleanup flow failed.
 */
public class FeedCleanupFailedException extends RuntimeException {

    private static final long serialVersionUID = -85111900673520561L;

    /**
     * Constructs a {@code FeedCleanupFailedException} with the specified detail message.
     *
     * @param message the detail message
     */
    public FeedCleanupFailedException(final String message) {
        super(message);
    }
}
