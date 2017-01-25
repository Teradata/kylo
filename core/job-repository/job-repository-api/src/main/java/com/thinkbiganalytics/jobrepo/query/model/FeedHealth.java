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

import java.util.Date;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface FeedHealth {

  Long getHealthyCount();

  void setHealthyCount(Long healthyCount);

  Long getUnhealthyCount();

  void markHealthCountsSet();

  void setUnhealthyCount(Long unhealthyCount);

  String getFeed();

  void setFeed(String feed);

  ExecutedFeed getLastOpFeed();

  void setLastOpFeed(ExecutedFeed lastOpFeed);

  Long getAvgRuntime();

  void setAvgRuntime(Long avgRuntime);

  Date getLastUnhealthyTime();

  void setLastUnhealthyTime(Date lastUnhealthyTime);

  boolean isHealthy();

  String getFeedState(ExecutedFeed feed);

  String getLastOpFeedState();

  public static enum STATE {
    WAITING, RUNNING
  }
}
