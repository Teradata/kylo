/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import org.springframework.context.annotation.Bean;

import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasourceProvider;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.tag.TagProvider;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplateProvider;

/**
 * Defines the beans used by JcrPropertyTest which override the mocks of JcrTestConfig.
 */
public class JcrPropertyTestConfig {
    
    @Bean
    public CategoryProvider categoryProvider() {
        return new JcrCategoryProvider();
    }
    
    @Bean
    public DatasourceProvider datasourceProvider() {
        return new JcrDatasourceProvider();
    }
    
    @Bean
    public FeedProvider feedProvider() {
        return new JcrFeedProvider();
    }
    
    @Bean
    public FeedManagerTemplateProvider feedManagerTemplateProvider() {
        return new JcrFeedTemplateProvider();
    }
    
    @Bean
    public TagProvider tagProvider() {
        return new TagProvider();
    }
    
    @Bean
    public ExtensibleTypeProvider provider() {
        return new JcrExtensibleTypeProvider();
    }
    
    @Bean
    public JcrMetadataAccess metadata() {
        return new JcrMetadataAccess();
    }

}
