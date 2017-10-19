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

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Cache backed by a repository that will quickly get access to a Feed based upon its unique categorySystemName.feedSystemName
 */
public class OpsManagerFeedCacheByName extends UserCacheBean<String, OpsManagerFeed, JpaOpsManagerFeed, JpaOpsManagerFeed.ID> {

    private static final Logger log = LoggerFactory.getLogger(OpsManagerFeedCacheByName.class);

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
    public JpaOpsManagerFeed fetchByIdWithAcl(String cacheKey) {
        return repository.findByNameWithAcl(cacheKey);
    }

    @Override
    public JpaOpsManagerFeed fetchById(String cacheKey) {
        return repository.findByNameWithoutAcl(cacheKey);
    }

    @Override

    public List<JpaOpsManagerFeed> fetchForIds(Set<String> cacheKeys) {
        return repository.findByNamesWithoutAcl(cacheKeys);
    }

    @Override
    @EntityAccessControlled
    public List<JpaOpsManagerFeed> fetchForIdsWithAcl(Set<String> cacheKeys) {
        return repository.findByNamesWithAcl(cacheKeys);
    }

    @Override
    public String getFeedId(OpsManagerFeed item) {
        return item.getId().toString();
    }

    @Override
    public OpsManagerFeed transform(JpaOpsManagerFeed dbItem) {
        return dbItem;
    }


    public OpsManagerFeedCacheByName() {

    }

    public String getCacheKey(JpaOpsManagerFeed dbItem) {
        return dbItem.getName();
    }


}
