package com.thinkbiganalytics.search.rest.model;

/*-
 * #%L
 * kylo-search-rest-model
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
 * A key-value pair
 */
public class Pair {

    private String key;
    private Object value;

    public Pair() {
    }

    /**
     * Construct a pair
     * @param key key
     * @param value value
     */
    public Pair(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Get the key for the pair
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the key for the pair
     * @param key key for the pair
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Get the value for the pair
     * @return value for the pair
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the value for the pair
     * @param value value for the pair
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
