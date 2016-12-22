package com.thinkbiganalytics.nifi.v2.core.cleanup;

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

    /** Map of feed category and name to listener */
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
