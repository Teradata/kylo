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


/**
 * Run a global search (via a search engine) and return results.
 */
public interface Search extends Serializable {

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
