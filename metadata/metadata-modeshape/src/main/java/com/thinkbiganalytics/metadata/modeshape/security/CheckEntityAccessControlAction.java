/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.datasource.security.DatasourceAccessControl;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.api.security.RoleNotFoundException;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

/**
 * An action invoked early post-metadata configuration that checks if access control has been enabled or disabled.
 * If it was enabled when previously disabled then roles are setup.  If it was disabled when previously enabled then
 * an exception is thrown to fail startup.
 */
@Order(PostMetadataConfigAction.EARLY_ORDER)
public class CheckEntityAccessControlAction implements PostMetadataConfigAction {

    private static final Logger log = LoggerFactory.getLogger(CheckEntityAccessControlAction.class);
    
    @Inject
    private MetadataAccess metadata;

    @Inject
    private AccessController accessController;
    
    @Inject
    private SecurityRoleProvider roleProvider;
    
    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    @Inject
    private CategoryProvider categoryProvider;
    
    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private FeedManagerTemplateProvider feedManagerTemplateProvider;

    
    @Override
    public void run() {
        metadata.commit(() -> {
            Node securityNode = JcrUtil.getNode(JcrMetadataAccess.getActiveSession(), SecurityPaths.SECURITY.toString());
            boolean propertyEnabled = accessController.isEntityAccessControlled();
            boolean metadataEnabled = wasAccessControllEnabled(securityNode);
            
            if (metadataEnabled == true && propertyEnabled == false) {
                log.error(  "\n*************************************************************************************************************************************\n"
                            + "Kylo has previously been started with entity access control enabled and the current configuration is attempting to set it as disabled\n"
                            + "*************************************************************************************************************************************");
                throw new IllegalStateException("Entity access control is configured as disabled when it was previously enabled");
            } else if (metadataEnabled == false && propertyEnabled == true) {
                ensureDefaultEntityRoles();
            }

            JcrPropertyUtil.setProperty(securityNode, SecurityPaths.ENTITY_ACCESS_CONTROL_ENABLED, propertyEnabled);
        }, MetadataAccess.SERVICE);
    }

    private boolean wasAccessControllEnabled(Node securityNode) {
        if (JcrPropertyUtil.hasProperty(securityNode, SecurityPaths.ENTITY_ACCESS_CONTROL_ENABLED)) {
            return JcrPropertyUtil.getBoolean(securityNode, SecurityPaths.ENTITY_ACCESS_CONTROL_ENABLED);
        } else {
            return roleProvider.getRoles().values().stream().anyMatch(roles -> roles.size() > 0);
        }
    }


    private void ensureDefaultEntityRoles() {
        createDefaultRoles();

        ensureTemplateAccessControl();
        ensureCategoryAccessControl();
        ensureFeedAccessControl();
    }

    private void createDefaultRoles() {
        // Create default roles
        SecurityRole feedEditor = createDefaultRole(SecurityRole.FEED, "editor", "Editor", "Allows a user to edit, enable/disable, delete and export feed. Allows access to job operations for feed. If role inherited via a category, allows these operations for feeds under that category.",
                                                    FeedAccessControl.EDIT_DETAILS,
                                                    FeedAccessControl.DELETE,
                                                    FeedAccessControl.ACCESS_OPS,
                                                    FeedAccessControl.ENABLE_DISABLE,
                                                    FeedAccessControl.EXPORT);

        //admin can do everything the editor does + change perms
        createDefaultRole(SecurityRole.FEED, "admin", "Admin", "All capabilities defined in the 'Editor' role along with the ability to change the permissions", feedEditor,
                          FeedAccessControl.CHANGE_PERMS);

        createDefaultRole(SecurityRole.FEED, "readOnly", "Read-Only", "Allows a user to view the feed and access job operations",
                          FeedAccessControl.ACCESS_DETAILS,
                          FeedAccessControl.ACCESS_OPS);

        SecurityRole templateEditor = createDefaultRole(SecurityRole.TEMPLATE, "editor", "Editor", "Allows a user to edit,export a template",
                                                        TemplateAccessControl.ACCESS_TEMPLATE,
                                                        TemplateAccessControl.EDIT_TEMPLATE,
                                                        TemplateAccessControl.DELETE,
                                                        TemplateAccessControl.CREATE_FEED,
                                                        TemplateAccessControl.EXPORT);
        createDefaultRole(SecurityRole.TEMPLATE, "admin", "Admin", "All capabilities defined in the 'Editor' role along with the ability to change the permissions", templateEditor,
                          TemplateAccessControl.CHANGE_PERMS);

        createDefaultRole(SecurityRole.TEMPLATE, "readOnly", "Read-Only", "Allows a user to view the template", TemplateAccessControl.ACCESS_TEMPLATE);

        SecurityRole categoryEditor = createDefaultRole(SecurityRole.CATEGORY, "editor", "Editor", "Allows a user to edit, export and delete category. Allows creating feeds under the category",
                                                        CategoryAccessControl.ACCESS_CATEGORY,
                                                        CategoryAccessControl.EDIT_DETAILS,
                                                        CategoryAccessControl.EDIT_SUMMARY,
                                                        CategoryAccessControl.CREATE_FEED,
                                                        CategoryAccessControl.DELETE);

        createDefaultRole(SecurityRole.CATEGORY, "admin", "Admin", "All capabilities defined in the 'Editor' role along with the ability to change the permissions", categoryEditor,
                          CategoryAccessControl.CHANGE_PERMS);

        createDefaultRole(SecurityRole.CATEGORY, "readOnly", "Read-Only", "Allows a user to view the category", CategoryAccessControl.ACCESS_CATEGORY);

        createDefaultRole(SecurityRole.CATEGORY, "feedCreator", "Feed Creator", "Allows a user to create a new feed using this category", CategoryAccessControl.ACCESS_DETAILS,
                          CategoryAccessControl.CREATE_FEED);

        final SecurityRole datasourceEditor = createDefaultRole(SecurityRole.DATASOURCE, "editor", "Editor", "Allows a user to edit,delete datasources",
                                                                DatasourceAccessControl.ACCESS_DATASOURCE,
                                                                DatasourceAccessControl.EDIT_DETAILS,
                                                                DatasourceAccessControl.EDIT_SUMMARY,
                                                                DatasourceAccessControl.DELETE);
        createDefaultRole(SecurityRole.DATASOURCE, "admin", "Admin", "All capabilities defined in the 'Editor' role along with the ability to change the permissions", datasourceEditor,
                          DatasourceAccessControl.CHANGE_PERMS);
        createDefaultRole(SecurityRole.DATASOURCE, "readOnly", "Read-Only", "Allows a user to view the datasource", DatasourceAccessControl.ACCESS_DATASOURCE);
    }
    
