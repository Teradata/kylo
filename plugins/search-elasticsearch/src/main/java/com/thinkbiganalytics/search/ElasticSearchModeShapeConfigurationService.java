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

import com.thinkbiganalytics.search.api.RepositoryIndexConfiguration;
import com.thinkbiganalytics.search.config.ElasticSearchClientConfiguration;

import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.schematic.document.EditableDocument;
import org.modeshape.schematic.document.Editor;
import org.modeshape.schematic.document.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * ModeShape configuration for Elasticsearch integration
 */
public class ElasticSearchModeShapeConfigurationService implements RepositoryIndexConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchModeShapeConfigurationService.class);

    private ElasticSearchClientConfiguration clientConfig;
    private static final String ELASTIC_SEARCH = "elasticsearch";

    public ElasticSearchModeShapeConfigurationService(ElasticSearchClientConfiguration config) {
        this.clientConfig = config;
    }

    @Override
    public RepositoryConfiguration build() {
        RepositoryConfiguration repositoryConfiguration;

        final String EMPTY_CONFIG = "{}";
        final String KYLO_CATEGORIES = "kylo-categories";
        final String KYLO_FEEDS = "kylo-feeds";
        final String INDEXES = "indexes";
        final String INDEX_PROVIDERS = "indexProviders";

        try {
            repositoryConfiguration = RepositoryConfiguration.read(EMPTY_CONFIG);
        } catch (ParsingException | FileNotFoundException e) {
            log.error("Error loading the repository configuration", e);
            return null;
        }

        Editor editor = repositoryConfiguration.edit();
        EditableDocument indexesDocument = editor.getOrCreateDocument(INDEXES);

        EditableDocument categoriesIndexDocument = indexesDocument.getOrCreateDocument(KYLO_CATEGORIES);
        EditableDocument feedsIndexDocument = indexesDocument.getOrCreateDocument(KYLO_FEEDS);
        categoriesIndexDocument.putAll(getCategoriesIndexConfiguration());
        feedsIndexDocument.putAll(getFeedsIndexConfiguration());

        EditableDocument indexProvidersDocument = editor.getOrCreateDocument(INDEX_PROVIDERS);
        EditableDocument elasticSearchIndexProviderDocument = indexProvidersDocument.getOrCreateDocument(ELASTIC_SEARCH);
        elasticSearchIndexProviderDocument.putAll(getElasticSearchIndexProviderConfiguration());

        repositoryConfiguration = new RepositoryConfiguration(editor, repositoryConfiguration.getName());
        return repositoryConfiguration;
    }

    private Map<String, Object> getCategoriesIndexConfiguration() {
        final String TEXT = "text";
        final String TBA_CATEGORY = "tba:category";
        final String COLUMNS = "jcr:title (STRING), jcr:description (STRING), tba:systemName (STRING)";

        Map<String, Object> categoriesIndexConfigurationMap = new HashMap<>();
        categoriesIndexConfigurationMap.put("kind", TEXT);
        categoriesIndexConfigurationMap.put("provider", ELASTIC_SEARCH);
        categoriesIndexConfigurationMap.put("synchronous", false);
        categoriesIndexConfigurationMap.put("nodeType", TBA_CATEGORY);
        categoriesIndexConfigurationMap.put("columns", COLUMNS);
        return categoriesIndexConfigurationMap;
    }

    private Map<String, Object> getFeedsIndexConfiguration() {
        final String TEXT = "text";
        final String TBA_FEED_SUMMARY = "tba:feedSummary";
        final String COLUMNS = "jcr:title (STRING), jcr:description (STRING), tba:tags (STRING), tba:category(STRING), tba:systemName (STRING)";

        Map<String, Object> feedsIndexConfigurationMap = new HashMap<>();
        feedsIndexConfigurationMap.put("kind", TEXT);
        feedsIndexConfigurationMap.put("provider", ELASTIC_SEARCH);
        feedsIndexConfigurationMap.put("synchronous", false);
        feedsIndexConfigurationMap.put("nodeType", TBA_FEED_SUMMARY);
        feedsIndexConfigurationMap.put("columns", COLUMNS);
        return feedsIndexConfigurationMap;
    }

    private Map<String, Object> getElasticSearchIndexProviderConfiguration() {
        final String ES_PROVIDER_CLASS = "org.modeshape.jcr.index.elasticsearch.EsIndexProvider";

        Map<String, Object> elasticSearchIndexProviderConfigurationMap = new HashMap<>();
        elasticSearchIndexProviderConfigurationMap.put("classname", ES_PROVIDER_CLASS);
        elasticSearchIndexProviderConfigurationMap.put("host", clientConfig.getHost());
        elasticSearchIndexProviderConfigurationMap.put("port", clientConfig.getRestPort());
        return elasticSearchIndexProviderConfigurationMap;
    }
}
