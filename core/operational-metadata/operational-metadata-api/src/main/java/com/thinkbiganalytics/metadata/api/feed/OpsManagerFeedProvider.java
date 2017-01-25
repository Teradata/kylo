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
