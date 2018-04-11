package com.thinkbiganalytics.nifi.provenance.jms;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
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
import com.thinkbiganalytics.jms.Queues;
import com.thinkbiganalytics.jms.SendJmsMessage;
import com.thinkbiganalytics.jms.Topics;
import com.thinkbiganalytics.nifi.provenance.model.RemoteEventMessageResponse;
import com.thinkbiganalytics.nifi.provenance.model.RemoteEventeMessageResponseHolder;
import com.thinkbiganalytics.nifi.provenance.repo.FeedStatisticsManager;
import com.thinkbiganalytics.nifi.provenance.repo.RemoteProvenanceEventService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.Topic;

/**
 * Send ProvenanceEvent data to JMS queues
 * 2 Queues are used.  The Queue names are constants shared with Kylo Operations Manager found in the {@link Queues} class.
 * Queues.PROVENANCE_EVENT_STATS_QUEUE  is the Statistics Queue name for creating the Summary statistics
 * Queues.FEED_MANAGER_QUEUE is the Batch Provenance Events Queue for creating the Jobs/Steps in Kylo
 *
 * 1 Topic is used (Topics.NIFI_STREAMING_FEEDS_TOPIC) to transfer Streaming feed metadata to NiFi
 * The topic is needed if NiFi is clustered
 */
public class RemoteProvenanceEventJmsListener {

    private static final Logger logger = LoggerFactory.getLogger(RemoteProvenanceEventJmsListener.class);

    @Inject
    SendJmsMessage sendJmsMessage;

    @Autowired
    @Qualifier("jmsTemplate")
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Inject
    private JmsService jmsService;

    private String uniqueIdentifier;

    public RemoteProvenanceEventJmsListener() {
        //@todo make more unique with HostName
        uniqueIdentifier = UUID.randomUUID().toString();
    }

    private Topic nifiRemoteProvenanceEventRequestTopic;


    private Topic nifiRemoteProvenanceEventResponseTopic;


    @PostConstruct
    public void postConstruct() {
        logger.info("Created NiFi RemoteProvenanceEventJmsListener ");
        nifiRemoteProvenanceEventResponseTopic = jmsService.getTopic(Topics.NIFI_REMOTE_PROVENANCE_EVENT_RESPONSE_TOPIC);
    }

    /**
     * Notify others about the Remote Send Event data
     * this will be called during the JmsSender timer thread (default every 3 seconds)
     * @see com.thinkbiganalytics.nifi.provenance.repo.JmsSender
     * @see FeedStatisticsManager#gatherStatistics()
     * @see FeedStatisticsManager#initGatherStatisticsTimerThread()
     */
    public void notifyOtherNodesAboutRemoteInputPortSendEvents() {
       //Notify other Nodes of this Feed Flowfile data for those Remote Input Port SEND events
       List<RemoteEventMessageResponse> responseList = RemoteProvenanceEventService.getInstance().processRemoteInputPortSendEvents();
        if(responseList != null && !responseList.isEmpty()) {
            jmsMessagingTemplate.convertAndSend(nifiRemoteProvenanceEventResponseTopic, new RemoteEventeMessageResponseHolder(responseList, uniqueIdentifier));
        }
    }


    /**
     * Process the Remote Input Send event and Feed FlowFile data and add/map it back to this nodes mapping
     *
     * @param remoteEventMessages
     */
    @JmsListener(destination = Topics.NIFI_REMOTE_PROVENANCE_EVENT_RESPONSE_TOPIC, containerFactory = JmsConstants.TOPIC_LISTENER_CONTAINER_FACTORY)
    public void respondToRemoteInputSendEvents(RemoteEventeMessageResponseHolder remoteEventeMessages) {
        if(isResponseFromDifferentServer(remoteEventeMessages)) {
            RemoteProvenanceEventService.getInstance().loadRemoteEventFlowData(remoteEventeMessages);
        }
    }

    private boolean isResponseFromDifferentServer(RemoteEventeMessageResponseHolder response){
        return !uniqueIdentifier.equalsIgnoreCase(response.getServerId());
    }

}
