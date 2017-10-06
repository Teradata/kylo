package com.thinkbiganalytics.search;

/*-
 * #%L
 * kylo-search-solr
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

import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.search.api.Search;
import com.thinkbiganalytics.search.api.SearchIndex;
import com.thinkbiganalytics.search.config.SolrSearchClientConfiguration;
import com.thinkbiganalytics.search.rest.model.SearchResult;
import com.thinkbiganalytics.search.transform.SolrSearchResultTransform;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Service to search Solr
 */
public class SolrSearchService implements Search {

    static Logger log = LoggerFactory.getLogger(SolrSearchService.class);

    private SolrSearchClientConfiguration clientConfig;
    private HttpSolrClient client;

    public SolrSearchService(SolrSearchClientConfiguration config) {
        this.clientConfig = config;
        log.info("Search engine: Solr");
    }

    @Override
    public void delete(@Nonnull final String indexName, @Nonnull final String typeName, @Nonnull final String id, @Nonnull final String schema, @Nonnull final String table) {
        buildRestClient();
        final String dataCollectionName = "kylo-data";
        try {
            //deletes the schema
            client.deleteById(indexName, id);
            client.commit(indexName);
            log.info("Deleted schema document for index={}, type={}, id={}", indexName, typeName, id);
        } catch (final IOException | SolrServerException e) {
            log.warn("Failed to delete document in index:{} with id:{} [{}]", indexName, id, e);
        }

        try {
            //delete the data
            client.deleteByQuery(dataCollectionName,"kylo_schema:" + schema + " AND kylo_table:" + table);
            client.commit(dataCollectionName);
            log.info("Deleted data for index={}, schema={}, table={}", dataCollectionName, schema, table);
        } catch (final IOException | SolrServerException e) {
            log.warn("Failed to delete data for index={}, schema={}, table={} [{}]", dataCollectionName, schema, table, e);
        }
    }

    @Override
    public void commit(@Nonnull final String indexName) {
        buildRestClient();
        try {
            client.commit(indexName);
        } catch (final IOException | SolrServerException e) {
            log.warn("Failed to commit changes to index: {}", indexName, e);
        }
    }

    @Override
    public void index(@Nonnull final String indexName, @Nonnull final String typeName, @Nonnull final String id, @Nonnull final Map<String, Object> fields) {
        buildRestClient();
        try {
            client.add(indexName, createDocument(id, fields));
        } catch (final IOException | SolrServerException e) {
            log.warn("Failed to index document in index:{} with id:{}", indexName, id, e);
        }
    }

    @Override
    public SearchResult search(String query, int size, int start) {
        buildRestClient();
        QueryResponse solrResponse = executeSearch(query, size, start);
        return transformResult(query, size, start, solrResponse);
    }

    private void buildRestClient() {
        if (this.client == null) {
            String urlString = "http://" + clientConfig.getHost() + ":" + clientConfig.getPort() + "/solr/";
            client = new HttpSolrClient.Builder(urlString).build();
        }
    }

    /**
     * Creates a new Solr document to be indexed.
     */
    @Nonnull
    private SolrInputDocument createDocument(@Nonnull final String id, @Nonnull final Map<String, Object> fields) {
        final SolrInputDocument document = new SolrInputDocument();
        document.setField("id", id);
        fields.forEach((key, value) -> document.setField(key, (value instanceof String) ? value : ObjectMapperSerializer.serialize(value)));
        return document;
    }

    private QueryResponse executeSearch(String query, int size, int start) {
        final String COLLECTION_LIST = "kylo-data," + SearchIndex.DATASOURCES;    //Solr admin to configure beforehand
        final String ALL_FIELDS = "*";
        final String BOLD_HIGHLIGHT_START = "<font style='font-weight:bold'>";
        final String BOLD_HIGHLIGHT_END = "</font>";

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", query);
        solrQuery.setRows(size);
        solrQuery.setStart(start);
        solrQuery.setParam("collection", COLLECTION_LIST);
        solrQuery.setHighlight(true);
        solrQuery.set("hl.fl", ALL_FIELDS);
        solrQuery.setHighlightSimplePre(BOLD_HIGHLIGHT_START);
        solrQuery.setHighlightSimplePost(BOLD_HIGHLIGHT_END);
        solrQuery.setHighlightRequireFieldMatch(false);

        try {
            return client.query("kylo-data", solrQuery);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException("Error encountered during search.");
        }
    }

    private SearchResult transformResult(String query, int size, int start, QueryResponse solrResponse) {
        SolrSearchResultTransform solrSearchResultTransform = new SolrSearchResultTransform();
        return solrSearchResultTransform.transformResult(query, size, start, solrResponse);
    }
}
