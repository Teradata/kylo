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

import org.apache.nifi.controller.ControllerService;

import java.util.Collection;
import java.util.Optional;

public interface SavepointController extends ControllerService {

    /**
     * Return the savepoint provider
     *
     * @return the provider
     */
    SavepointProvider getProvider();

    /**
     * For a given processor id get the next flowfile in queue that should be processed by the SetSavepoint
     *
     * @param processorId the processor id
     * @return the flowflie id which should be processed
     */
    Optional<String> getNextFlowFile(String processorId) throws CacheNotInitializedException;

    /**
     * If the cache needs initializing for a given processor, initialize it and return the next available flow file id
     *
     * @param processorId      the processor id
     * @param newFlowfiles     the collection of flowfiles to add to the cache
     * @param savepointEntries a collection of save point entries to add to the cache
     * @return the next flow file id that should be processed
     */
    Optional<String> initializeAndGetNextFlowFile(String processorId, Collection<String> newFlowfiles, Collection<SavepointEntry> savepointEntries);


    /**
     * Add a flow file for a given processor back to the cache queue
     * This is useful if the lock is unobtainable and the system needs to retry
     *
     * @param processorId the processor id
     * @param flowfileId  the flow file to re process
     */
    void putFlowfileBack(String processorId, String flowfileId);


}
