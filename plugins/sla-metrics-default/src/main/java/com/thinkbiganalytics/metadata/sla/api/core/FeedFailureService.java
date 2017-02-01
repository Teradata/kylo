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

import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 * Service to listen for feed failure events and notify listeners when a feed fails
 */
@Component
public class FeedFailureService {


    @Inject
    private MetadataEventService eventService;

    /**
     * Event listener for failure events
     */
    private final MetadataEventListener<FeedOperationStatusEvent> failedFeedEventListener = new FailedFeedEventDispatcher();


    /**
     * Map with the Latest recorded Feed Failure
     */
    private Map<String, LastFeedFailure> lastFeedFailureMap = new HashMap<>();

    /**
     * Map with the Latest recorded failure that has been assessed by the FeedFailureMetricAssessor
     */
    private Map<String, LastFeedFailure> lastAssessedFeedFailureMap = new HashMap<>();

    /**
     * Adds listeners for transferring events.
     */
    @PostConstruct
    public void addEventListener() {
        eventService.addListener(failedFeedEventListener);
    }

    /**
     * Removes listeners and stops transferring events.
     */
    @PreDestroy
    public void removeEventListener() {
        eventService.removeListener(failedFeedEventListener);
    }

    /**
     * populate latest failure events
     */
    private class FailedFeedEventDispatcher implements MetadataEventListener<FeedOperationStatusEvent> {

        @Override
        public void notify(@Nonnull final FeedOperationStatusEvent event) {
            if (FeedOperation.State.FAILURE.equals(event.getData().getState())) {
                event.getData().getFeedName();
                lastFeedFailureMap.put(event.getData().getFeedName(), new LastFeedFailure(event.getData().getFeedName()));
            }
        }
    }

    /**
     * Should we assess the failure.  If so mark the latest as being assesed as a failure
     */
    public boolean hasFailure(String feedName) {
        LastFeedFailure lastFeedFailure = lastFeedFailureMap.get(feedName);
        LastFeedFailure lastAssessedFailure = lastAssessedFeedFailureMap.get(feedName);

        if (lastFeedFailure != null) {
            if (lastAssessedFailure == null || (lastAssessedFailure != null && lastFeedFailure.isAfter(lastAssessedFailure.getDateTime()))) {
                //reassign it as the lastAssessedFailure
                lastAssessedFeedFailureMap.put(feedName, lastFeedFailure);
                return true;
            }
        }
        return false;
    }

    public static class LastFeedFailure {

        private String feedName;
        private DateTime dateTime;

        public LastFeedFailure() {

        }

        public LastFeedFailure(String feedName) {
            this.feedName = feedName;
            this.dateTime = DateTime.now();
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

        public void setDateTime(DateTime dateTime) {
            this.dateTime = dateTime;
        }

        public boolean isAfter(DateTime time) {
            return dateTime != null && dateTime.isAfter(time);
        }
    }

}
