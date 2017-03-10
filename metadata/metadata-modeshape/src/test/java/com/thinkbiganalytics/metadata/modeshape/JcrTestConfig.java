/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

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

import org.mockito.Mockito;
import org.modeshape.jcr.RepositoryConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.auth.jaas.LoginConfiguration;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinitionProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedEntityActionsProvider;
import com.thinkbiganalytics.metadata.modeshape.tag.TagProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.security.AccessController;

/**
 * Defines mocks for most JCR-based providers and other components, and configures a ModeShape test repository.
 */
@Configuration
public class JcrTestConfig {

    @Bean
    public RepositoryConfiguration metadataRepoConfig() throws IOException {
        ClassPathResource res = new ClassPathResource("/test-metadata-repository.json");
        return RepositoryConfiguration.read(res.getURL());
    }

    @Bean(name = "servicesModeShapeLoginConfiguration")
    @Primary
    public LoginConfiguration restModeShapeLoginConfiguration() {
        return Mockito.mock(LoginConfiguration.class);
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
    
    @Bean
    public CategoryProvider categoryProvider() {
        return Mockito.mock(CategoryProvider.class);
    }

    @Bean
    public ServiceLevelAgreementProvider slaProvider() {
        return Mockito.mock(ServiceLevelAgreementProvider.class);
    }

    @Bean
    public DatasourceProvider datasourceProvider() {
        return Mockito.mock(DatasourceProvider.class);
    }

    @Bean
    public ExtensibleTypeProvider extensibleTypeProvider() {
        return Mockito.mock(ExtensibleTypeProvider.class);
    }

    @Bean
    public AccessController accessController() {
        return Mockito.mock(AccessController.class);
    }
    
    @Bean
    public JcrAllowedEntityActionsProvider allowedEntityActionsProvider() {
        
        
        JcrAllowedEntityActionsProvider provider = Mockito.mock(JcrAllowedEntityActionsProvider.class);
        when(provider.getAllowedActions(any(String.class))).thenReturn(Optional.empty());
        when(provider.getAvailableActions(any(String.class))).thenReturn(Optional.empty());
        return provider;
    }

    @Bean
    public UserProvider userProvider() {
        return Mockito.mock(UserProvider.class);
    }

    @Bean
    public ExtensibleEntityProvider extensibleEntityProvider() {
        return Mockito.mock(ExtensibleEntityProvider.class);
    }

    @Bean
    public FeedProvider feedProvider() {
        return Mockito.mock(FeedProvider.class);
    }

    @Bean
    public TagProvider tagProvider() {
        return Mockito.mock(TagProvider.class);
    }

    @Bean
    public CategoryProvider feedManagerCategoryProvider() {
        return Mockito.mock(CategoryProvider.class);
    }

    @Bean
    public FeedManagerTemplateProvider feedManagerTemplateProvider() {
        return Mockito.mock(FeedManagerTemplateProvider.class);
    }

    @Bean
    public DatasourceDefinitionProvider datasourceDefinitionProvider() {
        return Mockito.mock(DatasourceDefinitionProvider.class);
    }

    @Bean
    public FeedServiceLevelAgreementProvider jcrFeedSlaProvider() {
        return Mockito.mock(FeedServiceLevelAgreementProvider.class);
    }

}
