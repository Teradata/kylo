package com.thinkbiganalytics.metadata.cache;
/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.transform.JobModelTransform;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.event.feed.OperationStatus;
import com.thinkbiganalytics.metadata.api.event.job.DataConfidenceJobDetected;
import com.thinkbiganalytics.metadata.api.event.job.DataConfidenceJobEvent;
import com.thinkbiganalytics.metadata.api.feed.LatestFeedJobExecution;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.cache.util.TimeUtil;
import com.thinkbiganalytics.metadata.config.RoleSetExposingSecurityExpressionRoot;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedAclCache;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 9/27/17.
 */
public class DataConfidenceJobsCache implements TimeBasedCache<CheckDataJob> {

    private static final Logger log = LoggerFactory.getLogger(DataConfidenceJobsCache.class);

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;

    @Inject
    private FeedAclCache feedAclCache;

    @Inject
    private MetadataEventService metadataEventService;

    private List<CheckDataJob> latestCache = null;

    private AtomicBoolean needsRefresh = new AtomicBoolean(true);

    private DateTime needsRefreshAt = null;


    private final BatchJobExecutionUpdatedListener batchJobExecutionUpdatedListener = new BatchJobExecutionUpdatedListener();

    private final DataConfidenceJobDetectedListener dataConfidenceJobDetectedListener = new DataConfidenceJobDetectedListener();

    @PostConstruct
    private void init() {
        metadataEventService.addListener(batchJobExecutionUpdatedListener);
        metadataEventService.addListener(dataConfidenceJobDetectedListener);
    }


    private LoadingCache<Long, List<CheckDataJob>> checkDataJobCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build(new CacheLoader<Long, List<CheckDataJob>>() {
        @Override
        public List<CheckDataJob> load(Long millis) throws Exception {
            return fetchDataConfidenceSummary();
        }
    });


    private List<CheckDataJob> fetchDataConfidenceSummary() {
        if (latestCache == null || needsRefresh.get()) {
            DateTime refrshStart = DateTime.now();
            latestCache = metadataAccess.read(() -> {

                List<? extends LatestFeedJobExecution> latestCheckDataJobs = opsManagerFeedProvider.findLatestCheckDataJobs();

                if (latestCheckDataJobs != null) {
                    return latestCheckDataJobs.stream().map(latestFeedJobExecution -> JobModelTransform.checkDataJob(latestFeedJobExecution)).collect(Collectors.toList());
                    // return new DataConfidenceSummary(checkDataJobs, 60);
                } else {
                    return Collections.emptyList();
                }
            }, MetadataAccess.SERVICE);
            //reset the refresh flag including if any updates happened while we were loading
            if (needsRefreshAt != null) {
                needsRefresh.set(needsRefreshAt.isAfter(refrshStart));
            } else {
                needsRefresh.set(false);
            }
            log.debug("Loaded Data Confidence Summary from the database");
            return latestCache;
        } else {
            log.debug("Returning Cached Data Confidence Summary");
            return latestCache;
        }
    }

    public List<CheckDataJob> getDataConfidenceSummary(Long time) {
        return checkDataJobCache.getUnchecked(time);
    }

    public List<CheckDataJob> getUserDataConfidenceJobs() {
        Long time = TimeUtil.getTimeNearestFiveSeconds();
        return getUserDataConfidenceJobs(time);
    }

    public List<CheckDataJob> getUserDataConfidenceJobs(Long time) {
        RoleSetExposingSecurityExpressionRoot userContext = feedAclCache.userContext();
        return getUserDataConfidenceJobs(time,userContext);
    }

    public List<CheckDataJob> getUserDataConfidenceJobs(Long time, RoleSetExposingSecurityExpressionRoot userContext) {
        return getDataConfidenceSummary(time).stream().filter(checkDataJob -> feedAclCache.hasAccess(userContext, checkDataJob.getFeedId())).collect(Collectors.toList());
    }


    @Override
    public List<CheckDataJob> getUserCache(Long time) {
        return getUserDataConfidenceJobs(time);
    }

    @Override
    public List<CheckDataJob> getCache(Long time) {
        return getDataConfidenceSummary(time);
    }

    @Override
    public boolean isAvailable() {
        return feedAclCache.isUserCacheAvailable();
    }


    private class BatchJobExecutionUpdatedListener implements MetadataEventListener<FeedOperationStatusEvent> {

        public void notify(@Nonnull final FeedOperationStatusEvent metadataEvent) {
            OperationStatus change = metadataEvent.getData();
            if (FeedOperation.FeedType.CHECK == change.getFeedType()) {
                needsRefreshAt = DateTime.now();
                needsRefresh.set(true);
            }


        }
    }

    private class DataConfidenceJobDetectedListener implements MetadataEventListener<DataConfidenceJobEvent> {

        public void notify(@Nonnull final DataConfidenceJobEvent metadataEvent) {
            DataConfidenceJobDetected change = metadataEvent.getData();
            needsRefreshAt = DateTime.now();
            needsRefresh.set(true);
        }
    }
}
