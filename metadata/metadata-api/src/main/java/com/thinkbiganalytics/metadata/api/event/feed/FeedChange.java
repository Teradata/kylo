/**
 *
 */
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

import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.Feed.State;

import java.util.Objects;

/**
 *
 */
public class FeedChange extends MetadataChange {

    private static final long serialVersionUID = 1L;

    private final Feed.ID feedId;
    private final Feed.State feedState;

    public FeedChange(ChangeType change, ID feedId, State feedState) {
        this(change, "", feedId, feedState);
    }

    public FeedChange(ChangeType change, String descr, ID feedId, State feedState) {
        super(change, descr);
        this.feedId = feedId;
        this.feedState = feedState;
    }

    public Feed.ID getFeedId() {
        return feedId;
    }

    public Feed.State getFeedState() {
        return feedState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.feedState, this.feedId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FeedChange) {
            FeedChange that = (FeedChange) obj;
            return super.equals(that) &&
                   Objects.equals(this.feedId, that.feedId) &&
                   Objects.equals(this.feedState, that.feedState);
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Feed change ");
        return sb
            .append("(").append(getChange()).append(") - ")
            .append("ID: ").append(this.feedId)
            .append(" feed state: ").append(this.feedState)
            .toString();

    }
}
