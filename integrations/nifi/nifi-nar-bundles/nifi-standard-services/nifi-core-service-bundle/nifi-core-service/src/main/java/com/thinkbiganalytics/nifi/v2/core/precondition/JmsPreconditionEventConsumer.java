/*
 * Copyright (c) 2016. Teradata Inc.
 */
package com.thinkbiganalytics.nifi.v2.core.precondition;

import com.thinkbiganalytics.metadata.event.jms.MetadataTopics;
import com.thinkbiganalytics.metadata.rest.model.event.DatasourceChangeEvent;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;
import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionEventConsumer;
import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Sean Felten
 */
public class JmsPreconditionEventConsumer implements PreconditionEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(JmsPreconditionEventConsumer.class);

    private ConcurrentMap<String, Set<PreconditionListener>> listeners = new ConcurrentHashMap<>();


    @JmsListener(destination = MetadataTopics.PRECONDITION_TRIGGER, containerFactory = "metadataListenerContainerFactory")
    public void receiveMetadataChange(DatasourceChangeEvent event) {
        LOG.debug("Received JMS message - topic: {}, message: {}", MetadataTopics.PRECONDITION_TRIGGER, event);
        LOG.info("Received datasource change event - feed: {}, dataset count: {}",
                event.getFeed().getSystemName(), event.getDatasets().size());

        for (Dataset ds : event.getDatasets()) {
            Set<PreconditionListener> listeners = this.listeners.get(ds.getDatasource().getName());

            if (listeners != null) {
                for (PreconditionListener listener : listeners) {
                    LOG.debug("Notifying preconditon listener: {}", listener);
                    listener.triggered(event);
                }
            }
        }

    }

    public void addListener(String datasourceName, PreconditionListener listener) {
        Set<PreconditionListener> set = this.listeners.get(datasourceName);
        if (set == null) {
            set = new HashSet<>();
            this.listeners.put(datasourceName, set);
        }
        set.add(listener);
    }

    public void removeListener(PreconditionListener listener) {
        this.listeners.values().remove(listener);
    }

}
