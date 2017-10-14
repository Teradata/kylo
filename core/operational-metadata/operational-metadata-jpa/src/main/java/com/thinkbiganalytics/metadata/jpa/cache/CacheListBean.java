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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cache data into a common key with the values pushed into a List
 */
public class CacheListBean<K, V> {

    private Cache<K, List<V>> cache = CacheBuilder.newBuilder().build();

    private AtomicBoolean populated = new AtomicBoolean(false);


    public List<V> get(K key) {
        List<V> list = cache.getIfPresent(key);
        if (list == null) {
            return new ArrayList<V>();
        }
        return list;
    }

    public void add(K key, V value) {
        List<V> values = cache.getIfPresent(key);
        if (values == null) {
            values = new ArrayList<V>();
            cache.put(key, values);
        }
        values.add(value);
    }

    public void remove(K key, V value) {
        List<V> values = cache.getIfPresent(key);
        if (value != null && values != null) {
            values.removeIf(v -> isEqual(v, value));
        }
    }

    public boolean isEqual(V value1, V value2) {
        return value1.equals(value2);
    }

    public void removeAll(Map<K, List<V>> map) {
        map.entrySet().stream().forEach(e -> {
            e.getValue().stream().forEach(v -> remove(e.getKey(), v));
        });
    }

    public void add(Map<K, List<V>> values) {
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

    public Long size(){
        return cache.size();
    }


}
