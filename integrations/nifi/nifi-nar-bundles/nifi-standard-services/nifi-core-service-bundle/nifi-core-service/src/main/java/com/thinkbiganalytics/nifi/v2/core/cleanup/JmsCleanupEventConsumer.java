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

import com.thinkbiganalytics.metadata.event.jms.MetadataQueues;
import com.thinkbiganalytics.metadata.rest.model.event.FeedCleanupTriggerEvent;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupEventConsumer;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupListener;
import com.thinkbiganalytics.nifi.v2.core.precondition.JmsPreconditionEventConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

/**
 * Gets the JMS consumer for cleanup events.
 */
public class JmsCleanupEventConsumer implements CleanupEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(JmsPreconditionEventConsumer.class);

    /**
     * Map of feed category and name to listener
     */
    @Nonnull
    private final ConcurrentMap<String, CleanupListener> listeners = new ConcurrentHashMap<>();

    @Override
    public void addListener(@Nonnull String category, @Nonnull String feedName, @Nonnull CleanupListener listener) {
        listeners.put(generateKey(category, feedName), listener);
    }

    /**
     * Processes cleanup events.
     *
     * @param event the cleanup event
     */
    @JmsListener(destination = MetadataQueues.CLEANUP_TRIGGER, containerFactory = "metadataListenerContainerFactory")
    public void receiveEvent(@Nonnull final FeedCleanupTriggerEvent event) {
        LOG.debug("Received JMS message - topic: {}, message: {}", MetadataQueues.CLEANUP_TRIGGER, event);
        LOG.info("Received feed cleanup trigger event: {}", event);

        CleanupListener listener = listeners.get(generateKey(event.getCategoryName(), event.getFeedName()));
        if (listener != null) {
            listener.triggered(event);
        }
    }

    @Override
    public void removeListener(@Nonnull CleanupListener listener) {
        listeners.values().remove(listener);
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
