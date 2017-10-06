/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade.v080;

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
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.datasource.security.DatasourceAccessControl;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

/**
 * Adds the entity-level permissions for the feed manager.
 */
@Component("feedManagerSecurityUpgradeAction080")
@Order(800)  // Order only relevant during fresh installs
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
    public boolean isTargetFreshInstall() {
        return true;
    }

    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("Defining feed manager entity permissions for version: {}", startingVersion);

        //@formatter:off
        builder
            .module(AllowedActions.SERVICES)
                .action(FeedServicesAccessControl.ACCESS_TABLES)
                .action(FeedServicesAccessControl.ACCESS_VISUAL_QUERY)
                .action(FeedServicesAccessControl.ACCESS_SERVICE_LEVEL_AGREEMENTS)
                .action(FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENTS)
                .add()
            .module(AllowedActions.FEED)
                .action(FeedAccessControl.ACCESS_FEED)
                .action(FeedAccessControl.EDIT_SUMMARY)
                .action(FeedAccessControl.ACCESS_DETAILS)
                .action(FeedAccessControl.EDIT_DETAILS)
                .action(FeedAccessControl.DELETE)
                .action(FeedAccessControl.ENABLE_DISABLE)
                .action(FeedAccessControl.EXPORT)
                .action(FeedAccessControl.ACCESS_OPS)
                .action(FeedAccessControl.CHANGE_PERMS)
                .add()
            .module(AllowedActions.CATEGORY)
                .action(CategoryAccessControl.ACCESS_CATEGORY)
                .action(CategoryAccessControl.EDIT_SUMMARY)
                .action(CategoryAccessControl.ACCESS_DETAILS)
                .action(CategoryAccessControl.EDIT_DETAILS)
                .action(CategoryAccessControl.DELETE)
                .action(CategoryAccessControl.CREATE_FEED)
                .action(CategoryAccessControl.CHANGE_PERMS)
                .add()
            .module(AllowedActions.TEMPLATE)
                .action(TemplateAccessControl.ACCESS_TEMPLATE)
                .action(TemplateAccessControl.EDIT_TEMPLATE)
                .action(TemplateAccessControl.DELETE)
                .action(TemplateAccessControl.EXPORT)
                .action(TemplateAccessControl.CREATE_FEED)
                .action(TemplateAccessControl.CHANGE_PERMS)
                .add()
            .module(AllowedActions.DATASOURCE)
                .action(DatasourceAccessControl.ACCESS_DATASOURCE)
                .action(DatasourceAccessControl.EDIT_SUMMARY)
                .action(DatasourceAccessControl.ACCESS_DETAILS)
                .action(DatasourceAccessControl.EDIT_DETAILS)
                .action(DatasourceAccessControl.DELETE)
                .action(DatasourceAccessControl.CHANGE_PERMS)
                .add()
            .build();
        //@formatter:on
    }

}
