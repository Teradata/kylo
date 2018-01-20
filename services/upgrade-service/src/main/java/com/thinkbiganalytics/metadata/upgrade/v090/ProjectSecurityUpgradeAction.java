/**
 *
 */
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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.project.security.ProjectAccessControl;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Adds the services-level permissions for the feed manager.
 */
@Component("projectSecurityUpgradeAction")
@Order(900)  // Order only relevant during fresh installs
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class ProjectSecurityUpgradeAction implements UpgradeState {

    private static final Logger logger = LoggerFactory.getLogger(ProjectSecurityUpgradeAction.class);

    @Inject
    private ActionsModuleBuilder builder;


    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.9", "0", "");
    }

    @Override
    public boolean isTargetFreshInstall() {
        return true;
    }

    @PostConstruct
    public void postConstruct() {
        logger.debug("ProjectSecurityUpgradeAction bean was created");
    }

    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        logger.info("Defining project permissions for version: {}", startingVersion);

        //@formatter:off
        builder
            .module(AllowedActions.SERVICES)
            .action(ProjectAccessControl.ACCESS_PROJECT)
            .action(ProjectAccessControl.EDIT_PROJECT)
            .action(ProjectAccessControl.DELETE_PROJECT)
            .action(ProjectAccessControl.CHANGE_PERMS)
            .add()
            .module(AllowedActions.PROJECTS)
            .action(ProjectAccessControl.ACCESS_PROJECT)
            .action(ProjectAccessControl.EDIT_PROJECT)
            .action(ProjectAccessControl.DELETE_PROJECT)
            .action(ProjectAccessControl.CHANGE_PERMS)
            .add()
            .build();
        //@formatter:on
    }

}
