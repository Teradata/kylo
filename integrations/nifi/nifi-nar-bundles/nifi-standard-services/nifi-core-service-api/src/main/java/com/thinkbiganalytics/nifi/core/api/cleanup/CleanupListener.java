package com.thinkbiganalytics.nifi.core.api.cleanup;

import com.thinkbiganalytics.metadata.rest.model.event.FeedCleanupTriggerEvent;

/**
 * Listens for cleanup events.
 */
public interface CleanupListener {

    /**
     * Processes the specified cleanup event.
     *
     * @param event the cleanup event
     */
    void triggered(FeedCleanupTriggerEvent event);
}
