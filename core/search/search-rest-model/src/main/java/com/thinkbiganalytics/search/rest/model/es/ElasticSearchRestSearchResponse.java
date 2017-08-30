package com.thinkbiganalytics.search.rest.model.es;

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

import org.apache.http.HttpEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Store results from Elasticsearch retrieved using rest client
 */
public class ElasticSearchRestSearchResponse {

    private String requestLine;
    private String host;
    private String statusLine;
    private Long tookInMillis;
    private List<String> headers;
    private List<ElasticSearchRestSearchHit> elasticSearchRestSearchHits;
    private HttpEntity rawEntity;
    private long totalResults;

    public ElasticSearchRestSearchResponse() {
        headers = new ArrayList<>();
        elasticSearchRestSearchHits = new ArrayList<>();
    }

    public String getRequestLine() {
        return requestLine;
    }

    public void setRequestLine(String requestLine) {
        this.requestLine = requestLine;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    public Long getTookInMillis() {
        return tookInMillis;
    }

    public void setTookInMillis(Long tookInMillis) {
        this.tookInMillis = tookInMillis;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<ElasticSearchRestSearchHit> getElasticSearchRestSearchHits() {
        return elasticSearchRestSearchHits;
    }

    public void setElasticSearchRestSearchHits(List<ElasticSearchRestSearchHit> elasticSearchRestSearchHits) {
        this.elasticSearchRestSearchHits = elasticSearchRestSearchHits;
    }

    public HttpEntity getRawEntity() {
        return rawEntity;
    }

    public void setRawEntity(HttpEntity rawEntity) {
        this.rawEntity = rawEntity;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }
}
