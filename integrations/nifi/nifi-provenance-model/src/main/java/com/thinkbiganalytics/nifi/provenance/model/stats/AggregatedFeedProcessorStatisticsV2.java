package com.thinkbiganalytics.nifi.provenance.model.stats;

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

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Group Stats by Feed and Processor
 */
public class AggregatedFeedProcessorStatisticsV2 extends AggregatedFeedProcessorStatistics implements  Serializable {

    public AggregatedFeedProcessorStatisticsV2(){
    super();
    }

    public AggregatedFeedProcessorStatisticsV2(String startingProcessorId, String collectionId, Long collectionIntervalMillis) {
        super(startingProcessorId, collectionId, collectionIntervalMillis);
    }

    public AggregatedFeedProcessorStatisticsV2(String startingProcessorId, String collectionId, Long collectionIntervalMillis, String feedName) {
        super(startingProcessorId, collectionId, collectionIntervalMillis);
        this.feedName = feedName;
    }

    private String feedName;

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }
}
