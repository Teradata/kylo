package com.thinkbiganalytics.metadata.api.feed;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount;

import org.joda.time.ReadablePeriod;

import java.io.Serializable;
import java.util.List;

/**
 * Provider interface for accessing/processing Feeds
 */
public interface OpsManagerFeedProvider {


    /**
     * Return the id representing the unique feed identifier
     *
     * @return the unique feed id
     */
    OpsManagerFeed.ID resolveId(Serializable id);

    /**
     * Find a feed by its feed name {@link OpsManagerFeed#getName()}
     *
     * @return the feed
     */
    OpsManagerFeed findByName(String name);

    /**
     * Find a feed by its unique id
     *
     * @return the feed
     */
    OpsManagerFeed findById(OpsManagerFeed.ID id);

    /**
     * Find all feeds matching a list of feed ids
     *
     * @return the feeds matching the list of ids
     */
    List<? extends OpsManagerFeed> findByFeedIds(List<OpsManagerFeed.ID> ids);

    /**
     * Returns a list of all the feed names
     */
    List<String> getFeedNames();

    /**
     * Save a feed
     */
    void save(List<? extends OpsManagerFeed> feeds);

    /**
     * save a feed with a specific feed id and name
     * This is used to save an initial record for a feed when a feed is created
     *
     * @return the saved feed
     */
    OpsManagerFeed save(OpsManagerFeed.ID feedManagerId, String systemName);

    /**
     * Delete a feed and all of its operational metadata (i.e. jobs, steps, etc)
     */
    void delete(OpsManagerFeed.ID id);

    /**
     * Determine if a feed is running
     *
     * @return true if the feed is running a job now, false if not
     */
    boolean isFeedRunning(OpsManagerFeed.ID id);

    /**
     * Return summary health information about the feeds in the system
     *
     * @return summary health information about the feeds in the system
     */
    List<? extends FeedHealth> getFeedHealth();

    /**
     * Return summary health information about a specific feed
     *
     * @return summary health information about a specific feed
     */
    FeedHealth getFeedHealth(String feedName);

    /**
     * Return job status count information for a given feed and a timeframe grouped by day
     * Useful for generating timebased charts of job executions and their status by each day for a given feed
     *
     * @param period time to look back from now
     * @return job status count information for a given feed and a timeframe grouped by day
     */
    List<JobStatusCount> getJobStatusCountByDateFromNow(String feedName, ReadablePeriod period);

    /**
     * find the latest job executions of the type {@link com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed.FeedType#CHECK}
     */
    List<? extends LatestFeedJobExecution> findLatestCheckDataJobs();

    /**
     * change the {@link BatchJobExecution#getStatus()} of all {@link com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution.JobStatus#FAILED} Jobs to be {@link
     * com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution.JobStatus#ABANDONED}
     */
    void abandonFeedJobs(String feedName);


    /**
     * subscribe to feed deletion events
     *
     * @param listener a delete feed listener
     */
    void subscribeFeedDeletion(DeleteFeedListener listener);
}
