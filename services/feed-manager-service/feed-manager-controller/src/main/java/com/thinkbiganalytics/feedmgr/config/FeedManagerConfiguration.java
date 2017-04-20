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


import com.thinkbiganalytics.es.ElasticSearch;
import com.thinkbiganalytics.es.ElasticSearchClientConfig;
import com.thinkbiganalytics.feedmgr.nifi.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.SpringCloudContextEnvironmentChangedListener;
import com.thinkbiganalytics.feedmgr.nifi.SpringEnvironmentProperties;
import com.thinkbiganalytics.feedmgr.rest.Model;
import com.thinkbiganalytics.feedmgr.service.AccessControlledEntityTransform;
import com.thinkbiganalytics.feedmgr.service.DefaultJobService;
import com.thinkbiganalytics.feedmgr.service.EncryptionService;
import com.thinkbiganalytics.feedmgr.service.FeedManagerMetadataService;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.category.CategoryModelTransform;
import com.thinkbiganalytics.feedmgr.service.category.DefaultFeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.category.FeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceService;
import com.thinkbiganalytics.feedmgr.service.feed.DefaultFeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.feed.ExportImportFeedService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerPreconditionService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedModelTransform;
import com.thinkbiganalytics.feedmgr.service.feed.datasource.DerivedDatasourceFactory;
import com.thinkbiganalytics.feedmgr.service.security.DefaultSecurityService;
import com.thinkbiganalytics.feedmgr.service.security.SecurityService;
import com.thinkbiganalytics.feedmgr.service.template.DefaultFeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.ExportImportTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateUtil;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementModelTransform;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.jobrepo.service.JobService;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.core.feed.FeedPreconditionService;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Spring Bean configuration for feed manager
 */
@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
public class FeedManagerConfiguration {

    @Inject
    private Environment env;

    public FeedManagerConfiguration() {
    }

    @Bean
    public FeedManagerFeedService feedManagerFeedService() {
        return new DefaultFeedManagerFeedService();
    }

    @Bean
    public FeedManagerCategoryService feedManagerCategoryService() {
        return new DefaultFeedManagerCategoryService();
    }


    @Bean
    public FeedManagerTemplateService feedManagerTemplateService() {
        return new DefaultFeedManagerTemplateService();
    }

    @Bean
    public FeedModelTransform feedModelTransformer() {
        return new FeedModelTransform();
    }


    @Bean
    public TemplateModelTransform templateModelTransform() {
        return new TemplateModelTransform();
    }


    @Bean
    public CategoryModelTransform categoryModelTransform() {
        return new CategoryModelTransform();
    }


    @Bean
    public FeedManagerPreconditionService feedManagerPreconditionService() {
        return new FeedManagerPreconditionService();
    }

    @Bean
    public SpringEnvironmentProperties springEnvironmentProperties() {
        return new SpringEnvironmentProperties();
    }

    @Bean
    public SpringCloudContextEnvironmentChangedListener springEnvironmentChangedListener() {
        return new SpringCloudContextEnvironmentChangedListener();
    }

    @Bean
    public MetadataService metadataService() {
        return new FeedManagerMetadataService();
    }

    @Bean
    public SecurityService securityService() {
        return new DefaultSecurityService();
    }

    @Bean
    public ExportImportTemplateService exportImportTemplateService() {
        return new ExportImportTemplateService();
    }

    @Bean
    public ExportImportFeedService exportImportFeedService() {
        return new ExportImportFeedService();
    }

    @Bean
    public PropertyExpressionResolver propertyExpressionResolver() {
        return new PropertyExpressionResolver();
    }

    @Bean
    public NifiFlowCache nifiFlowCache() {
        return new NifiFlowCache();
    }


    @Bean
    public ServiceLevelAgreementService serviceLevelAgreementService() {
        return new ServiceLevelAgreementService();
    }


    @Bean(name = "elasticSearchClientConfig")
    public ElasticSearchClientConfig elasticSearchClientConfig() {
        String host = env.getProperty("elasticsearch.host");
        String configPort = env.getProperty("elasticsearch.port");
        String clusterName = env.getProperty("elasticsearch.clustername");
        Integer port = null;
        try {
            port = Integer.parseInt(configPort);
        } catch (NumberFormatException e) {
            Assert.notNull(port,
                           "The Elastic Search Port property 'elasticsearch.port' must be configured and must be a valid number in the application.properties file. ");
        }
        Assert.notNull(host,
                       "The Elastic Search Host property: 'elasticsearch.host' must be configured in the application.properties file. ");

        Assert.notNull(clusterName,
                       "The Elastic Search cluster property: 'elasticsearch.clustername' must be configured in the application.properties file. ");

        ElasticSearchClientConfig config = new ElasticSearchClientConfig();
        config.setHost(host);
        config.setPort(port);
        config.setClusterName(clusterName);
        return config;

    }


    @Bean
    public ElasticSearch elasticSearch() {
        return new ElasticSearch(elasticSearchClientConfig());
    }

    @Bean
    public FeedPreconditionService feedPreconditionService() {
        return new FeedPreconditionService();
    }


    @Bean
    public DatasourceService datasourceService() {
        return new DatasourceService();
    }

    @Bean
    public DerivedDatasourceFactory derivedDatasourceFactory() {
        return new DerivedDatasourceFactory();
    }

    @Bean
    public JobService jobService() {
        return new DefaultJobService();
    }

    @Bean
    public EncryptionService encryptionService() {
        return new EncryptionService();
    }

    @Bean
    public RegisteredTemplateService registeredTemplateService() {
        return new RegisteredTemplateService();
    }

    @Bean
    public RegisteredTemplateUtil registeredTemplateUtil() {
        return new RegisteredTemplateUtil();
    }

    @Bean
    public UploadProgressService uploadProgressService() {
        return new UploadProgressService();
    }

    /**
     * Transforms objects between {@link com.thinkbiganalytics.metadata.rest.model.data.Datasource} and {@link com.thinkbiganalytics.metadata.api.datasource.Datasource}.
     *
     * @param datasourceProvider the {@link com.thinkbiganalytics.metadata.api.datasource.Datasource} provider
     * @param textEncryptor      the encryption provider
     * @param niFiRestClient     the NiFi REST client
     * @param securityService    the security service
     * @return the model transformer
     */
    @Bean
    @Nonnull
    public DatasourceModelTransform datasourceModelTransform(@Nonnull final DatasourceProvider datasourceProvider, @Nonnull final TextEncryptor textEncryptor,
                                                             @Nonnull final NiFiRestClient niFiRestClient, @Nonnull final SecurityService securityService) {
        return new DatasourceModelTransform(datasourceProvider, textEncryptor, niFiRestClient, securityService);
    }

    /**
     * Transforms objects between different feed models and domain objects.
     *
     * @param datasourceTransform the {@code Datasource} object transformer
     * @return the model transformer
     */
    @Bean
    @Nonnull
    public Model model(@Nonnull final DatasourceModelTransform datasourceTransform) {
        return new Model(datasourceTransform);
    }

    /**
     * Transforms SLA objects between the REST model and the domain object.
     *
     * @param model the model transformer
     * @return the SLA transformer
     */
    @Bean
    @Nonnull
    public ServiceLevelAgreementModelTransform serviceLevelAgreementModelTransform(@Nonnull final Model model) {
        return new ServiceLevelAgreementModelTransform(model);
    }

    @Bean
    AccessControlledEntityTransform accessControlledEntityTransform() {
        return new AccessControlledEntityTransform();
    }
}
