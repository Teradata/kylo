package com.thinkbiganalytics.feedmgr.service.feed.reindexing;

/*-
 * #%L
 * thinkbig-ui-feed-manager
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

/**
 * Thrown when a feed is currently running.
 * Checked when history reindexing is requested and feed is currently running.
 */
public class FeedCurrentlyRunningException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String categoryName;
    private final String feedName;

    public FeedCurrentlyRunningException(String categoryName, String feedName) {
        super("Feed with category = " + categoryName + " and name = " + feedName + " is currently running.");
        this.categoryName = categoryName;
        this.feedName = feedName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getFeedName() {
        return feedName;
    }
}
