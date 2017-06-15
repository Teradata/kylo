package com.thinkbiganalytics.metadata.upgrade.version.latest;

/*-
 * #%L
 * kylo-operational-metadata-upgrade-service
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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.KyloVersionUtil;
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
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;
import com.thinkbiganalytics.metadata.upgrade.KyloUpgrader;
import com.thinkbiganalytics.metadata.upgrade.UpgradeState;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.jcr.ItemNotFoundException;

@Component
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class UpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(UpgradeAction.class);

    @Inject
    private CategoryProvider categoryProvider;
    @Inject
    private FeedProvider feedProvider;
    @Inject
    private FeedManagerTemplateProvider feedManagerTemplateProvider;
    @Inject
    private SecurityRoleProvider roleProvider;
    @Inject
    private AllowedEntityActionsProvider actionsProvider;
    @Inject
    private AccessController accessController;

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        KyloVersion current = KyloVersionUtil.getBuildVersion();
        return version.getMajorVersion().equals(current.getMajorVersion()) 
                        && version.getMinorVersion().equals(current.getMinorVersion()) 
                        && version.getPointVersion().equals(current.getPointVersion());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#upgradeFrom(com.thinkbiganalytics.metadata.api.app.KyloVersion)
     */
    @Override
    public void upgradeFrom(KyloVersion startingVersion) {
        log.info("Upgrading from version: " + startingVersion);
        
        ensureFeedTemplateFeedRelationships();

        if (accessController.isEntityAccessControlled()) {
            ensureDefaultEntityRoles();
        }
    }
    
    private void ensureFeedTemplateFeedRelationships() {
        //ensure the templates have the feed relationships
        List<Feed> feeds = feedProvider.findAll();
        if (feeds != null) {
            feeds.stream().forEach(feed -> {
                FeedManagerTemplate template = feed.getTemplate();
                if (template != null) {
                    //ensure the template has feeds.
                    List<Feed> templateFeeds = null;
                    try {
                        templateFeeds = template.getFeeds();
                    } catch (MetadataRepositoryException e) {
                        //templateFeeds are weak references.
                        //if the template feeds return itemNotExists we need to reset it
                        Throwable rootCause = ExceptionUtils.getRootCause(e);
                        if (rootCause != null && rootCause instanceof ItemNotFoundException) {
                            //reset the reference collection.  It will be rebuilt in the subsequent call
                            JcrPropertyUtil.removeAllFromSetProperty(((JcrFeedTemplate) template).getNode(), JcrFeedTemplate.FEEDS);
                        }
                    }
                    if (templateFeeds == null || !templateFeeds.contains(feed)) {
                        template.addFeed(feed);
                        feedManagerTemplateProvider.update(template);
                    }
                }
            });

        }

        feedProvider.populateInverseFeedDependencies();
    }

    private void ensureDefaultEntityRoles() {
        createDefaultRoles();

        ensureTemplateRoles();
        ensureCategoryRoles();
        ensureFeedRoles();
    }

    private void createDefaultRoles() {
        // Create default roles
        SecurityRole feedEditor = createDefaultRole(SecurityRole.FEED, "editor", "Editor", "Allows a user to edit, enable/disable, delete, export, and access job operations.",
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

        SecurityRole categoryEditor = createDefaultRole(SecurityRole.CATEGORY, "editor", "Editor", "Allows a user to edit, export, delete, and create feeds using this category",
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

    private void ensureFeedRoles() {
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

    private void ensureCategoryRoles() {
        List<Category> categories = categoryProvider.findAll();
        if (categories != null) {
            List<SecurityRole> roles = this.roleProvider.getEntityRoles(SecurityRole.CATEGORY);
            Optional<AllowedActions> allowedActions = this.actionsProvider.getAvailableActions(AllowedActions.CATEGORY);
            categories.stream().forEach(category -> {
                Principal owner = category.getOwner() != null ? category.getOwner() : JcrMetadataAccess.getActiveUser();
                allowedActions.ifPresent(actions -> ((JcrCategory) category).enableAccessControl((JcrAllowedActions) actions, owner, roles));
            });
        }
    }

    private void ensureTemplateRoles() {
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
