package com.thinkbiganalytics.security.service.user;

/*-
 * #%L
 * thinkbig-security-controller
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
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * Configures the actions for users and groups.
 */
@Configuration
public class UsersGroupsSecurityConfiguration {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ActionsModuleBuilder builder;

    @Bean
    public PostMetadataConfigAction usersGroupsSecurityConfigAction() {
        //@formatter:off

        return () -> metadata.commit(() -> {
            return builder
                        .module(AllowedActions.SERVICES)
                            .action(UsersGroupsAccessContol.USERS_GROUPS_SUPPORT)
                            .action(UsersGroupsAccessContol.ACCESS_USERS)
                            .action(UsersGroupsAccessContol.ADMIN_USERS)
                            .action(UsersGroupsAccessContol.ACCESS_GROUPS)
                            .action(UsersGroupsAccessContol.ADMIN_GROUPS)
                            .add()
                        .build();

            }, MetadataAccess.SERVICE);

        //@formatter:on
    }
}
