package com.thinkbiganalytics.nifi.v2.core.savepoint;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Cache of the processor along with a Queue of the flowfiles that are available to be processed by that processor.
 * Flowfile availablity needs to match the following criteria:
 * - Flowfile state is not in a  "WAIT" state (meaning the system is waiting for some other action i.e. expire or retry)
 */
public class SavepointProcessorCache implements DistributedCacheListener<String, SavepointEntry> {

    private static final Logger log = LoggerFactory.getLogger(SavepointProcessorCache.class);

    Map<String, Deque<String>> processorFlowfileCache = new ConcurrentHashMap<>();

    private Set<String> waitingFlowfiles = new HashSet<>();

    private Set<String> initializedProcessors = new HashSet<>();


    public boolean isInitialized(String processorId) {
        return initializedProcessors.contains(processorId);
    }

    public void markInitialized(String processorId) {
        initializedProcessors.add(processorId);
    }


    public Optional<String> getNextFlowFile(String processorId) throws CacheNotInitializedException {

        if (!isInitialized(processorId)) {
            log.info("Cache is not initialized for {} ", processorId);
            throw new CacheNotInitializedException("Savepoint cache for processor: " + processorId + " has not been initialized");
        }
        Optional<String> flowfile = Optional.empty();
        Deque<String> queue = processorFlowfileCache.get(processorId);
        if (queue != null && !queue.isEmpty()) {
            log.info("Removing first from queue for processor {} ", processorId);
            flowfile = Optional.ofNullable(queue.poll());
            log.info("Removed {} for processor {} ", flowfile, processorId);
        }
        //mark the processor as being accessed
        markInitialized(processorId);

        return flowfile;
    }

    public void putFlowfile(String processorId, String flowFileId) {
        if (!processorFlowfileCache.containsKey(processorId)) {
            processorFlowfileCache.put(processorId, new LinkedBlockingDeque<>());
        }
        processorFlowfileCache.get(processorId).add(flowFileId);
    }

    public void putFlowfileBack(String processorId, String flowFileId) {
        if (!processorFlowfileCache.containsKey(processorId)) {
            processorFlowfileCache.put(processorId, new LinkedBlockingDeque<>());
        }
        processorFlowfileCache.get(processorId).addFirst(flowFileId);
    }

    public void putSavepoint(SavepointEntry entry) {
        if (entry != null && entry.getProcessorList() != null) {
            entry.getProcessorList().stream().forEach(p -> {
                String flowFileId = p.getFlowFileId();
                String processorId = p.getProcessorId();
                if (SavepointEntry.SavePointState.WAIT != entry.getState(p.getProcessorId())) {
                    //  waitingFlowfiles.remove(flowFileId);
                    log.info("Adding to cache - processor: {} , ff: {} ", processorId, flowFileId);
                    putFlowfile(processorId, flowFileId);
                } else {
                    //remove if its in a wait state
                    if (processorFlowfileCache.containsKey(processorId)) {
                        //TODO this might not be needed as the get should remove the flowfile from the queue
                        // waitingFlowfiles.add(flowFileId);
                        log.info("WAITING flow file.  Removing from cache - processor: {} , ff: {} ", processorId, flowFileId);
                        boolean removed = processorFlowfileCache.get(processorId).remove(flowFileId);

                    }
                }
            });
        }
    }


    @Override
    public void put(String savepointId, SavepointEntry entry) {
        putSavepoint(entry);
    }

    @Override
    public void removed(String savepointId) {
        //no op
    }

}
