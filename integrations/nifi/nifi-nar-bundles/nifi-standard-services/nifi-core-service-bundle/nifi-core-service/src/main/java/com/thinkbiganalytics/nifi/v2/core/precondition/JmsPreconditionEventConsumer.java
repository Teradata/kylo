/*
 * Copyright (c) 2016. Teradata Inc.
 */
package com.thinkbiganalytics.nifi.v2.core.precondition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import com.thinkbiganalytics.metadata.event.jms.MetadataTopics;
import com.thinkbiganalytics.metadata.rest.model.event.FeedPreconditionTriggerEvent;
import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionEventConsumer;
import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionListener;

/**
 * @author Sean Felten
 */
public class JmsPreconditionEventConsumer implements PreconditionEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(JmsPreconditionEventConsumer.class);

    private ConcurrentMap<String, PreconditionListener> listeners = new ConcurrentHashMap<>();


    @JmsListener(destination = MetadataTopics.PRECONDITION_TRIGGER, containerFactory = "metadataListenerContainerFactory")
    public void receiveEvent(FeedPreconditionTriggerEvent event) {
        LOG.debug("Received JMS message - topic: {}, message: {}", MetadataTopics.PRECONDITION_TRIGGER, event);
        LOG.info("Received feed precondition trigger event: {}", event);
        
        PreconditionListener listener = this.listeners.get(generateKey(event.getCategory(), event.getFeedName()));

        if (listener != null) {
            listener.triggered(event);
        }

    }

    public void addListener(String category, String feedName, PreconditionListener listener) {
        this.listeners.put(generateKey(category, feedName), listener);
    }

    public void removeListener(PreconditionListener listener) {
        this.listeners.values().remove(listener);
    }
    
    private String generateKey(String category, String feedName) {
        return category + "." + feedName;
    }
}
