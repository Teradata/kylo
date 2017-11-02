package com.thinkbiganalytics.jobrepo.query.model.transform;

/*-
 * #%L
 * thinkbig-job-repository-core
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

import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.DefaultFeedHealth;
import com.thinkbiganalytics.jobrepo.query.model.DefaultFeedStatus;
import com.thinkbiganalytics.jobrepo.query.model.DefaultFeedSummary;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;
import com.thinkbiganalytics.jobrepo.query.model.FeedStatus;
import com.thinkbiganalytics.metadata.api.feed.FeedSummary;
import com.thinkbiganalytics.metadata.api.feed.LatestFeedJobExecution;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Transform Feed domain model objects to REST friendly objects
 */
public class FeedModelTransform {

    /**
     * Transform the BatchJobExectution into an ExecutedFeed object
     *
     * @param jobExecution the job to transform
     * @param feed         the feed this job relates to
     * @return the ExecutedFeed transformed from the BatchJobExecution
     */
    public static ExecutedFeed executedFeed(BatchJobExecution jobExecution, OpsManagerFeed feed) {
        return executedFeed(jobExecution, feed.getName());
    }

    /**
     * Transform the BatchJobExectution into an ExecutedFeed object
     *
     * @param jobExecution the job to transform
     * @param feedName     the name of the feed the job relates to
     * @return the ExecutedFeed transformed from the BatchJobExecution
     */
    public static ExecutedFeed executedFeed(BatchJobExecution jobExecution, String feedName) {

        DefaultExecutedFeed executedFeed = new DefaultExecutedFeed();
        executedFeed.setFeedExecutionId(jobExecution.getJobExecutionId());
        executedFeed.setStartTime(jobExecution.getStartTime());
        executedFeed.setEndTime(jobExecution.getEndTime());
        executedFeed.setExitCode(jobExecution.getExitCode().name());
        executedFeed.setExitStatus(jobExecution.getExitMessage());
        executedFeed.setStatus(ExecutionStatus.valueOf(jobExecution.getStatus().name()));
        executedFeed.setName(feedName);
        executedFeed.setRunTime(ModelUtils.runTime(jobExecution.getStartTime(), jobExecution.getEndTime()));
        executedFeed.setTimeSinceEndTime(ModelUtils.timeSince(jobExecution.getStartTime(), jobExecution.getEndTime()));
        executedFeed.setFeedInstanceId(jobExecution.getJobInstance().getJobInstanceId());
        return executedFeed;

    }


    /**
     * Transform the LatestJobExecution into an ExecutedFeed object
     *
     * @return the ExecutedFeed from the LatestFeedJobExecution
     */
    public static ExecutedFeed executedFeed(LatestFeedJobExecution jobExecution) {

        DefaultExecutedFeed executedFeed = new DefaultExecutedFeed();
        executedFeed.setFeedExecutionId(jobExecution.getJobExecutionId());
        executedFeed.setStartTime(jobExecution.getStartTime());
        executedFeed.setEndTime(jobExecution.getEndTime());
        executedFeed.setExitCode(jobExecution.getExitCode().name());
        executedFeed.setExitStatus(jobExecution.getExitMessage());
        executedFeed.setStatus(ExecutionStatus.valueOf(jobExecution.getStatus().name()));
        executedFeed.setName(jobExecution.getFeedName());
        executedFeed.setRunTime(ModelUtils.runTime(jobExecution.getStartTime(), jobExecution.getEndTime()));
        executedFeed.setTimeSinceEndTime(ModelUtils.timeSince(jobExecution.getStartTime(), jobExecution.getEndTime()));
        executedFeed.setFeedInstanceId(jobExecution.getJobInstanceId());
        return executedFeed;

    }

    /**
     * Transform the FeedHealth object into an Executed feed
     *
     * @return the ExecutedFeed from the FeedHealth object
     */
    public static ExecutedFeed executedFeed(com.thinkbiganalytics.metadata.api.feed.FeedHealth feedHealth) {

        DefaultExecutedFeed executedFeed = new DefaultExecutedFeed();
        executedFeed.setFeedExecutionId(feedHealth.getJobExecutionId());
        executedFeed.setStartTime(feedHealth.getStartTime());
        executedFeed.setEndTime(feedHealth.getEndTime());
        executedFeed.setExitCode(feedHealth.getExitCode().name());
        executedFeed.setExitStatus(feedHealth.getExitMessage());
        executedFeed.setStatus(ExecutionStatus.valueOf(feedHealth.getStatus().name()));
        executedFeed.setName(feedHealth.getFeedName());
        executedFeed.setRunTime(ModelUtils.runTime(feedHealth.getStartTime(), feedHealth.getEndTime()));
        executedFeed.setTimeSinceEndTime(ModelUtils.timeSince(feedHealth.getStartTime(), feedHealth.getEndTime()));
        executedFeed.setFeedInstanceId(feedHealth.getJobInstanceId());
        return executedFeed;

    }

