package com.thinkbiganalytics.feedmgr.config;


import java.net.URI;

import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.service.ExportImportTemplateService;
import com.thinkbiganalytics.feedmgr.service.category.JpaFeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerPreconditionService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedModelTransformer;
import com.thinkbiganalytics.feedmgr.service.feed.JpaFeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.template.JpaFeedManagerTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.thinkbiganalytics.feedmgr.nifi.SpringEnvironmentProperties;
import com.thinkbiganalytics.feedmgr.service.category.FeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.feedmgr.service.FeedManagerMetadataService;
import com.thinkbiganalytics.feedmgr.service.MetadataService;

/**
 * Created by sr186054 on 2/26/16.
 */
@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages={"com.thinkbiganalytics"})
public class FeedManagerConfiguration {

    public FeedManagerConfiguration(){
    }

    @Autowired
    private Environment env;


    @Bean
    public FeedManagerFeedService feedManagerFeedService(){
        return new JpaFeedManagerFeedService();
    }

    @Bean
    public FeedManagerCategoryService feedManagerCategoryService(){
        return new JpaFeedManagerCategoryService();
    }


    @Bean
    public FeedManagerTemplateService feedManagerTemplateService(){
        return new JpaFeedManagerTemplateService();
    }

    @Bean
    public FeedModelTransformer feedModelTransformer(){
        return new FeedModelTransformer();
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
    public MetadataService metadataService(){
        return new FeedManagerMetadataService();
    }

    @Bean
    public ExportImportTemplateService exportImportTemplateService() {
        return new ExportImportTemplateService();
    }

    @Bean
    public PropertyExpressionResolver propertyExpressionResolver(){
        return new PropertyExpressionResolver();
    }

}
