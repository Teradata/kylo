/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade.v070;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
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
@Component("feedManagerSecurityUpgradeAction070")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class FeedManagerSecurityUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(FeedManagerSecurityUpgradeAction.class);

    @Inject
    private ActionsModuleBuilder builder;


    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.7", "0", "");
    }

    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("Defining feed manager datasource permissions for version: {}", startingVersion);

        //@formatter:off
        builder
            .module(AllowedActions.SERVICES)
                .action(FeedServicesAccessControl.ACCESS_DATASOURCES)
                .action(FeedServicesAccessControl.EDIT_DATASOURCES)
                .action(FeedServicesAccessControl.ADMIN_DATASOURCES)
                .add()
            .build();
        //@formatter:on
    }

}
