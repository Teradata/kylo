package com.thinkbiganalytics.metadata.upgrade.v090;

/*-
 * #%L
 * kylo-upgrade-service
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.category.security.JcrCategoryAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrUserDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.security.JcrDatasourceAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.feed.security.JcrFeedAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;
import com.thinkbiganalytics.metadata.modeshape.template.security.JcrTemplateAllowedActions;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

/**
 * Ensures that all categories have the new, mandatory feedRoleMemberships node.
 */
@Component("versionableFeedUpgradeAction084")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class AclPrincipalTypeUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(AclPrincipalTypeUpgradeAction.class);
    
    @Inject
    private UserProvider userProvider;
    
    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private CategoryProvider categoryProvider;
    
    @Inject
    private FeedManagerTemplateProvider templateProvider;
    
    @Inject
    private DatasourceProvider datasourceProvider;
    
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.9", "0", "");
    }

    @Override
    public void upgradeTo(final KyloVersion startingVersion) {
        log.info("Recording principal types for ACLs for version: {}", startingVersion);
        
        Set<String> groupNames = StreamSupport.stream(this.userProvider.findGroups().spliterator(), false)
            .map(UserGroup::getSystemName)
            .collect(Collectors.toSet());

        upgradeTemplates(groupNames);
        upgradeCategories(groupNames);
        upgradeFeeds(groupNames);
        upgradeDataSources(groupNames);

    }

    private void upgradeTemplates(Set<String> groupNames) {
        this.templateProvider.findAll().stream()
            .map(JcrFeedTemplate.class::cast)
            .map(template -> template.getAllowedActions())
            .map(JcrTemplateAllowedActions.class::cast)
            .forEach(allowed -> upgrade(allowed, groupNames));
    }

    private void upgradeCategories(Set<String> groupNames) {
        this.categoryProvider.findAll().stream()
            .map(JcrCategory.class::cast)
            .map(category -> category.getAllowedActions())
            .map(JcrCategoryAllowedActions.class::cast)
            .forEach(allowed -> upgrade(allowed, groupNames));
    }

    private void upgradeFeeds(Set<String> groupNames) {
        this.feedProvider.findAll().stream()
            .map(JcrFeed.class::cast)
            .map(feed -> feed.getAllowedActions())
            .map(JcrFeedAllowedActions.class::cast)
            .forEach(allowed -> upgrade(allowed, groupNames));
    }

    private void upgradeDataSources(Set<String> groupNames) {
        this.datasourceProvider.getDatasources().stream()
            .filter(JcrUserDatasource.class::isInstance)
            .map(JcrUserDatasource.class::cast)
            .map(ds -> ds.getAllowedActions())
            .map(JcrDatasourceAllowedActions.class::cast)
            .forEach(allowed -> upgrade(allowed, groupNames));
    }
    
    private boolean isUpgradable(Principal principal) {
        return ! (principal instanceof UsernamePrincipal || 
                        principal instanceof GroupPrincipal || 
                        principal.getName().equals("admin") || 
                        principal.getName().equals("everyone"));
    }
    
    private void upgrade(JcrAllowedActions allowed, Set<String> groupNames) {
        allowed.streamActions()
            .forEach(action -> {
                allowed.getPrincipalsAllowedAll(action).stream()
                    .filter(this::isUpgradable)
                    .forEach(principal -> {
                        // If the principal name does not match a group name then assume it is a user.
                        if (groupNames.contains(principal.getName())) {
                            GroupPrincipal group = new GroupPrincipal(principal.getName());
                            allowed.enable(group, action);
                        } else {
                            UsernamePrincipal newPrincipal = new UsernamePrincipal(principal.getName());
                            allowed.enable(newPrincipal, action);
                        }
                        
                        allowed.disable(principal, action);
                    });
            });
        
    }

//    private void upgradeAllowableActions(Set<String> groupNames) {
//        try {
//            Session session = JcrMetadataAccess.getActiveSession();
//            String query = "SELECT * FROM [" + JcrAllowableAction.NODE_TYPE + "] ";
//            QueryResult result = JcrQueryUtil.query(session, query);
//            NodeIterator itr = result.getNodes();
//            int count = 0;
//            
//            while (itr.hasNext()) {
//                Node actionNode = itr.nextNode();
//                
//                upgradeAclPrincipals(actionNode, groupNames);
//                count++;
//                
//                if (count % 100 == 0) log.info("Completed {} principal type upgrades for permitted actions", count);
//            }
//        } catch (RepositoryException e) {
//            throw new UpgradeException("Failed to upgrade principal types in ACLs", e);
//        }
//    }
//
//    private void upgradeAclPrincipals(Node node, Set<String> groupNames) throws RepositoryException {
//        Map<Principal, Set<Privilege>> privsMap = JcrAccessControlUtil.getAllPrivileges(node);
//        
//        for (Entry<Principal, Set<Privilege>> entry : privsMap.entrySet()) {
//            if (entry.getKey() instanceof SimplePrincipal) {
//                Principal derived = derivePrincipal(entry.getKey(), groupNames);
//                
//                // Never remove the special "admin" principal from any ACL, and do not remove
//                // an entry if the derived principal is the same as the original.
//                if (! entry.getKey().getName().equals("admin") && ! entry.getKey().equals(derived)) {
//                    JcrAccessControlUtil.removeAllPermissions(node, entry.getKey());
//                }
//                
//                // Re-add the same privileges for the newly-derived principal.
//                JcrAccessControlUtil.addPermissions(node, derived, entry.getValue().stream().toArray(Privilege[]::new));
//            }
//        }
//    }
//
//    private Principal derivePrincipal(Principal principal, Set<String> groupNames) {
//        // If the principal name does not match a group name then assume it is a user.
//        if (groupNames.contains(principal.getName())) {
//            return new GroupPrincipal(principal.getName());
//        } else {
//            return new UsernamePrincipal(principal.getName());
//        }
//    }
    
    
//    private class FeedUpgradeAllowedActions extends JcrFeedAllowedActions {
//
//        public FeedUpgradeAllowedActions(JcrFeedAllowedActions allowed, FeedOpsAccessControlProvider opsAccessProvider) {
//            super(allowed.getNode(), opsAccessProvider);
//        }
//    }
//    
//    private class CategoryUpgradeAllowedActions extends JcrCategoryAllowedActions {
//        
//        public CategoryUpgradeAllowedActions(JcrCategoryAllowedActions allowed) {
//            super(allowed.getNode());
//        }
//    }
//    
//    private class TemplateUpgradeAllowedActions extends JcrTemplateAllowedActions {
//
//        public TemplateUpgradeAllowedActions(JcrTemplateAllowedActions allowed) {
//            super(allowed.getNode());
//        }
//    }
//    
//    private class DatasourceUpgradeAllowedActions extends JcrDatasourceAllowedActions {
//
//        public DatasourceUpgradeAllowedActions(JcrDatasourceAllowedActions allowed) {
//            super(allowed.getNode());
//        }
//    }
}
