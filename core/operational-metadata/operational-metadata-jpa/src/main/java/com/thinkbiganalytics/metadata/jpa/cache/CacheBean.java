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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

/**
 * Cache values based upon a unique key
 */
public class CacheBean<K, V> {

    protected Cache<K, V> cache;

    private AtomicBoolean populated = new AtomicBoolean(false);

    public CacheBean() {
        cache = CacheBuilder.newBuilder().build();
    }


    @Nullable
    public V get(K key) {
        return cache.getIfPresent(key);

    }

    public void add(K key, V value) {
        cache.put(key, value);
    }

    public void remove(K key) {
        invalidate(key);
    }

    public void removeAll(Map<K, List<V>> map) {
        removeAll(map.keySet());
    }

    public void removeAll(Collection<K> keys) {
        invalidateAll(keys);
    }

    public void add(Map<K, V> values) {
        cache.putAll(values);
    }

    public void invalidate(K key) {
        cache.invalidate(key);
    }

    public void invalidateAll(Collection<K> keys) {
        cache.invalidateAll(keys);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public void setPopulated(boolean isPopulated) {
        populated.set(isPopulated);
    }

    public boolean isPopulated() {
        return populated.get();
    }


}
