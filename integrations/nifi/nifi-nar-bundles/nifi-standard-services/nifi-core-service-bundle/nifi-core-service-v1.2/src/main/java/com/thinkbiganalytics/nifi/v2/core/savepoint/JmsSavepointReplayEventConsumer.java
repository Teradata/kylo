package com.thinkbiganalytics.nifi.v2.core.savepoint;

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
import com.thinkbiganalytics.nifi.savepoint.api.SavepointReplayEventConsumer;
import com.thinkbiganalytics.nifi.savepoint.api.SavepointReplayEventListener;
import com.thinkbiganalytics.nifi.savepoint.api.SavepointTopics;
import com.thinkbiganalytics.nifi.savepoint.model.SavepointReplayEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import java.util.ArrayList;
import java.util.List;

//import org.springframework.jms.annotation.JmsListener;

/**
 * Consumes the savepoint replay events in JMS
 */
public class JmsSavepointReplayEventConsumer implements SavepointReplayEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(JmsSavepointReplayEventConsumer.class);
    private List<SavepointReplayEventListener> listeners = new ArrayList<>();

    /**
     * default constructor
     */
    public JmsSavepointReplayEventConsumer() {
        LOG.info("Creating new JmsSavepointReplayEventConsumer ");
    }

    @JmsListener(destination = SavepointTopics.REPLAY_SAVEPOINT_TOPIC, containerFactory = JmsConstants.TOPIC_LISTENER_CONTAINER_FACTORY)
    public void receiveEvent(SavepointReplayEvent event) {
        LOG.debug("{} Received JMS message - topic: {}, message: {}", this, SavepointTopics.REPLAY_SAVEPOINT_TOPIC, event);
        LOG.info("{} Received feed savepoint replay trigger event with {} listeners.  Event: {}", this, listeners.size(), event);

        this.listeners.stream().forEach(listener -> listener.triggered(event));

    }

    @Override
    public void addListener(SavepointReplayEventListener listener) {
        LOG.info("{} Adding listener for SavepointReplayEvent");
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(SavepointReplayEventListener listener) {
        LOG.info("{} Removing listener {}", this, listener);
        this.listeners.remove(listener);
    }

}
