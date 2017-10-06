package com.thinkbiganalytics.metadata.event.jms;

/*-
 * #%L
 * thinkbig-metadata-rest-controller
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
import javax.jms.Queue;

/**
 * Listens for metadata events that should be transferred to a JMS topic.
 */
public class JmsChangeEventDispatcher {

    /**
     * Event listener for cleanup events
     */
    private final MetadataEventListener<CleanupTriggerEvent> cleanupListener = new CleanupTriggerDispatcher();

    /**
     * Event listener for precondition events
     */
    private final MetadataEventListener<PreconditionTriggerEvent> preconditionListener = new PreconditionTriggerDispatcher();

    /**
     * JMS topic for triggering feeds for cleanup
     */
    @Inject
    @Named("cleanupTriggerQueue")
    private Queue cleanupTriggerQueue;
    /**
     * Metadata event bus
     */
    @Inject
    private MetadataEventService eventService;
    /**
     * Feed object provider
     */
    @Inject
    private FeedProvider feedProvider;
    /**
     * Spring JMS messaging template
     */
    @Inject
    private JmsMessagingTemplate jmsMessagingTemplate;
    /**
     * Metadata transaction wrapper
     */
    @Inject
    private MetadataAccess metadata;
    /**
     * JMS topic for triggering feeds based on preconditions
     */
    @Inject
    @Named("preconditionTriggerQueue")
    private Queue preconditionTriggerQueue;

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
                Feed feed = feedProvider.getFeed(metadataEvent.getData());
                jmsEvent.setFeedName(feed.getName());
                jmsEvent.setCategoryName(feed.getCategory().getSystemName());
                return jmsEvent;
            }, MetadataAccess.SERVICE);

            jmsMessagingTemplate.convertAndSend(cleanupTriggerQueue, jmsEvent);
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
                Feed feed = feedProvider.getFeed(event.getData());
                triggerEv.setFeedName(feed.getName());
                triggerEv.setCategory(feed.getCategory().getSystemName());
                return triggerEv;
            }, MetadataAccess.SERVICE);

            jmsMessagingTemplate.convertAndSend(preconditionTriggerQueue, triggerEv);
        }
    }
}
