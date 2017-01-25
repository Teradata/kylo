package com.thinkbiganalytics.nifi.rest.model;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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

/**
 * Status of a NiFi node.
 */
public class NiFiClusterSummary {

    private Integer connectedNodeCount;
    private String connectedNodes;
    private Boolean isClustered;
    private Boolean isConnectedToCluster;
    private Integer totalNodeCount;

    public Integer getConnectedNodeCount() {
        return connectedNodeCount;
    }

    public void setConnectedNodeCount(Integer connectedNodeCount) {
        this.connectedNodeCount = connectedNodeCount;
    }

    public String getConnectedNodes() {
        return connectedNodes;
    }

    public void setConnectedNodes(String connectedNodes) {
        this.connectedNodes = connectedNodes;
    }

    public Boolean getClustered() {
        return isClustered;
    }

    public void setClustered(Boolean clustered) {
        isClustered = clustered;
    }

    public Boolean getConnectedToCluster() {
        return isConnectedToCluster;
    }

    public void setConnectedToCluster(Boolean connectedToCluster) {
        isConnectedToCluster = connectedToCluster;
    }

    public Integer getTotalNodeCount() {
        return totalNodeCount;
    }

    public void setTotalNodeCount(Integer totalNodeCount) {
        this.totalNodeCount = totalNodeCount;
    }
}
