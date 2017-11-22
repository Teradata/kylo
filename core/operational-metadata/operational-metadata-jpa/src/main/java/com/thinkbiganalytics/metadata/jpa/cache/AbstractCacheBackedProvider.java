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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.cluster.ClusterMessage;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.ClusterServiceMessageReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Cache backed provider that will store the data on save into a backing cache and notify other listeners when the cache is updated/deleted
 */
public abstract class AbstractCacheBackedProvider<T, ID extends Serializable> implements ClusterServiceMessageReceiver {

    private static final Logger log = LoggerFactory.getLogger(AbstractCacheBackedProvider.class);

    protected JpaRepository repository;

    private AtomicBoolean populatedCache = new AtomicBoolean(false);

    private AtomicBoolean populatingCache = new AtomicBoolean(false);

    private List<CacheBackedProviderListener> cacheListeners = new ArrayList<>();

    @Inject
    protected ClusterService clusterService;


    public void subscribeListener(CacheBackedProviderListener listener) {
        this.cacheListeners.add(listener);
    }


    private int refreshThreads = 2;
    private ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("CacheBackedProviderRefresher-pool-%d").setDaemon(true).build();
    private ExecutorService executor = Executors.newFixedThreadPool(refreshThreads, threadFactory);

    // create an executor that provide ListenableFuture instances
    final ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(executor);

    protected LoadingCache<ID, T> cache = CacheBuilder.newBuilder().build(new CacheLoader<ID, T>() {

        @Override
        public T load(ID key) throws Exception {
            T value = null;
            try {
                value = fetchForKey(key);
                onAddedItem(key, value);
            } catch (Exception e) {

            }
            return value;
        }

        @Override
        public ListenableFuture<T> reload(ID key, T oldValue) throws Exception {
            return listeningExecutor.submit(() -> fetchForKey(key));
        }


    });

    public AbstractCacheBackedProvider(JpaRepository repository) {
        this.repository = repository;
    }

    public abstract ID getId(T value);

    public abstract String getClusterMessageKey();

    public abstract String getProviderName();

    private T fetchForKey(ID key) {
        return (T) repository.findOne(key);
    }

    /**
     * find by Id
     */
    public T findById(ID id) {
        return cache.getUnchecked(id);
    }

    /**
     * Returns all from the Cache only
     */
    public List<T> findAll() {
        return cache.asMap().values().stream().collect(Collectors.toList());
    }


    private void onAddedItem(ID key, T value) {
        cacheListeners.stream().forEach(listener -> listener.onAddedItem(key, value));
    }

    private void onRemovedItem(T value) {
        cacheListeners.stream().forEach(listener -> listener.onRemovedItem(value));
    }

    private void onRemoveAll() {
        cacheListeners.stream().forEach(listener -> listener.onRemoveAll());
    }


    private void addItems(List<T> values) {
        values.stream().forEach(v -> addItem(getId(v), v));
    }

    private void addItem(ID key, T value) {
        cache.put(key, value);
        onAddedItem(key, value);
    }

    private void removeItem(T value) {
        cache.invalidate(getId(value));
        onRemovedItem(value);
    }

    private void removeItems(Collection<T> values) {
        values.stream().forEach(v -> removeItem(v));
    }

    private void removeAll() {
        cache.invalidateAll();
        onRemoveAll();
    }


    public <S extends T> List<S> saveList(Iterable<S> values) {
        List<S> list = repository.save(values);
        list.stream().forEach(v -> addItem(getId(v), v));
        clusterService.sendMessageToOthers(getClusterMessageKey(), new CacheBackedProviderClusterMessage(CacheBackedProviderClusterMessage.Type.ADDED, Lists.newArrayList(list)));
        return list;
    }

    public T save(T value) {
        value = (T) repository.save(value);
        addItem(getId(value), value);
        clusterService.sendMessageToOthers(getClusterMessageKey(), new CacheBackedProviderClusterMessage(CacheBackedProviderClusterMessage.Type.ADDED, Lists.newArrayList(value)));
        return value;
    }

    public void delete(T value) {
        repository.delete(value);
        removeItem(value);
        clusterService.sendMessageToOthers(getClusterMessageKey(), new CacheBackedProviderClusterMessage(CacheBackedProviderClusterMessage.Type.REMOVED, Lists.newArrayList(value)));
    }

    public void delete(Iterable<? extends T> values) {
        repository.delete(values);
        List<T> list = Lists.newArrayList(values);
        removeItems(list);
        clusterService.sendMessageToOthers(getClusterMessageKey(), new CacheBackedProviderClusterMessage(CacheBackedProviderClusterMessage.Type.REMOVED, list));
    }

    private boolean canRefresh() {
        return (populatingCache.compareAndSet(false, true));
    }

    protected Collection<T> populateCache() {
        refresh();
        return cache.asMap().values();
    }

    private void refresh() {
        try {
            log.info("Populating Cache for {} ", getProviderName());
            Stopwatch stopwatch = Stopwatch.createStarted();
            addItems(repository.findAll());
            populatedCache.set(true);
            cacheListeners.stream().forEach(CacheBackedProviderListener::onPopulated);
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
                } else {
                    log.info("Cluster message received to Refresh the cache, but uanble to as it is still being populated.");
                }
            } else if (CacheBackedProviderClusterMessage.Type.ADDED == content.getType()) {
                addItems(content.getItems());
            }
        }
    }
}
