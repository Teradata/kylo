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


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelListener;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
public class JGroupsClusterService extends ReceiverAdapter implements ClusterService {

    private static final Logger log = LoggerFactory.getLogger(JGroupsClusterService.class);

    List<Address> members;

    JChannel channel;

    @Value("${kylo.cluster.jgroupsConfigFile:#{null}}")
    private String jgroupsConfigFile;

    @Value("${kylo.cluster.acknowledge:false}")
    private boolean sendAcknowledgementMessage = false;

    @Value("${kylo.cluster.pending.acknowledge.expire.minutes:30}")
    private Integer pendingMessageAcknowledgeTime = 30;

    private static final String CLUSTER_NAME = "internal-kylo-cluster";

    private static final String ALL_TOPIC = "!!!ALL!!!";


    private List<ClusterServiceListener> listeners = new ArrayList<>();

    //private List<ClusterServiceMessageReceiver> messageReceivers = new ArrayList<>();

    private Map<String, List<ClusterServiceMessageReceiver>> messageReceivers = new ConcurrentHashMap<>();

    public void subscribe(ClusterServiceListener listener) {
        listeners.add(listener);
    }

    public void subscribe(ClusterServiceMessageReceiver messageReceiver) {
        messageReceivers.computeIfAbsent(ALL_TOPIC, t -> new ArrayList<>()).add(messageReceiver);
    }

    public void subscribe(ClusterServiceMessageReceiver messageReceiver, String... topics) {
        Arrays.stream(topics).forEach(topic -> messageReceivers.computeIfAbsent(topic, t -> new ArrayList<>()).add(messageReceiver));
    }

    private String ENSURE_MESSAGE_DELIVERY_TYPE = "ENSURE_MESSAGE_DELIVERY";

    Cache<String, MessageDeliveryStatus> ensureMessageDeliveryMap = CacheBuilder.newBuilder().expireAfterWrite(pendingMessageAcknowledgeTime, TimeUnit.MINUTES).build();

    //  private Map<String, MessageDeliveryStatus> ensureMessageDeliveryMap = new ConcurrentHashMap<>();

    private DefaultClusterNodeSummary clusterNodeSummary;


    @Override
    public boolean isAcknowledgingMessages() {
        return sendAcknowledgementMessage;
    }

    /**
     * Start a channel on the cluster
     */
    @Override
    public void start() throws Exception {
        if (channel != null) {
            log.info("Kylo cluster has already been started");
        } else if (StringUtils.isNotBlank(jgroupsConfigFile)) {
            try {
                channel = new JChannel(jgroupsConfigFile);
                String name = Util.generateLocalName();
                channel.setName("Kylo - " + name);
                channel.setReceiver(this);
                channel.addChannelListener(new Listener());
                channel.connect(CLUSTER_NAME);
                channel.enableStats(true);
                clusterNodeSummary = new DefaultClusterNodeSummary(channel.getAddressAsString());
                clusterNodeSummary.connected();
            } catch (FileNotFoundException e) {
                log.error("Unable to find the jgroups cluster configuration file {}.  Kylo is not clustered ", jgroupsConfigFile);
            }
        }
    }

    public void stop() throws Exception {
        if (channel != null) {
            log.info("Stopping {} ", getAddressAsString());
            channel.disconnect();
            clusterNodeSummary.disconnected();
        }

    }

    @Override
    public boolean isClustered() {
        return this.channel != null;
    }

    @Override
    public String getAddressAsString() {
        if (isClustered()) {
            return channel.getAddressAsString();
        } else {
            return "localhost";
        }
    }

    public Address getAddress() {
        if (isClustered()) {
            return channel.getAddress();
        }
        return null;
    }

    /**
     * Update the members reference and determine what node should be primary if needed
     *
     * @param view the view that just joined the cluster
     */
    public void updateMembers(View view) {
        //snapshot prev members
        final List<Address> previousMembers = members != null ? members : new ArrayList<>();
        members = view.getMembers();
        log.info("Cluster membership changed: There are now {} members in the cluster. {} ", members.size(), members);
        List<String> previous = previousMembers.stream().map(m -> m.toString()).collect(Collectors.toList());
        listeners.stream().forEach(listener -> listener.onClusterMembershipChanged(previous, getMembersAsString()));
    }


    /**
     * Called when a group joins the cluster
     */
    public void viewAccepted(View view) {
        updateMembers(view);
    }

