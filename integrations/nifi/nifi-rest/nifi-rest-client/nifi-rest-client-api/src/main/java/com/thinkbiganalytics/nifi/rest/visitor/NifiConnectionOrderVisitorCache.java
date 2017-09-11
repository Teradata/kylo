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


import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.RemoteProcessGroupDTO;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache of the visted process groups to help save time from calling out to NiFi REST client for process group/processor information
 */
public class NifiConnectionOrderVisitorCache {

    private Map<String, NifiConnectionOrderVisitorCachedItem> cache = new ConcurrentHashMap<>();

    private Map<String, ProcessGroupDTO> processGroupCache = new ConcurrentHashMap<>();

    private Map<String, RemoteProcessGroupDTO> remoteProcessGroupCache = new ConcurrentHashMap<>();

    public void add(ProcessGroupDTO processGroupDTO) {
        processGroupCache.computeIfAbsent(processGroupDTO.getId(), groupId -> processGroupDTO);
    }

    public void add(RemoteProcessGroupDTO processGroupDTO) {
        remoteProcessGroupCache.computeIfAbsent(processGroupDTO.getId(), groupId -> processGroupDTO);
    }

    public Map<String, ProcessGroupDTO> getProcessGroupCache() {
        return processGroupCache;
    }

    public Map<String, RemoteProcessGroupDTO> getRemoteProcessGroupCache() {
        return remoteProcessGroupCache;
    }

    public Optional<ProcessGroupDTO> getProcessGroup(String processGroupId) {
        return Optional.ofNullable(processGroupCache.get(processGroupId));
    }

    public Optional<RemoteProcessGroupDTO> getRemoteProcessGroup(String processGroupId) {
        return Optional.ofNullable(remoteProcessGroupCache.get(processGroupId));
    }


    /**
     * After walking a feed flow processgroup add the Nifi process group to the cache so any subsequent calls to find this group wont have to hit the NiFi REST client
     */
    public void add(NifiConnectionOrderVisitorCachedItem visitedGroup) {
        cache.computeIfAbsent(visitedGroup.getProcessGroupId(), groupId -> visitedGroup);
        if (visitedGroup.getProcessGroup().getDto() != null) {
            add(visitedGroup.getProcessGroup().getDto());
        }
    }

    public Map<String, NifiConnectionOrderVisitorCachedItem> getCache() {
        return cache;
    }
}
