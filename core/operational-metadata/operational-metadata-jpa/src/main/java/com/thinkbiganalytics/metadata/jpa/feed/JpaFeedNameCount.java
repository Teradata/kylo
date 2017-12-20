package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.api.feed.FeedNameCount;

public class JpaFeedNameCount implements FeedNameCount {

    private String feedName;
    private Long count;

    public JpaFeedNameCount() {

    }

    public JpaFeedNameCount(String feedName, Long count) {
        this.feedName = feedName;
        this.count = count;
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    @Override
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
