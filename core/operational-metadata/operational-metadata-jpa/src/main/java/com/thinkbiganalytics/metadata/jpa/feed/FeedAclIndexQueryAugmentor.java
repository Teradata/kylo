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
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.thinkbiganalytics.metadata.config.RoleSetExposingSecurityExpressionRoot;
import com.thinkbiganalytics.metadata.jpa.feed.security.JpaFeedOpsAclEntry;
import com.thinkbiganalytics.metadata.jpa.feed.security.JpaFeedOpsAclEntry.PrincipalType;
import com.thinkbiganalytics.metadata.jpa.feed.security.QJpaFeedOpsAclEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * Secures queries by checking whether access to them is allowed by having matching roles for current
 * user principal in FeedAclIndex table
 */
public abstract class FeedAclIndexQueryAugmentor implements QueryAugmentor {

    private static final Logger LOG = LoggerFactory.getLogger(FeedAclIndexQueryAugmentor.class);
    private static final StringTemplate CONSTANT_ONE = Expressions.stringTemplate("1");
    private static final BooleanExpression ONE_EQUALS_ONE = CONSTANT_ONE.eq(CONSTANT_ONE);

    protected abstract <S, T, ID extends Serializable> Path<Object> getFeedId(JpaEntityInformation<T, ID> entityInformation, Root<S> root);

    protected abstract ComparablePath<UUID> getFeedId();

    protected abstract QOpsManagerFeedId getOpsManagerFeedId();

    @Override
    public <S, T, ID extends Serializable> Specification<S> augment(Specification<S> spec, Class<S> domainClass,
                                                                    JpaEntityInformation<T, ID> entityInformation) {
        LOG.debug("QueryAugmentor.augment");

        return (root, query, criteriaBuilder) -> {
            //and exists (select 1 from JpaFeedOpsAclEntry as x where {root}.id = x.feedId and x.principalName in :#{principal.roleSet})

            RoleSetExposingSecurityExpressionRoot userCxt = getUserContext();
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<JpaFeedOpsAclEntry> fromAcl = subquery.from(JpaFeedOpsAclEntry.class);

            subquery.select(fromAcl.get("feedId"));

            Path<Object> feedId = getFeedId(entityInformation, root);
            javax.persistence.criteria.Predicate rootFeedIdEqualToAclFeedId = criteriaBuilder.equal(feedId, fromAcl.get("feedId"));

            javax.persistence.criteria.Predicate aclPrincipalInGroups = fromAcl.get("principalName").in(userCxt.getGroups());
            javax.persistence.criteria.Predicate aclPrincipalTypeIsGroup = criteriaBuilder.equal(fromAcl.get("principalType"), PrincipalType.GROUP);
            javax.persistence.criteria.Predicate acePrincipalGroupMatch = criteriaBuilder.and(aclPrincipalInGroups, aclPrincipalTypeIsGroup);
            javax.persistence.criteria.Predicate aclPrincipalEqUser = criteriaBuilder.equal(fromAcl.get("principalName"), userCxt.getName());
            javax.persistence.criteria.Predicate aclPrincipalTypeIsUser = criteriaBuilder.equal(fromAcl.get("principalType"), PrincipalType.USER);
            javax.persistence.criteria.Predicate acePrincipalUserMatch = criteriaBuilder.and(aclPrincipalEqUser, aclPrincipalTypeIsUser);
            javax.persistence.criteria.Predicate acePrincipalMatch = criteriaBuilder.or(acePrincipalGroupMatch, acePrincipalUserMatch);

            javax.persistence.criteria.Predicate feedIdEqualsAndPrincipalMatch = criteriaBuilder.and(rootFeedIdEqualToAclFeedId, acePrincipalMatch);
            subquery.where(feedIdEqualsAndPrincipalMatch);

            javax.persistence.criteria.Predicate securingPredicate = criteriaBuilder.exists(subquery);

            if (spec != null) {
                javax.persistence.criteria.Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
                return criteriaBuilder.and(predicate, securingPredicate);
            } else {
                return securingPredicate;
            }
        };
    }

    @Override
    public List<Predicate> augment(Predicate[] predicate) {
        LOG.debug("FeedAclIndexQueryAugmentor.augment(Predicate[])");
        QOpsManagerFeedId feed = getOpsManagerFeedId();

        BooleanExpression exists = generateExistsExpression(feed);

        List<Predicate> predicates = new ArrayList<>();
        predicates.addAll(Arrays.asList(predicate));
        predicates.add(exists);

        return predicates;
    }

    /**
     * Generates the Exist expression for the feed to feedacl table
     * @param feedId
     * @return exists expression
     */
    private static BooleanExpression generateExistsExpression(QOpsManagerFeedId feedId) {
        LOG.debug("FeedAclIndexQueryAugmentor.generateExistsExpression(QOpsManagerFeedId)");

        RoleSetExposingSecurityExpressionRoot userCxt = getUserContext();
        QJpaFeedOpsAclEntry aclEntry = QJpaFeedOpsAclEntry.jpaFeedOpsAclEntry;
        JPQLQuery<JpaFeedOpsAclEntry> subquery = JPAExpressions.selectFrom(aclEntry).where(aclEntry.feed.id.eq(feedId)
                                                                                               .and(aclEntry.principalName.in(userCxt.getGroups()).and(aclEntry.principalType.eq(PrincipalType.GROUP))
                                                                                                        .or(aclEntry.principalName.eq(userCxt.getName()).and(aclEntry.principalType.eq(PrincipalType.USER))))
        );
       return subquery.exists();
    }

    public static BooleanExpression generateExistsExpression(QOpsManagerFeedId id, boolean entityAccessControlled) {
        if (entityAccessControlled) {
            return generateExistsExpression(id);
        } else {
            return ONE_EQUALS_ONE;
        }
    }

    @Override
    public <S, T, ID extends Serializable> CriteriaQuery<Long> getCountQuery(EntityManager entityManager, JpaEntityInformation<T, ID> entityInformation, Specification<S> spec, Class<S> domainClass) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<S> root = query.from(domainClass);

        if (query.isDistinct()) {
            query.select(builder.countDistinct(root));
        } else {
            query.select(builder.count(root));
        }

        Specification<S> secured = this.augment(spec, domainClass, entityInformation);
        query.where(secured.toPredicate(root, query, builder));

        return query;
    }
    
    private static RoleSetExposingSecurityExpressionRoot getUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new RoleSetExposingSecurityExpressionRoot(authentication);
    }
}
