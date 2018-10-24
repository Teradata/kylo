package com.thinkbiganalytics.metadata.upgrade.fresh;

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
import com.thinkbiganalytics.metadata.modeshape.security.AccessControlConfigurator;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Initializes and updates the service-level available actions based on the current state of the 
 * services' actions prototype tree.
 */
@Component("createRolesActionFreshInstall")
@Order(CreateRolesAction.ORDER)
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class CreateRolesAction implements UpgradeAction {

    private static final Logger log = LoggerFactory.getLogger(CreateRolesAction.class);
    
    public static final int ORDER = UpgradeAction.LATE_ORDER + 1;

    @Inject
    private AccessControlConfigurator configurator;
    
    @Inject
    private AccessController accessController;
    
    @Override
    public boolean isTargetPreFreshInstall(KyloVersion finalVersion) {
        return true;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#upgradeFrom(com.thinkbiganalytics.metadata.api.app.KyloVersion)
     */
    @Override
    public void upgradeTo(KyloVersion version) {
        if (this.accessController.isEntityAccessControlled()) {
            log.info("Setting up default entity roles for version: " + version);
            configurator.configureDefaultEntityRoles();
        }
    }
}
