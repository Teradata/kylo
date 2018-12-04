package com.thinkbiganalytics.nifi.v2.core.cleanup;

/*-
 * #%L
 * thinkbig-nifi-core-service
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

import com.thinkbiganalytics.jms.JmsConstants;
import com.thinkbiganalytics.metadata.event.jms.MetadataQueues;
import com.thinkbiganalytics.metadata.rest.model.event.FeedCleanupTriggerEvent;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupEventConsumer;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

/**
 * Gets the JMS consumer for cleanup events.
 */
public class JmsCleanupEventConsumer implements CleanupEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(JmsCleanupEventConsumer.class);

    /**
     * Map of feed category and name to listener
     */
    @Nonnull
    private final ConcurrentMap<String, CleanupListener> feedListeners = new ConcurrentHashMap<>();
    
    /**
     * Set of listeners that get notified of events for all feeds.
     */
    @Nonnull
    private final Set<CleanupListener> anyFeedListeners = ConcurrentHashMap.newKeySet();
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.cleanup.CleanupEventConsumer#addListener(com.thinkbiganalytics.nifi.core.api.cleanup.CleanupListener)
     */
    @Override
    public void addListener(CleanupListener listener) {
        LOG.debug("Adding listener for any feed, consumer {}", this);
        anyFeedListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.cleanup.CleanupEventConsumer#addListener(java.lang.String, java.lang.String, com.thinkbiganalytics.nifi.core.api.cleanup.CleanupListener)
     */
    @Override
    public void addListener(@Nonnull String category, @Nonnull String feedName, @Nonnull CleanupListener listener) {
        String key = generateKey(category, feedName);
        LOG.debug("Adding listener for {}, consumer {}", key, this);
        feedListeners.put(key, listener);
    }

    /**
     * Processes cleanup events.
     *
     * @param event the cleanup event
     */
    @JmsListener(destination = MetadataQueues.CLEANUP_TRIGGER, containerFactory = JmsConstants.QUEUE_LISTENER_CONTAINER_FACTORY)
    public void receiveEvent(@Nonnull final FeedCleanupTriggerEvent event) {
        LOG.debug("Received JMS message - topic: {}, message: {}, consumer {}", MetadataQueues.CLEANUP_TRIGGER, event, this);
        LOG.info("Received feed cleanup trigger event: {}", event);

        String key = generateKey(event.getCategoryName(), event.getFeedName());
        LOG.debug("Looking up listener for {}", key);
        CleanupListener listener = feedListeners.get(key);
        if (listener != null) {
            LOG.debug("Found listener for {}, triggering event {}", key, event);
            listener.triggered(event);
        } else {
            LOG.debug("Found no listener for {}", key);
        }
        
        this.anyFeedListeners.forEach(anyListener -> {
            LOG.debug("Triggering event {} for listener {}", event, anyListener);
            anyListener.triggered(event);
        });
    }

    /**
     * removes the listener that was previously added with addListener
     *
     * @param listener the listener to be removed
     */
    @Override
    public void removeListener(@Nonnull CleanupListener listener) {
        LOG.debug("Remove listener {}", listener);
        feedListeners.values().remove(listener);
        anyFeedListeners.remove(listener);
    }

    /**
     * Gets the key for the specified feed.
     *
     * @param category the category system name
     * @param feedName the feed system name
     * @return the map key
     */
    @Nonnull
    private String generateKey(String category, String feedName) {
        return category + "." + feedName;
    }
}
