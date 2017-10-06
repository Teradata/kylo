package com.thinkbiganalytics.feedmgr.config;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.feedmgr.nifi.NifiConnectionService;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.SpringEnvironmentProperties;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCacheImpl;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.category.FeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.category.InMemoryFeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.feed.InMemoryFeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.InMemoryFeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataAction;
import com.thinkbiganalytics.metadata.api.MetadataCommand;
import com.thinkbiganalytics.metadata.api.MetadataExecutionException;
import com.thinkbiganalytics.metadata.api.MetadataRollbackAction;
import com.thinkbiganalytics.metadata.api.MetadataRollbackCommand;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.core.dataset.InMemoryDatasourceProvider;
import com.thinkbiganalytics.metadata.core.feed.InMemoryFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataJcrConfigurator;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementScheduler;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.v1.rest.client.NiFiRestClientV1;
import com.thinkbiganalytics.nifi.v1.rest.model.NiFiPropertyDescriptorTransformV1;
import com.thinkbiganalytics.security.AccessController;

import org.mockito.Mockito;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.api.txn.TransactionManagerLookup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.security.Principal;
import java.util.Properties;

import javax.jcr.Credentials;
import javax.jcr.Repository;

/**
 */
@Configuration
public class TestSpringConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() throws Exception {
        final PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        Properties properties = new Properties();
        properties.setProperty("nifi.remove.inactive.versioned.feeds", "true");
        propertySourcesPlaceholderConfigurer.setProperties(properties);
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public AccessController accessController() {
        return Mockito.mock(AccessController.class);
    }

    @Bean
    public FeedServiceLevelAgreementProvider feedServiceLevelAgreementProvider() {
        return Mockito.mock(FeedServiceLevelAgreementProvider.class);
    }

    @Bean
    public ServiceLevelAgreementService serviceLevelAgreementService() {
        return new ServiceLevelAgreementService();
    }

    @Bean
    public ServiceLevelAgreementProvider serviceLevelAgreementProvider() {
        return new InMemorySLAProvider();
    }

    @Bean
    public NifiFlowCache nifiFlowCache() {
        return new NifiFlowCacheImpl();
    }

    @Bean
    public ModeShapeEngine modeShapeEngine() {
        return Mockito.mock(ModeShapeEngine.class);
    }

    @Bean
    public FeedManagerFeedProvider feedManagerFeedProvider() {
        return Mockito.mock(FeedManagerFeedProvider.class);
    }

    @Bean
    public MetadataJcrConfigurator metadataJcrConfigurator() {
        return Mockito.mock(MetadataJcrConfigurator.class);
    }

    @Bean
    public MetadataService metadataService() {
        return Mockito.mock(MetadataService.class);
    }

    @Bean
    public NifiConnectionService nifiConnectionService() {
        return new NifiConnectionService();
    }

    @Bean
    public ServiceLevelAgreementScheduler serviceLevelAgreementScheduler() {
        return new ServiceLevelAgreementScheduler() {
            @Override
            public void scheduleServiceLevelAgreement(ServiceLevelAgreement sla) {

            }

            @Override
            public void enableServiceLevelAgreement(ServiceLevelAgreement sla) {

            }

            @Override
            public void disableServiceLevelAgreement(ServiceLevelAgreement sla) {

            }

            @Override
            public boolean unscheduleServiceLevelAgreement(ServiceLevelAgreement.ID slaId) {
                return false;
            }

            @Override
            public boolean unscheduleServiceLevelAgreement(ServiceLevelAgreement sla) {
                return false;
            }
        };
    }

    @Bean
    FeedProvider feedProvider() {
        return new InMemoryFeedProvider();
    }

    @Bean(name = "metadataJcrRepository")
    public Repository repository() {
        return Mockito.mock(Repository.class);
    }

    ;

    @Bean
    public TransactionManagerLookup txnLookup() {
        return Mockito.mock(TransactionManagerLookup.class);
    }

    @Bean
    public JcrMetadataAccess jcrMetadataAccess() {
        // Transaction behavior not enforced in memory-only mode;
        return new JcrMetadataAccess() {
            @Override
            public <R> R commit(MetadataCommand<R> cmd, Principal... principals) {
                try {
                    return cmd.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }

            @Override
            public <R> R read(MetadataCommand<R> cmd, Principal... principals) {
                try {
                    return cmd.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }

            @Override
            public <R> R commit(Credentials creds, MetadataCommand<R> cmd) {
                try {
                    return cmd.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }

            @Override
            public <R> R commit(MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd, Principal... principals) {
                return commit(cmd, principals);
            }

            @Override
            public void commit(MetadataAction action, MetadataRollbackAction rollbackAction, Principal... principals) {
                commit(action, principals);
            }

            @Override
            public <R> R read(Credentials creds, MetadataCommand<R> cmd) {
                try {
                    return cmd.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }
        };
    }

    @Bean
    MetadataAccess metadataAccess() {
        // Transaction behavior not enforced in memory-only mode;
        return new MetadataAccess() {
            @Override
            public <R> R commit(MetadataCommand<R> cmd, Principal... principals) {
                try {
                    return cmd.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }

            @Override
            public <R> R read(MetadataCommand<R> cmd, Principal... principals) {
                try {
                    return cmd.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }

            @Override
            public void commit(MetadataAction action, Principal... principals) {
                try {
                    action.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }

            @Override
            public <R> R commit(MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd, Principal... principals) {
                return commit(cmd, principals);
            }

            @Override
            public void commit(MetadataAction action, MetadataRollbackAction rollbackAction, Principal... principals) {
                commit(action, principals);
            }

            @Override
            public void read(MetadataAction cmd, Principal... principals) {
                try {
                    cmd.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }
        };
    }

    @Bean
    public DatasourceProvider datasetProvider() {
        return new InMemoryDatasourceProvider();
    }

    @Bean
    public FeedManagerFeedService feedManagerFeedService() {
        return new InMemoryFeedManagerFeedService();
    }

    @Bean
    public FeedManagerCategoryService feedManagerCategoryService() {
        return new InMemoryFeedManagerCategoryService();
    }

    @Bean
    FeedManagerTemplateService feedManagerTemplateService() {
        return new InMemoryFeedManagerTemplateService();
    }

    @Bean
    NifiRestClientConfig nifiRestClientConfig() {
        return new NifiRestClientConfig();
    }

    @Bean
    PropertyExpressionResolver propertyExpressionResolver() {
        return new PropertyExpressionResolver();
    }

    @Bean
    SpringEnvironmentProperties springEnvironmentProperties() {
        return new SpringEnvironmentProperties();
    }

    @Bean
    public LegacyNifiRestClient legacyNifiRestClient() {
        return new LegacyNifiRestClient();
    }

    @Bean
    NiFiRestClient niFiRestClient() {
        return new NiFiRestClientV1(nifiRestClientConfig());
    }

    @Bean
    NiFiPropertyDescriptorTransform propertyDescriptorTransform() {
        return new NiFiPropertyDescriptorTransformV1();
    }
}
