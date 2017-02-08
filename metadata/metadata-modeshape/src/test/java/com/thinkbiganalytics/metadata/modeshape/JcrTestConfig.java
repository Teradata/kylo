/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape;

/*-
 * #%L
 * thinkbig-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.scheduler.JobScheduler;

import org.mockito.Mockito;
import org.modeshape.jcr.RepositoryConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 *
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
