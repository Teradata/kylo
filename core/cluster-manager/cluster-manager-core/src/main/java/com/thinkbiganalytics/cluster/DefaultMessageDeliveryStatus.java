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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AtomicLongMap;

import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sr186054 on 10/13/17.
 */
public class DefaultMessageDeliveryStatus implements MessageDeliveryStatus {

    private Long sentTime;

    @JsonSerialize(as = StandardClusterMessage.class)
    @JsonDeserialize(as = StandardClusterMessage.class)
    private ClusterMessage message;

    private Set<String> sentTo = new HashSet<>();
    private Set<String> receivedFrom = new HashSet<>();

    private AtomicLongMap<String> redeliveryCount = AtomicLongMap.create();

    public DefaultMessageDeliveryStatus() {

    }

    public DefaultMessageDeliveryStatus(ClusterMessage message) {
        this.message = message;
        this.sentTime = DateTime.now().getMillis();
    }

    public DefaultMessageDeliveryStatus(ClusterMessage message, Set<String> sentTo) {
        this.message = message;
        this.sentTime = DateTime.now().getMillis();
        this.sentTo = sentTo;
    }

    @Override
    public void sentTo(String to) {
        sentTo.add(to);
    }

    @Override
    public void redeliveredTo(String to) {
        redeliveryCount.getAndIncrement(to);
    }

    @Override
    public void receivedFrom(String from) {
        receivedFrom.add(from);
    }

    @Override
    public boolean isComplete() {
        return receivedFrom.size() >= sentTo.size();
    }

    @Override
    public Set<String> getNodesAwaitingMessage() {
        return Sets.difference(sentTo, receivedFrom);
    }

    @Override
    public Long getSentTime() {
        return sentTime;
    }

    @Override
    public boolean isTimeLongerThan(Long millis) {
        Long diff = DateTime.now().getMillis() - sentTime;
        return diff > millis;
    }

    @Override
    public ClusterMessage getMessage() {
        return message;
    }
}