    /**
     * Transform the FeedHealth object into an Executed feed
     *
     * @return the ExecutedFeed from the FeedHealth object
     */
    public static ExecutedFeed executedFeed(FeedSummary feedSummary) {

        DefaultExecutedFeed executedFeed = new DefaultExecutedFeed();
        executedFeed.setFeedExecutionId(feedSummary.getJobExecutionId());
        executedFeed.setStartTime(feedSummary.getStartTime());
        executedFeed.setEndTime(feedSummary.getEndTime());
        if(feedSummary.getExitCode() != null) {
            executedFeed.setExitCode(feedSummary.getExitCode().name());
        }
        else {
            executedFeed.setExitCode(ExecutionConstants.ExitCode.UNKNOWN.name());
        }
        executedFeed.setExitStatus(feedSummary.getExitMessage());
        if(feedSummary.getStatus() != null) {
            executedFeed.setStatus(ExecutionStatus.valueOf(feedSummary.getStatus().name()));
        }
        else {
            executedFeed.setStatus(ExecutionStatus.UNKNOWN);
        }
        executedFeed.setName(feedSummary.getFeedName());
        executedFeed.setRunTime(ModelUtils.runTime(feedSummary.getStartTime(), feedSummary.getEndTime()));
        executedFeed.setTimeSinceEndTime(ModelUtils.timeSince(feedSummary.getStartTime(), feedSummary.getEndTime()));
        executedFeed.setFeedInstanceId(feedSummary.getJobInstanceId());
        return executedFeed;

    }

    /**
     * Transforms the FeedHealth domain object to the Rest model object
     */
    public static List<FeedHealth> feedHealth(List<? extends com.thinkbiganalytics.metadata.api.feed.FeedHealth> domain) {
        if (domain != null && !domain.isEmpty()) {
            return domain.stream().map(d -> feedHealth(d)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Transform the FeedHealth domain object to the REST friendly FeedHealth object
     *
     * @return the transformed FeedHealth object
     */
    public static FeedHealth feedHealth(com.thinkbiganalytics.metadata.api.feed.FeedHealth domain) {
        FeedHealth feedHealth = new DefaultFeedHealth();
        feedHealth.setUnhealthyCount(domain.getFailedCount());
        feedHealth.setHealthyCount(domain.getCompletedCount());
        feedHealth.setFeed(domain.getFeedName());
        feedHealth.setFeedId(domain.getFeedId() != null ? domain.getFeedId().toString() : null);
        feedHealth.setLastOpFeed(executedFeed(domain));
        feedHealth.setStream(domain.isStream());
        feedHealth.setRunningCount(domain.getRunningCount());
        return feedHealth;
    }


    /**
     * Transform the FeedHealth domain object to the REST friendly FeedHealth object
     *
     * @return the transformed FeedHealth object
     */
    public static FeedHealth feedHealth(FeedSummary domain) {
        FeedHealth feedHealth = new DefaultFeedHealth();
        feedHealth.setUnhealthyCount(domain.getFailedCount());
        feedHealth.setHealthyCount(domain.getCompletedCount());
        feedHealth.setFeed(domain.getFeedName());
        feedHealth.setFeedId(domain.getFeedId() != null ? domain.getFeedId().toString() : null);
        feedHealth.setLastOpFeed(executedFeed(domain));
        feedHealth.setStream(domain.isStream());
        feedHealth.setRunningCount(domain.getRunningCount());
        return feedHealth;
    }


    /**
     * Transforms the feed object to the REST friendly FeedHealth object, assuming the feed has not yet executed.
     *
     * @param domain the feed object
     * @return the transformed FeedHealth object
     */
    @Nonnull
    public static FeedHealth feedHealth(@Nonnull final OpsManagerFeed domain) {
        final FeedHealth feedHealth = new DefaultFeedHealth();
        feedHealth.setUnhealthyCount(0L);
        feedHealth.setHealthyCount(0L);
        feedHealth.setFeed(domain.getName());
        feedHealth.setFeedId(domain.getId() != null ? domain.getId().toString() : null);
        feedHealth.setStream(domain.isStream());
        return feedHealth;
    }

    /**
     * Transform the list of FeedHealth objects to a FeedStatus object summarizing the feeds.
     *
     * @return the FeedStatus summarizing the Feeds for the list of FeedHealth objects
     */
    public static FeedStatus feedStatus(List<FeedHealth> feedHealth) {

        DefaultFeedStatus status = new DefaultFeedStatus(feedHealth);
        return status;

    }


    public static FeedStatus feedStatusFromFeedSummary(List<com.thinkbiganalytics.jobrepo.query.model.FeedSummary> feedSummaryList) {

        DefaultFeedStatus status = new DefaultFeedStatus();
        status.populate(feedSummaryList);
        return status;

    }

    /**
     * Transforms the feed object to a FeedStatus object summarizing the feed, assuming the feed has not yet executed.
     *
     * @param domain the feed object
     * @return the FeedStatus summarizing the Feed
     */
    @Nonnull
    public static FeedStatus feedStatus(@Nonnull final OpsManagerFeed domain) {
        final DefaultFeedStatus status = new DefaultFeedStatus(null);
        status.getFeedSummary().add(new DefaultFeedSummary(feedHealth(domain)));
        return status;
    }
}
