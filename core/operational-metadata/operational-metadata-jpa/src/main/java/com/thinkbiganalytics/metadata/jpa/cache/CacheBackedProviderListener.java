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

/**
 * Listener for those caches that need to be updated when the base cache is updated.
 * Usually these <ID,T> refer to the Database Id, and Database object
 *
 * @param <ID> the base cache Id
 * @param <T>  the base cached item
 */
public interface CacheBackedProviderListener<ID, T> {

    void onAddedItem(ID key, T value);

    void onRemovedItem(T value);

    void onRemoveAll();

    void onPopulated();
}
