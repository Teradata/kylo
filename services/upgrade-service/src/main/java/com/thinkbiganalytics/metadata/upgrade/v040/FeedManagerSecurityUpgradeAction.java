/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade.v040;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

/**
 * Adds the services-level permissions for the feed manager.
 */
@Component("feedManagerSecurityUpgradeAction040")
@Order(400)  // Order only relevant during fresh installs
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class FeedManagerSecurityUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(FeedManagerSecurityUpgradeAction.class);

    @Inject
    private ActionsModuleBuilder builder;


    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.4", "0", "");
    }
    
    @Override
    public boolean isTargetFreshInstall() {
        return true;
    }

    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("Defining feed manager permissions for version: {}", startingVersion);

        //@formatter:off
        builder
            .module(AllowedActions.SERVICES)
                .action(FeedServicesAccessControl.FEEDS_SUPPORT)
                .action(FeedServicesAccessControl.ACCESS_FEEDS)
                .action(FeedServicesAccessControl.EDIT_FEEDS)
                .action(FeedServicesAccessControl.IMPORT_FEEDS)
                .action(FeedServicesAccessControl.EXPORT_FEEDS)
                .action(FeedServicesAccessControl.ADMIN_FEEDS)
                .action(FeedServicesAccessControl.ACCESS_CATEGORIES)
                .action(FeedServicesAccessControl.EDIT_CATEGORIES)
                .action(FeedServicesAccessControl.ADMIN_CATEGORIES)
                .action(FeedServicesAccessControl.ACCESS_TEMPLATES)
                .action(FeedServicesAccessControl.EDIT_TEMPLATES)
                .action(FeedServicesAccessControl.IMPORT_TEMPLATES)
                .action(FeedServicesAccessControl.EXPORT_TEMPLATES)
                .action(FeedServicesAccessControl.ADMIN_TEMPLATES)
                .add()
            .build();
        //@formatter:on
    }

}
