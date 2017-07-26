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
public class AggregatedFeedProcessorStatistics implements Serializable {

    String startingProcessorId;
    String processGroup;
    Map<String, AggregatedProcessorStatistics> processorStats = new ConcurrentHashMap<>();
    private String collectionId;
    private Long totalEvents = 0L;
    private Long minEventId = 0L;
    private Long maxEventId = 0L;
    private Long collectionIntervalMillis;

    public AggregatedFeedProcessorStatistics() {
    }

    public AggregatedFeedProcessorStatistics(String startingProcessorId, String collectionId, Long collectionIntervalMillis) {
        this.startingProcessorId = startingProcessorId;
        this.collectionId = collectionId;
        this.collectionIntervalMillis = collectionIntervalMillis;
    }

    /**
     * Add the event to compute statistics
     */
    public void addEventStats(ProvenanceEventRecordDTO event) {
       // processorStats.computeIfAbsent(event.getComponentId(), processorId -> new AggregatedProcessorStatistics(processorId, event.getComponentName(), collectionId)).add(event);
        totalEvents++;
        if (event.getEventId() < minEventId) {
            minEventId = event.getEventId();
        }
        if (event.getEventId() > maxEventId) {
            maxEventId = event.getEventId();
        }
        if (StringUtils.isBlank(processGroup) && StringUtils.isNotBlank(event.getFeedProcessGroupId())) {
            processGroup = event.getFeedProcessGroupId();
        }
    }

    public String getStartingProcessorId() {
        return startingProcessorId;
    }

    public void setStartingProcessorId(String startingProcessorId) {
        this.startingProcessorId = startingProcessorId;
    }

    public String getProcessGroup() {
        return processGroup;
    }

    public Long getMaxEventId() {
        return maxEventId;
    }

    public Map<String, AggregatedProcessorStatistics> getProcessorStats() {
        return processorStats;
    }

    public boolean hasStats(){
        return processorStats.values().stream().anyMatch(s -> s.hasStats());
    }

    public void clear(String newCollectionId) {
        this.collectionId = newCollectionId;
        processorStats.entrySet().forEach(e -> e.getValue().clear());
    }

    public String getCollectionId() {
        return collectionId;
    }

    public Long getCollectionIntervalMillis() {
        return collectionIntervalMillis;
    }

    public void setCollectionIntervalMillis(Long collectionIntervalMillis) {
        this.collectionIntervalMillis = collectionIntervalMillis;
    }
}
