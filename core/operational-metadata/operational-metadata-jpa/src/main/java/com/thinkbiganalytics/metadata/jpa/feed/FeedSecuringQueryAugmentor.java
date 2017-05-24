package com.thinkbiganalytics.metadata.jpa.feed;

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

import com.querydsl.core.types.dsl.ComparablePath;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Query augmentor which checks whether access to feed is allowed by ACLs defined in FeedAclIndexEntry table by
 * adding following expression to queries: <br>
 * <code>and exists (select 1 from JpaFeedOpsAclEntry as x where feed.id = x.feedId and x.principalName in :#{principal.roleSet})</code>
 */
public class FeedSecuringQueryAugmentor extends FeedAclIndexQueryAugmentor {

    @Override
    protected <S, T, ID extends Serializable> Path<Object> getFeedId(JpaEntityInformation<T, ID> entityInformation, Root<S> root) {
        SingularAttribute<?, ?> idAttribute = entityInformation.getIdAttribute();
        return root.get(idAttribute.getName());
    }

    @Override
    protected ComparablePath<UUID> getFeedId() {
        QJpaOpsManagerFeed root = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        return root.id.uuid;
    }

    @Override
    protected QOpsManagerFeedId getOpsManagerFeedId() {
       return QJpaOpsManagerFeed.jpaOpsManagerFeed.id;
    }
}
