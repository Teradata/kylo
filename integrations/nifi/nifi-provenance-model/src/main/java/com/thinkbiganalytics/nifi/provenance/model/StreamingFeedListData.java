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
 * Object to hold a feeds input processor ids.
 * this is transferred to NiFi via JMS on the Topic that notifies NiFi of streaming feed(s) in Kylo to assist with Provenance
 * This object is part of the FeedBatchStreamTypeHolder payload
 */
public class StreamingFeedListData implements Serializable {

    private List<String> inputProcessorIds;
    private String feedName;


    public List<String> getInputProcessorIds() {
        if (inputProcessorIds == null) {
            inputProcessorIds = new ArrayList<>();
        }
        return inputProcessorIds;
    }

    public void setInputProcessorIds(List<String> inputProcessorIds) {
        this.inputProcessorIds = inputProcessorIds;
    }

    public void addInputProcessorId(String inputProcessorId) {
        getInputProcessorIds().add(inputProcessorId);
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

}
