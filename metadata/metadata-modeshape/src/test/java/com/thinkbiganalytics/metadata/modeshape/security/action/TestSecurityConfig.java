/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.config.ActionsGroupBuilder;

/**
 *
 * @author Sean Felten
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
    private ActionsGroupBuilder builder;

    @Bean
    public PostMetadataConfigAction configAuthorization() {
        return () -> metadata.commit(() -> {
            // JcrTool tool = new JcrTool(true);
            // tool.printSubgraph(JcrMetadataAccess.getActiveSession(), "/metadata");

            return builder
                            .group("services")
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
        }, MetadataAccess.SERVICE);
    }
}
