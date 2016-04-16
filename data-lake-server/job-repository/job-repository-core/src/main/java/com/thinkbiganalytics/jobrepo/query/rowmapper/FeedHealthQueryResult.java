/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.query.rowmapper;

import java.util.Date;

/**
 * Created by sr186054 on 1/5/16.
 */
public class FeedHealthQueryResult {
    private String feed;
    private Long count;
    private String health;
    private Date endTime;

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
