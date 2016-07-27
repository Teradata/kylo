package com.thinkbiganalytics.metadata.rest.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import javax.annotation.Nonnull;

/**
 * An event that triggers the cleanup of a feed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedCleanupTriggerEvent implements Serializable {

    private static final long serialVersionUID = 1012854550068482906L;

    /** Category system name */
    private String categoryName;

    /** Feed id */
    private String feedId;

    /** Feed system name */
    private String feedName;

    /**
     * Constructs a {@code FeedCleanupTriggerEvent}.
     */
    public FeedCleanupTriggerEvent() {}

    /**
     * Constructs a {@code FeedCleanupTriggerEvent} with the specified feed id.
     *
     * @param id the feed id
     */
    public FeedCleanupTriggerEvent(@Nonnull final String id) {
        this.feedId = id;
    }

    /**
     * Gets the target feed id.
     *
     * @return the feed id
     */
    public String getFeedId() {
        return feedId;
    }

    /**
     * Sets the target feed id.
     *
     * @param feedId the feed id
     */
    public void setFeedId(@Nonnull final String feedId) {
        this.feedId = feedId;
    }

    /**
     * Gets the target feed name.
     *
     * @return the feed system name
     */
    public String getFeedName() {
        return feedName;
    }

    /**
     * Sets the target feed name.
     *
     * @param feedName the feed system name
     */
    public void setFeedName(@Nonnull final String feedName) {
        this.feedName = feedName;
    }

    /**
     * Gets the target category name.
     *
     * @return the category system name
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Sets the target category name.
     *
     * @param categoryName the category system name
     */
    public void setCategoryName(@Nonnull final String categoryName) {
        this.categoryName = categoryName;
    }

    @Nonnull
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + (feedId != null ? feedId : categoryName + "." + feedName);
    }
}
