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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.distributed.cache.client.Deserializer;
import org.apache.nifi.distributed.cache.client.DistributedMapCacheClient;
import org.apache.nifi.distributed.cache.client.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * This class provide the savepoint management functions such as register, release, and retry.
 */
public class DistributedSavepointProviderImpl implements SavepointProvider {

    private static final Logger logger = LoggerFactory.getLogger(DistributedSavepointProviderImpl.class);

    /**
     * Maximum time a thread can hold a lock before its reclaimable by another process
     */
    private static final Long MAX_LOCK_TIME = 5000L;

    /**
     * Provides a reverse lookup of a savepoint based on a flowfile ID
     */
    private final CacheWrapper<String, String> reverseCache;

    /**
     * Savepoint cache
     */
    private final CacheWrapper<String, SavepointEntry> savePoints;

    /**
     * Lock registry to ensure only one process can modify a savepoint entry at a time
     */
    private final CacheWrapper<String, Lock> locks;

    /**
     * De/serialize objects
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Serializer<String> lockKeySerializer = (value, output) -> {
        if (value != null) {
            value = "L:" + value;
            output.write(value.getBytes(StandardCharsets.UTF_8));
        }
    };

    private static final Serializer<String> cacheKeySerializer = (value, output) -> {
        if (value != null) {
            value = "C:" + value;
            output.write(value.getBytes(StandardCharsets.UTF_8));
        }
    };

    private static final Serializer<String> reverseKeySerializer = (value, output) -> {
        if (value != null) {
            value = "R:" + value;
            output.write(value.getBytes(StandardCharsets.UTF_8));
        }
    };

    private static final Serializer<Object> stringSerializer = (Object value, OutputStream output) -> {
        if (value != null) {
            output.write(value.toString().getBytes(StandardCharsets.UTF_8));
        }
    };

    private static final Serializer<Object> objectValueSerializer = (Object value, OutputStream output) -> {
        String sval = objectMapper.writeValueAsString(value);
        logger.debug("Serializing {}", sval);

        output.write(sval.getBytes());
    };

    private static final Deserializer<SavepointEntry> entryValueDeserializer = (value) -> {

        String sValue = new String(value, StandardCharsets.UTF_8);
        logger.debug("Deserializing entry {}", sValue);
        return (StringUtils.isEmpty(sValue) ? null : objectMapper.readValue(sValue, SavepointEntry.class));
    };

    private static final Deserializer<Lock> lockValueDeserializer = (value) -> {
        String sValue = new String(value, StandardCharsets.UTF_8);
        logger.debug("Deserializing lock {}", sValue);
        return (StringUtils.isEmpty(sValue) ? null : objectMapper.readValue(sValue, Lock.class));
    };

    private static final Deserializer<String> stringDeserializer = input -> input == null ? null : new String(input, StandardCharsets.UTF_8);


    public DistributedSavepointProviderImpl(DistributedMapCacheClient cacheClient) {

        this.savePoints = new CacheWrapper<>(cacheKeySerializer, objectValueSerializer, entryValueDeserializer, cacheClient);
        this.locks = new CacheWrapper<>(lockKeySerializer, objectValueSerializer, lockValueDeserializer, cacheClient);
        this.reverseCache = new CacheWrapper<>(reverseKeySerializer, stringSerializer, stringDeserializer, cacheClient);
    }

    /**
     * Listen for changes on the Distributed Cache
     *
     * @param listener the listener for the cache changes
     */
    public void subscribeDistributedSavepointChanges(DistributedCacheListener listener) {
        this.savePoints.addListener(listener);
    }

    @Override
    public String resolveByFlowFileUUID(String uuid) {
        return reverseCache.get(uuid);
    }

    @Override
    /**
     *  Confirms the retry request has been handled and returns the entry into a wait state
     */
    public void commitRetry(String savepointId, String processorId, Lock lock) throws InvalidLockException, InvalidSetpointException {
        if (!isValidLock(lock)) {
            throw new InvalidLockException();
        }
        logger.debug("Commit retry savepointId {} processorId {}", savepointId, processorId);
        SavepointEntry entry = lookupEntryWithGuarantee(savepointId);
        entry.waitState(processorId);
        savePoints.put(savepointId, entry);
    }

    /**
     * After a flowfile has been dispositioned it will be removed from the savepoint entry. Once all
     * processors have been removed, the entire savepoint entry will be removed from the savePoints.
     */
    @Override
    public void commitRelease(String savepointId, String processorId, Lock lock) throws InvalidLockException, InvalidSetpointException {
        if (!isValidLock(lock)) {
            throw new InvalidLockException();
        }
        logger.debug("Commit release savepointId {} processorId {}", savepointId, processorId);
        SavepointEntry entry = lookupEntryWithGuarantee(savepointId);
        SavepointEntry.Processor processor = entry.remove(processorId);
        reverseCache.remove(processor.getFlowFileId());

        // Removes entry from savePoints if no more work
        if (entry.isEmpty()) {
            savePoints.remove(savepointId);
        }
    }

