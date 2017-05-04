package com.thinkbiganalytics.metadata.api.common;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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
 * A Key,Timestamp persistence to mark/detect when an item needs updating.
 */
public interface ItemLastModifiedProvider {

    /**
     * Check to see if a value saved in memory for a given key is up to date
     *
     * @param key              the identifier key to check for the latest timestame
     * @param lastModifiedTime the timestamp to check against the one persisted
     * @return true if the timestamp matches the one persisted for this key (the item is up to date), false if not.  False means the item is not up to date
     */
    boolean isUpdated(String key, Long lastModifiedTime);

    /**
     * Update the persisted value marking the current time as the persistent last modified time
     *
     * @param key   a key identifying the timestamp marker
     * @param value the value to be persisted
     * @return the last modified item persisted
     */
    ItemLastModified update(String key, String value);

    /**
     * Delete an item from last modified table
     *
     * @param key a key identifying the timestamp marker
     */
    void delete(String key);

    /**
     * Find an item by its key.
     *
     * @param key the key to look for
     * @return the item, or null if not found
     */
    ItemLastModified findByKey(String key);
}
