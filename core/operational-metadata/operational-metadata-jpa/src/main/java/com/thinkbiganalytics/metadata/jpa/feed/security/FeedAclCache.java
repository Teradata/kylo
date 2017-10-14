package com.thinkbiganalytics.metadata.jpa.feed.security;
/*-
 * #%L
 * kylo-operational-metadata-jpa
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

import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAclEntry;
import com.thinkbiganalytics.metadata.config.RoleSetExposingSecurityExpressionRoot;
import com.thinkbiganalytics.metadata.jpa.cache.CacheBackedProviderListener;
import com.thinkbiganalytics.metadata.jpa.cache.CacheListBean;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.inject.Inject;

/**
 * Created by sr186054 on 9/29/17.
 */
public class FeedAclCache extends CacheListBean<String, FeedOpsAclEntry> implements CacheBackedProviderListener<JpaFeedOpsAclEntry.EntryId, JpaFeedOpsAclEntry> {

    private static final Logger log = LoggerFactory.getLogger(FeedAclCache.class);
    @Inject
    AccessController accessController;


    public boolean isAvailable() {
        return isPopulated();
    }

    public boolean hasAccess(RoleSetExposingSecurityExpressionRoot userContext, String feedId) {
        if (StringUtils.isBlank(feedId) || !accessController.isEntityAccessControlled()) {
            return true;
        }
        return get(feedId).stream()
            .anyMatch(acl -> ((acl.getPrincipalType() == FeedOpsAclEntry.PrincipalType.GROUP && userContext.getGroups().contains(acl.getPrincipalName()))
                              || (acl.getPrincipalType() == FeedOpsAclEntry.PrincipalType.USER && userContext.getName().equals(acl.getPrincipalName()))));
    }


    public boolean hasAccess(String feedId) {
        return hasAccess(userContext(), feedId);
    }


    public RoleSetExposingSecurityExpressionRoot userContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new RoleSetExposingSecurityExpressionRoot(authentication);
    }


    public boolean isUserCacheAvailable() {
        return (!accessController.isEntityAccessControlled() || (accessController.isEntityAccessControlled() && isAvailable()));
    }


    private String getKey(FeedOpsAclEntry entry) {
        return entry.getFeedId().toString();
    }

    @Override
    public void onAddedItem(JpaFeedOpsAclEntry.EntryId key, JpaFeedOpsAclEntry value) {
        add(key.getUuid().toString(), value);
    }

    @Override
    public void onRemovedItem(JpaFeedOpsAclEntry value) {
        remove(getKey(value), value);
    }

    @Override
    public void onRemoveAll() {
        invalidateAll();
    }

    @Override
    public void onPopulated() {
        log.info("FeedAclCache populated.");
        setPopulated(true);
    }

    @Override
    public boolean isEqual(FeedOpsAclEntry value1, FeedOpsAclEntry value2) {
        return value1.getId().equals(value2.getId());
    }
}
