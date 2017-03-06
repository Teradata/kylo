package com.thinkbiganalytics.metadata.upgrade;

/*-
 * #%L
 * thinkbig-operational-metadata-upgrade-service
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.app.KyloVersion;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.user.User;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.metadata.jpa.feed.JpaOpsManagerFeed;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.support.FeedNameUtil;

@Order(PostMetadataConfigAction.LATE_ORDER + 100)
public class UpgradeKyloService implements PostMetadataConfigAction {

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
    private PasswordEncoder passwordEncoder;
    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    
    public void run() {
        upgradeCheck();
    };

    /**
     * checks the upgrade for Kylo and updates the version if needed
     */
    public void upgradeCheck() {
        KyloVersion version = kyloVersionProvider.getKyloVersion();
        
        if (version == null) {
            setupFreshInstall();
        }
        
        if (version == null || version.getMajorVersionNumber() == null || (version.getMajorVersionNumber() != null && version.getMajorVersionNumber()< 0.4f)) {
            version = upgradeTo0_4_0();
        }
        ensureFeedTemplateFeedRelationships();
        // migrateUnusedFeedProperties();
        version = metadataAccess.commit(() -> {
            //ensure/update the version
            KyloVersion kyloVersion = kyloVersionProvider.updateToCurrentVersion();
            return kyloVersion;
        }, MetadataAccess.SERVICE);

        log.info("Upgrade check complete for Kylo {}", version.getVersion());
    }

    /**
     * 
     */
    private void setupFreshInstall() {
        metadataAccess.commit(() -> {
            User dladmin = createDefaultUser("dladmin", "Data Lake Administrator", "thinkbig");
            User analyst = createDefaultUser("analyst", "Analyst", "analyst");
            User designer = createDefaultUser("designer", "Designer", "designer");
            User operator = createDefaultUser("operator", "Operator", "operator");
            
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
                                               FeedServicesAccessControl.ACCESS_TEMPLATES);
                                allowed.enable(designersGroup.getRootPrincial(), 
                                               OperationsAccessControl.ACCESS_OPS,
                                               FeedServicesAccessControl.EDIT_FEEDS,
                                               FeedServicesAccessControl.IMPORT_FEEDS,
                                               FeedServicesAccessControl.EXPORT_FEEDS,
                                               FeedServicesAccessControl.EDIT_CATEGORIES,
                                               FeedServicesAccessControl.CREATE_DATASOURCES,
                                               FeedServicesAccessControl.EDIT_TEMPLATES,
                                               FeedServicesAccessControl.IMPORT_TEMPLATES,
                                               FeedServicesAccessControl.EXPORT_TEMPLATES,
                                               FeedServicesAccessControl.ADMIN_TEMPLATES);
                                allowed.enable(analystsGroup.getRootPrincial(), 
                                               OperationsAccessControl.ACCESS_OPS,
                                               FeedServicesAccessControl.EDIT_FEEDS,
                                               FeedServicesAccessControl.IMPORT_FEEDS,
                                               FeedServicesAccessControl.EXPORT_FEEDS,
                                               FeedServicesAccessControl.EDIT_CATEGORIES,
                                               FeedServicesAccessControl.ACCESS_TEMPLATES);
                            });
        }, MetadataAccess.SERVICE);
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
                                templateFeeds =  template.getFeeds();
                            }catch(MetadataRepositoryException e){
                                //templateFeeds are weak references.
                                //if the template feeds return itemNotExists we need to reset it
                                Throwable rootCause = ExceptionUtils.getRootCause(e);
                                if(rootCause != null && rootCause instanceof ItemNotFoundException ) {
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
    public KyloVersion upgradeTo0_4_0() {

        return metadataAccess.commit(() -> metadataAccess.commit(() -> {

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

            //update the version
            return kyloVersionProvider.updateToCurrentVersion();
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
            user.setPassword(passwordEncoder.encode(password));
            user.setDisplayName(displayName);
        }
        
        return user;
    }

    protected UserGroup createDefaultGroup(String groupName, String title) {
        UserGroup newGroup = userProvider.ensureGroup(groupName);
        newGroup.setTitle(title);
        return newGroup;
    }

}
