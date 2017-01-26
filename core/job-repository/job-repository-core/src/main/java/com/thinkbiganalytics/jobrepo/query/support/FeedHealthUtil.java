package com.thinkbiganalytics.jobrepo.query.support;

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

import com.thinkbiganalytics.jobrepo.query.model.DefaultFeedHealth;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to convert ExecutedFeeds to FeedHealth objects
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