    /**
     * Called when a node (including this node) receives a message
     *
     * @param msg a message
     */
    public void receive(Message msg) {

        ClusterMessage clusterMessage = (ClusterMessage) msg.getObject();
        String from = msg.getSrc().toString();
        Set<ClusterServiceMessageReceiver>
            receivers =
            messageReceivers.entrySet().stream().filter(e -> ALL_TOPIC.equalsIgnoreCase(e.getKey()) || e.getKey().equalsIgnoreCase(clusterMessage.getType())).flatMap(e -> e.getValue().stream())
                .collect(Collectors.toSet());
        log.info("Receiving message from {} of type: {}, notifying {} receivers ", msg.getSrc(), clusterMessage.getType(), receivers.size());
        receivers.stream().forEach(messageReceiver -> {
            try {
                messageReceiver.onMessageReceived(from, clusterMessage);
            } catch (Exception e) {
                log.error("Error procesing onMessageReceived from {} topic: {}, message: {} ", from, clusterMessage.getType(), clusterMessage.getMessage(), e);
            }
        });
        clusterNodeSummary.messageReceived(clusterMessage.getType());
        acknowledgeMessage(from, clusterMessage);
    }

    private void acknowledgeMessage(String from, ClusterMessage clusterMessage) {
        //Acknowledge receiving the message
        if (ENSURE_MESSAGE_DELIVERY_TYPE.equalsIgnoreCase(clusterMessage.getType())) {
            EnsureMessageDeliveryMessage ensureMessageDeliveryMessage = (EnsureMessageDeliveryMessage) clusterMessage.getMessage();
            MessageDeliveryStatus status = ensureMessageDeliveryMap.getIfPresent(ensureMessageDeliveryMessage.getMessageId());
            if (status != null) {
                status.receivedFrom(from);
                if (status.isComplete()) {
                    ensureMessageDeliveryMap.invalidate(ensureMessageDeliveryMessage.getMessageId());
                    log.debug("Successfully acknowledged message deliver of {}, type: {}", clusterMessage.getId(), clusterMessage.getType());
                }
            }
        } else {
            sendAcknowledgementMessage(from, clusterMessage);
        }
    }


    protected class Listener implements ChannelListener {

        protected Listener() {
        }

        public void channelClosed(Channel channel) {
            log.info("*** Channel closed {},{}", channel.getName(), channel.getView().getMembers());
            listeners.stream().forEach(listener -> listener.onClosed(membersAsString(channel.getView().getMembers())));
        }

        public void channelConnected(Channel channel) {
            log.info("*** Channel connected {},{}", channel.getName(), channel.getView().getMembers());
            listeners.stream().forEach(listener -> listener.onConnected(membersAsString(channel.getView().getMembers())));
        }

        public void channelDisconnected(Channel channel) {
            listeners.stream().forEach(listener -> listener.onDisconnected(membersAsString(channel.getView().getMembers())));
        }
    }

    public void clusterEnabled() {
        if (channel == null) {
            throw new UnsupportedOperationException(" This is not a clustered Kylo");
        }
    }

    /**
     * All messages are converted to a ClusterMessage
     *
     * @param message a message to send
     */
    @Override
    public void sendMessage(String type, Serializable message) {
        if (isClustered()) {
            try {
                String id = newMessageId();
                log.info("Sending message with id: {}, of type:{} to ALL from {}", id, type, channel.getAddressAsString());
                ClusterMessage clusterMessage = new StandardClusterMessage(id, type, message);
                MessageDeliveryStatus status = new DefaultMessageDeliveryStatus(clusterMessage, new HashSet<String>(getMembersAsString()));
                if (sendAcknowledgementMessage) {
                    //store this message id in the acknowledgement map
                    ensureMessageDeliveryMap.put(clusterMessage.getId(), status);
                }
                channel.send(null, clusterMessage);
            } catch (Exception e) {
                log.error("Unable to send message of type: {} to other nodes: {} ", type, e.getMessage(), e);
            }
        }
    }

    @Override
    public void sendMessageToOther(final String other, final String type, final Serializable message) {
        if (isClustered()) {
            try {
                final Optional<Address> address = getOtherMembers().stream()
                    .filter(member -> other.equalsIgnoreCase(member.toString()))
                    .findFirst();
                if (address.isPresent()) {
                    String id = newMessageId();
                    log.info("Sending message with id: {}, of type:{} to {} from {}", id, type, address, channel.getAddressAsString());
                    ClusterMessage clusterMessage = new StandardClusterMessage(id, type, message);
                    MessageDeliveryStatus status = new DefaultMessageDeliveryStatus(clusterMessage);
                    if (sendAcknowledgementMessage) {
                        ensureMessageDeliveryMap.put(clusterMessage.getId(), status);
                    }
                    channel.send(address.get(), clusterMessage);
                    clusterNodeSummary.messageSent(type);
                    status.sentTo(address.toString());
                } else {
                    throw new IllegalArgumentException("Cluster node does not exist: " + other);
                }
            } catch (final Exception e) {
                log.error("Unable to send message of type: {} to other node:{}, {} ", type, other, e.getMessage(), e);
            }
        }
    }

    private String newMessageId() {
        return UUID.randomUUID().toString();
    }


