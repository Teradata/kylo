/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.query.model;

import java.util.Date;

/**
 * Created by sr186054 on 1/5/16.
 */
public class DefaultFeedHealthQueryResult implements FeedHealthQueryResult {
    private String feed;
    private Long count;
    private String health;
    private Date endTime;

    @Override
    public String getFeed() {
        return feed;
    }

    @Override
    public void setFeed(String feed) {
        this.feed = feed;
    }

    @Override
    public Long getCount() {
        return count;
    }

    @Override
    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String getHealth() {
        return health;
    }

    @Override
    public void setHealth(String health) {
        this.health = health;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
