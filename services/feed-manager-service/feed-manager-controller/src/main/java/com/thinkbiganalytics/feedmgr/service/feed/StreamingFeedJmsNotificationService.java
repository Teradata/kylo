package com.thinkbiganalytics.feedmgr.service.feed;
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

import com.thinkbiganalytics.feedmgr.nifi.NifiConnectionListener;
import com.thinkbiganalytics.feedmgr.nifi.NifiConnectionService;
import com.thinkbiganalytics.feedmgr.nifi.cache.NiFiFlowCacheListener;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.jms.JmsConstants;
import com.thinkbiganalytics.jms.JmsService;
import com.thinkbiganalytics.jms.Topics;
import com.thinkbiganalytics.metadata.api.cache.CacheBackedProviderListener;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.jpa.feed.OpsManagerFeedCacheByName;
import com.thinkbiganalytics.nifi.provenance.model.StreamingFeedListData;
import com.thinkbiganalytics.nifi.provenance.model.StreamingFeedListHolder;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.Topic;

/**
 * JMS service the send and receive Streaming Feed Metadata information to JMS that will be captured by NiFi to assist in provenance processing.
 */
public class StreamingFeedJmsNotificationService {

    private static final Logger log = LoggerFactory.getLogger(StreamingFeedJmsNotificationService.class);

    @Inject
    private OpsManagerFeedCacheByName opsManagerFeedCacheByName;

    @Inject
    private OpsManagerFeedProvider opsManagerFeedProvider;

    @Inject
    private NifiFlowCache nifiFlowCache;

    @Autowired
    @Qualifier("jmsTemplate")
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Inject
    private JmsService jmsService;

    /**
     * The topic to send/receive messages about streaming feed metadata
     */
    private Topic nifiStreamingFeedsTopic;

    @Inject
    private NifiConnectionService nifiConnectionService;

    /**
     * Flag to indicate if we have sent NiFi the list of streaming feeds
     */
    private AtomicBoolean initializing = new AtomicBoolean(false);

    private enum Status {
        WAITING, INITIALIZING, INITIALIZED
    }

    private Status status = Status.WAITING;

    /**
     * Listener to act when NiFi is up, and Kylo's metadata is available.
     * This will send NiFi the list of streaming feeds via JMS
     */
    private FeedBatchStreamStatusNiFiFlowCacheListener niFiFlowCacheListener = new FeedBatchStreamStatusNiFiFlowCacheListener();

    @PostConstruct
    private void init() {
        nifiFlowCache.subscribe(niFiFlowCacheListener);
        nifiConnectionService.subscribeConnectionListener(niFiFlowCacheListener);
        opsManagerFeedProvider.subscribeListener(niFiFlowCacheListener);
        nifiStreamingFeedsTopic = jmsService.getTopic(Topics.NIFI_STREAMING_FEEDS_TOPIC);
    }

    /**
     * Notify NiFi when a streaming feed is updated.
     * This will notify NiFi of the new Streaming feed input processor id data
     *
     * @param nifiProcessGroup the new feed process group
     * @param feed             the feed metadata
     */
    public void updateNiFiStatusJMSTopic(NifiProcessGroup nifiProcessGroup, FeedMetadata feed) {
        //post this to JMS
        StreamingFeedListHolder streamingFeedList = new StreamingFeedListHolder();
        streamingFeedList.add(toFeedBatchStreamType(nifiProcessGroup, feed));
        jmsMessagingTemplate.convertAndSend(nifiStreamingFeedsTopic, streamingFeedList);
    }


    /**
     * Listen for messages on the Streaming feed topic.
     * Kylo only cares about message coming in from NiFi that are of type of "REQUEST".  This indicates NiFi wants to know about Kylo's Streaming feeds
     *
     * @param streamingFeedList the payload of the message
     */
    @JmsListener(destination = Topics.NIFI_STREAMING_FEEDS_TOPIC, containerFactory = JmsConstants.TOPIC_LISTENER_CONTAINER_FACTORY)
    public void receiveMessage(StreamingFeedListHolder streamingFeedList) {
        if (streamingFeedList.isRequest()) {
            notifyNifiOfStreamingFeeds();
        }
    }

