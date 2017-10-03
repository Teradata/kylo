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

import java.io.Serializable;
import java.util.List;

/**
 * Generic message object to notify other cluster members that a cached item was updated
 */
public class CacheBackedProviderClusterMessage<T> implements Serializable {

    public static enum Type {
        REMOVED, ADDED, REFRESHED
    }

    private Type type;
    private List<T> items;

    public CacheBackedProviderClusterMessage(Type type, List<T> items) {
        this.type = type;
        this.items = items;
    }

    public List<T> getItems() {
        return items;
    }

    public Type getType() {
        return type;
    }
}
