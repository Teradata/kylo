package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;

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
