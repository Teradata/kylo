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
 * Summary object indicating the health of a feed
 */
public interface FeedHealth {

    /**
     * Return a count of jobs for this feed that are healthy (either Successful, or had their Failures (if any) handled and "Abandoned")
     *
     * @return a count of the healthy jobs on this feed
     */
    Long getHealthyCount();

    /**
     * set the count of healthy jobs for this feed
     */
    void setHealthyCount(Long healthyCount);

    /**
     * Return a count of the jobs that are unhealthy (Failed, for this feed)
     * @return the count of unhealthy jobs for this feed
     */
    Long getUnhealthyCount();

    /**
     * set the unhealthy job count for this feed
     */
    void setUnhealthyCount(Long unhealthyCount);


    /**
     * The feed Id
     * @return the feed id
     */
    String getFeedId();

    /**
     *
     * set the feed id
     */
    void setFeedId(String feedId);

    /**
     * Return the feed name
     *
     * @return the feed name
     */
    String getFeed();

    /**
     * set the feed name
     */
    void setFeed(String feed);

    /**
     * Return the latest job execution for this feed
     *
     * @return the latest job execution for this fed
     */
    ExecutedFeed getLastOpFeed();

    /**
     * set the latest job execution for this feed
     */
    void setLastOpFeed(ExecutedFeed lastOpFeed);

    /**
     * Return the average runtime for this feed
     *
     * @return the average runtime, in millis, for this feed
     */
    Long getAvgRuntime();

    /**
     * Set the average runtime, in millis, for this feed
     */
    void setAvgRuntime(Long avgRuntime);

    /**
     * Return a date indicating the last time this feed was unhealthy
     *
     * @return the date this feed was last unhealthy
     */
    Date getLastUnhealthyTime();

    /**
     * set the date this feed was last unhealthy
     */
    void setLastUnhealthyTime(Date lastUnhealthyTime);

    /**
     * Return true if healthy, false if not
     *
     * @return true if healthy, false if not
     */
    boolean isHealthy();

    /**
     * For a given Feed, return the {@link STATE}
     *
     * @return the status of the feed
     */
    String getFeedState(ExecutedFeed feed);

    /**
     * Return the  {@link STATE}  of the latest job for this feed
     *
     * @return the  {@link STATE}  of the latest job for this feed
     */
    String getLastOpFeedState();


    /**
     *
     * @return true if streaming feed
     */
    boolean isStream();

    /**
     * Set the streaming flag
     * @param stream true if stream, false if not
     */
    void setStream(boolean stream);


    /**
     *
     * @return the number of jobs running for the feed
     */
    Long getRunningCount();

    /**
     * set the running count for this feed
     * @param runningCount the number of jobs running for this feed
     */
    void setRunningCount(Long runningCount);

    /**
     * State indicating if the Feed has jobs Waiting (idle) or Running
     */
    enum STATE {
        WAITING, RUNNING
    }
}