    protected SecurityRole createDefaultRole(@Nonnull final String entityName, @Nonnull final String roleName, @Nonnull final String title, final String desc, @Nonnull final SecurityRole baseRole,
                                             final Action... actions) {
        final Stream<Action> baseActions = baseRole.getAllowedActions().getAvailableActions().stream().flatMap(AllowableAction::stream);
        final Action[] allowedActions = Stream.concat(baseActions, Stream.of(actions)).toArray(Action[]::new);
        return createDefaultRole(entityName, roleName, title, desc, allowedActions);
    }

    protected SecurityRole createDefaultRole(String entityName, String roleName, String title, String desc, Action... actions) {
        Supplier<SecurityRole> createIfNotFound = () -> {
            SecurityRole role = roleProvider.createRole(entityName, roleName, title, desc);
            role.setPermissions(actions);
            return role;
        };

        Function<SecurityRole, SecurityRole> ensureActions = (role) -> {
            role.setDescription(desc);
            if (actions != null) {
                List<Action> actionsList = Arrays.asList(actions);
                boolean needsUpdate = actionsList.stream().anyMatch(action -> !role.getAllowedActions().hasPermission(action));
                if (needsUpdate) {
                    role.setPermissions(actions);
                }
            }
            return role;
        };

        try {
            return roleProvider.getRole(entityName, roleName).map(ensureActions).orElseGet(createIfNotFound);

        } catch (RoleNotFoundException e) {
            return createIfNotFound.get();
        }
    }

    private void ensureFeedAccessControl() {
        List<Feed> feeds = feedProvider.findAll();
        if (feeds != null) {
            List<SecurityRole> roles = this.roleProvider.getEntityRoles(SecurityRole.FEED);
            Optional<AllowedActions> allowedActions = this.actionsProvider.getAvailableActions(AllowedActions.FEED);
            feeds.stream().forEach(feed -> {
                Principal owner = feed.getOwner() != null ? feed.getOwner() : JcrMetadataAccess.getActiveUser();
                allowedActions.ifPresent(actions -> ((JcrFeed) feed).enableAccessControl((JcrAllowedActions) actions, owner, roles));
            });
        }
    }

    private void ensureCategoryAccessControl() {
        List<Category> categories = categoryProvider.findAll();
        if (categories != null) {
            List<SecurityRole> catRoles = this.roleProvider.getEntityRoles(SecurityRole.CATEGORY);
            List<SecurityRole> feedRoles = this.roleProvider.getEntityRoles(SecurityRole.FEED);

            Optional<AllowedActions> allowedActions = this.actionsProvider.getAvailableActions(AllowedActions.CATEGORY);
            categories.stream().forEach(category -> {
                Principal owner = category.getOwner() != null ? category.getOwner() : JcrMetadataAccess.getActiveUser();
                allowedActions.ifPresent(actions -> ((JcrCategory) category).enableAccessControl((JcrAllowedActions) actions, owner, catRoles, feedRoles));
            });
        }
    }

    private void ensureTemplateAccessControl() {
        List<FeedManagerTemplate> templates = feedManagerTemplateProvider.findAll();
        if (templates != null) {
            List<SecurityRole> roles = this.roleProvider.getEntityRoles(SecurityRole.TEMPLATE);
            Optional<AllowedActions> allowedActions = this.actionsProvider.getAvailableActions(AllowedActions.TEMPLATE);
            templates.stream().forEach(template -> {
                Principal owner = template.getOwner() != null ? template.getOwner() : JcrMetadataAccess.getActiveUser();
                allowedActions.ifPresent(actions -> ((JcrFeedTemplate) template).enableAccessControl((JcrAllowedActions) actions, owner, roles));
            });
        }
    }

}
