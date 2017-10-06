package com.thinkbiganalytics.search;

/*-
 * #%L
 * kylo-search-elasticsearch
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

import com.thinkbiganalytics.search.api.Search;
import com.thinkbiganalytics.search.api.SearchIndex;
import com.thinkbiganalytics.search.config.ElasticSearchClientConfiguration;
import com.thinkbiganalytics.search.rest.model.SearchResult;
import com.thinkbiganalytics.search.transform.ElasticSearchSearchResultTransform;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Service to search Elasticsearch
 */
public class ElasticSearchService implements Search {

    static Logger log = LoggerFactory.getLogger(ElasticSearchService.class);

    private ElasticSearchClientConfiguration clientConfig;
    private Client client;

    public ElasticSearchService(ElasticSearchClientConfiguration config) {
        this.clientConfig = config;
        log.info("Search engine: Elasticsearch");
    }

    @Override
    public void delete(@Nonnull final String indexName, @Nonnull final String typeName, @Nonnull final String id, @Nonnull final String schema, @Nonnull final String table) {
        buildTransportClient();
        client.prepareDelete(indexName, typeName, id).execute()
            .addListener(new ActionListener<DeleteResponse>() {
                @Override
                public void onResponse(final DeleteResponse deleteResponse) {
                    log.debug("Completed deleting of index:{} type:{} id:{}", indexName, typeName, id);
                }

                @Override
                public void onFailure(final Throwable e) {
                    log.warn("Failed deleting of index:{} type:{}, id:{}", indexName, typeName, id, e);
                }
            });
    }

    @Override
    public void commit(@Nonnull final String indexName) {
        buildTransportClient();
        client.admin().indices().prepareRefresh(indexName).execute()
            .addListener(new ActionListener<RefreshResponse>() {
                @Override
                public void onResponse(final RefreshResponse refreshResponse) {
                    log.debug("Committed index: {}", indexName);
                }

                @Override
                public void onFailure(final Throwable e) {
                    log.warn("Failed to commit index: {}", indexName, e);
                }
            });
    }

    @Override
    public void index(@Nonnull final String indexName, @Nonnull final String typeName, @Nonnull final String id, @Nonnull final Map<String, Object> fields) {
        buildTransportClient();
        client.prepareIndex(indexName, typeName, id).setSource(fields).execute()
            .addListener(new ActionListener<IndexResponse>() {
                @Override
                public void onResponse(final IndexResponse indexResponse) {
                    log.debug("Completed indexing of index:{} type:{} id:{}", indexName, typeName, id);
                }

                @Override
                public void onFailure(final Throwable e) {
                    log.warn("Failed indexing of index:{} type:{} id:{}", indexName, typeName, id, e);
                }
            });
    }

    @Override
    public SearchResult search(String query, int size, int start) {
        buildTransportClient();
        String queryForExecution = rewriteQuery(query);
        SearchResponse searchResponse = executeSearch(queryForExecution, size, start);
        return transformResult(query, size, start, searchResponse);
    }

    private void buildTransportClient() {
        if (this.client == null) {
            try {
                Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", clientConfig.getClusterName())
                    .build();

                client = TransportClient.builder()
                    .settings(settings)
                    .build()
                    .addTransportAddress(new InetSocketTransportAddress
                                             (InetAddress.getByName(clientConfig.getHost()), clientConfig.getTransportPort())
                    );
            } catch (UnknownHostException e) {
                throw new RuntimeException("Error encountered during search.");
            }
        }
    }

    private SearchResponse executeSearch(String query, int size, int start) {
        final String KYLO_CATEGORIES_METADATA = "kylo-categories-metadata";
        final String KYLO_CATEGORIES_DEFAULT = "kylo-categories-default";
        final String KYLO_FEEDS_METADATA = "kylo-feeds-metadata";
        final String KYLO_FEEDS_DEFAULT = "kylo-feeds-default";
        final String KYLO_DATA = "kylo-data";
        final String ALL_FIELDS = "*";
        final String BOLD_HIGHLIGHT_START = "<font style='font-weight:bold'>";
        final String BOLD_HIGHLIGHT_END = "</font>";

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch()
            .setQuery(QueryBuilders.queryStringQuery(query))
            .setFrom(start)
            .setSize(size)
            .setIndicesOptions(IndicesOptions.lenientExpandOpen())
            .setIndices(KYLO_CATEGORIES_METADATA,
                        KYLO_CATEGORIES_DEFAULT,
                        KYLO_FEEDS_METADATA,
                        KYLO_FEEDS_DEFAULT,
                        KYLO_DATA,
                        SearchIndex.DATASOURCES)
            .addHighlightedField(ALL_FIELDS)
            .setHighlighterPreTags(BOLD_HIGHLIGHT_START)
            .setHighlighterPostTags(BOLD_HIGHLIGHT_END)
            .setHighlighterRequireFieldMatch(false);

        return searchRequestBuilder.execute().actionGet();
    }

    private SearchResult transformResult(String query, int size, int start, SearchResponse searchResponse) {
        ElasticSearchSearchResultTransform elasticSearchSearchResultTransform = new ElasticSearchSearchResultTransform();
        return elasticSearchSearchResultTransform.transformResult(query, size, start, searchResponse);
    }

    private String rewriteQuery(String query) {
        final String SINGLE_STAR = "*";
        final String DOUBLE_STAR = "**";

        if ((query != null) && (query.trim().equals(SINGLE_STAR))) {
            return DOUBLE_STAR;
        }

        return query;
    }
}
