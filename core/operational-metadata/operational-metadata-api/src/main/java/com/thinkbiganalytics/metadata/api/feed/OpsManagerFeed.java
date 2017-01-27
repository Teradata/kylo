package com.thinkbiganalytics.metadata.api.feed;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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


import java.io.Serializable;

/**
 * Represents a feed in the operational store.
 *
 */
public interface OpsManagerFeed {

    /**
     * The type of feed
     * FEED is the default type and represents the majority of feeds in the system
     * CHECK represents a Data Confidence check feed.  CHECK feeds are new feed flows that have a pointer back to a specific feed for which to do a Data Confidence check on.
     */
    enum FeedType {
        FEED, CHECK
    }

    /**
     * The ID for the Feed
     */
    interface ID extends Serializable {

    }

    /**
     * @return the unique ID of this Feed
     */
    ID getId();

    /**
     * @return the name of this Feed
     */
    String getName();


    /**
     * {@link FeedType#FEED} is the default type and represents the majority of feeds in the system
     * {@link FeedType#CHECK} represents a Data Confidence check feed.  {@link FeedType#CHECK} feeds are new feed flows that have a pointer back to a specific feed for which to do a Data Confidence check on.
     *
     * @return the type of feed
     */
    FeedType getFeedType();


}
