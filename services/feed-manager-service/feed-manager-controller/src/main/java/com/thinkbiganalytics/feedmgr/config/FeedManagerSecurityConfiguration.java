/**
 *
 */
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

import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.security.action.config.ActionsGroupBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * Configures the allowable actions for feed management.
 * @author Sean Felten
 */
@Configuration
public class FeedManagerSecurityConfiguration {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ActionsGroupBuilder builder;

    @Bean
    public PostMetadataConfigAction feedManagerSecurityConfigAction() {
        return () -> metadata.commit(() -> {
            return builder
                            .group("services")
                                .action(FeedsAccessControl.FEEDS_SUPPORT)
                                .action(FeedsAccessControl.ACCESS_FEEDS)
                                .action(FeedsAccessControl.EDIT_FEEDS)
                                .action(FeedsAccessControl.IMPORT_FEEDS)
                                .action(FeedsAccessControl.EXPORT_FEEDS)
                                .action(FeedsAccessControl.ADMIN_FEEDS)
                                .action(FeedsAccessControl.ACCESS_CATEGORIES)
                                .action(FeedsAccessControl.EDIT_CATEGORIES)
                                .action(FeedsAccessControl.ADMIN_CATEGORIES)
                                .action(FeedsAccessControl.ACCESS_TEMPLATES)
                                .action(FeedsAccessControl.EDIT_TEMPLATES)
                                .action(FeedsAccessControl.IMPORT_TEMPLATES)
                                .action(FeedsAccessControl.EXPORT_TEMPLATES)
                                .action(FeedsAccessControl.ADMIN_TEMPLATES)
                                .add()
                            .build();
            }, MetadataAccess.SERVICE);
    }
}