    @Override
    /**
     * Registers a new savepoint releasing any existing any existing savepoints
     */
    public void register(String savepointId, String processorId, String flowFileId, Lock lock) throws InvalidLockException {
        if (!isValidLock(lock)) {
            throw new InvalidLockException();
        }
        logger.debug("Register savepointId {} processorId {} flowFile Id {}", savepointId, processorId, flowFileId);
        SavepointEntry entry = lookupOrCreateEntry(savepointId);

        // release any other flowfiles before registering
        entry.register(processorId, flowFileId);

        // Create a reverse lookup on the flowFile
        reverseCache.put(flowFileId, savepointId);
        savePoints.put(savepointId, entry);
    }

    @Override
    /**
     * Releases any flowfiles held by a savepoint
     */
    public void release(String savepointId, Lock lock, boolean success) throws InvalidLockException, InvalidSetpointException {
        if (!isValidLock(lock)) {
            throw new InvalidLockException();
        }
        logger.debug("Release savepointId {}", savepointId);
        SavepointEntry entry = lookupEntryWithGuarantee(savepointId);
        entry.releaseAll(success);
        savePoints.put(savepointId, entry);

    }

    @Override
    /**
     * Instructs a savepoint to retry by sending the flowfile again. The actual retry may be handled
     * by a separate process.
     */
    public void retry(String savepointId, Lock lock) throws InvalidLockException, InvalidSetpointException {
        if (!isValidLock(lock)) {
            throw new InvalidLockException();
        }
        logger.debug("Retry savepointId {}", savepointId);
        SavepointEntry entry = lookupEntryWithGuarantee(savepointId);
        entry.retry();
        savePoints.put(savepointId, entry);

    }

    private SavepointEntry lookupEntryWithGuarantee(String savepointId) throws InvalidSetpointException {
        SavepointEntry entry = lookupEntry(savepointId);
        if (entry == null) {
            throw new InvalidSetpointException(savepointId);
        }
        return entry;
    }

    @Override
    /**
     * Lookup a savepoint entry
     */
    public SavepointEntry lookupEntry(String savepointId) {
        return savePoints.get(savepointId);
    }

    private SavepointEntry lookupOrCreateEntry(String savepointId) {
        SavepointEntry entry = lookupEntry(savepointId);
        if (entry == null) {
            entry = new SavepointEntry();
        }
        return entry;
    }

    private Boolean isValidLock(Lock lock) {
        if (true) {
            return true;
        }
        if (lock == null) {
            return false;
        }
        return (!lock.isExpired(MAX_LOCK_TIME));
    }

    /**
     * Attempts to grab a lock for the savepoint. A lock allows the savepoint entry to be safely modified.
     *
     * @return Lock lock value if lock was obtained or null if no lock can be obtained
     */
    @Override
    public Lock lock(String savepointId) throws IOException {
        // Grab a lock
        Lock newLock = new Lock(savepointId);
        if (!locks.putIfAbsent(savepointId, newLock)) {
            Lock oldLock = locks.get(savepointId);
            if (oldLock != null) {
                // If lock time exceeded then release and claim lock
                if (oldLock.isExpired(MAX_LOCK_TIME)) {
                    logger.info("Lock time exceeded timout allowance for savepoint {}. Attempting to reclaim.", savepointId);
                    if (!locks.replace(savepointId, oldLock, newLock)) {
                        // Someone else claimed our lock
                        newLock = null;
                    }
                } else {
                    logger.debug("Lock busy for savepoint {}", savepointId);
                    newLock = null;
                }
            }
        }
        return newLock;
    }

    @Override
    /**
     * Release a lock.
     */
    public void unlock(Lock lock) throws IOException {
        locks.remove(lock.getSavepointId());
    }

    /**
     * Wrapper for distributed cache
     */
    static class CacheWrapper<K, V> {

        private DistributedMapCacheClient cacheClient;

        private Serializer<String> keySerializer;

        private Serializer<Object> valueSerializer;

        private Deserializer<V> valueDeserializer;

        private List<DistributedCacheListener<K, V>> distributedCacheListeners = new ArrayList<>();

        public CacheWrapper(Serializer<String> keySerializer, Serializer<Object> valueSerializer, Deserializer<V> valueDeserializer, DistributedMapCacheClient
            cacheClient) {
            this.keySerializer = keySerializer;
            this.valueSerializer = valueSerializer;
            this.valueDeserializer = valueDeserializer;
            this.cacheClient = cacheClient;
        }

        public void addListener(DistributedCacheListener listener) {
            this.distributedCacheListeners.add(listener);
        }

        public boolean remove(K key) {
            try {
                boolean removed = cacheClient.remove(key, (Serializer<K>) keySerializer);
                distributedCacheListeners.stream().forEach(l -> l.removed(key));
                return removed;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void put(K key, V value) {
            try {
                cacheClient.put(key, value, (Serializer<K>) keySerializer, valueSerializer);
                distributedCacheListeners.stream().forEach(l -> l.put(key, value));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean putIfAbsent(K key, V value) {
            try {
                boolean put = cacheClient.putIfAbsent(key, value, (Serializer<K>) keySerializer, valueSerializer);
                if (put) {
                    distributedCacheListeners.stream().forEach(l -> l.put(key, value));
                }
                return put;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean replace(K key, V oldValue, V newValue) {
            try {
                // TODO: emulate atomic replace
                cacheClient.put(key, newValue, (Serializer<K>) keySerializer, valueSerializer);
                distributedCacheListeners.stream().forEach(l -> l.put(key, newValue));
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public V get(K key) {
            try {
                return cacheClient.get(key, (Serializer<K>) keySerializer, valueDeserializer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
