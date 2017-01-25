package com.thinkbiganalytics.nifi.rest.visitor;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sr186054 on 12/24/16.
 */
public class NifiConnectionOrderVisitorCache {

    private Map<String, NifiConnectionOrderVisitorCachedItem> cache = new ConcurrentHashMap<>();

    private Map<String, ProcessGroupDTO> processGroupCache = new ConcurrentHashMap<>();

    public void add(ProcessGroupDTO processGroupDTO) {
        processGroupCache.computeIfAbsent(processGroupDTO.getId(), groupId -> processGroupDTO);
    }

    public Optional<ProcessGroupDTO> getProcessGroup(String processGroupId) {
        return Optional.ofNullable(processGroupCache.get(processGroupId));
    }


    public void add(NifiConnectionOrderVisitorCachedItem visitedGroup) {
        cache.computeIfAbsent(visitedGroup.getProcessGroupId(), groupId -> visitedGroup);
        if (visitedGroup.getProcessGroup().getDto() != null) {
            add(visitedGroup.getProcessGroup().getDto());
        }
    }

    public Optional<NifiConnectionOrderVisitorCachedItem> getCachedGroup(String processGroupId) {
        return Optional.ofNullable(cache.get(processGroupId));
    }

    public Optional<NifiVisitableProcessGroup> getCachedGroupForVisiting(String processGroupId) {
        if (cache.containsKey(processGroupId)) {
            NifiConnectionOrderVisitorCachedItem cachedItem = cache.get(processGroupId);
            NifiVisitableProcessGroup group = cachedItem.getProcessGroup();
            return Optional.of(group.asCacheableProcessGroup());
        }
        return Optional.empty();
    }


}
