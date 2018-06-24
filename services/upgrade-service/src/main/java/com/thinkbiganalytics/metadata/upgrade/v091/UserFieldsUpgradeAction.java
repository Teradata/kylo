package com.thinkbiganalytics.metadata.upgrade.v091;

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
import javax.jcr.Session;

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.UserFieldDescriptors;
import com.thinkbiganalytics.metadata.modeshape.extension.ExtensionsConstants;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.upgrade.v083.HiveColumnsUpgradeAction;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * Removes category node reference property from all feeds.  A feed's category is now only accessed through
 * the feed node's parentage.
 */
@Component("UserFieldsUpgradeAction091")
@Profile(KyloUpgrader.KYLO_UPGRADE)
@Order(HiveColumnsUpgradeAction.UPGRADE_ORDER - 1) // Run before HiveColumnsUpgradeAction when upgrading to v0.8.3
public class UserFieldsUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(UserFieldsUpgradeAction.class);
    
    @Inject
    private ExtensibleTypeProvider extensibleTypeProvider;
    
    @Inject
    private CategoryProvider categoryProvider;

    @Inject
    private FeedProvider feedProvider;
    
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "3", "") || version.matches("0.9", "1", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Refactoring user field management: {}", targetVersion);
        
        if (targetVersion.matches("0.8", "3", "")) {
            // When upgrading to v0.8.3 this must be initialized in order not to
            // break HiveColumnsUpgradeAction. 
            initializeGlobalFields();
        }
        
        if (targetVersion.matches("0.9", "1", "")) {
            initializeGlobalFields();
            
            this.extensibleTypeProvider.getTypes().forEach(type -> {
                String typeName = type.getName();
                
                if (typeName.equals(ExtensionsConstants.USER_CATEGORY)) {
                    upgradeGlobalCategoryFields(type);
                } else if (typeName.equals(ExtensionsConstants.USER_FEED)) {
                    upgradeGlobalFeedFields(type);
                } else if (typeName.startsWith(ExtensionsConstants.USER_CATEGORY)) {
                    upgradeCategoryFeedFields(type);
                }
            });
        }
    }

    private void initializeGlobalFields() {
        Session session = JcrMetadataAccess.getActiveSession();
        JcrUtil.getOrCreateNode(JcrUtil.getNode(session, EntityUtil.pathForCategory()), EntityUtil.CATEGORY_USER_FIELDS, UserFieldDescriptors.NODE_TYPE);
        JcrUtil.getOrCreateNode(JcrUtil.getNode(session, EntityUtil.pathForCategory()), EntityUtil.FEED_USER_FIELDS, UserFieldDescriptors.NODE_TYPE);
    }

    private void upgradeGlobalCategoryFields(ExtensibleType type) {
        this.categoryProvider.setUserFields(type.getUserFieldDescriptors());
        this.extensibleTypeProvider.deleteType(type.getId());
    }

    private void upgradeGlobalFeedFields(ExtensibleType type) {
        this.feedProvider.setUserFields(type.getUserFieldDescriptors());
        this.extensibleTypeProvider.deleteType(type.getId());
    }

    private void upgradeCategoryFeedFields(ExtensibleType type) {
        String[] nameParts = type.getName().split(":");  // tba:category:<category name>:feed
        String catName = nameParts[2];
        Category category = this.categoryProvider.findBySystemName(catName);
        
        if (category != null) {
            this.categoryProvider.setFeedUserFields(category.getId(), type.getUserFieldDescriptors());
            this.extensibleTypeProvider.deleteType(type.getId());
        }
    }
}
