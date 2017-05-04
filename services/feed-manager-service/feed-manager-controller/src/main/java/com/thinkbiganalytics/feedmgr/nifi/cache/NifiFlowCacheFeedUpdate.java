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

import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowConnection;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import java.util.Collection;

/**
 * Created by sr186054 on 4/30/17.
 */
public class NifiFlowCacheFeedUpdate {

    private String feedName;
    private boolean isStream;
    private String feedProcessGroupId;
    private Collection<NifiFlowProcessor> processors;
    private Collection<NifiFlowConnection> connections;


    public NifiFlowCacheFeedUpdate(){

    }

    public NifiFlowCacheFeedUpdate(String feedName, boolean isStream, String feedProcessGroupId, Collection<NifiFlowProcessor> processors,
                                   Collection<NifiFlowConnection> connections) {
        this.feedName = feedName;
        this.isStream = isStream;
        this.feedProcessGroupId = feedProcessGroupId;
        this.processors = processors;


        this.connections = connections;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public boolean isStream() {
        return isStream;
    }

    public void setStream(boolean stream) {
        isStream = stream;
    }

    public String getFeedProcessGroupId() {
        return feedProcessGroupId;
    }

    public void setFeedProcessGroupId(String feedProcessGroupId) {
        this.feedProcessGroupId = feedProcessGroupId;
    }

    public Collection<NifiFlowProcessor> getProcessors() {
        return processors;
    }

    public void setProcessors(Collection<NifiFlowProcessor> processors) {
        this.processors = processors;
    }

    public Collection<NifiFlowConnection> getConnections() {
        return connections;
    }

    public void setConnections(Collection<NifiFlowConnection> connections) {
        this.connections = connections;
    }
}
