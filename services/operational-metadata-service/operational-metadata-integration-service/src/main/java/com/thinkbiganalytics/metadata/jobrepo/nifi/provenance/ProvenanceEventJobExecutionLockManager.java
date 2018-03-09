package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;
/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Lock on a given key
 */
public class ProvenanceEventJobExecutionLockManager {

    static Map<String, Lock> locks = new ConcurrentHashMap<>();

    public static Lock getLock(String key) {
        Lock lock = locks.putIfAbsent(key, new ReentrantLock());
        if (lock == null) {
            lock = locks.get(key);
        }
        return lock;

    }

    public static void releaseLock(String key) {
        Lock lock = locks.get(key);
        if (lock != null) {
            lock.unlock();
            locks.remove(key);
        }
    }

}
