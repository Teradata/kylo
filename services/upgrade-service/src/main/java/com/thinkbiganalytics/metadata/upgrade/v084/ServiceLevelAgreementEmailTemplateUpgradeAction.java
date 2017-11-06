package com.thinkbiganalytics.metadata.upgrade.v084;

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component("slaEmailTemplateUpgradeAction084")
@Order(Ordered.LOWEST_PRECEDENCE)
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class ServiceLevelAgreementEmailTemplateUpgradeAction implements UpgradeState {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ActionsModuleBuilder builder;

    private static final Logger log = LoggerFactory.getLogger(ServiceLevelAgreementEmailTemplateUpgradeAction.class);

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "4", "");
    }

    @Override
    public boolean isTargetFreshInstall() {
        return true;
    }


    public void upgradeTo(final KyloVersion startingVersion) {
        metadata.commit(() -> builder
            .module(AllowedActions.SERVICES)
            .action(FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE)
            .add()
            .build(), MetadataAccess.SERVICE);
    }

}
