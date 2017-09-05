/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinitionProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.domaintype.DomainTypeProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasourceDefinitionProvider;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasourceProvider;
import com.thinkbiganalytics.metadata.modeshape.domaintype.JcrDomainTypeProvider;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.op.JobRepoFeedOperationsProvider;
import com.thinkbiganalytics.metadata.modeshape.service.JcrIndexService;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrFeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.modeshape.tag.TagProvider;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplateProvider;
import com.thinkbiganalytics.metadata.modeshape.user.JcrUserProvider;
import com.thinkbiganalytics.search.api.Search;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.ObservationManager;

/**
 *
 */
@Configuration
public class MetadataJcrConfig {

    @Bean
    public UserProvider userProvider() {
        // TODO consider moving this to its own configuration, and perhaps the whole user management 
        // to a separate module than the metadata one.
        return new JcrUserProvider();
    }

    @Bean
    public ExtensibleTypeProvider extensibleTypeProvider() {
        return new JcrExtensibleTypeProvider();
    }

    @Bean
    public ExtensibleEntityProvider extensibleEntityProvider() {
        return new JcrExtensibleEntityProvider();
    }

    @Bean
    public CategoryProvider categoryProvider() {
        return new JcrCategoryProvider();
    }

    @Bean
    public FeedProvider feedProvider() {
        return new JcrFeedProvider();
    }

    @Bean
    public FeedOperationsProvider feedOperationsProvider() {
        return new JobRepoFeedOperationsProvider();
    }

    @Bean
    public TagProvider tagProvider() {
        return new TagProvider();
    }

    @Bean
    public DatasourceProvider datasourceProvider() {
        return new JcrDatasourceProvider();
    }

    @Bean
    public CategoryProvider feedManagerCategoryProvider() {
        return new JcrCategoryProvider();
    }

    @Bean
    public FeedManagerTemplateProvider feedManagerTemplateProvider() {
        return new JcrFeedTemplateProvider();
    }

    @Bean
    public DatasourceDefinitionProvider datasourceDefinitionProvider() {
        return new JcrDatasourceDefinitionProvider();
    }

//    @Bean
//    public FeedProvider feedProvider() {
//        return new InMemoryFeedProvider();
//    }
//
//    @Bean
//    public DatasourceProvider datasetProvider() {
//        return new InMemoryDatasourceProvider();
//    }

    @Bean
    public JcrServiceLevelAgreementProvider slaProvider() {
        return new JcrServiceLevelAgreementProvider();
    }

    @Bean
    public FeedServiceLevelAgreementProvider jcrFeedSlaProvider() {
        return new JcrFeedServiceLevelAgreementProvider();
    }

    @Bean
    public JcrMetadataAccess metadataAccess() {
        return new JcrMetadataAccess();
    }

    @Bean(initMethod = "configure")
    public MetadataJcrConfigurator jcrConfigurator(List<PostMetadataConfigAction> postConfigActions) {
        return new MetadataJcrConfigurator(postConfigActions);
    }

    /**
     * Guarantees that at least one action exists, otherwise the list injection above will fail.
     */
    @Bean
    protected PostMetadataConfigAction dummyAction() {
        return new PostMetadataConfigAction() {
            @Override
            public void run() {
                // Do nothing.
            }
        };
    }

    @Bean
    public DomainTypeProvider domainTypeProvider() {
        return new JcrDomainTypeProvider();
    }

    @Bean
    @ConditionalOnProperty(prefix="config", value="search.engine")
    public JcrIndexService indexService(final Search search, final DatasourceProvider datasourceProvider, final MetadataAccess metadataAccess, final Repository repository) {
        final JcrIndexService indexService = new JcrIndexService(search, datasourceProvider, metadataAccess);
        try {
            final ObservationManager observationManager = repository.login().getWorkspace().getObservationManager();
            observationManager.addEventListener(indexService,
                                                Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED,
                                                EntityUtil.pathForDerivedDatasource(),
                                                true,
                                                null,
                                                null,
                                                false);
        } catch (final RepositoryException e) {
            throw new MetadataRepositoryException("Failed to register index service: " + e, e);
        }
        return indexService;
    }
}
