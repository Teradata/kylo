package com.thinkbiganalytics.feedmgr.config;

import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.SpringEnvironmentProperties;
import com.thinkbiganalytics.feedmgr.service.category.FeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.category.InMemoryFeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.feed.InMemoryFeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.InMemoryFeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.core.dataset.InMemoryDatasourceProvider;
import com.thinkbiganalytics.metadata.core.feed.InMemoryFeedProvider;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementScheduler;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 7/21/16.
 */
@Configuration
public class TestSpringConfiguration {

    @Bean
    public ServiceLevelAgreementService serviceLevelAgreementService() {
        return new ServiceLevelAgreementService();
    }

    @Bean
    public ServiceLevelAgreementProvider serviceLevelAgreementProvider() {
        return new InMemorySLAProvider();
    }

    @Bean
    public ServiceLevelAgreementScheduler serviceLevelAgreementScheduler() {
        return new ServiceLevelAgreementScheduler() {
            @Override
            public void scheduleServiceLevelAgreement(ServiceLevelAgreement sla) {

            }
        };
    }

    @Bean
    FeedProvider feedProvider() {
        return new InMemoryFeedProvider();
    }

    @Bean
    MetadataAccess metadataAccess() {
        // Transaction behavior not enforced in memory-only mode;
        return new MetadataAccess() {
            @Override
            public <R> R commit(Command<R> cmd) {
                return cmd.execute();
            }

            @Override
            public <R> R read(Command<R> cmd) {
                return cmd.execute();
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
    public NifiRestClient nifiRestClient() {
        return new NifiRestClient(nifiRestClientConfig());
    }


}
