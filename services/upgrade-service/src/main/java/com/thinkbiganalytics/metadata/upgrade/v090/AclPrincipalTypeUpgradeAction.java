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
import java.security.acl.Group;
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
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.user.User;
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
import com.thinkbiganalytics.security.BasePrincipal;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

/**
 * Ensures that all categories have the new, mandatory feedRoleMemberships node.
 */
@Component("aclPrincipalTypeUpgradeAction090")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class AclPrincipalTypeUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(AclPrincipalTypeUpgradeAction.class);
    
    @Inject
    private AllowedEntityActionsProvider actionsProvider;
    
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
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Recording principal types for ACLs for version: {}", targetVersion);
        
        Set<String> groupNames = StreamSupport.stream(this.userProvider.findGroups().spliterator(), false)
            .map(UserGroup::getSystemName)
            .collect(Collectors.toSet());
        
        Set<String> userNames = StreamSupport.stream(this.userProvider.findUsers().spliterator(), false)
                        .map(User::getSystemName)
                        .collect(Collectors.toSet());

        upgradeServices(userNames, groupNames);
        upgradeDataSources(userNames, groupNames);
        upgradeFeeds(userNames, groupNames);
        upgradeCategories(userNames, groupNames);
        upgradeTemplates(userNames, groupNames);

    }

    /**
     * @param groupNames
     */
    private void upgradeServices(Set<String> userNames, Set<String> groupNames) {
        actionsProvider.getAllowedActions(AllowedActions.SERVICES)
            .map(JcrAllowedActions.class::cast)
            .ifPresent(allowed -> upgrade(allowed, userNames, groupNames));
    }

    private void upgradeTemplates(Set<String> userNames, Set<String> groupNames) {
        this.templateProvider.findAll().stream()
            .map(JcrFeedTemplate.class::cast)
            .map(template -> template.getAllowedActions())
            .map(JcrTemplateAllowedActions.class::cast)
            .forEach(allowed -> upgrade(allowed, userNames, groupNames));
    }

    private void upgradeCategories(Set<String> userNames, Set<String> groupNames) {
        this.categoryProvider.findAll().stream()
            .map(JcrCategory.class::cast)
            .map(category -> category.getAllowedActions())
            .map(JcrCategoryAllowedActions.class::cast)
            .forEach(allowed -> upgrade(allowed, userNames, groupNames));
    }

    private void upgradeFeeds(Set<String> userNames, Set<String> groupNames) {
        this.feedProvider.findAll().stream()
            .map(JcrFeed.class::cast)
            .map(feed -> feed.getAllowedActions())
            .map(JcrFeedAllowedActions.class::cast)
            .forEach(allowed -> upgrade(allowed, userNames, groupNames));
    }

    private void upgradeDataSources(Set<String> userNames, Set<String> groupNames) {
        this.datasourceProvider.getDatasources().stream()
            .filter(JcrUserDatasource.class::isInstance)
            .map(JcrUserDatasource.class::cast)
            .map(ds -> ds.getAllowedActions())
            .map(JcrDatasourceAllowedActions.class::cast)
            .forEach(allowed -> upgrade(allowed, userNames, groupNames));
    }
    
    private boolean isUpgradable(Principal principal) {
        return ! (principal instanceof UsernamePrincipal || 
                        principal instanceof GroupPrincipal || 
                        principal.getName().equals("admin") || 
                        principal.getName().equals("everyone"));
    }
    
    private void upgrade(JcrAllowedActions allowed, Set<String> userNames, Set<String> groupNames) {
        allowed.streamActions()
            .forEach(action -> {
                allowed.getPrincipalsAllowedAll(action).stream()
                    .filter(this::isUpgradable)
                    .forEach(principal -> {
                        // Re-add known groups and users principals of the correct type.
                        if (groupNames.contains(principal.getName())) {
                            GroupPrincipal group = new GroupPrincipal(principal.getName());
                            allowed.enable(group, action);
                        } else if (userNames.contains(principal.getName())) {
                            UsernamePrincipal newPrincipal = new UsernamePrincipal(principal.getName());
                            allowed.enable(newPrincipal, action);
                        } 
                    });
            });
        
        // Clean out any generic principals not upgraded to the correct type.
        allowed.streamActions()
            .forEach(action -> {
                allowed.getPrincipalsAllowedAll(action).stream()
                    .filter(this::isUpgradable)
                    .forEach(principal -> {
                        if (! (principal instanceof UsernamePrincipal || principal instanceof Group)) {
                            allowed.disable(new RemovedPrincipal(principal), action);
                        }
                    });
            });
    }
    
    private static class RemovedPrincipal extends BasePrincipal {
        private static final long serialVersionUID = 1L;

        public RemovedPrincipal(Principal principal) {
            super(principal.getName());
        }
    }
}
