package com.thinkbiganalytics.metadata.rest.model.event;

/*-
 * #%L
 * thinkbig-metadata-rest-model
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import javax.annotation.Nonnull;

/**
 * An event that triggers the cleanup of a feed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedCleanupTriggerEvent implements Serializable {

    private static final long serialVersionUID = 1012854550068482906L;

    /**
     * Category system name
     */
    private String categoryName;

    /**
     * Feed id
     */
    private String feedId;

    /**
     * Feed system name
     */
    private String feedName;

    /**
     * Constructs a {@code FeedCleanupTriggerEvent}.
     */
    public FeedCleanupTriggerEvent() {
    }

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
