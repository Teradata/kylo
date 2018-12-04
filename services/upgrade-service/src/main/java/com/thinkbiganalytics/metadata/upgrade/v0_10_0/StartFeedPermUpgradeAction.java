package com.thinkbiganalytics.metadata.upgrade.v0_10_0;

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

import javax.inject.Inject;

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedEntityActionsProvider;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Adds the start permission to the feed editor and admin roles.
 * <p>
 * Corrects upgrade bug for 0.9.1 where the permission was not actually being addded to the feed.
 */
@Component("startFeedPermUpgradeAction0.10.0")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class StartFeedPermUpgradeAction implements UpgradeAction {

    private static final Logger log = LoggerFactory.getLogger(StartFeedPermUpgradeAction.class);
    
    @Inject
    private AccessController accessController;
    
    @Inject
    private ActionsModuleBuilder actionsBuilder;

    @Inject
    private SecurityRoleProvider roleProvider;

    @Inject
    private JcrAllowedEntityActionsProvider actionsProvider;

    @Inject
    private FeedProvider feedProvider;
    
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.10", "0", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Add start feed permission to roles: {}", targetVersion);

        if (this.accessController.isEntityAccessControlled()) {
            // Add the new "start" action to the set of feed actions.
            actionsBuilder
                .module(AllowedActions.FEED)
                    .action(FeedAccessControl.START)
                    .add()
                .build();
            
            // Grant the start action permission to the editor and admin roles
            this.roleProvider.getRole(SecurityRole.FEED, "editor").ifPresent(role -> role.setPermissions(FeedAccessControl.START));
            this.roleProvider.getRole(SecurityRole.FEED, "admin").ifPresent(role -> role.setPermissions(FeedAccessControl.START));
            
            // Ensure the new action is integrated into the all of the existing feed instances.
            this.feedProvider.getFeeds().forEach(feed -> {
                JcrFeed jcrFeed = (JcrFeed) feed;
                Principal owner = feed.getOwner();
                JcrAllowedActions feedAllowed = actionsProvider.updateEntityAllowedActions(AllowedActions.FEED, jcrFeed);
                
                // Enable the permission to the owner, admin, and any principals in the editor and admin roles.
                jcrFeed.updateRolePermissions();
                feedAllowed.enable(owner, FeedAccessControl.START);
                feedAllowed.enable(JcrMetadataAccess.ADMIN, FeedAccessControl.START);
            });
        }
    }
}
