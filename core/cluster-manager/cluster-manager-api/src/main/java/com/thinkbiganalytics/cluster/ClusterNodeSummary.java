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
import java.util.Map;

/**
 * Created by sr186054 on 10/30/17.
 */
public interface ClusterNodeSummary {

    Long getMessagesSent();

    Long getMessagesReceived();

    Long getLastReceivedMessageTimestamp();

    void messageSent(String type);

    void messageReceived(String type);

    Long getMessagesSentForType(String type);

    Long getMessagesReceivedForType(String type);

    String getNodeAddress();

    Long getLastSentMessageTimestamp();

    Map<String,Long> getMessagesReceivedByType();

    Map<String,Long> getMessagesSentByType();

    Map<String,Object> getChannelStats();
}
