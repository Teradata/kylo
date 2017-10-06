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
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedEntityActionsProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jcr.Node;

@Component("servicesSecurityUpgradeActionFreshInstall")
@Order(Ordered.LOWEST_PRECEDENCE - 1)  // Must execute before CreateDefaultUsersGroupsAction
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class ServicesSecurityUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(ServicesSecurityUpgradeAction.class);

    @Inject
    private JcrAllowedEntityActionsProvider actionsProvider;

    @Override
    public boolean isTargetFreshInstall() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#upgradeFrom(com.thinkbiganalytics.metadata.api.app.KyloVersion)
     */
    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("Setting up Services permissions for version: " + startingVersion);
        
        Node securityNode = JcrUtil.getNode(JcrMetadataAccess.getActiveSession(), SecurityPaths.SECURITY.toString());
        Node svcAllowedNode = JcrUtil.getOrCreateNode(securityNode, AllowedActions.SERVICES, JcrAllowedActions.NODE_TYPE);

        actionsProvider.createEntityAllowedActions(AllowedActions.SERVICES, svcAllowedNode);
    }
}
