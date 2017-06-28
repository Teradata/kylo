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
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.datasource.security.DatasourceAccessControl;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.metadata.modeshape.security.DefaultAccessController;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;

import org.mockito.Mockito;
import org.modeshape.jcr.RepositoryConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.Nonnull;

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
    public FeedOpsAccessControlProvider opsAccessProvider() {
        return Mockito.mock(FeedOpsAccessControlProvider.class);
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
    public PostMetadataConfigAction feedManagerSecurityConfigAction(@Nonnull final MetadataAccess metadata, @Nonnull final ActionsModuleBuilder builder) {
        //@formatter:off

        return () -> metadata.commit(() -> {
            return builder
                            .module(AllowedActions.FEED)
                                .action(FeedAccessControl.ACCESS_FEED)
                                .action(FeedAccessControl.EDIT_SUMMARY)
                                .action(FeedAccessControl.ACCESS_DETAILS)
                                .action(FeedAccessControl.EDIT_DETAILS)
                                .action(FeedAccessControl.DELETE)
                                .action(FeedAccessControl.ENABLE_DISABLE)
                                .action(FeedAccessControl.EXPORT)
                                .action(FeedAccessControl.ACCESS_OPS)
                                .action(FeedAccessControl.CHANGE_PERMS)
                                .add()
                            .module(AllowedActions.CATEGORY)
                                .action(CategoryAccessControl.ACCESS_CATEGORY)
                                .action(CategoryAccessControl.EDIT_SUMMARY)
                                .action(CategoryAccessControl.ACCESS_DETAILS)
                                .action(CategoryAccessControl.EDIT_DETAILS)
                                .action(CategoryAccessControl.DELETE)
                                .action(CategoryAccessControl.CREATE_FEED)
                                .action(CategoryAccessControl.CHANGE_PERMS)
                                .add()
                            .module(AllowedActions.TEMPLATE)
                                .action(TemplateAccessControl.ACCESS_TEMPLATE)
                                .action(TemplateAccessControl.EDIT_TEMPLATE)
                                .action(TemplateAccessControl.DELETE)
                                .action(TemplateAccessControl.EXPORT)
                                .action(TemplateAccessControl.CREATE_FEED)
                                .action(TemplateAccessControl.CHANGE_PERMS)
                                .add()
                            .module(AllowedActions.DATASOURCE)
                                .action(DatasourceAccessControl.ACCESS_DATASOURCE)
                                .action(DatasourceAccessControl.EDIT_SUMMARY)
                                .action(DatasourceAccessControl.ACCESS_DETAILS)
                                .action(DatasourceAccessControl.EDIT_DETAILS)
                                .action(DatasourceAccessControl.DELETE)
                                .action(DatasourceAccessControl.CHANGE_PERMS)
                                .add()
                            .build();
            }, MetadataAccess.SERVICE);

        // @formatter:on
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        final Properties properties = new Properties();
        properties.setProperty("security.entity.access.controlled", "true");

        final PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setProperties(properties);
        return configurer;
    }
}
