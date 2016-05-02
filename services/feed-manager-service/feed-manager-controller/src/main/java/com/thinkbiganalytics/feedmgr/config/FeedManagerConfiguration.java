package com.thinkbiganalytics.feedmgr.config;


import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.thinkbiganalytics.feedmgr.service.FeedManagerCategoryProvider;
import com.thinkbiganalytics.feedmgr.service.FeedManagerFeedProvider;
import com.thinkbiganalytics.feedmgr.service.FeedManagerTemplateProvider;
import com.thinkbiganalytics.feedmgr.service.InMemoryFeedManagerCategoryProvider;
import com.thinkbiganalytics.feedmgr.service.InMemoryFeedManagerFeedProvider;
import com.thinkbiganalytics.feedmgr.service.InMemoryFeedManagerTemplateProvider;
import com.thinkbiganalytics.feedmgr.service.InMemoryMetadataService;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.metadata.rest.client.MetadataClient;

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



    @Bean(name="metadataClient")
    public MetadataClient metadataClient(){
        return new MetadataClient(URI.create("http://localhost:8077/api/metadata/"));
    }


    @Bean
    public FeedManagerFeedProvider feedManagerFeedProvider(){
        return new InMemoryFeedManagerFeedProvider();
    }

    @Bean
    public FeedManagerCategoryProvider feedManagerCategoryProvider(){
        return new InMemoryFeedManagerCategoryProvider();
    }


    @Bean
    public FeedManagerTemplateProvider feedManagerTemplateProvider(){
        return new InMemoryFeedManagerTemplateProvider();
    }




    @Bean
    public MetadataService metadataService(){
        return new InMemoryMetadataService();
    }




}
