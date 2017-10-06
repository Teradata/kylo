package com.thinkbiganalytics.metadata.jpa.jobrepo.job;
/*-
 * #%L
 * kylo-operational-metadata-jpa
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

import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.event.feed.OperationStatus;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

import javax.inject.Inject;

/**
 * Notify other listeners when a Job Execution changes status
 */
public class JobExecutionChangedNotifier {


    @Inject
    private MetadataEventService eventService;



    protected static FeedOperation.State asOperationState(BatchJobExecution.JobStatus status) {
        switch (status) {
            case ABANDONED:
                return FeedOperation.State.ABANDONED;
            case COMPLETED:
                return FeedOperation.State.SUCCESS;
            case FAILED:
                return FeedOperation.State.FAILURE;
            case STARTED:
                return FeedOperation.State.STARTED;
            case STARTING:
                return FeedOperation.State.STARTED;
            case STOPPING:
                return FeedOperation.State.STARTED;
            case STOPPED:
                return FeedOperation.State.CANCELED;
            case UNKNOWN:
                return FeedOperation.State.STARTED;
            default:
                return FeedOperation.State.FAILURE;
        }
    }


    public void notifyStopped(BatchJobExecution jobExecution, OpsManagerFeed feed, String status) {

        notifyOperationStatusEvent(jobExecution, feed, FeedOperation.State.CANCELED, status);

    }

    public void notifyDataConfidenceJob(BatchJobExecution jobExecution, OpsManagerFeed feed, String status) {
        if (feed == null) {
            feed = jobExecution.getJobInstance().getFeed();
        }
        FeedOperation.State state = asOperationState(jobExecution.getStatus());
        if (StringUtils.isBlank(status)) {
            status = "Job " + jobExecution.getJobExecutionId() + " " + state.name().toLowerCase() + " for feed: " + (feed != null ? feed.getName() : null);
        }
        this.eventService.notify(newFeedOperationStatusEvent(jobExecution.getJobExecutionId(), feed, state, status));
    }


    public void notifySuccess(BatchJobExecution jobExecution, OpsManagerFeed feed, String status) {

        notifyOperationStatusEvent(jobExecution, feed, FeedOperation.State.SUCCESS, status);

    }

    public void notifyAbandoned(BatchJobExecution jobExecution, OpsManagerFeed feed, String status) {

        notifyOperationStatusEvent(jobExecution, feed, FeedOperation.State.ABANDONED, status);

    }

    public void notifyStarted(BatchJobExecution jobExecution, OpsManagerFeed feed, String status) {
        notifyOperationStatusEvent(jobExecution, feed, FeedOperation.State.STARTED, status);

    }

    public void notifyOperationStatusEvent(BatchJobExecution jobExecution, OpsManagerFeed feed, FeedOperation.State state, String status) {

        if (feed == null) {
            feed = jobExecution.getJobInstance().getFeed();
        }
        if (StringUtils.isBlank(status)) {
            status = "Job " + jobExecution.getJobExecutionId() + " " + state.name().toLowerCase() + " for feed: " + (feed != null ? feed.getName() : null);
        }
        this.eventService.notify(newFeedOperationStatusEvent(jobExecution.getJobExecutionId(), feed, state, status));

    }

    private FeedOperationStatusEvent newFeedOperationStatusEvent(Long jobExecutionId, OpsManagerFeed feed, FeedOperation.State state, String status) {
        String feedName = feed != null ? feed.getName() : null;
        Feed.ID feedId = feed != null ? feed.getId() : null;
        FeedOperation.FeedType feedType = feed != null ? FeedOperation.FeedType.valueOf(feed.getFeedType().name()) : FeedOperation.FeedType.FEED;
        return new FeedOperationStatusEvent(new OperationStatus(feedId, feedName, feedType, new OpId(jobExecutionId), state, status));
    }

    protected static class OpId implements FeedOperation.ID {

        private final String idValue;

        public OpId(Serializable value) {
            this.idValue = value.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (getClass().isAssignableFrom(obj.getClass())) {
                OpId that = (OpId) obj;
                return Objects.equals(this.idValue, that.idValue);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.idValue);
        }

        @Override
        public String toString() {
            return this.idValue;
        }
    }


}
