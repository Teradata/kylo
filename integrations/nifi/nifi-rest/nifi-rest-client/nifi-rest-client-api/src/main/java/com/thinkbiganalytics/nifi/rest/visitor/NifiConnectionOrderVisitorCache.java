package com.thinkbiganalytics.nifi.rest.visitor;

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
