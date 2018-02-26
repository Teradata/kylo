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

import java.io.IOException;

/**
 * Provides operations on the savepoint such as the ability to register, release, and retry savepoints
 */
public interface SavepointProvider {

    /**
     * Resolve the savepoint identifier given the UUID of the flowfile held by a SetSavepoint processor
     *
     * @param uuid the flowfile UUID
     * @return the savepoint identifier or null if not found
     */
    String resolveByFlowFileUUID(String uuid);

    /**
     * Confirms the retry operation has occurred for the savepoint
     *
     * @param savepointId the savepoint id
     * @param processorId the processor id
     * @param lock        the lock
     */
    void commitRetry(String savepointId, String processorId, Lock lock) throws InvalidLockException, InvalidSetpointException;

    /**
     * Confirms a release operation has been performed on the savepoint
     *
     * @param savepointId the savepoint id
     * @param processorId the processor id
     * @param lock        the lock
     */
    void commitRelease(String savepointId, String processorId, Lock lock) throws InvalidLockException, InvalidSetpointException;

    /**
     * Registers a flowfile for a savepoint
     *
     * @param savepointId the savepoint id
     * @param processorId the processor id
     * @param flowFileId  the flowfile id
     * @param lock        the lock
     */
    void register(String savepointId, String processorId, String flowFileId, Lock lock) throws InvalidLockException;

    /**
     * Releases any flowfiles held by a savepoint
     *
     * @param savepointId the savepoint id
     * @param lock        the lock
     */
    void release(String savepointId, Lock lock, boolean success) throws InvalidLockException, InvalidSetpointException;

    /**
     * Instructs a savepoint to retry the flowfile
     *
     * @param savepointId the savepoint id
     * @param lock        the lock
     */
    void retry(String savepointId, Lock lock) throws InvalidLockException, InvalidSetpointException;

    /**
     * Lookup state of a savepoint
     *
     * @param savepointId the savepoint id
     */
    SavepointEntry lookupEntry(String savepointId);

    /**
     * Acquires a lock for the savepoint. A lock is required in order to alter savepoint state.
     *
     * @param savepointId the savepoint id
     * @return a lock object or null if no lock can be obtained
     */
    Lock lock(String savepointId) throws IOException;

    /**
     * Releases the lock
     *
     * @param lock the lock object
     */
    void unlock(Lock lock) throws IOException;


    /**
     * Subscribe to changes in the Distributed Cache
     *
     * @param listener the listener that will get called when items are changed/removed
     */
    void subscribeDistributedSavepointChanges(DistributedCacheListener listener);
}
