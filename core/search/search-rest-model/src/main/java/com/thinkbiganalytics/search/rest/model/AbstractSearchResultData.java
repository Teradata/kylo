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
 * An abstract implementation of {@link SearchResultData}
 */
public abstract class AbstractSearchResultData implements SearchResultData {

    private String icon;
    private SearchResultType type;
    private String color;
    private Map<String, Object> rawData;
    private List<Pair> highlights;


    /**
     * {@inheritDoc}
     */
    @Override
    public String getIcon() {
        return icon;
    }

    /**
     * Set the UI icon for displaying the search result
     *
     * @param icon icon label
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResultType getType() {
        return type;
    }

    /**
     * Set the type of search result
     *
     * @param type {@link SearchResultType}
     */
    public void setType(SearchResultType type) {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getRawData() {
        return rawData;
    }

    /**
     * Set the raw data for the search result
     *
     * @param rawData raw data as a map
     */
    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColor() {
        return color;
    }

    /**
     * Set the UI color for displaying the search result
     *
     * @param color string specifying color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Pair> getHighlights() {
        return highlights;
    }

    /**
     * Set the snippets that produced the positive search matches
     *
     * @param highlights list of key-value pairs ({@link Pair}) describing snippets
     */
    public void setHighlights(List<Pair> highlights) {
        this.highlights = highlights;
    }
}
