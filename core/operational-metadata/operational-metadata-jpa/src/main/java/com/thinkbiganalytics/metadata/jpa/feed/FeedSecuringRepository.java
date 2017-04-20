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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

/**
 * Repository which ensures that access to feeds is secured by FeedAclIndex table
 */
public class FeedSecuringRepository extends AugmentableQueryRepositoryImpl {

    private static final Logger LOG = LoggerFactory.getLogger(FeedSecuringRepository.class);

    public FeedSecuringRepository(JpaEntityInformation entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.setAugmentor(new FeedSecuringQueryAugmentor());
    }

    public List<JpaOpsManagerFeed> findByName(String name) {
        LOG.debug("FeedSecuringRepository.findByName");

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<JpaOpsManagerFeed> query = builder.createQuery(JpaOpsManagerFeed.class);
        Root<JpaOpsManagerFeed> root = query.from(JpaOpsManagerFeed.class);

        Expression<Boolean> nameIsEqualToParam = builder.equal(root.get("name"), name);

        Specification<JpaOpsManagerFeed> spec = null;
        Specification<JpaOpsManagerFeed> secured = augmentor.augment(spec, JpaOpsManagerFeed.class, entityInformation);
        query.where(builder.and(nameIsEqualToParam, secured.toPredicate(root, query, builder)));

        return entityManager.createQuery(query).getResultList();
    }

}
