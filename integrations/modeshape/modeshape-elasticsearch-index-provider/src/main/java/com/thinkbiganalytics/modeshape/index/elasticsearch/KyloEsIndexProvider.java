package com.thinkbiganalytics.modeshape.index.elasticsearch;

/*-
 * #%L
 * kylo-modeshape-elasticsearch-index-provider
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

import com.thinkbiganalytics.spring.SpringApplicationContext;

import org.modeshape.jcr.NodeTypes;
import org.modeshape.jcr.api.index.IndexDefinition;
import org.modeshape.jcr.cache.change.ChangeSetAdapter;
import org.modeshape.jcr.index.elasticsearch.EsIndexProvider;
import org.modeshape.jcr.index.elasticsearch.EsManagedIndexBuilder;
import org.modeshape.jcr.spi.index.provider.ManagedIndexBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Kylo's extension of modeshape's default index provider for Elasticsearch integration
 */
public class KyloEsIndexProvider extends EsIndexProvider {

    private static final Logger log = LoggerFactory.getLogger(KyloEsIndexProvider.class);
    private String host;
    private int port;
    private KyloEsClient kyloClient;

    @Override
    protected void doInitialize() throws RepositoryException {
        log.debug("Using KyloEsIndexProvider");
        //Params are set here:
        //com.thinkbiganalytics.search.ElasticSearchRestModeShapeConfigurationService#getElasticSearchIndexProviderConfiguration()
        kyloClient = new KyloEsClient(host, port);
        SpringApplicationContext.autowire(kyloClient);
        log.info("Initialized Elasticsearch provider for ModeShape");
    }

    @Override
    protected ManagedIndexBuilder getIndexBuilder(IndexDefinition defn,
                                                  String workspaceName,
                                                  NodeTypes.Supplier nodeTypesSupplier,
                                                  ChangeSetAdapter.NodeTypePredicate matcher) {
        return EsManagedIndexBuilder.create(kyloClient, context(), defn, nodeTypesSupplier, workspaceName, matcher);
    }
}
