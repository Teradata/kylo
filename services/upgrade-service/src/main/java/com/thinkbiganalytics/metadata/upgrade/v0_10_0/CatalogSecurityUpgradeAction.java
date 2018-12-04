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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.metadata.api.catalog.security.ConnectorAccessControl;
import com.thinkbiganalytics.metadata.api.user.UserProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.security.AccessControlConfigurator;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeAction;
import com.thinkbiganalytics.server.upgrade.UpgradeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.Session;


/**
 * This action defines the permissions and roles introduce for the catalog in Kylo v0.10.0.
 */
@Component("catalogSecurityUpgradeAction0.10.0")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class CatalogSecurityUpgradeAction implements UpgradeAction {

    private static final Logger log = LoggerFactory.getLogger(CatalogSecurityUpgradeAction.class);
    

    @Inject
    private AccessControlConfigurator configurator;
    
    @Inject
    private AccessController accessController;

    @Inject
    private ActionsModuleBuilder builder;

    @Inject
    private AllowedEntityActionsProvider actionsProvider;
    
    @Inject
    private UserProvider userProvider;
    
    private volatile boolean upgrading = true;
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.server.upgrade.UpgradeAction#isTargetPreFreshInstall(com.thinkbiganalytics.KyloVersion)
     */
    @Override
    public boolean isTargetPreFreshInstall(KyloVersion currentVersion) {
        // This method is only called when there is a fresh install, i.e. not an upgrade.
        this.upgrading = false;
        return true;
    }
   
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        this.upgrading = true;
        return version.matches("0.10", "0", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Setting up catalog access control: {}", targetVersion);
        Session session = JcrMetadataAccess.getActiveSession();
        
        JcrUtil.getOrCreateNode(JcrUtil.getNode(session, SecurityPaths.PROTOTYPES), "connector", "tba:allowedActions");
        JcrUtil.getOrCreateNode(JcrUtil.getNode(session, SecurityPaths.ROLES), "connector", "tba:rolesFolder");
        
        // Define the new catalog actions both when upgrading or during a fresh install.
        //@formatter:off
        builder
            .module(AllowedActions.SERVICES)
                .action(FeedServicesAccessControl.ACCESS_CATALOG)
                .action(FeedServicesAccessControl.ACCESS_CONNECTORS)
                .action(FeedServicesAccessControl.ADMIN_CONNECTORS)
                .add()
            .module(AllowedActions.CONNECTOR)
                .action(ConnectorAccessControl.ACCESS_CONNECTOR)
                .action(ConnectorAccessControl.EDIT_CONNECTOR)
                .action(ConnectorAccessControl.ACTIVATE_CONNECTOR)
                .action(ConnectorAccessControl.CHANGE_PERMS)
                .action(ConnectorAccessControl.CREATE_DATA_SOURCE)
                .add()
            .build();
        //@formatter:on
        
        // Define the new default roles and access control for the catalog only when upgrading.  
        // The CreateDefaultUsersGroupsAction will have already granted these during a fresh install.
        if (upgrading) {
            try {
                session.save();
                
                if (session.nodeExists("/metadata/security/prototypes/services/accessFeedsSupport/accessDatasources")) {
                    // Move the data source actions under the "accessCatalog" hierarchy.
                    session.getWorkspace().move("/metadata/security/prototypes/services/accessFeedsSupport/accessDatasources", "/metadata/security/prototypes/services/accessCatalog/accessDatasources");
                    // Reconfigure the services actions with the updated hierarchy.
                    configurator.configureServicesActions();
                    // Remove the old action node
                    session.removeItem("/metadata/security/services/accessFeedsSupport/accessDatasources");
                    session.save();
                }
            } catch (RepositoryException e) {
                throw new UpgradeException("Failed to move data source actions under the 'Access Catalog' hierarch", e);
            }
            
            this.configurator.createDefaultCatalogRoles();
            
            if (this.accessController.isEntityAccessControlled()) {
                this.configurator.ensureCatalogAccessControl();
            }
            
            // Add the catalog permissions to the appropriate default groups if they are present.
            userProvider.findGroupByName("designers")
                .ifPresent(designersGroup -> {
                    actionsProvider.getAllowedActions(AllowedActions.SERVICES)
                        .ifPresent(allowed -> {
                            allowed.enable(designersGroup.getRootPrincial(),
                                           FeedServicesAccessControl.ACCESS_CATALOG,
                                           FeedServicesAccessControl.ADMIN_CONNECTORS);
                        });
                });
            userProvider.findGroupByName("analysts")
                .ifPresent(analystsGroup -> {
                    actionsProvider.getAllowedActions(AllowedActions.SERVICES)
                        .ifPresent(allowed -> {
                            allowed.enable(analystsGroup.getRootPrincial(),
                                           FeedServicesAccessControl.ACCESS_CATALOG,
                                           FeedServicesAccessControl.ACCESS_CONNECTORS);
                        });
                });
        }
        
    }
}
