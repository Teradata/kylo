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
import com.thinkbiganalytics.metadata.api.security.MetadataAccessControl;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

/**
 * Adds the encryption permission for services.
 */
@Component("metadataSecurityUpgradeAction081")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class MetadataSecurityUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(MetadataSecurityUpgradeAction.class);

    @Inject
    private ActionsModuleBuilder builder;


    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "1", "");
    }

    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("Defining metadata permissions for version: {}", startingVersion);
        
        //@formatter:off
        builder
            .module(AllowedActions.SERVICES)
                .action(MetadataAccessControl.ACCESS_METADATA)
                .action(MetadataAccessControl.ADMIN_METADATA)
                .add()
            .build();
        //@formatter:on
    }

}
