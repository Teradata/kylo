package com.thinkbiganalytics.metadata.api.event.feed;

/*-
 * #%L
 * thinkbig-metadata-api
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
