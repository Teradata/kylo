package com.thinkbiganalytics.jobrepo.query.model;

/*-
 * #%L
 * thinkbig-job-repository-api
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

import java.util.List;

/**
 * Created by sr186054 on 4/14/16.
 */
public interface FeedStatus {

  void populate();

  List<FeedHealth> getFeeds();

  void setFeeds(List<FeedHealth> feeds);

  Integer getHealthyCount();

  void setHealthyCount(Integer healthyCount);

  Integer getFailedCount();

  void setFailedCount(Integer failedCount);

  Float getPercent();

  void setPercent(Integer percent);

  List<FeedHealth> getHealthyFeeds();

  void setHealthyFeeds(List<FeedHealth> healthyFeeds);

  List<FeedHealth> getFailedFeeds();

  void setFailedFeeds(List<FeedHealth> failedFeeds);

  List<FeedSummary> getFeedSummary();

  void setFeedSummary(List<FeedSummary> feedSummary);
}
