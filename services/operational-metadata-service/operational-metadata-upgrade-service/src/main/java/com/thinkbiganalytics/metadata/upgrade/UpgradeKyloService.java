package com.thinkbiganalytics.metadata.upgrade;

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

import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.datasource.security.DatasourceAccessControl;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.api.security.RoleNotFoundException;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.KyloVersionUtil;
import com.thinkbiganalytics.metadata.jpa.feed.JpaOpsManagerFeed;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;
import com.thinkbiganalytics.metadata.upgrade.version_0_7_1.UpgradeAction;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

@Order(PostMetadataConfigAction.LATE_ORDER + 100)
public class UpgradeKyloService implements PostMetadataConfigAction {
//public class UpgradeKyloService {

    private static final Logger log = LoggerFactory.getLogger(UpgradeKyloService.class);
    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;
    @Inject
    MetadataAccess metadataAccess;
    @Inject
    private KyloVersionProvider kyloVersionProvider;
    @Inject
    private FeedProvider feedProvider;
    @Inject
    private FeedManagerTemplateProvider feedManagerTemplateProvider;
    @Inject
    private CategoryProvider categoryProvider;
    @Inject
    private UserProvider userProvider;
    @Inject
    private SecurityRoleProvider roleProvider;
    @Inject
    private PasswordEncoder passwordEncoder;
    @Inject
    private AllowedEntityActionsProvider actionsProvider;


    public void run() {
       onMetadataStart();
    }

    public KyloVersion getCurrentVersion() {
        KyloVersion version = kyloVersionProvider.getCurrentVersion();
       if(version != null) {
           return new KyloVersionUtil.Version(version.getMajorVersion(), version.getMinorVersion());
       }
       else {
           return null;
       }
    }

    public boolean upgradeFrom(KyloVersion startingVersion) {
        if(startingVersion == null){
            setupFreshInstall();
        }
        else {
            //uncomment after we build the correct upgrade strategy.  For now call the 0.7.1 upgrade explicitly
            UpgradeAction upgradeAction = new UpgradeAction();
            upgradeAction.upgradeFrom(startingVersion);
           // getUpgradeState(startingVersion).ifPresent(upgrade -> upgrade.upgradeFrom(startingVersion));

        }
        // TODO: This current implementation assumes all upgrading occurs from the single state found above.
        // This should be changed to supporting a repeated upgrade path from starting ver->next ver->...->latest vers.
        kyloVersionProvider.updateToLatestVersion();
        return kyloVersionProvider.isUpToDate();
    }

    public Optional<UpgradeState> getUpgradeState(KyloVersion version) {
        try {
            String className = getPackageName(version) + ".UpgradeAction";
            @SuppressWarnings("unchecked")
            Class<UpgradeState> upgradeClass = (Class<UpgradeState>) Class.forName(className);
            return Optional.of(ConstructorUtils.invokeConstructor(upgradeClass));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalStateException("Unable to load upgrade state for version: " + version, e);
        }
    }

    protected String getPackageName(KyloVersion version) {
        return getClass().getPackage().getName() + ".version_" + createVersionTag(version);
    }

    protected String createVersionTag(KyloVersion version) {
        return version.getVersion().replaceAll("[.-]", "_");
    }




    /**
     * This code will run when the latest version is up and running
     */
    private void onMetadataStart() {
      if(kyloVersionProvider.isUpToDate()) {
           ensureFeedTemplateFeedRelationships();
          ensureDefaultEntityRoles();
       }
    }

