package com.thinkbiganalytics.jobrepo.query.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 9/2/15.
 */
public class DefaultFeedStatus implements FeedStatus {

    private List<FeedHealth> feeds;
    private Integer healthyCount = 0;
    private Integer failedCount = 0;
    float percent;
    private List<FeedHealth> healthyFeeds;
    private List<FeedHealth> failedFeeds;

    private List<FeedSummary> feedSummary;

    public DefaultFeedStatus(List<FeedHealth> feeds) {
        this.feeds = feeds;
        this.populate();

    }


    public DefaultFeedStatus() {

    }

    @Override
    public void populate() {
        this.healthyFeeds = new ArrayList<FeedHealth>();
        this.failedFeeds = new ArrayList<FeedHealth>();
        this.feedSummary = new ArrayList<>();
        healthyCount = 0;
        failedCount = 0;
        percent = 0f;
        if (feeds != null && !feeds.isEmpty()) {
            for (FeedHealth feedHealth : feeds) {
                if (feedHealth.isHealthy()) {
                    healthyCount++;
                    healthyFeeds.add(feedHealth);
                } else {
                    failedCount++;
                    failedFeeds.add(feedHealth);
                }
                this.feedSummary.add(new DefaultFeedSummary(feedHealth));
            }
            percent = (float) healthyCount / feeds.size();
        }
        if (percent > 0f) {
            DecimalFormat twoDForm = new DecimalFormat("##.##");
            this.percent = Float.valueOf(twoDForm.format(this.percent)) * 100;
        }

    }

    @Override
    public List<FeedHealth> getFeeds() {
        return feeds;
    }

    @Override
    public void setFeeds(List<FeedHealth> feeds) {
        this.feeds = feeds;
    }

    @Override
    public Integer getHealthyCount() {
        return healthyCount;
    }

    @Override
    public void setHealthyCount(Integer healthyCount) {
        this.healthyCount = healthyCount;
    }

    @Override
    public Integer getFailedCount() {
        return failedCount;
    }

    @Override
    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    @Override
    public Float getPercent() {
        return percent;
    }

    @Override
    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    @Override
    public List<FeedHealth> getHealthyFeeds() {
        return healthyFeeds;
    }

    @Override
    public void setHealthyFeeds(List<FeedHealth> healthyFeeds) {
        this.healthyFeeds = healthyFeeds;
    }

    @Override
    public List<FeedHealth> getFailedFeeds() {
        return failedFeeds;
    }

    @Override
    public void setFailedFeeds(List<FeedHealth> failedFeeds) {
        this.failedFeeds = failedFeeds;
    }

    @Override
    public List<FeedSummary> getFeedSummary() {
        return feedSummary;
    }

    @Override
    public void setFeedSummary(List<FeedSummary> feedSummary) {
        this.feedSummary = feedSummary;
    }
}
