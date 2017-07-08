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

import com.thinkbiganalytics.search.api.Search;
import com.thinkbiganalytics.search.config.SolrSearchClientConfiguration;
import com.thinkbiganalytics.search.rest.model.SearchResult;
import com.thinkbiganalytics.search.transform.SolrSearchResultTransform;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Service to search Solr
 */
public class SolrSearchService implements Search {

    static Logger logger = LoggerFactory.getLogger(SolrSearchService.class);

    private SolrSearchClientConfiguration clientConfig;
    private HttpSolrClient client;

    public SolrSearchService(SolrSearchClientConfiguration config) {
        this.clientConfig = config;
        logger.info("Search engine: Solr");
    }

    @Override
    public SearchResult search(String query, int size, int start) {
        buildRestClient();
        QueryResponse solrResponse = executeSearch(query, size, start);
        return transformResult(query, size, start, solrResponse);
    }

    private void buildRestClient() {
        if (this.client == null) {
            String urlString = "http://" + clientConfig.getHost() + ":" + clientConfig.getPort() + "/solr/" + "kylo-data";
            client = new HttpSolrClient.Builder(urlString).build();
        }
    }

    private QueryResponse executeSearch(String query, int size, int start) {
        final String COLLECTION_LIST = "kylo-data,kylo-schema-metadata";    //Solr admin to configure beforehand
        final String ALL_FIELDS = "*";
        final String BOLD_HIGHLIGHT_START = "<font style='font-weight:bold'>";
        final String BOLD_HIGHLIGHT_END = "</font>";

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", query);
        solrQuery.setRows(size);
        solrQuery.setStart(start);
        solrQuery.setParam("collection",COLLECTION_LIST);
        solrQuery.setHighlight(true);
        solrQuery.set("hl.fl", ALL_FIELDS);
        solrQuery.setHighlightSimplePre(BOLD_HIGHLIGHT_START);
        solrQuery.setHighlightSimplePost(BOLD_HIGHLIGHT_END);
        solrQuery.setHighlightRequireFieldMatch(false);

        try {
            return client.query(solrQuery);
        }
        catch (SolrServerException | IOException e) {
            throw new RuntimeException("Error encountered during search.");
        }
    }

    private SearchResult transformResult(String query, int size, int start, QueryResponse solrResponse) {
        SolrSearchResultTransform solrSearchResultTransform = new SolrSearchResultTransform();
        return solrSearchResultTransform.transformResult(query, size, start, solrResponse);
    }
}