    private void setupFreshInstall() {
        metadataAccess.commit(() -> {
            User dladmin = createDefaultUser("dladmin", "Data Lake Administrator", null);
            User analyst = createDefaultUser("analyst", "Analyst", null);
            User designer = createDefaultUser("designer", "Designer", null);
            User operator = createDefaultUser("operator", "Operator", null);

            // Create default groups if they don't exist.
            UserGroup adminsGroup = createDefaultGroup("admin", "Administrators");
            UserGroup opsGroup = createDefaultGroup("operations", "Operations");
            UserGroup designersGroup = createDefaultGroup("designer", "Designers");
            UserGroup analystsGroup = createDefaultGroup("analyst", "Analysts");
            UserGroup usersGroup = createDefaultGroup("user", "Users");



            // Add default users to their respective groups
            adminsGroup.addUser(dladmin);
            designersGroup.addUser(designer);
            analystsGroup.addUser(analyst);
            opsGroup.addUser(operator);
            usersGroup.addUser(dladmin);
            usersGroup.addUser(analyst);
            usersGroup.addUser(designer);
            usersGroup.addUser(operator);

            // Setup initial group access control.  Administrators group already has all rights.
            actionsProvider.getAllowedActions(AllowedActions.SERVICES) 
                .ifPresent((allowed) -> {
                    allowed.enable(opsGroup.getRootPrincial(),
                                   OperationsAccessControl.ADMIN_OPS,
                                   FeedServicesAccessControl.ACCESS_CATEGORIES,
                                   FeedServicesAccessControl.ACCESS_FEEDS,
                                   FeedServicesAccessControl.ACCESS_TEMPLATES,
                                   FeedServicesAccessControl.ACCESS_TABLES,
                                   FeedServicesAccessControl.ACCESS_SERVICE_LEVEL_AGREEMENTS);
                    allowed.enable(designersGroup.getRootPrincial(),
                                   OperationsAccessControl.ACCESS_OPS,
                                   FeedServicesAccessControl.EDIT_FEEDS,
                                   FeedServicesAccessControl.ACCESS_TABLES,
                                   FeedServicesAccessControl.IMPORT_FEEDS,
                                   FeedServicesAccessControl.EXPORT_FEEDS,
                                   FeedServicesAccessControl.EDIT_CATEGORIES,
                                   FeedServicesAccessControl.EDIT_DATASOURCES,
                                   FeedServicesAccessControl.EDIT_TEMPLATES,
                                   FeedServicesAccessControl.IMPORT_TEMPLATES,
                                   FeedServicesAccessControl.EXPORT_TEMPLATES,
                                   FeedServicesAccessControl.ADMIN_TEMPLATES,
                                   FeedServicesAccessControl.ACCESS_SERVICE_LEVEL_AGREEMENTS,
                                   FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENTS);
                    allowed.enable(analystsGroup.getRootPrincial(),
                                   OperationsAccessControl.ACCESS_OPS,
                                   FeedServicesAccessControl.EDIT_FEEDS,
                                   FeedServicesAccessControl.ACCESS_TABLES,
                                   FeedServicesAccessControl.IMPORT_FEEDS,
                                   FeedServicesAccessControl.EXPORT_FEEDS,
                                   FeedServicesAccessControl.EDIT_CATEGORIES,
                                   FeedServicesAccessControl.ACCESS_TEMPLATES,
                                   FeedServicesAccessControl.ACCESS_DATASOURCES,
                                   FeedServicesAccessControl.ACCESS_SERVICE_LEVEL_AGREEMENTS,
                                   FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENTS);
                });
        }, MetadataAccess.SERVICE);
    }

    private void createDefaultRoles() {
        metadataAccess.commit(() -> {
        // Create default roles
        SecurityRole feedEditor = createDefaultRole(SecurityRole.FEED, "editor", "Editor",
                                                    FeedAccessControl.EDIT_DETAILS,
                                                    FeedAccessControl.DELETE,
                                                    FeedAccessControl.ACCESS_OPS,
                                                    FeedAccessControl.ENABLE_DISABLE,
                                                    FeedAccessControl.EXPORT);
        //admin can do everything the editor does + change perms
        createDefaultRole(SecurityRole.FEED, "admin", "Admin", feedEditor, FeedAccessControl.CHANGE_PERMS);

        createDefaultRole(SecurityRole.FEED, "readOnly", "Read-Only", FeedAccessControl.ACCESS_DETAILS);

        SecurityRole templateEditor = createDefaultRole(SecurityRole.TEMPLATE, "editor", "Editor",
                                                        TemplateAccessControl.ACCESS_TEMPLATE,
                                                        TemplateAccessControl.EDIT_TEMPLATE,
                                                        TemplateAccessControl.CREATE_FEED,
                                                        TemplateAccessControl.EXPORT);
        createDefaultRole(SecurityRole.TEMPLATE, "admin", "Admin", templateEditor, TemplateAccessControl.CHANGE_PERMS);

        createDefaultRole(SecurityRole.TEMPLATE, "readOnly", "Read-Only", TemplateAccessControl.ACCESS_TEMPLATE);

        SecurityRole categoryEditor = createDefaultRole(SecurityRole.CATEGORY, "editor", "Editor",
                                                        CategoryAccessControl.ACCESS_CATEGORY,
                                                        CategoryAccessControl.EDIT_DETAILS,
                                                        CategoryAccessControl.EDIT_SUMMARY,
                                                        CategoryAccessControl.EXPORT,
                                                        CategoryAccessControl.CREATE_FEED,
                                                        CategoryAccessControl.DELETE);

        createDefaultRole(SecurityRole.CATEGORY, "admin", "Admin", categoryEditor, CategoryAccessControl.CHANGE_PERMS);

        createDefaultRole(SecurityRole.CATEGORY, "readOnly", "Read-Only", CategoryAccessControl.ACCESS_CATEGORY);

        createDefaultRole(SecurityRole.CATEGORY, "feedCreator", "Feed Creator", CategoryAccessControl.ACCESS_CATEGORY, CategoryAccessControl.CREATE_FEED);

        final SecurityRole datasourceEditor = createDefaultRole(SecurityRole.DATASOURCE, "editor", "Editor",
                                                                DatasourceAccessControl.ACCESS_DATASOURCE,
                                                                DatasourceAccessControl.EDIT_DETAILS,
                                                                DatasourceAccessControl.EDIT_SUMMARY,
                                                                DatasourceAccessControl.DELETE);
        createDefaultRole(SecurityRole.DATASOURCE, "admin", "Admin", datasourceEditor, DatasourceAccessControl.CHANGE_PERMS);
        createDefaultRole(SecurityRole.DATASOURCE, "readOnly", "Read-Only", DatasourceAccessControl.ACCESS_DATASOURCE);
        }, MetadataAccess.SERVICE);
    }

