package com.thinkbiganalytics.search;

/*-
 * #%L
 * kylo-search-elasticsearch-rest
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
import com.thinkbiganalytics.search.config.ElasticSearchRestClientConfiguration;

import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.schematic.document.EditableDocument;
import org.modeshape.schematic.document.Editor;
import org.modeshape.schematic.document.ParsingException;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * ModeShape configuration for Elasticsearch rest client integration
 */
// would be good to integrate this class with configuration for es transport client (avoid code repetition)
public class ElasticSearchRestModeShapeConfigurationService implements RepositoryIndexConfiguration {

    private final ElasticSearchRestClientConfiguration restClientConfig;
    private static final String ELASTIC_SEARCH = "elasticsearch";

    public ElasticSearchRestModeShapeConfigurationService(ElasticSearchRestClientConfiguration config) {
        this.restClientConfig = config;
    }

    @Override
    public RepositoryConfiguration build() {
        RepositoryConfiguration repositoryConfiguration;

        final String EMPTY_CONFIG = "{}";
        final String KYLO_CATEGORIES = "kylo-categories";
        final String KYLO_FEEDS = "kylo-feeds";
        final String KYLO_INTERNAL_FD1 = "kylo-internal-fd1";
        final String INDEXES = "indexes";
        final String INDEX_PROVIDERS = "indexProviders";

        try {
            repositoryConfiguration = RepositoryConfiguration.read(EMPTY_CONFIG);
        } catch (ParsingException | FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        Editor editor = repositoryConfiguration.edit();
        EditableDocument indexesDocument = editor.getOrCreateDocument(INDEXES);

        EditableDocument categoriesIndexDocument = indexesDocument.getOrCreateDocument(KYLO_CATEGORIES);
        EditableDocument feedsIndexDocument = indexesDocument.getOrCreateDocument(KYLO_FEEDS);
        EditableDocument feeds2IndexDocument = indexesDocument.getOrCreateDocument(KYLO_INTERNAL_FD1);
        categoriesIndexDocument.putAll(getCategoriesIndexConfiguration());
        feedsIndexDocument.putAll(getFeedSummaryIndexConfiguration());
        feeds2IndexDocument.putAll(getFeedDetailsIndexConfiguration());

        EditableDocument indexProvidersDocument = editor.getOrCreateDocument(INDEX_PROVIDERS);
        EditableDocument elasticSearchIndexProviderDocument = indexProvidersDocument.getOrCreateDocument(ELASTIC_SEARCH);
        elasticSearchIndexProviderDocument.putAll(getElasticSearchIndexProviderConfiguration());

        repositoryConfiguration = new RepositoryConfiguration(editor, repositoryConfiguration.getName());
        return repositoryConfiguration;
    }

    private Map<String, Object> getCategoriesIndexConfiguration() {
        final String TEXT = "text";
        final String TBA_CATEGORY = "tba:category";
        final String COLUMNS = "jcr:lastModified (STRING)";

        Map<String, Object> categoriesIndexConfigurationMap = new HashMap<>();
        categoriesIndexConfigurationMap.put("kind", TEXT);
        categoriesIndexConfigurationMap.put("provider", ELASTIC_SEARCH);
        categoriesIndexConfigurationMap.put("synchronous", false);
        categoriesIndexConfigurationMap.put("nodeType", TBA_CATEGORY);
        categoriesIndexConfigurationMap.put("columns", COLUMNS);
        return categoriesIndexConfigurationMap;
    }

    private Map<String, Object> getFeedSummaryIndexConfiguration() {
        final String TEXT = "text";
        final String TBA_FEED_SUMMARY = "tba:feedSummary";
        final String COLUMNS = "jcr:lastModified (STRING)";

        Map<String, Object> feedsIndexConfigurationMap = new HashMap<>();
        feedsIndexConfigurationMap.put("kind", TEXT);
        feedsIndexConfigurationMap.put("provider", ELASTIC_SEARCH);
        feedsIndexConfigurationMap.put("synchronous", false);
        feedsIndexConfigurationMap.put("nodeType", TBA_FEED_SUMMARY);
        feedsIndexConfigurationMap.put("columns", COLUMNS);
        return feedsIndexConfigurationMap;
    }

    private Map<String, Object> getFeedDetailsIndexConfiguration() {
        final String TEXT = "text";
        final String TBA_FEED = "tba:feedDetails";
        final String COLUMNS = "jcr:lastModified (STRING)";

        Map<String, Object> feeds2IndexConfigurationMap = new HashMap<>();
        feeds2IndexConfigurationMap.put("kind", TEXT);
        feeds2IndexConfigurationMap.put("provider", ELASTIC_SEARCH);
        feeds2IndexConfigurationMap.put("synchronous", false);
        feeds2IndexConfigurationMap.put("nodeType", TBA_FEED);
        feeds2IndexConfigurationMap.put("columns", COLUMNS);
        return feeds2IndexConfigurationMap;
    }

    private Map<String, Object> getElasticSearchIndexProviderConfiguration() {
        final String ES_PROVIDER_CLASS = "com.thinkbiganalytics.modeshape.index.elasticsearch.KyloEsIndexProvider";

        Map<String, Object> elasticSearchIndexProviderConfigurationMap = new HashMap<>();
        elasticSearchIndexProviderConfigurationMap.put("classname", ES_PROVIDER_CLASS);
        elasticSearchIndexProviderConfigurationMap.put("host", restClientConfig.getHost());
        elasticSearchIndexProviderConfigurationMap.put("port", restClientConfig.getPort());
        return elasticSearchIndexProviderConfigurationMap;
    }
}
