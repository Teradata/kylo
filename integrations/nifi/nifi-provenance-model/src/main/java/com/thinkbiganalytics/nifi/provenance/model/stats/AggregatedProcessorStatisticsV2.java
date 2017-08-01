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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Group Statistics by Processor
 */
public class AggregatedProcessorStatisticsV2 extends  AggregatedProcessorStatistics implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(AggregatedProcessorStatisticsV2.class);


    public AggregatedProcessorStatisticsV2(String processorId, String processorName, String collectionId) {
     super(processorId,processorName,collectionId);
    }
    public GroupedStats getStats(String sourceConnectionIdentifier){
      return  this.getStats().computeIfAbsent(sourceConnectionIdentifier, id -> new GroupedStatsV2(sourceConnectionIdentifier));
    }

}
