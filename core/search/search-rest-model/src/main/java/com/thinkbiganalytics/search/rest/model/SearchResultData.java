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

import java.util.List;
import java.util.Map;

/**
 * Stores the search result content returned from a search engine
 */
public interface SearchResultData {

    /**
     * Get the type of search result
     *
     * @return {@link SearchResultType}
     */
    SearchResultType getType();

    /**
     * Get the UI icon for displaying the search result
     *
     * @return icon label
     */
    String getIcon();

    /**
     * Get the UI color for displaying the search result
     *
     * @return color
     */
    String getColor();

    /**
     * Get the snippets that produced the positive search matches
     *
     * @return list of key-value pairs ({@link Pair}) describing snippets.
     */
    List<Pair> getHighlights();

    /**
     * Get the raw data for the search result
     *
     * @return raw data as a map
     */
    Map<String, Object> getRawData();
}
