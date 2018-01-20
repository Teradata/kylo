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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.alerts.rest.model.AlertSummaryGrouped;
import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.DataConfidenceSummary;
import com.thinkbiganalytics.jobrepo.query.model.FeedStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.metadata.cache.util.TimeUtil;
import com.thinkbiganalytics.metadata.config.RoleSetExposingSecurityExpressionRoot;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedAclCache;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.security.AccessController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    ServiceStatusCache serviceStatusCache;

    @Inject
    RunningJobsCache runningJobsCache;

    @Inject
    DataConfidenceJobsCache dataConfidenceJobsCache;

    @Value("${kylo.ops.mgr.dashboard.threads:20}")
    private int dashboardThreads = 20;

    ExecutorService executor = Executors.newFixedThreadPool(dashboardThreads, new ThreadFactoryBuilder().setNameFormat("kylo-dashboard-pool-%d").build());

    /**
     * Threaded Task that releases the barrier counter
     */
    private class ThreadedDashboardTask implements Runnable {
        CyclicBarrier barrier;
        Runnable runnable;
        String name;
        public ThreadedDashboardTask(CyclicBarrier barrier, Runnable runnable){
            this.barrier = barrier;
            this.runnable = runnable;
            this.name = UUID.randomUUID().toString();
        }
        public void run(){
            try {
                this.runnable.run();
            }catch (Exception e){

            }finally {
                try {
                    barrier.await();
                }catch (Exception e){

                }
            }
            }

    }

    /**
     * Action called after getting feed data to create the Dashboard view as it pertains to the User
     */
    private class DashboardAction implements  Runnable{

        private Long time;
        private Dashboard dashboard;

        private FeedHealthSummaryCache.FeedSummaryFilter feedSummaryFilter;
        private RoleSetExposingSecurityExpressionRoot userContext;

        public DashboardAction(Long time, RoleSetExposingSecurityExpressionRoot userContext,FeedHealthSummaryCache.FeedSummaryFilter feedSummaryFilter){
            this.time = time;
            this.feedSummaryFilter = feedSummaryFilter;
            this.userContext = userContext;
        }
        public void run() {
            try {
                DataConfidenceSummary dataConfidenceSummary = new DataConfidenceSummary(dataConfidenceJobsCache.getUserDataConfidenceJobs(time,userContext), 60);
                dashboard =
                    new Dashboard(time, userContext.getName(), feedHealthSummaryCache.getUserFeedHealthCounts(time, userContext), feedHealthSummaryCache.getUserFeedHealth(time, feedSummaryFilter,userContext),
                                  alertsCache.getUserCache(time,userContext), dataConfidenceSummary, serviceStatusCache.getUserCache(time));
            } catch (Exception e) {
                log.error("Error getting the dashboard ", e);
                throw new RuntimeException("Unable to get the Dashboard " + e.getMessage());
            }
        }

        public Dashboard getDashboard(){
            return dashboard;
        }
    }

    /**
     * We need the Acl List populated in order to do the correct fetch
     * Fetch the components of the dashboard in separate threads
     */
    public Dashboard getDashboard(FeedHealthSummaryCache.FeedSummaryFilter feedSummaryFilter) {
        Dashboard dashboard = null;
        if (!accessController.isEntityAccessControlled() || (accessController.isEntityAccessControlled() && feedAclCache.isAvailable())) {
            Long time = TimeUtil.getTimeNearestFiveSeconds();
            RoleSetExposingSecurityExpressionRoot userContext = feedAclCache.userContext();
            DashboardAction dashboardAction = new DashboardAction(time, userContext,feedSummaryFilter);
            CyclicBarrier barrier = new CyclicBarrier(5, dashboardAction);
            List<ThreadedDashboardTask> tasks = new ArrayList<>();
            tasks.add(new ThreadedDashboardTask(barrier,() -> feedHealthSummaryCache.getCache(time)));
            tasks.add(new ThreadedDashboardTask(barrier,() -> dataConfidenceJobsCache.getCache(time)));
            tasks.add(new ThreadedDashboardTask(barrier,() -> alertsCache.getCache(time)));
            tasks.add(new ThreadedDashboardTask(barrier,() -> serviceStatusCache.getCache(time)));
            tasks.stream().forEach(t -> executor.submit(t));
            try {
                barrier.await();
            }catch (Exception e) {
            }
            return dashboardAction.getDashboard();
        } else {
            return Dashboard.NOT_READY;
        }
    }

    public Map<String,Long> getUserFeedHealthCounts(){
        Long time = TimeUtil.getTimeNearestFiveSeconds();
       return feedHealthSummaryCache.getUserFeedHealthCounts(time);
    }


    public FeedStatus getUserFeedHealth(String feedName) {
        return feedHealthSummaryCache.getUserFeed(feedName);
    }

    public FeedStatus getUserFeedHealth() {
        return feedHealthSummaryCache.getUserFeeds();
    }

    public SearchResult getUserFeedHealthWithFilter(FeedHealthSummaryCache.FeedSummaryFilter filter) {
        return feedHealthSummaryCache.getUserFeedHealth(filter);
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


}