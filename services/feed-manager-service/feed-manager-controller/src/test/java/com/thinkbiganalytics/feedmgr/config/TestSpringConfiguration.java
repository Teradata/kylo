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
