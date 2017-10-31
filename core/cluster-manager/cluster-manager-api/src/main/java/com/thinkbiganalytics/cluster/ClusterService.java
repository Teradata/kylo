package com.thinkbiganalytics.cluster;

/*-
 * #%L
 * kylo-cluster-manager-api
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

import java.io.Serializable;
import java.util.List;

/**
 * Kylo cluster service
 */
public interface ClusterService {

    /**
     * Subscribe to cluster events such as when a node connects, disconnects
     * @param listener a listener for cluster evnets
     */
    void subscribe(ClusterServiceListener listener);


    /**
     * Subscribe to cluster message events
     * This will get notified for all topics
     * @param messageReceiver a receiver of a message sent by another cluster member
     */
    void subscribe(ClusterServiceMessageReceiver messageReceiver);

    /**
     * Subscrbe a set of topics
     * @param messageReceiver  the receiver
     * @param topic the topics to subscribe to
     */
    void subscribe(ClusterServiceMessageReceiver messageReceiver,String... topic);

    /**
     * Adds this node to the cluster
     * @throws Exception
     */
    void start() throws Exception;


    void stop() throws Exception;

    /**
     *
     * @return Returns the current node address in the cluster
     */
    String getAddressAsString();

    /**
     * If the node was added to the cluster via the {@link #start()} method
     * @return true if the node is part of a kylo cluster, false if not
     */
    boolean isClustered();

    /**
     * Send a message to everyone in the cluster, including this node
     * @param type the type describing the message
     * @param message a message to send
     */
    void sendMessage(String type, Serializable message);

    /**
     * Sends a message to the specified node.
     *
     * @param other   the address of the node to receive the message
     * @param type    the type describing the message
     * @param message a message to send
     * @throws IllegalArgumentException if the other node does cannot be found
     */
    void sendMessageToOther(String other, String type, Serializable message);

    /**
     * Send a message to everyone else in the cluster, not including this node
     * @param type the type describing the message
     * @param message a message to send
     */
    void sendMessageToOthers(String type,Serializable message);

    /**
     *
     * @return Return all member addresses including this nodes address
     */
    List<String> getMembersAsString();

    boolean isAcknowledgingMessages();

    /**
     *
     * @return Return other member addresses, excluding this address
     */
    List<String> getOtherMembersAsString();

    List<MessageDeliveryStatus> getMessagesAwaitingAcknowledgement();

    List<MessageDeliveryStatus> getMessagesAwaitingAcknowledgement(Long longerThanMillis);

    ClusterNodeSummary getClusterNodeSummary();
}
