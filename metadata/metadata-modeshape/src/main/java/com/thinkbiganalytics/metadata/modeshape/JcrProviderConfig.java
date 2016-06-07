/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.generic.GenericEntityProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.generic.JcrGenericEntityProvider;

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
