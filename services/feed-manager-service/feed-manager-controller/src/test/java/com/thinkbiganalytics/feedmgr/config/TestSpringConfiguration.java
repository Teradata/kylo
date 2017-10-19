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

import com.thinkbiganalytics.app.ServicesApplicationStartup;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.JGroupsClusterService;
import com.thinkbiganalytics.common.velocity.service.InMemoryVelocityTemplateProvider;
import com.thinkbiganalytics.common.velocity.service.VelocityTemplateProvider;
import com.thinkbiganalytics.metadata.api.sla.ServiceLevelAgreementActionTemplateProvider;
import com.thinkbiganalytics.nifi.rest.NiFiObjectCache;
import com.thinkbiganalytics.feedmgr.nifi.NifiConnectionService;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCacheClusterManager;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCacheImpl;
import com.thinkbiganalytics.feedmgr.rest.Model;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.category.CategoryModelTransform;
import com.thinkbiganalytics.feedmgr.service.category.FeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.category.InMemoryFeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedModelTransform;
import com.thinkbiganalytics.feedmgr.service.feed.InMemoryFeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.InMemoryFeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.NiFiTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateUtil;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.feedmgr.sla.DefaultServiceLevelAgreementService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementModelTransform;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataAction;
import com.thinkbiganalytics.metadata.api.MetadataCommand;
import com.thinkbiganalytics.metadata.api.MetadataExecutionException;
import com.thinkbiganalytics.metadata.api.MetadataRollbackAction;
import com.thinkbiganalytics.metadata.api.MetadataRollbackCommand;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroupProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.api.sla.ServiceLevelAgreementDescriptionProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.core.dataset.InMemoryDatasourceProvider;
import com.thinkbiganalytics.metadata.core.feed.InMemoryFeedProvider;
import com.thinkbiganalytics.metadata.jpa.cluster.NiFiFlowCacheClusterUpdateProvider;
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
import com.thinkbiganalytics.security.core.encrypt.EncryptionService;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.service.user.UserService;
import com.thinkbiganalytics.spring.SpringEnvironmentProperties;

import org.mockito.Mockito;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.api.txn.TransactionManagerLookup;
import org.springframework.cloud.config.server.encryption.EncryptionController;
import org.springframework.cloud.config.server.encryption.SingleTextEncryptorLocator;
import org.springframework.cloud.config.server.encryption.TextEncryptorLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.encrypt.TextEncryptor;

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
        return new DefaultServiceLevelAgreementService();
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


    @Bean
    RegisteredTemplateUtil registeredTemplateUtil() {
        return new RegisteredTemplateUtil();
    }

    @Bean
    RegisteredTemplateService registeredTemplateService() {
        return new RegisteredTemplateService();
    }

    @Bean
    public NiFiTemplateCache niFiTemplateCache() {
        return new NiFiTemplateCache();
    }

    @Bean
    FeedModelTransform feedModelTransform() {
        return new FeedModelTransform();
    }

    @Bean
    CategoryModelTransform categoryModelTransform() {
        return new CategoryModelTransform();
    }

    @Bean
    CategoryProvider feedManagerCategoryProvider() {
        return new Mockito().mock(CategoryProvider.class);
    }

    @Bean
    FeedManagerTemplateProvider feedManagerTemplateProvider() {
        return new Mockito().mock(FeedManagerTemplateProvider.class);
    }

    @Bean(name = "hiveJdbcTemplate")
    JdbcTemplate hiveJdbcTemplate() {
        return new Mockito().mock(JdbcTemplate.class);
    }

    @Bean(name = "kerberosHiveConfiguration")
    KerberosTicketConfiguration kerberosHiveConfiguration() {
        return new KerberosTicketConfiguration();
    }

    @Bean
    HadoopSecurityGroupProvider hadoopSecurityGroupProvider() {
        return new Mockito().mock(HadoopSecurityGroupProvider.class);
    }

    @Bean
    HiveService hiveService() {
        return new Mockito().mock(HiveService.class);
    }

    @Bean
    TemplateModelTransform templateModelTransform() {
        return new TemplateModelTransform();
    }

    @Bean
    EncryptionService encryptionService() {
        return new EncryptionService();
    }

    @Bean
    TextEncryptor textEncryptor(){
        return textEncryptorLocator().locate(null);
    }

    @Bean
    TextEncryptorLocator textEncryptorLocator() {
        return new SingleTextEncryptorLocator(null);
    }

    @Bean
    EncryptionController encryptionController() {
        return new EncryptionController(textEncryptorLocator());
    }

    @Bean
    ServiceLevelAgreementModelTransform serviceLevelAgreementModelTransform() {
        return new ServiceLevelAgreementModelTransform(Mockito.mock(Model.class));
    }

    @Bean
    SecurityModelTransform actionsTransform() {
        return Mockito.mock(SecurityModelTransform.class);
    }

    @Bean
    UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    KyloVersionProvider kyloVersionProvider() {
        return Mockito.mock(KyloVersionProvider.class);
    }

    @Bean
    ClusterService clusterService() {
        return new JGroupsClusterService();
    }

    @Bean
    NifiFlowCacheClusterManager nifiFlowCacheClusterManager() {
        return Mockito.mock(NifiFlowCacheClusterManager.class);
    }
    @Bean
    NiFiFlowCacheClusterUpdateProvider niFiFlowCacheClusterUpdateProvider(){
        return Mockito.mock(NiFiFlowCacheClusterUpdateProvider.class);
    }

    @Bean
    ServiceLevelAgreementDescriptionProvider serviceLevelAgreementDescriptionProvider(){
        return Mockito.mock(ServiceLevelAgreementDescriptionProvider.class);
    }


    @Bean
    public NiFiObjectCache createFeedBuilderCache(){
        return new NiFiObjectCache();
    }

    @Bean
    public RegisteredTemplateCache registeredTemplateCache() {
        return new RegisteredTemplateCache();
    }

    @Bean
    public ServicesApplicationStartup servicesApplicationStartup(){
        return Mockito.mock(ServicesApplicationStartup.class);
    }

    @Bean
    public VelocityTemplateProvider velocityTemplateProvider() {
        return new InMemoryVelocityTemplateProvider();
    }

    @Bean
    public ServiceLevelAgreementActionTemplateProvider serviceLevelAgreementActionTemplateProvider() {
        return Mockito.mock(ServiceLevelAgreementActionTemplateProvider.class);
    }

}
