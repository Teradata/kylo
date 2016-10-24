package com.thinkbiganalytics.security.service.user;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.security.action.config.ActionsGroupBuilder;

/**
 * Configures the actions for users and groups.
 */
@Configuration
public class UsersGroupsSecurityConfiguration {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ActionsGroupBuilder builder;

    @Bean
    public PostMetadataConfigAction usersGroupsSecurityConfigAction() {
        return () -> metadata.commit(() -> {
            return builder
                        .group("services")
                            .action(UsersGroupsAccessContol.USERS_GROUPS_SUPPORT)
                            .action(UsersGroupsAccessContol.ACCESS_USERS)
                            .action(UsersGroupsAccessContol.ADMIN_USERS)
                            .action(UsersGroupsAccessContol.ACCESS_GROUPS)
                            .action(UsersGroupsAccessContol.ADMIN_GROUPS)
                            .add()
                        .build();

            }, MetadataAccess.SERVICE);
    }
}
