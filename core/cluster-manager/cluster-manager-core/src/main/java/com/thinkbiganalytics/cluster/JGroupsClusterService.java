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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    private static final String CLUSTER_NAME = "internal-kylo-cluster";


    private List<ClusterServiceListener> listeners = new ArrayList<>();

    private List<ClusterServiceMessageReceiver> messageReceivers = new ArrayList<>();

    public void subscribe(ClusterServiceListener listener) {
        listeners.add(listener);
    }

    public void subscribe(ClusterServiceMessageReceiver messageReceiver) {
        messageReceivers.add(messageReceiver);
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
            } catch (FileNotFoundException e) {
                log.error("Unable to find the jgroups cluster configuration file {}.  Kylo is not clustered ", jgroupsConfigFile);
            }
        }
    }

    public void stop() throws Exception {
        if (channel != null) {
            log.info("Stopping {} ", getAddressAsString());
            channel.disconnect();
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
     * Called when a node (including this node) sends a message
     *
     * @param msg a message
     */
    public void receive(Message msg) {
        log.info("Receiving {} : {} ", msg.getSrc(), msg.getObject());
        messageReceivers.stream().forEach(messageReceiver -> {
            ClusterMessage clusterMessage = (ClusterMessage) msg.getObject();
            messageReceiver.onMessageReceived(msg.getSrc().toString(), clusterMessage);
        });

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
        clusterEnabled();
        try {
            log.info("Sending {} from {} ", message, this.channel.getAddressAsString());
            ClusterMessage msg = new StandardClusterMessage(type, message);
            channel.send(null, msg);
        } catch (Exception e) {
            e.printStackTrace();
            //throw send exception
        }
    }

    @Override
    public void sendMessageToOther(final String other, final String type, final Serializable message) {
        clusterEnabled();
        try {
            final Optional<Address> address = getOtherMembers().stream()
                .filter(member -> other.equalsIgnoreCase(member.toString()))
                .findFirst();
            if (address.isPresent()) {
                log.info("Sending message to {} from {}", address, channel.getAddressAsString());
                channel.send(address.get(), new StandardClusterMessage(type, message));
            } else {
                throw new IllegalArgumentException("Cluster node does not exist: " + other);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            //throw send exception
        }
    }

    @Override
    public void sendMessageToOthers(String type, Serializable message) {
        clusterEnabled();
        try {
            for (Address address : getOtherMembers()) {
                log.info("Sending message to {} from {} ", address, this.channel.getAddressAsString());
                ClusterMessage msg = new StandardClusterMessage(type, message);
                channel.send(address, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //throw send exception
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
}
