/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade.v081;

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
 * Adds the entity-level permissions for the feed manager.
 */
@Component("feedManagerSecurityUpgradeAction080")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class FeedManagerSecurityUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(FeedManagerSecurityUpgradeAction.class);

    @Inject
    private ActionsModuleBuilder builder;


    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "0", "");
    }

    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("Defining feed manager entity permissions for version: {}", startingVersion);

        //@formatter:off
        builder
            .module(AllowedActions.SERVICES)
                .action(FeedServicesAccessControl.ACCESS_GLOBAL_SEARCH)
                .add()
            .build();
        //@formatter:on
    }

}
