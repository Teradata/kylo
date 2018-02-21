package com.thinkbiganalytics.nifi.provenance.model;

/*-
 * #%L
 * thinkbig-nifi-provenance-model
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
import java.util.ArrayList;
import java.util.List;

/**
 * This is transferred to NiFi via JMS on the Topic that notifies NiFi of streaming feed(s) in Kylo to assist with Provenance
 */
public class StreamingFeedListHolder implements Serializable {

    private List<StreamingFeedListData> streamingFeedListData = new ArrayList<>();

    /**
     * create a new REQUEST for a list of the straming feeds.
     * NiFi will call this upon start if it is not initialized with the list of streaming feeds
     */
    public static StreamingFeedListHolder requestList = newRequest();

    /**
     * Flag to indicate the list of feedbatchstream types is the entire list in Kylo
     */
    private boolean entireList = false;

    /**
     * Request =  NiFi will issue a message requesting a list of the Streaming feeds in Kylo
     * Add = Kylo will issue a message with the feed(s) in Kylo that are streams
     * Remove = Kylo will issue a message of the feed(s) that have changed from stream to batch (i.e. no longer streaming feeds)
     */
    public enum MODE {
        ADD, REMOVE, REQUEST
    }

    private MODE mode = MODE.ADD;


    public StreamingFeedListHolder() {

    }

    private static StreamingFeedListHolder newRequest() {
        StreamingFeedListHolder feedBatchStreamTypeHolder = new StreamingFeedListHolder();
        feedBatchStreamTypeHolder.setMode(MODE.REQUEST);
        feedBatchStreamTypeHolder.setEntireList(true);
        return feedBatchStreamTypeHolder;
    }

    public List<StreamingFeedListData> getStreamingFeedListData() {
        return streamingFeedListData;
    }

    public void setStreamingFeedListData(List<StreamingFeedListData> streamingFeedListData) {
        this.streamingFeedListData = streamingFeedListData;
    }

    public void add(StreamingFeedListData feedBatchStreamType) {
        getStreamingFeedListData().add(feedBatchStreamType);
    }

    public boolean isEntireList() {
        return entireList;
    }

    public void setEntireList(boolean entireList) {
        this.entireList = entireList;
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    public boolean isAdd() {
        return MODE.ADD == mode;
    }

    public boolean isAddOrRemove() {
        return isAdd() || isRemove();
    }

    public boolean isRequest() {
        return MODE.REQUEST == mode;
    }

    public boolean isRemove() {
        return MODE.REMOVE == mode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StreamingFeedListHolder{");
        sb.append("streamingFeedListData size=").append(streamingFeedListData != null ? streamingFeedListData.size() : 0);
        sb.append(", entireList=").append(entireList);
        sb.append(", mode=").append(mode);
        sb.append('}');
        return sb.toString();
    }
}
