/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.security.AdminCredentials;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ModuleActionsBuilder;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class TestSecurityConfig {

    @Inject
    private JcrMetadataAccess metadata;
    
    @Inject
    private ModuleActionsBuilder builder;
    
    @PostConstruct
    public AllowedActions allowedServiceActions() {
        return metadata.commit(new AdminCredentials(), () -> {
//            JcrTool tool = new JcrTool(true);
//            tool.printSubgraph(JcrMetadataAccess.getActiveSession(), "/metadata");

            return builder
                        .group("services")
                            .action("manageAuthorization")
                                .title("Manage Authorization")
                                .description("Allows modification of authorization access control lists")
                                .add()
                            .action("manageOperations")
                                .title("Manage Operations")
                                .description("Allows access to operations")
                                .subAction("adminOperations")
                                    .title("Administer Operations")
                                    .add()
                                .add()
                            .action("accessFeedSupport")
                                .title("Access Feeds")
                                .description("Allows access of feeds and feed related functions")
                                .subAction("categoryAccess")
                                    .subAction("createCategories")
                                        .title("Create Categories")
                                        .add()
                                    .subAction("adminCategories")
                                        .title("Administer Categories")
                                        .add()
                                    .add()
                                .subAction("accessFeeds")
                                    .subAction("createFeeds")
                                        .title("Create Feeds")
                                        .add()
                                    .subAction("adminFeeds")
                                        .title("Administer Feeds")
                                        .add()
                                    .subAction("importFeeds")
                                        .title("Import Feeds")
                                        .add()
                                    .subAction("exportFeeds")
                                        .title("Export Feeds")
                                        .add()
                                    .add()
                                .subAction("accessTemplates")
                                    .subAction("createTemplates")
                                        .title("Create Templates")
                                        .add()
                                    .subAction("adminTemplates")
                                        .title("Administer Templates")
                                        .add()
                                    .add()
                                .add()
                            .add()
                        .build();
        });
    }
}
