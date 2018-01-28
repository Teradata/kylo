package com.thinkbiganalytics.metadata.sla.api.core;

/*-
 * #%L
 * thinkbig-sla-metrics-default
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

import com.thinkbiganalytics.app.ServicesApplicationStartup;
import com.thinkbiganalytics.app.ServicesApplicationStartupListener;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution.JobStatus.FAILED;

/**
 * Service to listen for feed failure events and notify listeners when a feed fails
 */
@Component
public class FeedFailureService {

    @Inject
    ServicesApplicationStartup servicesApplicationStartup;

    @Inject
    private BatchJobExecutionProvider batchJobExecutionProvider;

    @Inject
    private OpsManagerFeedProvider feedProvider;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private NifiFeedProcessorStatisticsProvider nifiFeedProcessorStatisticsProvider;


    public static LastFeedJob EMPTY_JOB = new LastFeedJob("empty", DateTime.now(), true);

    /**
     * Map with the Latest recorded failure that has been assessed by the FeedFailureMetricAssessor
     */
    private Map<String, LastFeedJob> lastAssessedFeedFailureMap = new HashMap<>();

    private FeedFailureServiceStatusStartupListener servicesApplicationStartupListener = new FeedFailureServiceStatusStartupListener();


    private Map<String, LastFeedJob> lastAssessedFeedMap = new HashMap<>();


    @PostConstruct
    private void init() {
        servicesApplicationStartup.subscribe(servicesApplicationStartupListener);
    }


    /**
     * Find the latest Job, first looking for any failures between the last time this service ran, and now.
     *
     * @param feedName feed to check
     */
    public LastFeedJob findLatestJob(String feedName) {
        LastFeedJob lastFeedJob = metadataAccess.read(() -> {

            LastFeedJob lastAssessedJob = lastAssessedFeedMap.getOrDefault(feedName, EMPTY_JOB);
            DateTime lastAssessedTime = lastAssessedJob.getDateTime();
            if (lastAssessedJob == EMPTY_JOB) {
                //attempt to get jobs since the app started
                lastAssessedTime = servicesApplicationStartupListener.getStartTime();
            }

            OpsManagerFeed feed = feedProvider.findByName(feedName);
            if (feed.isStream()) {
                List<NifiFeedProcessorStats> latestStats = nifiFeedProcessorStatisticsProvider.findLatestFinishedStatsSince(feedName, lastAssessedTime);
                Optional<NifiFeedProcessorStats> total = latestStats.stream().reduce((a, b) -> {
                    a.setFailedCount(a.getFailedCount() + b.getFailedCount());
                    if (b.getMinEventTime().isAfter(a.getMinEventTime())) {
                        a.setMinEventTime(b.getMinEventTime());
                    }
                    return a;
                });
                if (total.isPresent()) {
                    NifiFeedProcessorStats stats = total.get();
                    boolean success = stats.getFailedCount() == 0;
                    return new LastFeedJob(feedName, stats.getMinEventTime(), success);
                } else {
                    return EMPTY_JOB;
                }
            } else {
                List<? extends BatchJobExecution> latestJobs = batchJobExecutionProvider.findLatestFinishedJobForFeedSince(feedName, lastAssessedTime);

                BatchJobExecution latestJob = latestJobs.stream().sorted(Comparator.comparing(BatchJobExecution::getEndTime).reversed())
                    .filter(job -> FAILED.equals(job.getStatus()))
                    .findFirst()
                    .orElse(null);
                if (latestJob == null) {
                    //find the last job if there are no failures
                    latestJob = latestJobs.stream().sorted(Comparator.comparing(BatchJobExecution::getEndTime).reversed())
                        .findFirst().orElse(null);
                    // if the set doesnt have anything attempt to get the latest job
                    if (latestJob == null) {
                        latestJob = batchJobExecutionProvider.findLatestFinishedJobForFeed(feedName);
                    }
                }

                return latestJob != null ? new LastFeedJob(feedName, latestJob.getEndTime(), !FAILED.equals(latestJob.getStatus())) : EMPTY_JOB;

            }
        }, MetadataAccess.SERVICE);

        lastAssessedFeedMap.put(feedName, lastFeedJob);
        return lastFeedJob;
    }

    boolean isExistingFailure(LastFeedJob job) {
        if (job.isFailure()) {
            String feedName = job.getFeedName();
            LastFeedJob lastAssessedFailure = lastAssessedFeedFailureMap.get(feedName);
            if (lastAssessedFailure == null) {
                lastAssessedFeedFailureMap.put(feedName, job);
                return false;
            } else if (job.isAfter(lastAssessedFailure.getDateTime())) {
                //reassign it as the lastAssessedFailure
                lastAssessedFeedFailureMap.put(feedName, job);
                return true;
            } else {
                //last job is before or equals to last assessed job, nothing to do, we already cached a new one
                return true;
            }
        }
        return false;

    }


    public static class LastFeedJob {

        private String feedName;
        private DateTime dateTime;
        private boolean success = false;

        public LastFeedJob(String feedName, DateTime dateTime, boolean success) {
            this.feedName = feedName;
            this.dateTime = dateTime;
            this.success = success;
        }

        public String getFeedName() {
            return feedName;
        }

        public void setFeedName(String feedName) {
            this.feedName = feedName;
        }

        public DateTime getDateTime() {
            return dateTime;
        }

        public boolean isAfter(DateTime time) {
            return dateTime != null && dateTime.isAfter(time);
        }

        public boolean isFailure() {
            return !this.success;
        }
    }


    private class FeedFailureServiceStatusStartupListener implements ServicesApplicationStartupListener {

        DateTime startTime;

        @Override
        public void onStartup(DateTime startTime) {
            this.startTime = startTime;
        }

        public DateTime getStartTime() {
            if (startTime == null) {
                startTime = DateTime.now();
            }
            return startTime;
        }
    }

    public MetadataAccess getMetadataAccess() {
        return metadataAccess;
    }

    public void setMetadataAccess(MetadataAccess metadataAccess) {
        this.metadataAccess = metadataAccess;
    }
}
