package com.thinkbiganalytics.jobrepo.query.model;

/*-
 * #%L
 * thinkbig-job-repository-core
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Feed Status built from the transform class
 *
 * @see com.thinkbiganalytics.jobrepo.query.model.transform.FeedModelTransform
 */
public class DefaultFeedStatus implements FeedStatus {

    private List<FeedHealth> feeds;
    private Integer healthyCount = 0;
    private Integer failedCount = 0;
    private float percent;
    private List<FeedHealth> healthyFeeds;
    private List<FeedHealth> failedFeeds;

    private List<FeedSummary> feedSummary;

    public DefaultFeedStatus(List<FeedHealth> feeds) {
        this.feeds = feeds;
        this.populate();

    }
    public void populate(List<FeedSummary> feeds) {
        if(feeds != null && !feeds.isEmpty()) {
            this.feeds = feeds.stream().map(f -> f.getFeedHealth()).collect(Collectors.toList());
        }
        populate();

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
