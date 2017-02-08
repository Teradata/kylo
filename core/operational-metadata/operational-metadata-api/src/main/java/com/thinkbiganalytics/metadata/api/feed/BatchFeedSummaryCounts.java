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


/**
 * Represents summary data about Batch related jobs for a feed
 */
public interface BatchFeedSummaryCounts {

    /**
     * Return the Feed
     *
     * @return the feed
     */
    OpsManagerFeed getFeed();

    /**
     * Return the feed id0
     *
     * @return the feedid
     */
    OpsManagerFeed.ID getFeedId();

    /**
     * Return the feed name
     *
     * @return the feed name
     */
    String getFeedName();

    /**
     * Return a count of all the batch jobs executed for this feed
     *
     * @return a count of all the batch jobs executed for this feed
     */
    Long getAllCount();

    /**
     * Return a count of all the batch jobs that have failed for this feed
     *
     * @return a count of all the batch jobs that have failed for this feed
     */
    Long getFailedCount();

    /**
     * Return a count of all the batch jobs that have completed for this feed
     *
     * @return a count of all the batch jobs that have completed for this feed
     */
    Long getCompletedCount();

    /**
     * Return a count of all the batch jobs that have been abandoned for this feed
     *
     * @return a count of all the batch jobs that have been abandoned for this feed
     */
    Long getAbandonedCount();
}
