package com.thinkbiganalytics.feedmgr.service.feed;
/*-
 * #%L
 * thinkbig-job-repository-controller
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
import com.thinkbiganalytics.jms.JmsService;
import com.thinkbiganalytics.jobrepo.service.JobService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.nifi.savepoint.api.SavepointQueues;
import com.thinkbiganalytics.nifi.savepoint.api.SavepointTopics;
import com.thinkbiganalytics.nifi.savepoint.model.SavepointReplayEvent;
import com.thinkbiganalytics.nifi.savepoint.model.SavepointReplayResponseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
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

    @Inject
    private JobService jobService;

    @Inject
    private MetadataAccess metadataAccess;

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
        //mark it as running

        jmsMessagingTemplate.convertAndSend(savepointReplayTopic, event);
    }


    @JmsListener(destination = SavepointQueues.REPLAY_SAVEPOINT_RESPONE_QUEUE, containerFactory = JmsConstants.QUEUE_LISTENER_CONTAINER_FACTORY)
    public void receiveEvent(SavepointReplayResponseEvent event) {
        log.info("{} Received feed savepoint replay response event  {} ", event);

       /* if (event.getResponse() == SavepointReplayResponseEvent.RESPONSE.FAILURE) {
            //fail it if we get an error on the trigger
            metadataAccess.commit(() -> {
                //query for job and fail it if its not already failed
                this.jobService.failJobExecution(event.getJobExecutionId());
                return null;
            }, MetadataAccess.SERVICE);
        }
        */


    }


}