    /**
     * Tell NiFi of changes in Feed from a batch to a stream or stream to a batch).
     * This will send a message to the JMS topic notifying NiFi of the changes in the Feeds
     *
     * @param feedNames the feed names updated
     * @param isStream  true if stream, false if not
     * @return the payload sent to JMS
     */
    public StreamingFeedListHolder notifyNiFiOfFeedBatchStreamChange(Set<String> feedNames, boolean isStream) {
        StreamingFeedListHolder streamingFeedList = new StreamingFeedListHolder();
        streamingFeedList.setMode(isStream ? StreamingFeedListHolder.MODE.ADD : StreamingFeedListHolder.MODE.REMOVE);
        feedNames.stream().forEach(feed -> {
            List<String> inputProcessorIds = nifiFlowCache.getLatest().getFeedToInputProcessorIds().get(feed);
            if (inputProcessorIds != null && !inputProcessorIds.isEmpty()) {
                String feedProcessGroupId = nifiFlowCache.getLatest().getProcessorIdToFeedProcessGroupId().get(inputProcessorIds.get(0));
                StreamingFeedListData feedBatchStreamType = toFeedBatchStreamType(inputProcessorIds, feedProcessGroupId, feed, isStream);
                streamingFeedList.add(feedBatchStreamType);
            }
        });
        log.info("Notify JMS topic: {} of streaming feed changes {} ", Topics.NIFI_STREAMING_FEEDS_TOPIC, streamingFeedList);
        jmsMessagingTemplate.convertAndSend(nifiStreamingFeedsTopic, streamingFeedList);
        return streamingFeedList;
    }


    private StreamingFeedListHolder notifyNifiOfStreamingFeeds() {
        StreamingFeedListHolder streamingFeedList = new StreamingFeedListHolder();
        streamingFeedList.setEntireList(true);
        opsManagerFeedProvider.findAllWithoutAcl().stream().filter(feed -> feed.isStream()).forEach(feed -> {
            List<String> inputProcessorIds = nifiFlowCache.getLatest().getFeedToInputProcessorIds().get(feed.getName());
            if (inputProcessorIds != null && !inputProcessorIds.isEmpty()) {
                String feedProcessGroupId = nifiFlowCache.getLatest().getProcessorIdToFeedProcessGroupId().get(inputProcessorIds.get(0));
                StreamingFeedListData feedBatchStreamType = toFeedBatchStreamType(inputProcessorIds, feedProcessGroupId, feed);
                streamingFeedList.add(feedBatchStreamType);
            }
        });
        log.info("Notify JMS topic: {} of streaming feeds {} ", Topics.NIFI_STREAMING_FEEDS_TOPIC, streamingFeedList);
        jmsMessagingTemplate.convertAndSend(nifiStreamingFeedsTopic, streamingFeedList);
        return streamingFeedList;
    }

    /**
     * @param nifiProcessGroup the process group that holds the feed flow (i.e. the category process group)
     * @param feedMetadata     the metadata for the feed
     * @return the status type object that will be sent to the JMS queue for processing
     */
    private StreamingFeedListData toFeedBatchStreamType(NifiProcessGroup nifiProcessGroup, FeedMetadata feedMetadata) {
        StreamingFeedListData feedBatchStreamType = new StreamingFeedListData();
        feedBatchStreamType.setFeedName(feedMetadata.getFeedName());
        feedBatchStreamType.addInputProcessorId(nifiProcessGroup.getInputProcessor().getId());
        return feedBatchStreamType;
    }

    private StreamingFeedListData toFeedBatchStreamType(List<String> inputProcessorIds, String feedProcessGroupId, OpsManagerFeed feed) {
        StreamingFeedListData feedBatchStreamType = new StreamingFeedListData();
        feedBatchStreamType.setFeedName(feed.getName());
        feedBatchStreamType.setInputProcessorIds(inputProcessorIds);
        return feedBatchStreamType;
    }

    private StreamingFeedListData toFeedBatchStreamType(List<String> inputProcessorIds, String feedProcessGroupId, String feed, boolean isStream) {
        StreamingFeedListData feedBatchStreamType = new StreamingFeedListData();
        feedBatchStreamType.setFeedName(feed);
        feedBatchStreamType.setInputProcessorIds(inputProcessorIds);
        return feedBatchStreamType;
    }


    private class FeedBatchStreamStatusNiFiFlowCacheListener implements NiFiFlowCacheListener, NifiConnectionListener, CacheBackedProviderListener {

        @Override
        public void onCacheAvailable() {
            notifyNiFi();

        }

        @Override
        public void onCacheUnavailable() {

        }

        @Override
        public void onAddedItem(Object key, Object value) {
            //no op
        }

        @Override
        public void onRemovedItem(Object value) {
            //no op
        }

        @Override
        public void onRemoveAll() {
            //no op
        }

        @Override
        public void onNiFiConnected() {
            notifyNiFi();
        }

        @Override
        public void onNiFiDisconnected() {

        }

        @Override
        public void onPopulated() {
            notifyNiFi();
        }


        private void notifyNiFi() {
            if ((status == Status.WAITING || status == Status.INITIALIZED) && nifiFlowCache.isAvailable() && opsManagerFeedCacheByName.isPopulated() && initializing.compareAndSet(false, true)) {
                status = Status.INITIALIZING;
                notifyNifiOfStreamingFeeds();
                status = Status.INITIALIZED;
            }
        }
    }

}
