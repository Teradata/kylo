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
 * Represents summary data about feeds and their health in the system
 */
public interface FeedStatus {

    /**
     * set fields on this object
     */
    void populate();

    /**
     * Return the list of FeedHealth objects
     */
    List<FeedHealth> getFeeds();

    /**
     * set the list of feed health objects to summarize
     */
    void setFeeds(List<FeedHealth> feeds);

    /**
     * Return a count of the healthy feeds
     *
     * @return a count of the healthy feeds
     */
    Integer getHealthyCount();

    /**
     * set the healthy feed count
     */
    void setHealthyCount(Integer healthyCount);

    /**
     * Return a count of the failed feeds
     *
     * @return a count of the feeds that failed
     */
    Integer getFailedCount();

    /**
     * set the failed count
     */
    void setFailedCount(Integer failedCount);

    /**
     * Return a percent of the feeds that are healthy
     *
     * @return a precent of the feeds that are healthy
     */
    Float getPercent();

    /**
     * set the percent of healthy feeds
     */
    void setPercent(Integer percent);

    /**
     * Return a list of all the Healthy feeds
     *
     * @return a list of healthy feeds
     */
    List<FeedHealth> getHealthyFeeds();

    /**
     * set the list of healthy feeds
     */
    void setHealthyFeeds(List<FeedHealth> healthyFeeds);

    /**
     * Return a list of unhealthy feeds
     *
     * @return a list of unhealthy feeds
     */
    List<FeedHealth> getFailedFeeds();

    /**
     * set the unhealthy feeds
     */
    void setFailedFeeds(List<FeedHealth> failedFeeds);

    /**
     * Return a summary of the feeds
     */
    List<FeedSummary> getFeedSummary();

    /**
     * set the summar of the feeds
     */
    void setFeedSummary(List<FeedSummary> feedSummary);
}
