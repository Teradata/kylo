package com.thinkbiganalytics.metadata.api.event.feed;

import com.thinkbiganalytics.metadata.api.event.AbstractMetadataEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;

import javax.annotation.Nonnull;

/**
 * An event that triggers the cleanup of a feed.
 */
public class CleanupTriggerEvent extends AbstractMetadataEvent<Feed.ID> {

    private static final long serialVersionUID = 5322725584964504810L;

    /**
     * Constructs a {@code CleanupTriggerEvent} with the specified feed id.
     *
     * @param feedId the feed id
     */
    public CleanupTriggerEvent(@Nonnull final Feed.ID feedId) {
        super(feedId);
    }
}