    private void ensureDefaultEntityRoles() {
            createDefaultRoles();
            metadataAccess.commit(() -> {
              ensureFeedRoles();
                ensureCategoryRoles();
                ensureTemplateRoles();
            }, MetadataAccess.SERVICE);
    }

    private void ensureFeedRoles() {
        List<Feed> feeds = feedProvider.findAll();
        if (feeds != null) {
            List<SecurityRole> roles = this.roleProvider.getEntityRoles(SecurityRole.FEED);
            Optional<AllowedActions> allowedActions = this.actionsProvider.getAvailableActions(AllowedActions.FEED);
            feeds.stream().forEach(feed -> {
                roleProvider.getEntityRoles(SecurityRole.FEED).stream().forEach(securityRole -> {
                    allowedActions
                        .ifPresent(actions -> ((JcrFeed) feed).setupAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser(), roles));
                });
            });
        }
    }

    private void ensureCategoryRoles() {
        List<Category> categories = categoryProvider.findAll();
        if (categories != null) {
            List<SecurityRole> roles = this.roleProvider.getEntityRoles(SecurityRole.CATEGORY);
            Optional<AllowedActions> allowedActions = this.actionsProvider.getAvailableActions(AllowedActions.CATEGORY);
            categories.stream().forEach(category -> {
                roleProvider.getEntityRoles(SecurityRole.CATEGORY).stream().forEach(securityRole -> {
                    allowedActions
                        .ifPresent(actions -> ((JcrCategory) category).setupAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser(), roles));
                });
            });
        }
    }

    private void ensureTemplateRoles() {
        List<FeedManagerTemplate> templates = feedManagerTemplateProvider.findAll();
        if (templates != null) {
            List<SecurityRole> roles = this.roleProvider.getEntityRoles(SecurityRole.TEMPLATE);
            Optional<AllowedActions> allowedActions = this.actionsProvider.getAvailableActions(AllowedActions.TEMPLATE);
            templates.stream().forEach(template -> {
                roleProvider.getEntityRoles(SecurityRole.TEMPLATE).stream().forEach(securityRole -> {
                    allowedActions
                        .ifPresent(actions -> ((JcrFeedTemplate) template).setupAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser(), roles));
                });
            });
        }
    }

    /*
     * Ensure the Feed Template has the relationships setup to its related feeds
     */
    private void ensureFeedTemplateFeedRelationships() {
        metadataAccess.commit(() -> {

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

            return null;
        }, MetadataAccess.SERVICE);
    }

    /*
     * Migrate and remove or move any properties defined
     */
    private void migrateUnusedFeedProperties() {
        Set<String> propertiesToRemove = new HashSet<>();
        //propertiesToRemove.add('nametoremove');
        if (!propertiesToRemove.isEmpty()) {
            metadataAccess.commit(() -> {

                List<Feed> domainFeeds = feedProvider.findAll();
                for (Feed feedManagerFeed : domainFeeds) {

                    final PropertyIterator iterator;
                    try {
                        iterator = ((JcrFeed) feedManagerFeed).getNode().getProperties();
                    } catch (RepositoryException e) {
                        throw new MetadataRepositoryException("Failed to get properties for node: " + feedManagerFeed, e);
                    }
                    while (iterator.hasNext()) {
                        final Property property = iterator.nextProperty();
                        try {

                            if (propertiesToRemove.contains(property.getName())) {
                                property.remove();
                            }
                        } catch (Exception e) {

                        }
                    }
                }
                return null;
            }, MetadataAccess.SERVICE);
        }
    }

    /**
     * Performs the necessary actions to ensure that Kylo versions &lt; 0.4.0 are upgraded
     *
     * @return the Kylo version after upgrade attempt
     */
    public void upgradeTo0_4_0() {

        metadataAccess.commit(() -> metadataAccess.commit(() -> {

            for (Category category : categoryProvider.findAll()) {
                // Ensure each category has an allowedActions (gets create if not present.)
                category.getAllowedActions();
            }

            // get all feeds defined in feed manager
            List<Feed> domainFeeds = feedProvider.findAll();
            Map<String, Feed> feedManagerFeedMap = new HashMap<>();
            if (domainFeeds != null && !domainFeeds.isEmpty()) {
                List<OpsManagerFeed.ID> opsManagerFeedIds = new ArrayList<OpsManagerFeed.ID>();
                for (Feed feedManagerFeed : domainFeeds) {
                    opsManagerFeedIds.add(opsManagerFeedProvider.resolveId(feedManagerFeed.getId().toString()));
                    feedManagerFeedMap.put(feedManagerFeed.getId().toString(), feedManagerFeed);

                    // Ensure each feed has an allowedActions (gets create if not present.)
                    feedManagerFeed.getAllowedActions();
                }
                //find those that match
                List<? extends OpsManagerFeed> opsManagerFeeds = opsManagerFeedProvider.findByFeedIds(opsManagerFeedIds);
                if (opsManagerFeeds != null) {
                    for (OpsManagerFeed opsManagerFeed : opsManagerFeeds) {
                        feedManagerFeedMap.remove(opsManagerFeed.getId().toString());
                    }
                }

                List<OpsManagerFeed> feedsToAdd = new ArrayList<>();
                for (Feed feed : feedManagerFeedMap.values()) {
                    String fullName = FeedNameUtil.fullName(feed.getCategory().getName(), feed.getName());
                    OpsManagerFeed.ID opsManagerFeedId = opsManagerFeedProvider.resolveId(feed.getId().toString());
                    OpsManagerFeed opsManagerFeed = new JpaOpsManagerFeed(opsManagerFeedId, fullName);
                    feedsToAdd.add(opsManagerFeed);
                }
                log.info("Synchronizing Feeds from Feed Manager. About to insert {} feed ids/names into Operations Manager", feedsToAdd.size());
                opsManagerFeedProvider.save(feedsToAdd);
            }
            return null;

            //update the version
          //  return kyloVersionProvider.updateToLatestVersion();
        }), MetadataAccess.SERVICE);
    }


    protected User createDefaultUser(String username, String displayName, String password) {
        Optional<User> userOption = userProvider.findUserBySystemName(username);
        User user = null;

        // Create the user if it doesn't exists.
        if (userOption.isPresent()) {
            user = userOption.get();
        } else {
            user = userProvider.ensureUser(username);
            if (password != null) {
                user.setPassword(passwordEncoder.encode(password));
            }
            user.setDisplayName(displayName);
        }

        return user;
    }

    protected UserGroup createDefaultGroup(String groupName, String title) {
        UserGroup newGroup = userProvider.ensureGroup(groupName);
        newGroup.setTitle(title);
        return newGroup;
    }

    protected SecurityRole createDefaultRole(String entityName, String roleName, String title, Action... actions) {
        Supplier<SecurityRole> createIfNotFound = () -> {
                SecurityRole role = roleProvider.createRole(entityName, roleName, title, "");
                role.setPermissions(actions);
                return role;
        };
        try {
            return roleProvider.getRole(entityName, roleName)
                .orElseGet(createIfNotFound);
        }catch (RoleNotFoundException e){
            return createIfNotFound.get();
        }
    }

    /**
     * Constructs a new role using the specified base role and actions.
     *
     * @param entityName the folder name
     * @param roleName   the system name for the role
     * @param title      the human-readable name for the role
     * @param baseRole   the role with the default actions for this role
     * @param actions    additional actions for this role
     * @return the security role
     */
    protected SecurityRole createDefaultRole(@Nonnull final String entityName, @Nonnull final String roleName, @Nonnull final String title, @Nonnull final SecurityRole baseRole,
                                             final Action... actions) {
        final Stream<Action> baseActions = baseRole.getAllowedActions().getAvailableActions().stream().flatMap(AllowableAction::stream);
        final Action[] allowedActions = Stream.concat(baseActions, Stream.of(actions)).toArray(Action[]::new);
        return createDefaultRole(entityName, roleName, title, allowedActions);
    }



}
