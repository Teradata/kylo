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

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 */
public class AggregatedFeedProcessorStatisticsHolderV2 extends AggregatedFeedProcessorStatisticsHolder implements Serializable {


    Map<String,Long> processorIdRunningFlows = new HashMap<>();


    public AggregatedFeedProcessorStatisticsHolderV2() {
    }

    public Map<String, Long> getProcessorIdRunningFlows() {
        return processorIdRunningFlows;
    }

    public void setProcessorIdRunningFlows(Map<String, Long> processorIdRunningFlows) {
        this.processorIdRunningFlows = processorIdRunningFlows;
    }

    public String getCollectionId(){
        return super.collectionId;
    }

}
