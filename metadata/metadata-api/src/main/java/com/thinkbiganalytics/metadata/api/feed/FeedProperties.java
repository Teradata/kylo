package com.thinkbiganalytics.metadata.api.feed;

/**
 * Metadata properties for feeds.
 */
public class FeedProperties {

    /** Property key for enabling feed cleanup */
    public static final String CLEANUP_ENABLED = "tba:cleanupEnabled";

    /**
     * Instances of {@code FeedProperties} may not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private FeedProperties() {
        throw new UnsupportedOperationException();
    }
}
