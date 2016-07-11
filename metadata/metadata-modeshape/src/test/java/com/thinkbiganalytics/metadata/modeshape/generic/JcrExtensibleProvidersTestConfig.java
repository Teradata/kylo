/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.generic;

import java.io.IOException;

import org.mockito.Mockito;
import org.modeshape.jcr.RepositoryConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class JcrExtensibleProvidersTestConfig {
    
    @Bean
    public RepositoryConfiguration metadataRepoConfig() throws IOException {
        ClassPathResource res = new ClassPathResource("/test-metadata-repository.json");
        return RepositoryConfiguration.read(res.getURL());
    }
    
    @Bean
    public FeedOperationsProvider feedOperationsProvider() {
        return Mockito.mock(FeedOperationsProvider.class);
    }

}