    @Override
    public void sendMessageToOthers(String type, Serializable message) {
        if (isClustered()) {
            try {
                clusterNodeSummary.messageSent(type);
                String id = newMessageId();
                ClusterMessage clusterMessage = new StandardClusterMessage(id, type, message);
                MessageDeliveryStatus status = new DefaultMessageDeliveryStatus(clusterMessage);
                if (sendAcknowledgementMessage) {
                    ensureMessageDeliveryMap.put(clusterMessage.getId(), status);
                }
                for (Address address : getOtherMembers()) {
                    log.info("Sending message with id:{} of type:{} to {} from {} ", id, type, address, this.channel.getAddressAsString());
                    channel.send(address, clusterMessage);
                    status.sentTo(address.toString());
                }

            } catch (Exception e) {
                log.error("Unable to send message of type: {} to other nodes: {} ", type, e.getMessage(), e);
            }
        }
    }


    private void sendAcknowledgementMessage(String from, ClusterMessage message) {
        if (isClustered() && sendAcknowledgementMessage) {
            try {
                final Optional<Address> address = getOtherMembers().stream()
                    .filter(member -> from.equalsIgnoreCase(member.toString()))
                    .findFirst();
                if (address.isPresent()) {
                    //send it
                    EnsureMessageDeliveryMessage ensureMessageDeliveryMessage = new EnsureMessageDeliveryMessage(message.getId(), EnsureMessageDeliveryMessage.MESSAGE_ACTION.RECEIVED);
                    String id = newMessageId();
                    ClusterMessage clusterMessage = new StandardClusterMessage(id, ENSURE_MESSAGE_DELIVERY_TYPE, ensureMessageDeliveryMessage);
                    channel.send(address.get(), clusterMessage);
                } else {
                    throw new IllegalArgumentException("Cluster node does not exist: " + from);
                }
            } catch (final Exception e) {
                log.error("Unable to send acknowledgement of message: {} to {} ", message.getType(), from, e.getMessage(), e);
            }
        }

    }


    public void redeliverMessage(final String other, ClusterMessage clusterMessage) {
        if (isClustered()) {
            try {
                final Optional<Address> address = getOtherMembers().stream()
                    .filter(member -> other.equalsIgnoreCase(member.toString()))
                    .findFirst();
                if (address.isPresent()) {
                    log.info("Redeliver message with id: {}, of type:{} to {} from {}", clusterMessage.getId(), clusterMessage.getType(), address, channel.getAddressAsString());
                    MessageDeliveryStatus status = ensureMessageDeliveryMap.getIfPresent(clusterMessage.getId());
                    channel.send(address.get(), clusterMessage);
                    clusterNodeSummary.messageSent(clusterMessage.getType());
                    if (status != null) {
                        status.redeliveredTo(address.toString());
                    }
                } else {
                    throw new IllegalArgumentException("Cluster node does not exist: " + other);
                }
            } catch (final Exception e) {
                log.error("Unable to redeliver message of type: {} to: {}, ", clusterMessage.getType(), other, e.getMessage(), e);
            }
        }
    }


    public List<Address> getMembers() {
        return members != null ? members : Collections.emptyList();
    }

    public List<Address> getOtherMembers() {
        return getMembers().stream().filter(a -> !a.toString().equalsIgnoreCase(this.channel.getAddressAsString())).collect(Collectors.toList());
    }

    @Override
    public List<String> getMembersAsString() {
        return membersAsString(members);
    }

    private List<String> membersAsString(List<Address> members) {
        return members != null ? members.stream().map(a -> a.toString()).collect(Collectors.toList()) : Collections.emptyList();
    }

    @Override
    public List<String> getOtherMembersAsString() {
        return getMembersAsString().stream().filter(a -> !a.equalsIgnoreCase(this.channel.getAddressAsString())).collect(Collectors.toList());
    }

    public List<MessageDeliveryStatus> getMessagesAwaitingAcknowledgement() {
        return Lists.newArrayList(ensureMessageDeliveryMap.asMap().values());
    }

    /**
     * Find any Messages awaiting to be delivered longer than a certain time
     */
    public List<MessageDeliveryStatus> getMessagesAwaitingAcknowledgement(Long longerThanMillis) {
        return ensureMessageDeliveryMap.asMap().values().stream().filter(m -> m.isTimeLongerThan(longerThanMillis)).collect(Collectors.toList());
    }

    public void redeliverToAwaitingNodes(Long millis) {
        getMessagesAwaitingAcknowledgement(millis).stream().forEach(m -> {
            m.getNodesAwaitingMessage().stream().forEach(addess -> redeliverMessage(addess, m.getMessage()));
        });
    }

    public ClusterNodeSummary getClusterNodeSummary() {
        if (channel != null) {
            clusterNodeSummary.setChannelStats(channel.dumpStats());
        }
        return clusterNodeSummary;
    }
}
