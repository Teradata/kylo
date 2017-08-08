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

import com.thinkbiganalytics.search.rest.model.HiveColumn;
import com.thinkbiganalytics.search.rest.model.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Store a hit from Elasticsearch retrieved using rest client
 */
public class ElasticSearchRestSearchHit {

    private String indexName;
    private String indexType;
    private String rawHit;
    private List<Pair> source;
    private List<Pair> highlights;
    private List<HiveColumn> hiveColumns;

    public ElasticSearchRestSearchHit() {
        source = new ArrayList<>();
        highlights = new ArrayList<>();
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public String getRawHit() {
        return rawHit;
    }

    public void setRawHit(String rawHit) {
        this.rawHit = rawHit;
    }

    public List<Pair> getSource() {
        return source;
    }

    public void setSource(List<Pair> source) {
        this.source = source;
    }

    public List<Pair> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<Pair> highlights) {
        this.highlights = highlights;
    }

    public List<HiveColumn> getHiveColumns() {
        return hiveColumns;
    }

    public void setHiveColumns(List<HiveColumn> hiveColumns) {
        this.hiveColumns = hiveColumns;
    }

    public Object findValueForKeyInSourceWithDefault(String key, String defaultValue) {
        for (Pair pair : source) {
            if (pair.getKey().equals(key)) {
                return pair.getValue();
            }
        }
        return defaultValue;
    }

    public Object findValueForKeyInHighlightsWithDefault(String key, String defaultValue) {
        for (Pair pair : highlights) {
            if (pair.getKey().equals(key)) {
                return pair.getValue();
            }
        }
        return defaultValue;
    }
}
