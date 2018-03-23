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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(FeedFailureService.class);

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

    private static String EMPTY_FEED_JOB_NAME = "!!!EMPTY!!!";



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


    public boolean isEmptyJob(LastFeedJob lastFeedJob){
        return lastFeedJob.getFeedName().equals(EMPTY_FEED_JOB_NAME);
    }

    private LastFeedJob newEmptyFeedJob(DateTime dateTime){
        return new LastFeedJob(EMPTY_FEED_JOB_NAME, dateTime, true);
    }

    /**
     * Find the latest Job, first looking for any failures between the last time this service ran, and now.
     *
     * @param feedName feed to check
     */
    public LastFeedJob findLatestJob(String feedName) {
        LastFeedJob lastFeedJob = metadataAccess.read(() -> {

            LastFeedJob lastAssessedJob = lastAssessedFeedMap.getOrDefault(feedName, newEmptyFeedJob(DateTime.now()));
            LOG.debug("Feed failure service check.  LastAssessJob from map is {}",lastAssessedJob);
            DateTime lastAssessedTime = lastAssessedJob.getDateTime();
            if (isEmptyJob(lastAssessedJob)) {
                //attempt to get jobs since the app started
                lastAssessedTime = servicesApplicationStartupListener.getStartTime();
                lastAssessedJob.setDateTime(lastAssessedTime);
            }

            OpsManagerFeed feed = feedProvider.findByName(feedName);
            if(feed == null){
                LOG.error("Feed Failure Service check Error!!!  Unable to find feed for:  {}",feedName);
                return newEmptyFeedJob(DateTime.now());
            }
            if (feed.isStream()) {
                List<NifiFeedProcessorStats> latestStats = nifiFeedProcessorStatisticsProvider.findLatestFinishedStatsSince(feedName, lastAssessedTime);
                LOG.debug("Streaming Feed failure check for {}.  Found {} stats",feedName,latestStats.size());
                Optional<NifiFeedProcessorStats> total = latestStats.stream().reduce((a, b) -> {
                    a.setFailedCount(a.getFailedCount() + b.getFailedCount());
                    if (b.getMinEventTime().isAfter(a.getMinEventTime())) {
                        a.setMinEventTime(b.getMinEventTime());
                    }
                    return a;
                });
                LastFeedJob lastJob = null;
                if (total.isPresent()) {
                    NifiFeedProcessorStats stats = total.get();
                    boolean success = stats.getFailedCount() == 0;
                     lastJob = new LastFeedJob(feedName, stats.getMinEventTime(), success);

                } else {
                     lastJob = new LastFeedJob(feedName, lastAssessedTime,true);
                }
                LOG.debug("{} stats for feed. Streaming Feed failure returning {}",total.isPresent() ? "Found":"Did not find any",lastJob);
                return lastJob;
            } else {

                List<? extends BatchJobExecution> latestJobs = batchJobExecutionProvider.findLatestFinishedJobForFeedSince(feedName, lastAssessedTime);
                LOG.debug("Batch Feed failure check for {}.  Found {} jobs",feedName,latestJobs != null ? latestJobs.size() : 0);
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


                LastFeedJob lastJob = latestJob != null ? new LastFeedJob(feedName, latestJob.getEndTime(), !FAILED.equals(latestJob.getStatus()),latestJob.getJobExecutionId()) : newEmptyFeedJob(lastAssessedTime);
                LOG.debug("Batch Feed failure check returning {} for feed {}",lastJob,feedName);
                return lastJob;

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
        private Long batchJobExecutionId;

        public LastFeedJob(String feedName, DateTime dateTime, boolean success) {
            this.feedName = feedName;
            this.dateTime = dateTime;
            this.success = success;
        }

        public LastFeedJob(String feedName, DateTime dateTime, boolean success, Long batchJobExecutionId) {
            this.feedName = feedName;
            this.dateTime = dateTime;
            this.success = success;
            this.batchJobExecutionId = batchJobExecutionId;
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

        public void setDateTime(DateTime dateTime){
            this.dateTime = dateTime;
        }

        public boolean isAfter(DateTime time) {
            return dateTime != null && dateTime.isAfter(time);
        }

        public boolean isFailure() {
            return !this.success;
        }

        public Long getBatchJobExecutionId() {
            return batchJobExecutionId;
        }

        public void setBatchJobExecutionId(Long batchJobExecutionId) {
            this.batchJobExecutionId = batchJobExecutionId;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("LastFeedJob{");
            sb.append("feedName='").append(feedName).append('\'');
            sb.append(", dateTime=").append(dateTime);
            sb.append(", success=").append(success);
            if(batchJobExecutionId != null) {
                sb.append(", batchJobExecutionId=").append(batchJobExecutionId);
            }
            sb.append('}');
            return sb.toString();
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
