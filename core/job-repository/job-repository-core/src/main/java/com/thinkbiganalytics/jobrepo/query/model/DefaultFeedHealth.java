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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.jobrepo.query.support.FeedHealthUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Feed Health built from the transform class
 *
 * @see com.thinkbiganalytics.jobrepo.query.model.transform.FeedModelTransform
 */
public class DefaultFeedHealth implements FeedHealth {

    private String feedId;
    private String feed;
    private ExecutedFeed lastOpFeed;
    private Long avgRuntime;
    private Long healthyCount;
    private Long unhealthyCount;
    private Date lastUnhealthyTime;
    private boolean isStream;
    private Long runningCount;


    @Override
    public String getFeedId() {
        return feedId;
    }

    @Override
    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    @JsonIgnore
    public static List<FeedHealth> parseToList(List<ExecutedFeed> latestOpFeeds, Map<String, Long> avgRunTimes) {
        return FeedHealthUtil.parseToList(latestOpFeeds, avgRunTimes);

    }

    @Override
    public Long getHealthyCount() {
        return healthyCount;
    }

    @Override
    public void setHealthyCount(Long healthyCount) {
        this.healthyCount = healthyCount;
    }

    @Override
    public Long getUnhealthyCount() {

        if (unhealthyCount == null) {
            unhealthyCount = 0L;
        }
        return unhealthyCount;
    }

    @Override
    public void setUnhealthyCount(Long unhealthyCount) {
        this.unhealthyCount = unhealthyCount;
    }

    @Override
    public String getFeed() {
        return feed;
    }

    @Override
    public void setFeed(String feed) {
        this.feed = feed;
    }

    @Override
    public ExecutedFeed getLastOpFeed() {
        return lastOpFeed;
    }

    @Override
    public void setLastOpFeed(ExecutedFeed lastOpFeed) {
        this.lastOpFeed = lastOpFeed;
    }

    @Override
    public Long getAvgRuntime() {
        return avgRuntime;
    }

    @Override
    public void setAvgRuntime(Long avgRuntime) {
        this.avgRuntime = avgRuntime;
    }

    @Override
    public Date getLastUnhealthyTime() {
        return lastUnhealthyTime;
    }

    @Override
    public void setLastUnhealthyTime(Date lastUnhealthyTime) {
        this.lastUnhealthyTime = lastUnhealthyTime;
    }

    /**
     * Checks the last Processed Data Feed and if it is not in a FAILED State, mark it Healthy
     */
    @Override
    public boolean isHealthy() {
     return getUnhealthyCount() == 0L;
    }

    @Override
    public String getFeedState(ExecutedFeed feed) {
        STATE state = STATE.WAITING;
        if (feed != null) {
            ExecutionStatus status = feed.getStatus();
            if (ExecutionStatus.STARTED.equals(status) || ExecutionStatus.STARTING.equals(status)) {
                state = STATE.RUNNING;
            }
        }
        return state.name();
    }

    @Override
    public String getLastOpFeedState() {
        String state = getFeedState(lastOpFeed);
        return state;
    }

    @Override
    public Long getRunningCount() {
        return runningCount;
    }

    @Override
    public void setRunningCount(Long runningCount) {
        this.runningCount = runningCount;
    }

    public boolean isStream() {
        return isStream;
    }

    public void setStream(boolean stream) {
        isStream = stream;
    }
}
