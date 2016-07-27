package com.thinkbiganalytics.nifi.core.api.cleanup;

/**
 * Event bus for cleanup events.
 */
public interface CleanupEventConsumer {

    /**
     * Adds the specified listener for cleanup events.
     *
     * @param category the category system name
     * @param feedName the feed system name
     * @param listener the listener to be added
     */
    void addListener(String category, String feedName, CleanupListener listener);

    /**
     * Removes the specified listener.
     *
     * @param listener the listener to be removed
     */
    void removeListener(CleanupListener listener);
}
