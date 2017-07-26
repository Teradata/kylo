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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Store the execution metadata and actual results of a search
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {

    private String query;
    private Long totalHits;
    private Long tookInMillis;
    private Long from;
    private Long to;
    private String engine;

    private Map<SearchResultType, Long> searchResultsSummary;
    private List<SearchResultData> searchResults;

    public SearchResult() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(Long totalHits) {
        this.totalHits = totalHits;
    }

    public Long getFrom() {
        return this.from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return this.to;
    }

    public Long getTookInMillis() {
        return tookInMillis;
    }

    public void setTookInMillis(Long tookInMillis) {
        this.tookInMillis = tookInMillis;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public List<SearchResultData> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<SearchResultData> searchResults) {
        this.searchResults = searchResults;
    }

    public Map<SearchResultType, Long> getSearchResultsSummary() {
        return searchResultsSummary;
    }

    public void setSearchResultsSummary(Map<SearchResultType, Long> searchResultsSummary) {
        this.searchResultsSummary = searchResultsSummary;
    }
}
