package com.thinkbiganalytics.nifi.core.api.cleanup;

import com.thinkbiganalytics.metadata.rest.model.event.FeedCleanupTriggerEvent;

import javax.annotation.Nonnull;

/**
 * Listens for cleanup events.
 */
public interface CleanupListener {

    /**
     * Processes the specified cleanup event.
     *
     * @param event the cleanup event
     */
    void triggered(@Nonnull FeedCleanupTriggerEvent event);
}
