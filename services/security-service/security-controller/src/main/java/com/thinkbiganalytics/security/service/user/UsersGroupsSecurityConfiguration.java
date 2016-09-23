package com.thinkbiganalytics.security.service.user;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.security.action.config.ModuleActionsBuilder;

/**
 * Configures the actions for users and groups.
 */
@Configuration
public class UsersGroupsSecurityConfiguration {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ModuleActionsBuilder builder;

    @Bean
    public PostMetadataConfigAction usersGroupsSecurityConfigAction() {
        return () -> metadata.commit(() -> {
            return builder
                        .group("services")
                            .action(UsersGroupsAccessContol.USERS_GROUPS_SUPPORT)
                                .title("Access Users and Groups support")
                                .description("Allows access to user and group-related functions")
                                .subAction(UsersGroupsAccessContol.ACCESS_USERS)
                                    .title("Access Users")
                                    .description("Allows the ability to view existing users")
                                    .subAction(UsersGroupsAccessContol.ADMIN_USERS)
                                        .title("Administer Users")
                                        .description("Allows the ability to create and manage users")
                                        .add()
                                    .add()
                                .subAction(UsersGroupsAccessContol.ACCESS_GROUPS)
                                    .title("Access Groups")
                                    .description("Allows the ability to view existing groups")
                                    .subAction(UsersGroupsAccessContol.ADMIN_GROUPS)
                                        .title("Administer Groups")
                                        .description("Allows the ability to create and manage groups")
                                        .add()
                                    .add()
                                .add()
                            .add()
                        .build();

            }, MetadataAccess.SERVICE);
    }
}
