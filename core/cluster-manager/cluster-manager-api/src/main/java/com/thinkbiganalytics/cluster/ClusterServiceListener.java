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

import java.util.List;

/**
 * Listen for Cluster Events
 */
public interface ClusterServiceListener {

    /**
     * Called when a member is added/removed from the cluster
     * @param previousMembers the prev members in the cluster
     * @param currentMembers the current memebrs in the cluster
     */
    void onClusterMembershipChanged(List<String> previousMembers, List<String> currentMembers);

    /**
     * Called the first time a Node connects to the cluster
     * @param currentMembers the list of current members in the cluster
     */
    void onConnected(List<String> currentMembers);

    /**
     * Called when a node is disconnected
     * @param currentMembers the list of current members in the cluster
     */
    void onDisconnected(List<String> currentMembers);

    /**
     * Called when the cluster is closed
     * @param currentMembers  the list of current members in the cluster
     */
    void onClosed(List<String> currentMembers);

}
