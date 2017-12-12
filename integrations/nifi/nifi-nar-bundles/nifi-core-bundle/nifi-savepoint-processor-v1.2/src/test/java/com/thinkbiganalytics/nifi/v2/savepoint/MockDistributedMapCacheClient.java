package com.thinkbiganalytics.nifi.v2.savepoint;

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

import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.distributed.cache.client.Deserializer;
import org.apache.nifi.distributed.cache.client.DistributedMapCacheClient;
import org.apache.nifi.distributed.cache.client.Serializer;
import org.apache.tika.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockDistributedMapCacheClient extends AbstractControllerService implements DistributedMapCacheClient {

    private final ConcurrentMap<Object, Object> values = new ConcurrentHashMap<>();

    private <K> String serialize(K v, Serializer<K> serializer) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            serializer.serialize(v, os);
            return os.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }


    private <V>V deserialize(String svalue, Deserializer<V> deserializer) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            return deserializer.deserialize(svalue.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }



    @Override
    public <K, V> boolean putIfAbsent(final K key, final V value, final Serializer<K> keySerializer, final Serializer<V> valueSerializer) throws IOException {
        String skey = serialize(key, keySerializer);
        final Object retValue =
            values.putIfAbsent(skey, serialize(value, valueSerializer));
        return (retValue == null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> V getAndPutIfAbsent(final K key, final V value, final Serializer<K> keySerializer, final Serializer<V> valueSerializer,
                                      final Deserializer<V> valueDeserializer) throws IOException {
        String skey = serialize(key, keySerializer);
        return (V) values.putIfAbsent(skey, serialize(value, valueSerializer));
    }

    @Override
    public <K> boolean containsKey(final K key, final Serializer<K> keySerializer) throws IOException {
        String skey = serialize(key, keySerializer);
        return values.containsKey(skey);
    }

    @Override
    public <K, V> void put(final K key, final V value, final Serializer<K> keySerializer, final Serializer<V> valueSerializer) throws IOException {
        String skey = serialize(key, keySerializer);
        values.put(skey, serialize(value, valueSerializer));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> V get(final K key, final Serializer<K> keySerializer, final Deserializer<V> valueDeserializer) throws IOException {
        String skey = serialize(key, keySerializer);
        if(values.containsKey(skey)) {
           return (V) deserialize((String)values.get(skey), valueDeserializer);
        } else {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public <K> boolean remove(final K key, final Serializer<K> serializer) throws IOException {
        String skey = serialize(key, serializer);
        values.remove(skey);
        return true;
    }

    @Override
    public long removeByPattern(String regex) throws IOException {

        final List<Object> removed = new ArrayList<>();
        Pattern p = Pattern.compile(regex);
        for (Object key : values.keySet()) {
            Matcher m = p.matcher(key.toString());
            if (m.matches()) {
                removed.add(values.get(key));
            }
        }
        long recordsRemoved = removed.size();
        removed.forEach(values::remove);
        return recordsRemoved;
    }
}