/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade.v040;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.security.service.user.UsersGroupsAccessContol;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

/**
 * Adds the services-level permissions for users and groups.
 */
@Component("usersGroupsSecurityUpgradeAction040")
@Order(400)  // Order only relevant during fresh installs
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class UsersGroupsSecurityUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(UsersGroupsSecurityUpgradeAction.class);

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
        log.info("Defining users & groups permissions for version: {}", startingVersion);

        //@formatter:off
        builder
            .module(AllowedActions.SERVICES)
                .action(UsersGroupsAccessContol.USERS_GROUPS_SUPPORT)
                .action(UsersGroupsAccessContol.ACCESS_USERS)
                .action(UsersGroupsAccessContol.ADMIN_USERS)
                .action(UsersGroupsAccessContol.ACCESS_GROUPS)
                .action(UsersGroupsAccessContol.ADMIN_GROUPS)
                .add()
            .build();
        //@formatter:on
    }

}
