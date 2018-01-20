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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 10/12/17.
 */
public class ClusterServiceTester {

    @Inject
    private ClusterService clusterService;

    private String SIMPLE_MESSAGE_TYPE = "SIMPLE_TEST_MESSAGE";

    private SimpleMessageReceiver simpleMessageReceiver = new SimpleMessageReceiver();

    @PostConstruct
    private void init() {
        clusterService.subscribe(simpleMessageReceiver,SIMPLE_MESSAGE_TYPE);
    }

    public void sendSimpleMessage(String message) {
        clusterService.sendMessageToOthers(SIMPLE_MESSAGE_TYPE, message);
    }

    private class SimpleMessageReceiver implements ClusterServiceMessageReceiver {

        private SimpleClusterMessageTest latestMessage = null;

        @Override
        public void onMessageReceived(String from, ClusterMessage message) {

            if (SIMPLE_MESSAGE_TYPE.equalsIgnoreCase(message.getType())) {
                latestMessage = new SimpleClusterMessageTest(message.getType(), (String) message.getMessage(), from);
            }
        }

        public SimpleClusterMessageTest getLatestMessage() {
            return latestMessage;
        }
    }

    public boolean isClustered() {
        return clusterService.isClustered();
    }

    public List<String> getMembers() {
        return clusterService.getMembersAsString();
    }

    public SimpleClusterMessageTest getSimpleClusterMessage() {
        return simpleMessageReceiver.getLatestMessage();
    }


}
