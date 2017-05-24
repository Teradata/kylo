package com.thinkbiganalytics.feedmgr.nifi.cache;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.cluster.NiFiFlowCacheUpdateType;

import org.jgroups.Message;

/**
 * Object to hold the messages needed to sync other kylo clusters with the nifi flow cache updates
 */
public class NifiFlowCacheClusterUpdateMessage {


    private String updateKey;
    private NiFiFlowCacheUpdateType type;

    private String message;

    public NifiFlowCacheClusterUpdateMessage() {

    }

    public NifiFlowCacheClusterUpdateMessage(NiFiFlowCacheUpdateType type, String message){
      this(type,message,null);
    }

     public NifiFlowCacheClusterUpdateMessage(NiFiFlowCacheUpdateType type, String message, String updateKey) {
        this.type = type;
        this.message = message;
        this.updateKey = updateKey;
    }

    public NiFiFlowCacheUpdateType getType() {
        return type;
    }

    public void setType(NiFiFlowCacheUpdateType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUpdateKey() {
        return updateKey;
    }

    public void setUpdateKey(String updateKey) {
        this.updateKey = updateKey;
    }
}
