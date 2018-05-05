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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lock on a given key
 */
public class ProvenanceEventJobExecutionLockManager {

    static Map<String, JobLock> locks = new ConcurrentHashMap<>();

    public static JobLock getLock(String key) {
        JobLock lock = locks.putIfAbsent(key, new JobLock(key));
        if (lock == null) {
            lock = locks.get(key);
        }
        return lock;

    }

    public static void releaseLock(String key) {
        JobLock jobLock = locks.get(key);
        releaseLock(jobLock);
    }

    public static void releaseLock(JobLock jobLock) {
        if (jobLock != null) {
            ReentrantLock lock = jobLock.getLock();
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                locks.remove(jobLock.getKey());
            }
        }
    }


    public static class JobLock {

        ReentrantLock lock;
        String key;

        public JobLock(String key) {
            this.lock = new ReentrantLock();
            this.key = key;
        }

        public ReentrantLock getLock() {
            return lock;
        }

        public String getKey() {
            return key;
        }
    }
}
