package com.thinkbiganalytics.metadata.event.jms;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.CleanupTriggerEvent;
import com.thinkbiganalytics.metadata.api.event.feed.PreconditionTriggerEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.rest.model.event.FeedCleanupTriggerEvent;
import com.thinkbiganalytics.metadata.rest.model.event.FeedPreconditionTriggerEvent;

import org.springframework.jms.core.JmsMessagingTemplate;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Topic;

/**
 * Listens for metadata events that should be transferred to a JMS topic.
 */
public class JmsChangeEventDispatcher {

    /** JMS topic for triggering feeds for cleanup */
    @Inject
    @Named("cleanupTriggerTopic")
    private Topic cleanupTriggerTopic;

    /** Metadata event bus */
    @Inject
    private MetadataEventService eventService;

    /** Feed object provider */
    @Inject
    private FeedProvider feedProvider;

    /** Spring JMS messaging template */
    @Inject
    @Named("metadataMessagingTemplate")
    private JmsMessagingTemplate jmsMessagingTemplate;

    /** Metadata transaction wrapper */
    @Inject
    private MetadataAccess metadata;

    /** JMS topic for triggering feeds based on preconditions */
    @Inject
    @Named("preconditionTriggerTopic")
    private Topic preconditionTriggerTopic;

    /** Event listener for cleanup events */
    private final MetadataEventListener<CleanupTriggerEvent> cleanupListener = new CleanupTriggerDispatcher();

    /** Event listener for precondition events */
    private final MetadataEventListener<PreconditionTriggerEvent> preconditionListener = new PreconditionTriggerDispatcher();

    /**
     * Adds listeners for transferring events.
     */
    @PostConstruct
    public void addEventListener() {
        eventService.addListener(cleanupListener);
        eventService.addListener(preconditionListener);
    }

    /**
     * Removes listeners and stops transferring events.
     */
    @PreDestroy
    public void removeEventListener() {
        eventService.removeListener(cleanupListener);
        eventService.removeListener(preconditionListener);
    }

    /**
     * Transfers cleanup events to JMS.
     */
    private class CleanupTriggerDispatcher implements MetadataEventListener<CleanupTriggerEvent> {

        @Override
        public void notify(@Nonnull final CleanupTriggerEvent metadataEvent) {
            FeedCleanupTriggerEvent jmsEvent = new FeedCleanupTriggerEvent(metadataEvent.getData().toString());

            metadata.read(() -> {
                Feed<?> feed = feedProvider.getFeed(metadataEvent.getData());
                jmsEvent.setFeedName(feed.getName());
                jmsEvent.setCategoryName(feed.getCategory().getName());
                return jmsEvent;
            }, MetadataAccess.SERVICE);

            jmsMessagingTemplate.convertAndSend(cleanupTriggerTopic, jmsEvent);
        }
    }

    /**
     * Transfers precondition events to JMS.
     */
    private class PreconditionTriggerDispatcher implements MetadataEventListener<PreconditionTriggerEvent> {

        @Override
        public void notify(@Nonnull final PreconditionTriggerEvent event) {
            FeedPreconditionTriggerEvent triggerEv = new FeedPreconditionTriggerEvent(event.getData().toString());

            metadata.read(() -> {
                Feed<?> feed = feedProvider.getFeed(event.getData());
                triggerEv.setFeedName(feed.getName());
                triggerEv.setCategory(feed.getCategory().getName());
                return triggerEv;
            }, MetadataAccess.SERVICE);

            jmsMessagingTemplate.convertAndSend(preconditionTriggerTopic, triggerEv);
        }
    }
}
