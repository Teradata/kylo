package com.thinkbiganalytics.jobrepo.query.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.jobrepo.query.support.FeedHealthUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 9/2/15.
 */
public class DefaultFeedHealth implements FeedHealth {

    private String feed;
    private ExecutedFeed lastOpFeed; // remove NOOP exit status
    private Long avgRuntime;
    private Long healthyCount;
    private Long unhealthyCount;
    private boolean healthCountsSet = false;
    private Date lastUnhealthyTime;

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
    public void markHealthCountsSet() {
        healthCountsSet = true;
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
     *
     * @return
     */
    @Override
    public boolean isHealthy() {

        if (true) {
            return getUnhealthyCount() == 0L;
        }
        boolean healthy = true;
        ExecutionStatus lastOpFeedStatus = null;
        if (lastOpFeed != null) {
            lastOpFeedStatus = lastOpFeed.getStatus();
            healthy = !ExecutionStatus.FAILED.equals(lastOpFeedStatus) && !ExecutionStatus.UNKNOWN.equals(lastOpFeedStatus) && !ExecutionStatus.ABANDONED.equals(lastOpFeedStatus);
            //If the Feed fully completed, but it did so with a Failed Exit Code then markt his as unhealthy.
            if (ExecutionStatus.COMPLETED.equals(lastOpFeedStatus) && ExitStatus.FAILED.getExitCode().equalsIgnoreCase(lastOpFeed.getExitCode())) {
                healthy = false;
            }

        }
        return healthy;
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

    @JsonIgnore
    public static List<FeedHealth> parseToList(List<ExecutedFeed> latestOpFeeds, Map<String, Long> avgRunTimes) {
        return FeedHealthUtil.parseToList(latestOpFeeds, avgRunTimes, null);

    }


}
