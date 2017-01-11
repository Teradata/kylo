package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount;

import org.joda.time.ReadablePeriod;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 9/15/16.
 */
public interface OpsManagerFeedProvider {


    OpsManagerFeed.ID resolveId(Serializable id);

    OpsManagerFeed findByName(String name);

    OpsManagerFeed findById(OpsManagerFeed.ID id);

    List<? extends OpsManagerFeed> findByFeedIds(List<OpsManagerFeed.ID> ids);

    /**
     * Returns a list of all the Feed Names registered in the FEED table
     * @return
     */
    List<String> getFeedNames();

    void save(List<? extends OpsManagerFeed> feeds);

    OpsManagerFeed save(OpsManagerFeed.ID feedManagerId, String systemName);

    void delete(OpsManagerFeed.ID id);

    boolean isFeedRunning(OpsManagerFeed.ID id);

    List<? extends FeedHealth> getFeedHealth();

    FeedHealth getFeedHealth(String feedName);

    List<JobStatusCount>  getJobStatusCountByDateFromNow(String feedName, ReadablePeriod period);

    List<? extends LatestFeedJobExecution> findLatestCheckDataJobs();

    void abandonFeedJobs(String feedName);
}
