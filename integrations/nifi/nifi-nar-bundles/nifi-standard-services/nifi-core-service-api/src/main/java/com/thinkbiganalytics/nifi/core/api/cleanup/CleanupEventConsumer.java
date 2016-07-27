package com.thinkbiganalytics.nifi.core.api.cleanup;

import javax.annotation.Nonnull;

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
    void addListener(@Nonnull String category, @Nonnull String feedName, @Nonnull CleanupListener listener);

    /**
     * Removes the specified listener.
     *
     * @param listener the listener to be removed
     */
    void removeListener(@Nonnull CleanupListener listener);
}
