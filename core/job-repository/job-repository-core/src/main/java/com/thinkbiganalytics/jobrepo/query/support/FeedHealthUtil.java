package com.thinkbiganalytics.jobrepo.query.support;

import com.thinkbiganalytics.jobrepo.query.model.DefaultFeedHealth;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 4/14/16.
 */
public class FeedHealthUtil {


  public static List<FeedHealth> parseToList(List<ExecutedFeed> latestOpFeeds, Map<String, Long> avgRunTimes) {
    List<FeedHealth> list = new ArrayList<FeedHealth>();
    Map<String, FeedHealth> map = new HashMap<String, FeedHealth>();

    if (latestOpFeeds != null) {
      for (ExecutedFeed feed : latestOpFeeds) {
        String feedName = feed.getName();
        FeedHealth feedHealth = map.get(feedName);
        if (feedHealth == null) {
          feedHealth = new DefaultFeedHealth();
          feedHealth.setFeed(feedName);
          if (avgRunTimes != null) {
            feedHealth.setAvgRuntime(avgRunTimes.get(feedName));
          }
          list.add(feedHealth);
          map.put(feedName, feedHealth);
        }
        feedHealth.setLastOpFeed(feed);
      }
    }
    return list;

  }
}
