/**
 * 
 */
package com.thinkbiganalytics.jobrepo.rest.controller;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.security.action.config.ModuleActionsBuilder;

/**
 * Configures the allowable actions for feed management.
 * @author Sean Felten
 */
@Configuration
public class OperationsSecurityConfiguration {

    @Bean
    public PostMetadataConfigAction operationsSecurityConfigAction() {
        return new ConfigureAuthorizationAction();
    }
    
    public class ConfigureAuthorizationAction implements PostMetadataConfigAction {

        @Inject
        private MetadataAccess metadata;
        
        @Inject
        private ModuleActionsBuilder builder;

        @Override
        public void run() {
            metadata.commit(() -> {
                // Builds the allowable actions related to feeds to the services group
                return builder
                            .group("services")
                                .action(OperationsAccessControl.ACCESS_OPS)
                                    .title("Access Operational information")
                                    .description("Allows access to operational information like active feeds and execution history, etc.")
                                    .subAction(OperationsAccessControl.ADMIN_OPS)
                                        .title("Access Feeds")
                                        .description("Allows access to feeds")
                                        .add()
                                    .add()
                                .add()
                            .build();
            });
        }
    }
}
