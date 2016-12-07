package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;

/**
 * Created by sr186054 on 12/4/16.
 */
public interface BatchFeedSummaryCounts {

    OpsManagerFeed getFeed();

    OpsManagerFeed.ID getFeedId();

    String getFeedName();

    Long getAllCount();

    Long getFailedCount();

    Long getCompletedCount();

    Long getAbandonedCount();
}
