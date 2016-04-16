package com.thinkbiganalytics.jobrepo.query.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.concurrent.TimeUnit;

/**
 * Created by sr186054 on 9/3/15.
 */
public class DefaultFeedSummary implements FeedSummary {

  private FeedHealth feedHealth;


  public DefaultFeedSummary(FeedHealth feedHealth) {
    this.feedHealth = feedHealth;
  }

  public DefaultFeedSummary() {

  }


  @Override
  public String getFeed() {
    return feedHealth.getFeed();
  }

  @Override
  public String getState() {

    String state = feedHealth.getLastOpFeedState();

    return state;
  }


  @Override
  public String getLastStatus() {
    if (feedHealth.getLastOpFeed() != null && isWaiting()) {
      return feedHealth.getLastOpFeed().getStatus().name();
    } else {
      return "N/A";
    }
  }

  @Override
  public boolean isWaiting() {
    return DefaultFeedHealth.STATE.WAITING.equals(DefaultFeedHealth.STATE.valueOf(getState()));
  }

  @Override
  public boolean isRunning() {
    return DefaultFeedHealth.STATE.RUNNING.equals(DefaultFeedHealth.STATE.valueOf(getState()));
  }

  @Override
  public Long getTimeSinceEndTime() {
    if (feedHealth.getLastOpFeed() != null) {
      return feedHealth.getLastOpFeed().getTimeSinceEndTime();
    } else {
      return null;
    }
  }

  @Override
  @JsonIgnore
  public String formatTimeMinSec(Long millis) {
    if (millis == null) {
      return null;
    }

    Long hours = TimeUnit.MILLISECONDS.toHours(millis);
    Long min = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
    Long sec = TimeUnit.MILLISECONDS.toSeconds(millis) -
               TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
    String str = String.format("%d hr %d min %d sec",
                               hours, min, sec);
    if (hours == 0L) {

      if (min == 0L) {
        str = String.format("%d sec",
                            sec);
      } else {
        str = String.format("%d min %d sec",
                            min, sec);
      }

    }

    return str;
  }

  @Override
  public String getTimeSinceEndTimeString() {
    return formatTimeMinSec(getTimeSinceEndTime());
  }

  @Override
  public Long getRunTime() {
    if (feedHealth.getLastOpFeed() != null) {
      return feedHealth.getLastOpFeed().getRunTime();
    } else {
      return null;
    }
  }

  @Override
  public String getRunTimeString() {
    return formatTimeMinSec(getRunTime());
  }

  @Override
  public Long getAvgCompleteTime() {
    return feedHealth.getAvgRuntime();
  }

  @Override
  public String getAvgCompleteTimeString() {
    Long avgRunTime = feedHealth.getAvgRuntime();
    if (avgRunTime != null) {
      avgRunTime *= 1000;  //convert to millis
    }

    return formatTimeMinSec(avgRunTime);
  }

  @Override
  public boolean isHealthy() {
    return feedHealth.isHealthy();
  }


  @Override
  public String getLastExitCode() {
    if (feedHealth.getLastOpFeed() != null) {
      return feedHealth.getLastOpFeed().getExitCode();
    } else {
      return null;
    }
  }

  @Override
  public FeedHealth getFeedHealth() {
    return feedHealth;
  }
}
