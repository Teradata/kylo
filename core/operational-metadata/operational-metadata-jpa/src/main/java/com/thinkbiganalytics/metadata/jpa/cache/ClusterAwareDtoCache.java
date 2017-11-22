package com.thinkbiganalytics.metadata.jpa.cache;
/*-
 * #%L
 * kylo-operational-metadata-jpa
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

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.cluster.ClusterMessage;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.ClusterServiceMessageReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Cache backed provider that will store the data on save into a backing cache and notify other listeners when the cache is updated/deleted
 * E is the Entity that needs to transform to the DTO
 * EId is the id of the Entity
 * T is the DTO
 * TId is the id of the DTO
 */
public abstract class ClusterAwareDtoCache<E, EId, T, TId> implements ClusterServiceMessageReceiver {

    private static final Logger log = LoggerFactory.getLogger(ClusterAwareDtoCache.class);

    private AtomicBoolean populatedCache = new AtomicBoolean(false);

    private AtomicBoolean populatingCache = new AtomicBoolean(false);

    @Inject
    protected ClusterService clusterService;

    protected LoadingCache<TId, T> cache = CacheBuilder.newBuilder().build(new CacheLoader<TId, T>() {

        @Override
        public T load(TId key) throws Exception {
            E value = null;
            value = fetchForKey(entityIdForDtoId(key));
            T dto = transformEntityToDto(key, value);

            return dto;
        }


    });

    public ClusterAwareDtoCache() {

    }


    public abstract String getClusterMessageKey();

    public abstract String getProviderName();

    public abstract EId entityIdForDtoId(TId dtoId);


    public abstract TId dtoIdForEntity(E entity);

    public abstract TId getDtoId(T dto);

    public abstract T transformEntityToDto(TId dtoId, E entity);

    public T transformEntityToDto(E entity) {
        return transformEntityToDto(dtoIdForEntity(entity), entity);
    }

    public Long size() {
        return cache.size();
    }

    public abstract E fetchForKey(EId entityId);

    public abstract List<E> fetchAll();

    /**
     * find by Id
     */
    public T findById(TId id) {
        return cache.getUnchecked(id);
    }

    /**
     * Returns all from the Cache only
     */
    public List<T> findAll() {
        return cache.asMap().values().stream().collect(Collectors.toList());
    }


    private void addItem(TId key, T value) {
        cache.put(key, value);
    }

    private void addItem(T dto) {
        addItem(getDtoId(dto), dto);
    }

    private void removeItem(T value) {
        cache.invalidate(getDtoId(value));
    }

    private void removeItemById(TId id) {
        cache.invalidate(id);
    }

    private void removeItems(Collection<T> values) {
        values.stream().forEach(v -> removeItem(v));
    }

    private void removeAll() {
        cache.invalidateAll();
    }


    public List<T> save(List<E> values) {
        List<T> dtoList = values.stream().map(v -> transformEntityToDto(v)).collect(Collectors.toList());
        dtoList.forEach(dto -> addItem(dto));
        clusterService.sendMessageToOthers(getClusterMessageKey(), new CacheBackedProviderClusterMessage(CacheBackedProviderClusterMessage.Type.ADDED, Lists.newArrayList(dtoList)));
        return dtoList;
    }

    public T save(E value) {
        T dto = transformEntityToDto(value);
        addItem(dto);
        clusterService.sendMessageToOthers(getClusterMessageKey(), new CacheBackedProviderClusterMessage(CacheBackedProviderClusterMessage.Type.ADDED, Lists.newArrayList(dto)));
        return dto;
    }

    public void deleteByDtoId(TId dtoId) {
        T dto = cache.getUnchecked(dtoId);
        if (dto != null) {
            removeItemById(dtoId);
            clusterService.sendMessageToOthers(getClusterMessageKey(), new CacheBackedProviderClusterMessage(CacheBackedProviderClusterMessage.Type.REMOVED, Lists.newArrayList(dto)));
        }
    }

    public void delete(E value) {
        T dto = transformEntityToDto(value);
        removeItem(dto);
        clusterService.sendMessageToOthers(getClusterMessageKey(), new CacheBackedProviderClusterMessage(CacheBackedProviderClusterMessage.Type.REMOVED, Lists.newArrayList(dto)));
    }

    public void delete(List<E> values) {
        List<T> dtoList = values.stream().map(entity -> transformEntityToDto(entity)).collect(Collectors.toList());
        removeItems(dtoList);
        clusterService.sendMessageToOthers(getClusterMessageKey(), new CacheBackedProviderClusterMessage(CacheBackedProviderClusterMessage.Type.REMOVED, dtoList));
    }

    private boolean canRefresh() {
        return (populatingCache.compareAndSet(false, true));
    }

    public Collection<T> populateCache() {
        refresh();
        return cache.asMap().values();
    }

    private void refresh() {
        try {
            log.info("Populating Cache for {} ", getProviderName());
            Stopwatch stopwatch = Stopwatch.createStarted();
            List<E> entities = fetchAll();
            entities.stream().map(entity -> transformEntityToDto(entity)).forEach(dto -> addItem(dto));
            populatedCache.set(true);
            stopwatch.stop();
            log.info("Time to populate {} Cache {} ms", getProviderName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            populatedCache.set(false);
        } finally {
            populatingCache.set(false);
        }

    }

    public void refreshCache() {
        if (canRefresh()) {
            removeAll();
            populateCache();
            clusterService.sendMessageToOthers(getClusterMessageKey(), new CacheBackedProviderClusterMessage(CacheBackedProviderClusterMessage.Type.REFRESHED, null));
        }
    }

    public boolean isPopulated() {
        return populatedCache.get();
    }

    @Override
    public void onMessageReceived(String from, ClusterMessage message) {

        if (getClusterMessageKey().equalsIgnoreCase(message.getType())) {
            CacheBackedProviderClusterMessage content = (CacheBackedProviderClusterMessage) message.getMessage();
            if (CacheBackedProviderClusterMessage.Type.REMOVED == content.getType()) {
                removeItems(content.getItems());
            }
            if (CacheBackedProviderClusterMessage.Type.REFRESHED == content.getType()) {
                if (canRefresh()) {
                    removeAll();
                    populateCache();
                }
            } else if (CacheBackedProviderClusterMessage.Type.ADDED == content.getType()) {
                List<T> dtos = content.getItems();
                dtos.stream().forEach(dto -> addItem(dto));
            }
        }
    }

    public T getUnchecked(TId id) {
        return cache.getUnchecked(id);
    }
}
