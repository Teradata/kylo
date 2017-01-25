package com.thinkbiganalytics.nifi.v2.core.precondition;

import com.thinkbiganalytics.metadata.event.jms.MetadataQueues;
import com.thinkbiganalytics.metadata.rest.model.event.FeedPreconditionTriggerEvent;
import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionEventConsumer;
import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Sean Felten
 */
public class JmsPreconditionEventConsumer implements PreconditionEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(JmsPreconditionEventConsumer.class);

    private ConcurrentMap<String, PreconditionListener> listeners = new ConcurrentHashMap<>();

    public JmsPreconditionEventConsumer() {
        LOG.debug("New JmsPreconditionEventConsumer {}", this);
    }

    @JmsListener(destination = MetadataQueues.PRECONDITION_TRIGGER, containerFactory = "metadataListenerContainerFactory")
    public void receiveEvent(FeedPreconditionTriggerEvent event) {
        LOG.debug("{} Received JMS message - topic: {}, message: {}", this, MetadataQueues.PRECONDITION_TRIGGER, event);
        LOG.info("{} Received feed precondition trigger event: {}", this, event);

        String key = generateKey(event.getCategory(), event.getFeedName());
        LOG.debug("{} Looking up precondition listener for key '{}'", this, key);

        PreconditionListener listener = this.listeners.get(key);

        if (listener != null) {
            LOG.debug("{} Found precondition listener for key '{}'", this, key);
            listener.triggered(event);
        } else {
            LOG.debug("{} No precondition listeners found for key '{}'", this, key);
        }

    }

    public void addListener(String category, String feedName, PreconditionListener listener) {
        LOG.info("{} Adding listener for '{}.{}'", this, category, feedName);
        this.listeners.put(generateKey(category, feedName), listener);
    }

    public void removeListener(PreconditionListener listener) {
        LOG.info("{} Removing listener {}", this, listener);
        this.listeners.values().remove(listener);
    }

    private String generateKey(String category, String feedName) {
        return category + "." + feedName;
    }
}
