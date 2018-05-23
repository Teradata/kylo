package com.thinkbiganalytics.nifi.v2.savepoint;
/*-
 * #%L
 * kylo-nifi-core-processors
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

import com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointEntry;

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.FlowFileFilter;
import org.apache.nifi.processor.ProcessSession;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SavepointCacheInitializingFilter implements FlowFileFilter, SavepointFlowFileFilter {


    private SavepointContextData contextData;
    Map<Long, SavepointEntry> savepointsToCache = new TreeMap<>();

    Map<Long, String> flowfilesToCache = new TreeMap<>();


    private SavepointFlowFileFilter defaultFilter;

    public SavepointCacheInitializingFilter(SavepointContextData contextData) {
        this.contextData = contextData;
    }

    private SavepointEntry toSavepoint(String processorId, String flowfileId) {
        SavepointEntry savepointEntry = new SavepointEntry();
        savepointEntry.register(processorId, flowfileId);
        savepointEntry.retry();
        return savepointEntry;
    }

    public FlowFileFilterResult filter(FlowFile f) {
        final String savepointIdStr = contextData.getSavepointId().evaluateAttributeExpressions(f).getValue();
        SavepointEntry entry = contextData.getProvider().lookupEntry(savepointIdStr);
        if (entry == null || entry.getState(contextData.getProcessorId()) == null) {
            flowfilesToCache.put(f.getLastQueueDate(), f.getAttribute(CoreAttributes.UUID.key()));
        } else if (SavepointFlowFileFilterUtil.isExpired(f, contextData.getExpirationDuration())) {
            savepointsToCache.put(f.getLastQueueDate(), entry);
        } else if (SavepointEntry.SavePointState.WAIT != entry.getState(contextData.getProcessorId())) {
            savepointsToCache.put(f.getLastQueueDate(), entry);
        }
        return FlowFileFilterResult.REJECT_AND_CONTINUE;
    }

    public List<FlowFile> initializeAndFilter(ProcessSession session) {
        flowfilesToCache = new TreeMap<>();
        //initialize the map to cache
        session.get(this);
        //cache it
        List<String>
            nextFlowFiles =
            contextData.getController().initializeAndGetNextFlowFiles(contextData.getProcessorId(), flowfilesToCache.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()),
                                                                      savepointsToCache.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        //Optional<String> nextFlowFile = contextData.getController().initializeAndGetNextFlowFile(contextData.getProcessorId(), flowfilesToCache.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()), savepointsToCache.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        //refetch
        defaultFilter = new DefaultSavepointFlowFileFilter(nextFlowFiles, contextData);
        return session.get(defaultFilter);
    }

    @Override
    public boolean isAccepted(FlowFile flowFile) {
        if (defaultFilter != null) {
            return defaultFilter.isAccepted(flowFile);
        }
        return false;
    }

    @Override
    public boolean isRejected(FlowFile flowFile) {
        if (defaultFilter != null) {
            return defaultFilter.isRejected(flowFile);
        }
        return true;
    }

    @Override
    public Integer getAcceptedCount() {
        if (defaultFilter != null) {
            return defaultFilter.getAcceptedCount();
        }
        return 0;
    }

    @Override
    public Integer getRejectedCount() {
        if (defaultFilter != null) {
            return defaultFilter.getRejectedCount();
        }
        return 0;
    }

    @Override
    public Set<FlowFile> getRejectedFlowFiles() {
        if (defaultFilter != null) {
            return defaultFilter.getRejectedFlowFiles();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<FlowFile> getAcceptedFlowFiles() {
        if (defaultFilter != null) {
            return defaultFilter.getAcceptedFlowFiles();
        } else {
            return Collections.emptySet();
        }
    }

    public List<String> getFlowFileIdsToMatch() {
        if (defaultFilter != null) {
            return defaultFilter.getFlowFileIdsToMatch();
        } else {
            return Collections.emptyList();
        }
    }

}