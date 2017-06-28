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

import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class CacheSummary {

        private Map<String, Integer> summary = new HashMap<>();
        private Integer cachedSyncIds;

        public CacheSummary() {

        }

        private CacheSummary(Map<String, Integer> cacheIds) {
            this.summary = cacheIds;
            this.cachedSyncIds = cacheIds.keySet().size();
        }

        public static CacheSummary build(Map<String, NiFiFlowCacheSync> syncMap) {
            Map<String, Integer>
                cacheIds =
                syncMap.entrySet().stream().collect(Collectors.toMap(stringNiFiFlowCacheSyncEntry -> stringNiFiFlowCacheSyncEntry.getKey(),
                                                                     stringNiFiFlowCacheSyncEntry1 -> stringNiFiFlowCacheSyncEntry1.getValue().getSnapshot().getProcessorIdToFeedNameMap().size()));
            return new CacheSummary(cacheIds);
        }

        public Map<String, Integer> getSummary() {
            return summary;
        }

        public void setSummary(Map<String, Integer> summary) {
            this.summary = summary;
        }

        public Integer getCachedSyncIds() {
            return cachedSyncIds;
        }

        public void setCachedSyncIds(Integer cachedSyncIds) {
            this.cachedSyncIds = cachedSyncIds;
        }
    }
