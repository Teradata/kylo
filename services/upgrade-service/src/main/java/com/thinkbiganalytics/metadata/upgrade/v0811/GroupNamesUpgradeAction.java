package com.thinkbiganalytics.metadata.upgrade.v0811;

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
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;
import com.thinkbiganalytics.metadata.api.user.UserGroup;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("upgradeAction0811")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class GroupNamesUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(GroupNamesUpgradeAction.class);

    @Inject
    private UserProvider userProvider;
    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "1", "1");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#upgradeFrom(com.thinkbiganalytics.metadata.api.app.KyloVersion)
     */
    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("Upgrading from version: " + startingVersion);
        
        this.userProvider.findGroupByName("designer")
            .ifPresent(oldGrp -> {
                UserGroup designersGroup = createDefaultGroup("designers", "Designers");
    
                oldGrp.getUsers().forEach(user -> designersGroup.addUser(user));
    
                actionsProvider.getAllowedActions(AllowedActions.SERVICES)
                    .ifPresent((allowed) -> {
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
                                       FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENTS,
                                       FeedServicesAccessControl.ACCESS_GLOBAL_SEARCH);
                    });
    
                this.userProvider.deleteGroup(oldGrp);
            });

        this.userProvider.findGroupByName("analyst")
            .ifPresent(oldGrp -> {
                UserGroup analystsGroup = createDefaultGroup("analysts", "Analysts");
    
                oldGrp.getUsers().forEach(user -> analystsGroup.addUser(user));
    
                actionsProvider.getAllowedActions(AllowedActions.SERVICES)
                    .ifPresent((allowed) -> {
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
                                       FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENTS,
                                       FeedServicesAccessControl.ACCESS_GLOBAL_SEARCH);
                    });
    
                this.userProvider.deleteGroup(oldGrp);
            });
    }

    protected UserGroup createDefaultGroup(String groupName, String title) {
        UserGroup newGroup = userProvider.ensureGroup(groupName);
        newGroup.setTitle(title);
        return newGroup;
    }
}
