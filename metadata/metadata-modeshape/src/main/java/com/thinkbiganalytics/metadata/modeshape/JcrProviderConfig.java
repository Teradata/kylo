/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import javax.annotation.PostConstruct;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.generic.GenericEntityProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.core.dataset.InMemoryDatasourceProvider;
import com.thinkbiganalytics.metadata.core.feed.InMemoryFeedProvider;
import com.thinkbiganalytics.metadata.core.op.InMemoryDataOperationsProvider;
import com.thinkbiganalytics.metadata.modeshape.generic.JcrGenericEntityProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class JcrProviderConfig {
    
    @PostConstruct
    public void initializeMetadata() {
        metadataAccess().commit(new Command<String>() {
            @Override
            public String execute() {
                Session session = JcrMetadataAccess.getActiveSession();
                try {
                    NamespaceRegistry nsReg = session.getWorkspace().getNamespaceRegistry();
                    nsReg.registerNamespace(JcrMetadataAccess.META_PREFIX, "http://thinkbiganalytics.com/metadata");
                    
                    if (! session.nodeExists("/generic")) {
                        session.getRootNode().addNode("generic");
                    }
                } catch (RepositoryException e) {
                    throw new MetadataRepositoryException("Could not create initial JCR metadata", e);
                }
                
                return null;
            }
        });
    }

    @Bean
    public GenericEntityProvider genericEntitiyProvider() {
        return new JcrGenericEntityProvider();
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
