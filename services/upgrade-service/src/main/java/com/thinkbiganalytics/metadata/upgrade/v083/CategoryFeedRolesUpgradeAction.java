package com.thinkbiganalytics.metadata.upgrade.v083;

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
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.category.CategoryDetails;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.inject.Inject;

/**
 * Ensures that all categories have the new, mandatory feedRoleMemberships node.
 */
@Component("categoryFeedRolesUpgradeAction083")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class CategoryFeedRolesUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(CategoryFeedRolesUpgradeAction.class);

    @Inject
    private AccessController accessController;
    
    @Inject
    private CategoryProvider categoryProvider;

    @Inject
    private SecurityRoleProvider roleProvider;

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "3", "");
    }

    @Override
    public void upgradeTo(final KyloVersion startingVersion) {
        log.info("Upgrading category feed roles from version: {}", startingVersion);

        if (this.accessController.isEntityAccessControlled()) {
            final List<SecurityRole> feedRoles = this.roleProvider.getEntityRoles(SecurityRole.FEED);
            
            this.categoryProvider.findAll().stream()
                .map(JcrCategory.class::cast)
                .map(cat -> cat.getDetails().get())
                .filter(details -> ! JcrUtil.hasNode(details.getNode(), CategoryDetails.FEED_ROLE_MEMBERSHIPS))
                .forEach(details -> { 
                    log.info("Updating roles for category: {}", JcrUtil.getName(JcrUtil.getParent(details.getNode())));
                    details.enableFeedRoles(feedRoles); 
                    });
        }
    }
}
