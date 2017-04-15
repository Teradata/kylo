package com.thinkbiganalytics.feedmgr.config;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.datasource.security.DatasourceAccessControl;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * Configures the allowable actions for feed management.
 */
@Configuration
public class FeedManagerSecurityConfiguration {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ActionsModuleBuilder builder;

    @Bean
    public PostMetadataConfigAction feedManagerSecurityConfigAction() {
        //@formatter:off

        return () -> metadata.commit(() -> {
            return builder
                            .module(AllowedActions.SERVICES)
                                .action(FeedServicesAccessControl.FEEDS_SUPPORT)
                                .action(FeedServicesAccessControl.ACCESS_FEEDS)
                                .action(FeedServicesAccessControl.EDIT_FEEDS)
                                .action(FeedServicesAccessControl.IMPORT_FEEDS)
                                .action(FeedServicesAccessControl.EXPORT_FEEDS)
                                .action(FeedServicesAccessControl.ADMIN_FEEDS)
                                .action(FeedServicesAccessControl.ACCESS_TABLES)
                                .action(FeedServicesAccessControl.ACCESS_VISUAL_QUERY)
                                .action(FeedServicesAccessControl.ACCESS_CATEGORIES)
                                .action(FeedServicesAccessControl.EDIT_CATEGORIES)
                                .action(FeedServicesAccessControl.ADMIN_CATEGORIES)
                                .action(FeedServicesAccessControl.ACCESS_TEMPLATES)
                                .action(FeedServicesAccessControl.EDIT_TEMPLATES)
                                .action(FeedServicesAccessControl.IMPORT_TEMPLATES)
                                .action(FeedServicesAccessControl.EXPORT_TEMPLATES)
                                .action(FeedServicesAccessControl.ADMIN_TEMPLATES)
                                .action(FeedServicesAccessControl.ACCESS_DATASOURCES)
                                .action(FeedServicesAccessControl.ACCESS_SERVICE_LEVEL_AGREEMENTS)
                                .action(FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENTS)
                                .action(FeedServicesAccessControl.EDIT_DATASOURCES)
                                .action(FeedServicesAccessControl.ADMIN_DATASOURCES)
                                .add()
                            .module(AllowedActions.FEED)
                                .action(FeedAccessControl.ACCESS_FEED)
                                .action(FeedAccessControl.EDIT_SUMMARY)
                                .action(FeedAccessControl.ACCESS_DETAILS)
                                .action(FeedAccessControl.EDIT_DETAILS)
                                .action(FeedAccessControl.DELETE)
                                .action(FeedAccessControl.ENABLE_DISABLE)
                                .action(FeedAccessControl.EXPORT)
//                                .action(FeedAccessControl.SCHEDULE_FEED)
                                .action(FeedAccessControl.ACCESS_OPS)
                                .action(FeedAccessControl.CHANGE_PERMS)
                                .add()
                            .module(AllowedActions.CATEGORY)
                                .action(CategoryAccessControl.ACCESS_CATEGORY)
                                .action(CategoryAccessControl.EDIT_SUMMARY)
                                .action(CategoryAccessControl.ACCESS_DETAILS)
                                .action(CategoryAccessControl.EDIT_DETAILS)
                                .action(CategoryAccessControl.DELETE)
                                .action(CategoryAccessControl.EXPORT)
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
}
