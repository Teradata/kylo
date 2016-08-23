/**
 * 
 */
package com.thinkbiganalytics.feedmgr.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.metadata.config.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.security.AdminCredentials;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ModuleActionsBuilder;

/**
 * Configures the allowable actions for feed management.
 * @author Sean Felten
 */
@Configuration
public class FeedManagerSecurityConfiguration {

    @Bean
    public PostMetadataConfigAction configAuthorization() {
        return new ConfigureAuthorizationAction();
    }
    
    public class ConfigureAuthorizationAction implements PostMetadataConfigAction {

        @Inject
        private JcrMetadataAccess metadata;
        
        @Inject
        private ModuleActionsBuilder builder;

        @Override
        public void run() {
            metadata.commit(new AdminCredentials(), () -> {
                // Builds the allowable actions related to feeds to the services group
                return builder
                            .group("services")
                                .action(FeedsAccessControl.FEEDS_SUPPORT)
                                    .title("Access Feed Support")
                                    .description("Allow access to feeds and feed-related functions")
                                    .subAction(FeedsAccessControl.ACCESS_FEEDS)
                                        .title("Access Feeds")
                                        .description("Allows access to feeds")
                                        .subAction(FeedsAccessControl.CREATE_FEEDS)
                                            .title("Create Feeds")
                                            .description("Allows creating and managing of new feeds")
                                            .add()
                                        .subAction(FeedsAccessControl.ADMIN_FEEDS)
                                            .title("Administer Feeds")
                                            .description("Allows the administration of any feed; even those created by others")
                                            .add()
                                        .add()
                                    .subAction(FeedsAccessControl.ACCESS_CATEGORIES)
                                        .title("Access Categories")
                                        .description("Allows access to categories")
                                        .subAction(FeedsAccessControl.CREATE_CATEGORIES)
                                            .title("Create Categories")
                                            .description("Allows creating and managing of new categories")
                                            .add()
                                        .subAction(FeedsAccessControl.ADMIN_CATEGORIES)
                                            .title("Administer Categories")
                                            .description("Allows the administration of any category; even those created by others")
                                            .add()
                                        .add()
                                    .subAction(FeedsAccessControl.ACCESS_TEMPLATES)
                                        .title("Access Templates")
                                        .description("Allows access to feed templates")
                                        .subAction(FeedsAccessControl.CREATE_TEMPLATES)
                                            .title("Create Templates")
                                            .description("Allows creating and managing of new feed templates")
                                            .add()
                                        .subAction(FeedsAccessControl.ADMIN_TEMPLATES)
                                            .title("Administer Templates")
                                            .description("Allows the administration of any feed template; even those created by others")
                                            .add()
                                        .add()
                                    .add()
                                .add()
                            .build();
            });
        }
    }
}
