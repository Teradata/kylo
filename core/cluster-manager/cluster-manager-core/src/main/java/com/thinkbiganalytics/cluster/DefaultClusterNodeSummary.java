package com.thinkbiganalytics.cluster;
/*-
 * #%L
 * kylo-cluster-manager-core
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

import com.google.common.util.concurrent.AtomicLongMap;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 10/13/17.
 */
public class DefaultClusterNodeSummary implements ClusterNodeSummary {

    private String nodeAddress;

    AtomicLong messagesSent = new AtomicLong(0L);
    AtomicLong messagesReceived = new AtomicLong(0L);

    Long lastReceivedMessageTimestamp;
    Long lastSentMessageTimestamp;

    Map<String,Object> channelStats;

    AtomicLongMap<String> messagesReceivedByType = AtomicLongMap.create();
    AtomicLongMap<String> messagesSentByType = AtomicLongMap.create();

    Long disconnects = 0L;
    Long connects = 0L;

    public void disconnected() {
        disconnects++;
    }

    public void connected() {
        connects++;
    }


    public DefaultClusterNodeSummary(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    @Override
    public Long getMessagesSent() {
        return messagesSent.get();
    }

    @Override
    public Long getMessagesReceived() {
        return messagesReceived.get();
    }

    @Override
    public Long getLastReceivedMessageTimestamp() {
        return lastReceivedMessageTimestamp;
    }

    @Override
    public void messageSent(String type) {
        lastSentMessageTimestamp = DateTime.now().getMillis();
        messagesSent.incrementAndGet();
        messagesSentByType.getAndIncrement(type);
    }

    @Override
    public void messageReceived(String type) {
        lastReceivedMessageTimestamp = DateTime.now().getMillis();
        messagesReceived.incrementAndGet();
        messagesReceivedByType.getAndIncrement(type);
    }

    @Override
    public Long getMessagesSentForType(String type) {
        return messagesSentByType.get(type);
    }

    @Override
    public Long getMessagesReceivedForType(String type) {
        return messagesReceivedByType.get(type);
    }

    @Override
    public String getNodeAddress() {
        return nodeAddress;
    }

    @Override
    public Long getLastSentMessageTimestamp() {
        return lastSentMessageTimestamp;
    }

    public Map<String,Long> getMessagesReceivedByType() {
        return messagesReceivedByType.asMap();
    }

    public Map<String,Long> getMessagesSentByType() {
        return messagesSentByType.asMap();
    }

    @Override
    public Map<String, Object> getChannelStats() {
        return channelStats;
    }

    public void setChannelStats(Map<String, Object> channelStats) {
        this.channelStats = channelStats;
    }
}
