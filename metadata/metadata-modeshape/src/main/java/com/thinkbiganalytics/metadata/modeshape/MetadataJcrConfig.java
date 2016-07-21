/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.core.op.InMemoryDataOperationsProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrFeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasourceProvider;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.op.JobRepoFeedOperationsProvider;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.modeshape.tag.TagProvider;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplateProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class MetadataJcrConfig {
    
    @PostConstruct
    public void initializeMetadata() {
        jcrConfigurator().configure();
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
    public FeedManagerCategoryProvider feedManagerCategoryProvider() {
        return new JcrFeedManagerCategoryProvider();
    }

    @Bean
    public FeedManagerFeedProvider feedManagerFeedProvider(){
        return new JcrFeedManagerFeedProvider();
    }

    @Bean
    public FeedManagerTemplateProvider feedManagerTemplateProvider(){
        return new JcrFeedTemplateProvider();
    }

    @Bean
    public DataOperationsProvider dataOperationsProvider() {
        return new InMemoryDataOperationsProvider();
    }


    @Bean
    public JcrSearchProvider searchProvider() {
        return new JcrSearchProvider();
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
    public JcrMetadataAccess metadataAccess() {
        return new JcrMetadataAccess();
    }
    
    @Bean
    public MetadataJcrConfigurator jcrConfigurator() {
        return new MetadataJcrConfigurator();
    }

    
}
