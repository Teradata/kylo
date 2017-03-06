/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 *
 */
@Configuration
public class TestSecurityConfig {

    public static final Action MANAGE_AUTH = Action.create("manageAuthorization");
    public static final Action MANAGE_OPS = Action.create("manageOperations");
    public static final Action ADMIN_OPS = MANAGE_OPS.subAction("adminOperations");
    public static final Action FEED_SUPPORT = Action.create("accessFeedSupport");
    public static final Action ACCESS_CATEGORIES = FEED_SUPPORT.subAction("categoryAccess");
    public static final Action CREATE_CATEGORIES = ACCESS_CATEGORIES.subAction("createCategories");
    public static final Action ADMIN_CATEGORIES = ACCESS_CATEGORIES.subAction("adminCategories");
    public static final Action ACCESS_FEEDS = FEED_SUPPORT.subAction("accessFeeds");
    public static final Action CREATE_FEEDS = ACCESS_FEEDS.subAction("createFeeds");
    public static final Action IMPORT_FEEDS = ACCESS_FEEDS.subAction("importFeeds");
    public static final Action EXPORT_FEEDS = ACCESS_FEEDS.subAction("exportFeeds");
    public static final Action ADMIN_FEEDS = ACCESS_FEEDS.subAction("adminFeeds");
    public static final Action ACCESS_TEMPLATES = FEED_SUPPORT.subAction("accessTemplates");
    public static final Action CREATE_TEMPLATESS = ACCESS_TEMPLATES.subAction("adminTemplates");
    public static final Action ADMIN_TEMPLATES = ACCESS_TEMPLATES.subAction("adminCategories");

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ActionsModuleBuilder builder;

    @Bean
    public PostMetadataConfigAction configAuthorization() {
        return () -> metadata.commit(() -> {
            //@formatter:off

            // JcrTool tool = new JcrTool(true);
            // tool.printSubgraph(JcrMetadataAccess.getActiveSession(), "/metadata");

            return builder
                            .module(AllowedActions.SERVICES)
                                .action(MANAGE_AUTH)
                                .action(MANAGE_OPS)
                                .action(ADMIN_OPS)
                                .action(FEED_SUPPORT)
                                .action(ACCESS_CATEGORIES)
                                .action(CREATE_CATEGORIES)
                                .action(ADMIN_CATEGORIES)
                                .action(ACCESS_FEEDS)
                                .action(CREATE_FEEDS)
                                .action(ADMIN_FEEDS)
                                .action(IMPORT_FEEDS)
                                .action(EXPORT_FEEDS)
                                .action(ACCESS_TEMPLATES)
                                .action(CREATE_TEMPLATESS)
                                .action(ADMIN_TEMPLATES)
                                .add()
                            .build();

            //@formatter:on
        }, MetadataAccess.SERVICE);
    }
}
