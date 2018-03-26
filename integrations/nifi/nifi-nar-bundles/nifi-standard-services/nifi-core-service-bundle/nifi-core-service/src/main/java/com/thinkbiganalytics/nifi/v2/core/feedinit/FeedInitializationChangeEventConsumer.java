package com.thinkbiganalytics.nifi.v2.core.feedinit;

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
import com.thinkbiganalytics.metadata.event.jms.MetadataTopics;
import com.thinkbiganalytics.metadata.rest.model.event.FeedInitializationChangeEvent;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Consumes the precondition events in JMS
 */
public class FeedInitializationChangeEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(FeedInitializationChangeEventConsumer.class);

    private Queue<MetadataRecorder> metadataRecorders = new ConcurrentLinkedQueue<>();

    /**
     * default constructor
     */
    public FeedInitializationChangeEventConsumer() {
        super();
        LOG.debug("New FeedInitializationChangeEventConsumer {}", this);
    }
    
    public FeedInitializationChangeEventConsumer(MetadataRecorder recorder) {
        this.metadataRecorders.add(recorder);
    }

    @JmsListener(destination = MetadataTopics.FEED_INIT_STATUS_CHANGE, containerFactory = JmsConstants.TOPIC_LISTENER_CONTAINER_FACTORY)
    public void receiveEvent(FeedInitializationChangeEvent event) {
        LOG.debug("{} Received JMS message - topic: {}, message: {}", this, MetadataTopics.FEED_INIT_STATUS_CHANGE, event);
        LOG.info("{} Received feed initialization status change event: {}", this, event);

        if (this.metadataRecorders.isEmpty()) {
            LOG.debug("No metadata recorder registerd yet - ingoring event: {}", event);
        } else {
            this.metadataRecorders.forEach(r -> r.initializationStatusChanged(event.getFeedId(), event.getStatus()));
        }
    }

    public boolean addMetadataRecorder(MetadataRecorder recorder) {
        return this.metadataRecorders.add(recorder);
    }
    
    public boolean removeMetadataRecorder(MetadataRecorder recorder) {
        return this.metadataRecorders.remove(recorder);
    }
}
