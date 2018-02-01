package com.thinkbiganalytics.feedmgr.nifi;
/*-
 * #%L
 * kylo-feed-manager-controller
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

import com.thinkbiganalytics.jms.JmsService;
import com.thinkbiganalytics.nifi.savepoint.api.SavepointTopics;
import com.thinkbiganalytics.nifi.savepoint.model.SavepointReplayEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsMessagingTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.Topic;

/**
 * JMS service the send and receive Streaming Feed Metadata information to JMS that will be captured by NiFi to assist in provenance processing.
 */
public class SavepointReplayJmsEventService {

    private static final Logger log = LoggerFactory.getLogger(SavepointReplayJmsEventService.class);


    @Autowired
    @Qualifier("jmsTemplate")
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Inject
    private JmsService jmsService;

    /**
     * The topic to send/receive messages about streaming feed metadata
     */
    private Topic savepointReplayTopic;

    @PostConstruct
    private void init() {
        savepointReplayTopic = jmsService.getTopic(SavepointTopics.REPLAY_SAVEPOINT_TOPIC);
    }

    /**
     *
     * @param event
     */
    public void triggerSavepoint(SavepointReplayEvent event) {
        jmsMessagingTemplate.convertAndSend(savepointReplayTopic, event);
    }


}
