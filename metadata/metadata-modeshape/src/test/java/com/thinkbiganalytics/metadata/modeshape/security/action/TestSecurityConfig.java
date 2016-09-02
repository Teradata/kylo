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
import com.thinkbiganalytics.security.action.config.ModuleActionsBuilder;

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


    @Bean
    public PostMetadataConfigAction configAuthorization() {
        return new ConfigureAuthorizationAction();
    }
    
    public class ConfigureAuthorizationAction implements PostMetadataConfigAction {
        @Inject
        private MetadataAccess metadata;
        
        @Inject
        private ModuleActionsBuilder builder;
        
        public void run() {
            metadata.commit(() -> {
//                JcrTool tool = new JcrTool(true);
//                tool.printSubgraph(JcrMetadataAccess.getActiveSession(), "/metadata");

                return builder
                            .group("services")
                                .action(MANAGE_AUTH)
                                    .title("Manage Authorization")
                                    .description("Allows modification of authorization access control lists")
                                    .add()
                                .action(MANAGE_OPS)
                                    .title("Manage Operations")
                                    .description("Allows access to operations")
                                    .subAction(ADMIN_OPS)
                                        .title("Administer Operations")
                                        .add()
                                    .add()
                                .action(FEED_SUPPORT)
                                    .title("Access Feeds")
                                    .description("Allows access of feeds and feed related functions")
                                    .subAction(ACCESS_CATEGORIES)
                                        .subAction(CREATE_CATEGORIES)
                                            .title("Create Categories")
                                            .add()
                                        .subAction(ADMIN_CATEGORIES)
                                            .title("Administer Categories")
                                            .add()
                                        .add()
                                    .subAction(ACCESS_FEEDS)
                                        .subAction(CREATE_FEEDS)
                                            .title("Create Feeds")
                                            .add()
                                        .subAction(ADMIN_FEEDS)
                                            .title("Administer Feeds")
                                            .add()
                                        .subAction(IMPORT_FEEDS)
                                            .title("Import Feeds")
                                            .add()
                                        .subAction(EXPORT_FEEDS)
                                            .title("Export Feeds")
                                            .add()
                                        .add()
                                    .subAction(ACCESS_TEMPLATES)
                                        .subAction(CREATE_TEMPLATESS)
                                            .title("Create Templates")
                                            .add()
                                        .subAction(ADMIN_TEMPLATES)
                                            .title("Administer Templates")
                                            .add()
                                        .add()
                                    .add()
                                .add()
                            .build();
            }, MetadataAccess.SERVICE);
        }
    }
    
    
}
