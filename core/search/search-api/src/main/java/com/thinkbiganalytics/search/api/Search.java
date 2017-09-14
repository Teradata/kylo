package com.thinkbiganalytics.search.api;

/*-
 * #%L
 * kylo-search-api
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

import com.thinkbiganalytics.search.rest.model.SearchResult;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nonnull;


/**
 * Provides access to a search engine for global search and updating indexes.
 */
public interface Search extends Serializable {

    /**
     * Deletes the specified item if it exists.
     *
     * @param indexName the name of the index
     * @param typeName  the type of object
     * @param id        the unique identifier for the object
     * @param schema    the kylo schema related to the object
     * @param table     the kylo table related to the object
     */
    void delete(@Nonnull String indexName, @Nonnull String typeName, @Nonnull String id, @Nonnull String schema, @Nonnull String table);

    /**
     * Commits any changes made to the specified index.
     *
     * @param indexName the name of the index
     */
    void commit(@Nonnull String indexName);

    /**
     * Indexes the specified object.
     *
     * <p>The index will be created if it does not already exist. If the object already exists then it will be replaced.</p>
     *
     * @param indexName the name of the index
     * @param typeName  the type of object
     * @param id        the unique identifier for the object
     * @param fields    the properties of the object
     */
    void index(@Nonnull String indexName, @Nonnull String typeName, @Nonnull String id, @Nonnull Map<String, Object> fields);

    /**
     * Execute a search
     *
     * @param query Search query
     * @param size  Number of results to return
     * @param start Starting index for results (the ending index will be calculated using {@code size} parameter)
     * @return {@link SearchResult}
     */
    SearchResult search(String query, int size, int start);
}
