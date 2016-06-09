/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.generic.GenericEntityProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrFeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasourceProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.generic.JcrGenericEntityProvider;
import com.thinkbiganalytics.metadata.modeshape.tag.TagProvider;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplateProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class JcrProviderConfig {
    
    @PostConstruct
    public void initializeMetadata() {
        // TODO: Delegate this and other setup behavior to a JCR metadata configurator os some kind.
        metadataAccess().commit(new Command<String>() {
            @Override
            public String execute() {
                try {
                    Session session = JcrMetadataAccess.getActiveSession();
                    Node metadataNode = session.getRootNode().addNode("metadata", "tba:metadataFolder");

                    return metadataNode.getPath();
                } catch (RepositoryException e) {
                    throw new MetadataRepositoryException("Could not create initial JCR metadata", e);
                }
            }
        });
    }                
    


    @Bean
    public GenericEntityProvider genericEntitiyProvider() {
        return new JcrGenericEntityProvider();
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




//    @Bean
//    public FeedProvider feedProvider() {
//        return new InMemoryFeedProvider();
//    }
//
//    @Bean
//    public DatasourceProvider datasetProvider() {
//        return new InMemoryDatasourceProvider();
//    }
//    
//    @Bean
//    public DataOperationsProvider dataOperationsProvider() {
//        return new InMemoryDataOperationsProvider();
//    }
//    
//    @Bean
//    public ServiceLevelAgreementProvider slaProvider() {
//        return new InMemorySLAProvider();
//    }
    
    @Bean
    public MetadataAccess metadataAccess() {
        return new JcrMetadataAccess();
    }
    
}
