package com.thinkbiganalytics.jobrepo.query.model;


/**
 * Created by sr186054 on 4/14/16.
 */
public interface FeedSummary {
    String getFeed();

    String getState();

    String getLastStatus();

    boolean isWaiting();

    boolean isRunning();

    Long getTimeSinceEndTime();

    String formatTimeMinSec(Long millis);

    String getTimeSinceEndTimeString();

    Long getRunTime();

    String getRunTimeString();

    Long getAvgCompleteTime();

    String getAvgCompleteTimeString();

    boolean isHealthy();

    String getLastExitCode();

    FeedHealth getFeedHealth();
}
