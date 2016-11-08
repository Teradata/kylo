package com.thinkbiganalytics.metadata.sla.api.core;

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
 * Created by sr186054 on 11/8/16.
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
     * Transfers precondition events to JMS.
     */
    private class FailedFeedEventDispatcher implements MetadataEventListener<FeedOperationStatusEvent> {

        @Override
        public void notify(@Nonnull final FeedOperationStatusEvent event) {
            if (FeedOperation.State.FAILURE.equals(event.getState())) {
                event.getFeedName();
                lastFeedFailureMap.put(event.getFeedName(), new LastFeedFailure(event.getFeedName()));
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
            return dateTime != null && dateTime.isBefore(time);
        }
    }

}
