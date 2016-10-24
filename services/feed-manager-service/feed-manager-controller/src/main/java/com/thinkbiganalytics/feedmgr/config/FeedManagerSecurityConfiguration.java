/**
 *
 */
package com.thinkbiganalytics.feedmgr.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.security.action.config.ActionsGroupBuilder;

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
