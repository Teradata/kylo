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

import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     *NOTE: This is Access Controlled and will show only those feeds a user has access to
     * @return the feed
     */
    OpsManagerFeed findByName(String name);


    /**
     * Find a feed by its feed name {@link OpsManagerFeed#getName()}
     *NOTE: This is NOT Access Controlled and will show any feed that matches
     * @return the feed
     */
    OpsManagerFeed findByNameWithoutAcl(String name);

    /**
     * Find a feed by its unique id
     * NOTE: This is Access Controlled and will show only those feeds a user has access to
     * @return the feed
     */
    OpsManagerFeed findById(OpsManagerFeed.ID id);

    /**
     * Find all feeds matching a list of feed ids
     *NOTE: This is Access Controlled and will show only those feeds a user has access to
     * @return the feeds matching the list of ids
     */
    List<? extends OpsManagerFeed> findByFeedIds(List<OpsManagerFeed.ID> ids);

    /**
     * Return all feeds
     * @return
     */
    List<? extends OpsManagerFeed> findAllWithoutAcl();

    /**
     * Find Feeds that have the same system name in the kylo.FEED table
     * @return
     */
    List<? extends OpsManagerFeed> findFeedsWithSameName();

    /**
     *  Returns a list of all the feed categorySystemName.feedSystemName
     * NOTE: This is Access Controlled and will show only those feeds a user has access to
     * @return Returns a list of all the feed categorySystemName.feedSystemName
     */
    List<String> getFeedNames();


    /**
     * Get a Map of the category system name to list of feeds
     * NOTE: This is Access Controlled and will show only those feeds a user has access to
     * @return a Map of the category system name to list of feeds
     */
    public Map<String,List<OpsManagerFeed>> getFeedsGroupedByCategory();

    /**
     * Save a feed
     */
    void save(List<? extends OpsManagerFeed> feeds);

    /**
     * Feed Names to update the streaming flag
     * @param feedNames set of category.feed names
     * @param isStream true if stream, false if not
     */
    void updateStreamingFlag(Set<String> feedNames, boolean isStream);


    /**
     * For Batch Feeds that may start many flowfiles/jobs at once in a short amount of time
     * we don't necessarily want to show all of those as individual jobs in ops manager as they may merge and join into a single ending flow.
     * For a flood of starting jobs if ops manager receives more than 1 starting event within this given interval it will supress the creation of the next Job
     * Set this to -1L or 0L to bypass and always create a job instance per starting flow file.
     * @param feedNames a set of category.feed names
     * @param timeBetweenBatchJobs  a time in millis to supress new job creation
     */
    void updateTimeBetweenBatchJobs(Set<String> feedNames, Long timeBetweenBatchJobs);

    /**
     * save a feed with a specific feed id and name
     * This is used to save an initial record for a feed when a feed is created
     *
     * @return the saved feed
     */
    OpsManagerFeed save(OpsManagerFeed.ID feedManagerId, String systemName, boolean isStream, Long timeBetweenBatchJobs);

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

    List<? extends FeedSummary> findFeedSummary();

    DateTime getLastActiveTimeStamp(String feedName);
}
