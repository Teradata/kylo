package com.thinkbiganalytics.metadata.cache;

/*-
 * #%L
 * thinkbig-job-repository-controller
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

import com.thinkbiganalytics.alerts.rest.model.AlertSummaryGrouped;
import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.DataConfidenceSummary;
import com.thinkbiganalytics.jobrepo.query.model.FeedStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.cache.util.TimeUtil;
import com.thinkbiganalytics.metadata.config.RoleSetExposingSecurityExpressionRoot;
import com.thinkbiganalytics.metadata.jpa.feed.OpsFeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedAclCache;
import com.thinkbiganalytics.security.AccessController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

/**
 * Created by sr186054 on 9/21/17.
 */
@Component
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);


    @Inject
    AccessController accessController;

    @Inject
    FeedHealthSummaryCache feedHealthSummaryCache;

    @Inject
    AlertsCache alertsCache;

    @Inject
    FeedAclCache feedAclCache;

    @Inject
    OpsFeedManagerFeedProvider opsFeedManagerFeedProvider;

    @Inject
    ServiceStatusCache serviceStatusCache;

    @Inject
    RunningJobsCache runningJobsCache;

    @Inject
    DataConfidenceJobsCache dataConfidenceJobsCache;

    /**
     * We need the Acl List populated in order to do the correct fetch
     */
    public Dashboard getDashboard() {
        Dashboard dashboard = null;
        if (!accessController.isEntityAccessControlled() || (accessController.isEntityAccessControlled() && feedAclCache.isAvailable())) {
            Long time = TimeUtil.getTimeNearestFiveSeconds();
            RoleSetExposingSecurityExpressionRoot userContext = feedAclCache.userContext();
            List<Callable<Object>> tasks = new ArrayList<>();
            tasks.add(() -> feedHealthSummaryCache.getCache(time));
            tasks.add(() -> dataConfidenceJobsCache.getCache(time));
            tasks.add(() -> alertsCache.getCache(time));
            tasks.add(() -> serviceStatusCache.getCache(time));
            ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
            try {
                List<Future<Object>> results = pool.invokeAll(tasks);
                DataConfidenceSummary dataConfidenceSummary = new DataConfidenceSummary(dataConfidenceJobsCache.getUserCache(time), 60);
                dashboard =
                    new Dashboard(time, userContext.getName(), feedHealthSummaryCache.getUserFeeds(time), alertsCache.getUserCache(time), dataConfidenceSummary, serviceStatusCache.getUserCache(time));
            } catch (Exception e) {

            }
            pool.shutdown();
            return dashboard;
        } else {
            return Dashboard.NOT_READY;
        }
    }


    public FeedStatus getUserFeedHealth(String feedName) {
        return feedHealthSummaryCache.getUserFeed(feedName);
    }

    public FeedStatus getUserFeedHealth() {
        return feedHealthSummaryCache.getUserFeeds();
    }

    public List<CheckDataJob> getUserDataConfidenceJobs() {
        return dataConfidenceJobsCache.getUserDataConfidenceJobs();
    }

    public List<JobStatusCount> getUserRunningJobs() {
        return runningJobsCache.getUserRunningJobs();
    }


    public List<AlertSummaryGrouped> getUserAlertSummary() {
        return alertsCache.getUserAlertSummary();
    }

    public List<AlertSummaryGrouped> getUserAlertSummaryForFeedId(String feedId) {
        return alertsCache.getUserAlertSummaryForFeedId(feedId);
    }

    public List<AlertSummaryGrouped> getUserAlertSummaryForFeedName(String feedName) {
        return alertsCache.getUserAlertSummaryForFeedName(feedName);
    }

    public List<? extends OpsManagerFeed> getOpsManagerFeeds() {
        return opsFeedManagerFeedProvider.findAll();
    }

    public OpsManagerFeed getOpsManagerFeed(String feedName) {
        return opsFeedManagerFeedProvider.findByName(feedName);
    }

}