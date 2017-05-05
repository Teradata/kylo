package com.thinkbiganalytics.metadata.jpa.common;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.metadata.api.common.ItemLastModified;
import com.thinkbiganalytics.metadata.api.common.ItemLastModifiedProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JpaItemLastModifiedProvider implements ItemLastModifiedProvider {


    /**
     * The Spring Data Repository
     */
    private ItemLastModifiedRepository itemLastModifiedRepository;


    @Autowired
    public JpaItemLastModifiedProvider(ItemLastModifiedRepository itemLastModifiedRepository) {
        this.itemLastModifiedRepository = itemLastModifiedRepository;
    }

    /**
     * Check to see if a value saved in memory for a given key is up to date
     *
     * @param key              the identifier key to check for the latest timestame
     * @param lastModifiedTime the timestamp to check against the one persisted
     * @return true if the timestamp matches the one persisted for this key (the item is up to date), false if not.  False means the item is not up to date
     */
    @Override
    public boolean isUpdated(String key, Long lastModifiedTime) {
        return itemLastModifiedRepository.isUpdated(key, lastModifiedTime);
    }

    /**
     * Update the persisted value marking the current time as the persistent last modified time
     *
     * @param key a key identifying the timestamp marker
     * @return the last modified item persisted
     */
    @Override
    public ItemLastModified update(String key, String value) {
        JpaItemLastModified lastModified = itemLastModifiedRepository.findOne(key);
        if (lastModified == null) {
            lastModified = new JpaItemLastModified(key);
        }
        lastModified.setValue(value);
        lastModified.setModifiedTime(DateTimeUtil.getNowUTCTime());
        return itemLastModifiedRepository.save(lastModified);
    }

    /**
     * Delete an item from last modified table
     *
     * @param key a key identifying the timestamp marker
     */
    @Override
    public void delete(String key) {
        JpaItemLastModified lastModified = itemLastModifiedRepository.findOne(key);
        if (lastModified != null) {
            itemLastModifiedRepository.delete(lastModified);
        }
    }

    /**
     * Find an item by its key.  Return null if not found
     *
     * @param key the key to look for
     * @return the item, or null if not found
     */
    @Override
    public ItemLastModified findByKey(String key) {
        return itemLastModifiedRepository.findOne(key);
    }
}
