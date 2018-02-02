package com.thinkbiganalytics.metadata.rest.model.feed.reindex;

/*-
 * #%L
 * kylo-metadata-rest-model
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
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * List of feeds that require history data reindexing
 */
@SuppressWarnings("serial")
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedsForDataHistoryReindex implements Serializable {

    private Set<Feed> feeds;

    public FeedsForDataHistoryReindex() {
        super();
    }

    public FeedsForDataHistoryReindex(Set<Feed> feeds) {
        this.feeds = feeds;
    }

    public Set<Feed> getFeeds() {
        return feeds;
    }

    public int getFeedCount() {
        return (feeds!=null)?feeds.size():0;
    }

    public List<String> getFeedIds() {
        List<String> feedIds = new ArrayList<>();
        Objects.requireNonNull(feeds).forEach(feed -> feedIds.add(feed.getId()));
        return feedIds;
    }
}
