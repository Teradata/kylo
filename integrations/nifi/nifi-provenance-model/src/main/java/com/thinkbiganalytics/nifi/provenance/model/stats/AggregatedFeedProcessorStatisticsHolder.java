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

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 */
public class AggregatedFeedProcessorStatisticsHolder implements Serializable {

    DateTime minTime;
    DateTime maxTime;
    String collectionId;
    AtomicLong eventCount = new AtomicLong(0L);
    /**
     * Map of Starting processorId and stats related to it
     */
    Map<String, AggregatedFeedProcessorStatistics> feedStatistics = new ConcurrentHashMap<>();
    private Long minEventId = 0L;
    private Long maxEventId = 0L;

    public AggregatedFeedProcessorStatisticsHolder() {

        this.collectionId = UUID.randomUUID().toString();
    }



    public AtomicLong getEventCount() {
        return eventCount;
    }

    public Long getMinEventId() {
        return minEventId;
    }

    public Long getMaxEventId() {
        return maxEventId;
    }

    public Map<String, AggregatedFeedProcessorStatistics> getFeedStatistics() {
        return feedStatistics;
    }

    public void setFeedStatistics(Map<String, AggregatedFeedProcessorStatistics> feedStatistics) {
        this.feedStatistics = feedStatistics;
    }

    public void setFeedStatistics(List<AggregatedFeedProcessorStatistics> stats){
        if(stats != null){
            this.feedStatistics =    stats.stream().collect(Collectors.toMap(AggregatedFeedProcessorStatistics::getStartingProcessorId, Function.identity()));
        }
    }

    public void clear() {
        this.collectionId = UUID.randomUUID().toString();
        feedStatistics.entrySet().forEach(e -> e.getValue().clear(collectionId));
    }

    public boolean hasStats(){
        return feedStatistics.values().stream().anyMatch(s -> s.hasStats());
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AggregatedFeedProcessorStatisticsHolder{");
        sb.append("minTime=").append(minTime);
        sb.append(", maxTime=").append(maxTime);
        sb.append(", collectionId='").append(collectionId).append('\'');
        sb.append(", eventCount=").append(eventCount.get());
        sb.append('}');
        return sb.toString();
    }
}
