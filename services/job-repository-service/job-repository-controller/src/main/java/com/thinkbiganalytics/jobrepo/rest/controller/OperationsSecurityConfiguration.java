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
import com.thinkbiganalytics.security.action.config.ActionsGroupBuilder;

/**
 * Configures the allowable actions for feed management.
 * @author Sean Felten
 */
@Configuration
public class OperationsSecurityConfiguration {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ActionsGroupBuilder builder;

    @Bean
    public PostMetadataConfigAction operationsSecurityConfigAction() {
        return () -> metadata.commit(() -> {
            return builder
                            .group("services")
                                .action(OperationsAccessControl.ACCESS_OPS)
                                .action(OperationsAccessControl.ADMIN_OPS)
                                .add()
                            .build();
            }, MetadataAccess.SERVICE);
    }
}
