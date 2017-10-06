package com.thinkbiganalytics.metadata.jpa.feed;
/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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

import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.jpa.cache.UserCacheBean;
import com.thinkbiganalytics.metadata.jpa.common.EntityAccessControlled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Cache backed by a repository that will quickly get access to a Feed based upon its feed id
 */
public class OpsManagerFeedCacheById extends UserCacheBean<OpsManagerFeed.ID, OpsManagerFeed, JpaOpsManagerFeed, JpaOpsManagerFeed.ID> {

    private static final Logger log = LoggerFactory.getLogger(OpsManagerFeedCacheById.class);

    @Inject
    private OpsManagerFeedRepository repository;


    @Override
    @EntityAccessControlled
    public List<JpaOpsManagerFeed> fetchAllWithAcl() {
        return repository.findAllWithAcl();
    }

    @Override
    public List<JpaOpsManagerFeed> fetchAll() {
        return repository.findAll();
    }

    @Override
    @EntityAccessControlled
    public JpaOpsManagerFeed fetchByIdWithAcl(OpsManagerFeed.ID cacheKey) {
        return repository.findByIdWithAcl(cacheKey);
    }

    @Override
    public JpaOpsManagerFeed fetchById(OpsManagerFeed.ID cacheKey) {
        return repository.findByIdWithoutAcl(cacheKey);
    }

    @Override

    public List<JpaOpsManagerFeed> fetchForIds(Set<OpsManagerFeed.ID> cacheKeys) {
        return repository.findByFeedIdsWithoutAcl(new ArrayList<>(cacheKeys));
    }

    @Override
    @EntityAccessControlled
    public List<JpaOpsManagerFeed> fetchForIdsWithAcl(Set<OpsManagerFeed.ID> cacheKeys) {
        return repository.findByFeedIdsWithAcl(new ArrayList<>(cacheKeys));
    }

    @Override
    public String getFeedId(OpsManagerFeed item) {
        return item.getId().toString();
    }

    @Override
    public OpsManagerFeed transform(JpaOpsManagerFeed dbItem) {
        return dbItem;
    }


    public OpsManagerFeed.ID getCacheKey(JpaOpsManagerFeed dbItem) {
        return dbItem.getId();
    }

    public OpsManagerFeedCacheById() {

    }


}
