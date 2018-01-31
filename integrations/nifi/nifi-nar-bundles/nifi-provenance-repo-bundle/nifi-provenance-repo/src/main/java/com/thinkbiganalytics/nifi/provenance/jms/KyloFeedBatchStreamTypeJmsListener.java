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
import com.thinkbiganalytics.nifi.provenance.model.StreamingFeedListHolder;
import com.thinkbiganalytics.nifi.provenance.repo.FeedEventStatistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class KyloFeedBatchStreamTypeJmsListener {

    private static final Logger logger = LoggerFactory.getLogger(KyloFeedBatchStreamTypeJmsListener.class);
    /**
     * Flag to indicate this NiFi node has been intialized with the default list of streaming feeds
     */
    private boolean initialized = false;

    @Inject
    SendJmsMessage sendJmsMessage;

    @Autowired
    @Qualifier("jmsTemplate")
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Inject
    private JmsService jmsService;

    public KyloFeedBatchStreamTypeJmsListener() {

    }

    private Topic nifiStreamingFeedsTopic;


    @PostConstruct
    public void postConstruct() {
        logger.info("Created KyloFeedBatchStreamTypeJmsListener ");
        nifiStreamingFeedsTopic = jmsService.getTopic(Topics.NIFI_STREAMING_FEEDS_TOPIC);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void requestStreamingFeedMetadata() {
        jmsMessagingTemplate.convertAndSend(nifiStreamingFeedsTopic, StreamingFeedListHolder.requestList);
    }

    @JmsListener(destination = Topics.NIFI_STREAMING_FEEDS_TOPIC, containerFactory = JmsConstants.TOPIC_LISTENER_CONTAINER_FACTORY)
    public void receiveMessage(StreamingFeedListHolder feedBatchStreamTypeHolder) {
        if (feedBatchStreamTypeHolder.isAddOrRemove()) {
            logger.info("Received Message for {}, {}", Topics.NIFI_STREAMING_FEEDS_TOPIC, feedBatchStreamTypeHolder);
            Map<String, List<String>> streamingFeedProcessorIds = feedBatchStreamTypeHolder.getStreamingFeedListData().stream()
                //   .filter(feedBatchStreamType -> feedBatchStreamType.isStream())
                .collect(Collectors.toMap(list -> list.getFeedName(), list -> list.getInputProcessorIds()));

            if (feedBatchStreamTypeHolder.isAdd()) {
                if (feedBatchStreamTypeHolder.isEntireList() && !initialized) {
                    logger.info("Initializing the Streaming Feeds list from the {} JMS topic ", feedBatchStreamTypeHolder);
                    FeedEventStatistics.getInstance().setStreamingFeedProcessorIds(streamingFeedProcessorIds);
                    initialized = true;
                } else {
                    FeedEventStatistics.getInstance().addStreamingFeedProcessorIds(streamingFeedProcessorIds);
                }
            } else {
                FeedEventStatistics.getInstance().removeStreamingFeedProcessGroupIds(streamingFeedProcessorIds.keySet());
            }
        }
    }

}
