/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import java.io.IOException;

import org.mockito.Mockito;
import org.modeshape.jcr.RepositoryConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.scheduler.JobScheduler;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class JcrTestConfig {
    
    @Bean
    public RepositoryConfiguration metadataRepoConfig() throws IOException {
        ClassPathResource res = new ClassPathResource("/test-metadata-repository.json");
        return RepositoryConfiguration.read(res.getURL());
    }
    
    @Bean
    public FeedOperationsProvider feedOperationsProvider() {
        return Mockito.mock(FeedOperationsProvider.class);
    }

    @Bean
    public JobScheduler jobSchedule() {
        return Mockito.mock(JobScheduler.class);
    }

    @Bean
    public AlertManager alertManager() {
        return Mockito.mock(AlertManager.class);
    }

    @Bean
    public AlertProvider alertProvider() {
        return Mockito.mock(AlertProvider.class);
    }

    @Bean
    public MetadataEventService metadataEventService() {
        return Mockito.mock(MetadataEventService.class);
    }
    

    @Bean(name = "servicesModeShapeLoginConfiguration")
    @Primary
    public LoginConfiguration restModeShapeLoginConfiguration() {
        return Mockito.mock(LoginConfiguration.class);
    }


}
