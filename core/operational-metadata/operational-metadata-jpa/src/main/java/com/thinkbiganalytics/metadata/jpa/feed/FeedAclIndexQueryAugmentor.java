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

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.thinkbiganalytics.metadata.config.RoleSetExposingSecurityExpressionRoot;
import com.thinkbiganalytics.metadata.jpa.feed.security.JpaFeedOpsAclEntry;
import com.thinkbiganalytics.metadata.jpa.feed.security.QJpaFeedOpsAclEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Query augmentor which checks whether access to feed is allowed by ACLs defined in FeedAclIndexEntry table by
 * adding following expression to queries: <br>
 * <code>and exists (select 1 from JpaFeedOpsAclEntry as x where {root}.id = x.feedId and x.principalName in :#{principal.roleSet})</code>
 */
public class FeedAclIndexQueryAugmentor implements QueryAugmentor {

    private static final Logger LOG = LoggerFactory.getLogger(FeedAclIndexQueryAugmentor.class);

    public <S, T, ID extends Serializable> Specification<S> augment(Specification<S> spec, Class domainClass,
                                        JpaEntityInformation<T, ID> entityInformation) {
        LOG.debug("QueryAugmentor.augment");

        return (root, query, cb) -> {
            //and exists (select 1 from JpaFeedOpsAclEntry as x where {root}.id = x.feedId and x.principalName in :#{principal.roleSet})

            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<JpaFeedOpsAclEntry> fromAcl = subquery.from(JpaFeedOpsAclEntry.class);

            subquery.select(fromAcl.get("feedId"));

            SingularAttribute<?, ?> idAttribute = entityInformation.getIdAttribute();
            javax.persistence.criteria.Predicate rootFeedIdEqualToAclFeedId = cb.equal(root.get(idAttribute.getName()), fromAcl.get("feedId"));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Collection<String> principalRoles = authentication == null ? new ArrayList<>(0) : new RoleSetExposingSecurityExpressionRoot(authentication).getRoleSet();
            javax.persistence.criteria.Predicate aclPrincipalNameInRoles = fromAcl.get("principalName").in(principalRoles);

            javax.persistence.criteria.Predicate feedIdEqualsAndPrincipalInRoles = cb.and(rootFeedIdEqualToAclFeedId, aclPrincipalNameInRoles);
            subquery.where(feedIdEqualsAndPrincipalInRoles);

            javax.persistence.criteria.Predicate securingPredicate = cb.exists(subquery);

            if (spec != null) {
                javax.persistence.criteria.Predicate predicate = spec.toPredicate(root, query, cb);
                return cb.and(predicate, securingPredicate);
            } else {
                return securingPredicate;
            }
        };
    }

    @Override
    public List<Predicate> augment(Predicate[] predicate) {
        LOG.debug("FeedAclIndexQueryAugmentor.augment(Predicate[])");

        QJpaFeedOpsAclEntry aclEntry = QJpaFeedOpsAclEntry.jpaFeedOpsAclEntry;
        com.querydsl.core.types.Predicate p = null;


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<String> principalRoles = authentication == null ? new ArrayList<>(0) : new RoleSetExposingSecurityExpressionRoot(authentication).getRoleSet();

        QJpaOpsManagerFeed root = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        JPQLQuery<JpaFeedOpsAclEntry> subquery = JPAExpressions.selectFrom(aclEntry).where(aclEntry.feedId.eq(root.id.uuid).and(aclEntry.principalName.in(principalRoles)));

        BooleanExpression exists = subquery.exists();

        List<Predicate> predicates = new ArrayList<>();
        predicates.addAll(Arrays.asList(predicate));
        predicates.add(exists);

        return predicates;
    }

}
