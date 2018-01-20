package com.thinkbiganalytics.alerts.spi.defaults;

/*-
 * #%L
 * thinkbig-alerts-default
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

import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.alerts.api.EntityAwareAlertCriteria;
import com.thinkbiganalytics.alerts.spi.EntityIdentificationAlertContent;
import com.thinkbiganalytics.metadata.alerts.KyloEntityAwareAlertManager;
import com.thinkbiganalytics.metadata.api.alerts.KyloEntityAwareAlertSummary;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert;
import com.thinkbiganalytics.metadata.jpa.alerts.QJpaAlert;
import com.thinkbiganalytics.metadata.jpa.feed.FeedAclIndexQueryAugmentor;
import com.thinkbiganalytics.metadata.jpa.feed.QJpaOpsManagerFeed;
import com.thinkbiganalytics.metadata.jpa.sla.QJpaServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.jpa.support.CommonFilterTranslations;
import com.thinkbiganalytics.metadata.jpa.support.GenericQueryDslFilter;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class KyloEntityAwareAlertCriteria extends DefaultAlertCriteria implements EntityAwareAlertCriteria {

    private static final Logger log = LoggerFactory.getLogger(KyloEntityAwareAlertCriteria.class);

    private AccessController controller;

    private Set<EntityIdentificationAlertContent> entityCriteria = new HashSet<>();


    public KyloEntityAwareAlertCriteria(JPAQueryFactory queryFactory, AccessController controller) {
        super(queryFactory);
        this.controller = controller;
    }

    public KyloEntityAwareAlertCriteria entityCriteria(EntityIdentificationAlertContent entityIdentificationAlertContent, EntityIdentificationAlertContent... others) {
        if (entityIdentificationAlertContent != null) {
            this.entityCriteria.add(entityIdentificationAlertContent);
        }
        if (others != null) {
            Arrays.stream(others).forEach(c -> this.entityCriteria.add(c));
        }
        return this;
    }

    @Override
    public AlertCriteria transfer(AlertCriteria criteria) {
        AlertCriteria c = super.transfer(criteria);
        AtomicReference<KyloEntityAwareAlertCriteria> updated = new AtomicReference<>((KyloEntityAwareAlertCriteria) c);
        this.entityCriteria.forEach((t) -> updated.set(updated.get().entityCriteria(t)));
        return updated.get();
    }

    public JPAQuery<JpaAlert> createQuery() {
        QJpaAlert alert = QJpaAlert.jpaAlert;
        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        QJpaOpsManagerFeed slaFeed = new QJpaOpsManagerFeed("slaFeed");
        QJpaServiceLevelAgreementDescription sla = QJpaServiceLevelAgreementDescription.jpaServiceLevelAgreementDescription;

        JPAQuery<JpaAlert> query = queryFactory
            .select(alert).distinct()
            .from(alert)
            .leftJoin(feed).on(feed.id.uuid.eq(alert.entityId.value).and(alert.entityType.eq(Expressions.stringPath("'FEED'"))))
            .leftJoin(sla).on(sla.slaId.uuid.eq(alert.entityId.value).and(alert.entityType.eq(Expressions.stringPath("'SLA'"))))
            .leftJoin(sla.feeds, slaFeed)
            .limit(getLimit());

        List<Predicate> preds = filter(alert);
        boolean entityAccessControlled = !isAsServiceAccount() && controller.isEntityAccessControlled();
        preds.add(feed.isNull().or(feed.isNotNull().and(FeedAclIndexQueryAugmentor.generateExistsExpression(feed.id, entityAccessControlled))));
        preds.add(slaFeed.isNull().or(slaFeed.isNotNull().and(FeedAclIndexQueryAugmentor.generateExistsExpression(slaFeed.id, entityAccessControlled))));
        BooleanBuilder orFilter = orFilter(alert, feed, sla);
        addEntityFilter(alert, preds);
        // When limiting and using "after" criteria only, we need to sort ascending to get the next n values after the given id/time.
        // In all other cases sort descending. The results will be ordered correctly when aggregated by the provider.
        if (getLimit() != Integer.MAX_VALUE && getAfterTime() != null && getBeforeTime() == null) {
            query.orderBy(alert.createdTime.asc());
        } else {
            query.orderBy(alert.createdTime.desc());
        }
        return super.addWhere(query, preds, orFilter);

    }

    public JPAQuery<AlertSummary> createSummaryQuery() {
        QJpaAlert alert = QJpaAlert.jpaAlert;
        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        QJpaOpsManagerFeed slaFeed = new QJpaOpsManagerFeed("slaFeed");
        QJpaServiceLevelAgreementDescription sla = QJpaServiceLevelAgreementDescription.jpaServiceLevelAgreementDescription;

        JPAQuery
            query = queryFactory.select(
            Projections.bean(KyloEntityAwareAlertSummary.class,
                             alert.typeString.as("type"),
                             alert.subtype.as("subtype"),
                             alert.level.as("level"),
                             feed.id.as("feedId"),
                             feed.name.as("feedName"),
                             sla.slaId.as("slaId"),
                             sla.name.as("slaName"),
                             alert.count().as("count"),
                             alert.createdTimeMillis.max().as("lastAlertTimestamp"))
        )
            .from(alert)
            .leftJoin(feed).on(feed.id.uuid.eq(alert.entityId.value).and(alert.entityType.eq(Expressions.stringPath("'FEED'"))))
            .leftJoin(sla).on(sla.slaId.uuid.eq(alert.entityId.value).and(alert.entityType.eq(Expressions.stringPath("'SLA'"))))
            .leftJoin(sla.feeds, slaFeed)
            .groupBy(alert.typeString, alert.subtype, feed.id, feed.name, sla.slaId, sla.name, alert.level);
        List<Predicate> preds = filter(alert);
        boolean entityAccessControlled = !isAsServiceAccount() && controller.isEntityAccessControlled();
        preds.add(feed.isNull().or(feed.isNotNull().and(FeedAclIndexQueryAugmentor.generateExistsExpression(feed.id, entityAccessControlled))));
        preds.add(slaFeed.isNull().or(slaFeed.isNotNull().and(FeedAclIndexQueryAugmentor.generateExistsExpression(slaFeed.id, entityAccessControlled))));
        BooleanBuilder orFilter = orFilter(alert, feed, sla);
        addEntityFilter(alert, preds);

        return (JPAQuery<AlertSummary>) super.addWhere(query, preds, orFilter);

    }

    private String filterStringForFeedAlertEntities(String keyword) {
        return CommonFilterTranslations.feedFilters.keySet().stream().map(key -> key + "==" + keyword).collect(Collectors.joining(","));
    }

    private String filterStringForSlaAlertEntities(String keyword) {
        return KyloEntityAwareAlertManager.alertSlaFilters.keySet().stream().map(key -> key + "=~" + keyword).collect(Collectors.joining(","));
    }

    private void addOrFilter(EntityPathBase base, Map<String, String> filterMap, List<Predicate> preds, String filter) {
        filterMap.keySet().stream().forEach(key -> {
            String f = "";
            if (filter.contains(",")) {
                f = key + "==\"" + filter + "\"";
            } else {
                f = key + "=~" + filter;
            }
            preds.add(GenericQueryDslFilter.buildOrFilter(base, f));
        });
    }

    private void addEntityFilter(QJpaAlert alert, List<Predicate> preds) {
        if (entityCriteria != null && !entityCriteria.isEmpty()) {
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            List<Predicate> entityPreds = new ArrayList<>();
            entityCriteria.stream().forEach(c -> {
                entityPreds.add(alert.entityType.eq(c.getEntityType().name()).and(alert.entityId.value.eq(UUID.fromString(c.getEntityId()))));
            });
            BooleanBuilder allEntityPredicates = booleanBuilder.andAnyOf(entityPreds.toArray(new Predicate[entityPreds.size()]));
            preds.add(allEntityPredicates);

        }
    }

    private BooleanBuilder orFilter(QJpaAlert alert, QJpaOpsManagerFeed feed, QJpaServiceLevelAgreementDescription sla) {
        BooleanBuilder globalFilter = new BooleanBuilder();
        if (StringUtils.isNotBlank(getOrFilter())) {
            Lists.newArrayList(StringUtils.split(getOrFilter(), ",")).stream().forEach(filter -> {
                filter = StringUtils.trim(filter);
                if (filter != null) {
                    List<String> in = null;
                    if (filter.contains("||")) {
                        //replace the OR || with commas for IN clause
                        in = Arrays.asList(StringUtils.split(filter, "||")).stream().map(f -> StringUtils.trim(f)).collect(Collectors.toList());
                        filter = in.stream().collect(Collectors.joining(","));
                    }
                    BooleanBuilder booleanBuilder = new BooleanBuilder();
                    List<Predicate> preds = new ArrayList<>();
                    try {
                        Alert.State state = Alert.State.valueOf(filter.toUpperCase());
                        preds.add(alert.state.eq(state));
                    } catch (IllegalArgumentException e) {

                    }
                    if (in != null) {
                        preds.add(alert.description.in(in));
                        preds.add(alert.entityType.in(in));
                        preds.add(alert.typeString.in(in));
                        preds.add(alert.subtype.in(in));
                        //add in joins on the feed or sla name
                        addOrFilter(feed, CommonFilterTranslations.feedFilters, preds, filter);
                        addOrFilter(sla, KyloEntityAwareAlertManager.alertSlaFilters, preds, filter);
                    } else {
                        preds.add(alert.description.likeIgnoreCase(filter.concat("%")));
                        preds.add(alert.entityType.likeIgnoreCase(filter.concat("%")));
                        preds.add(alert.typeString.likeIgnoreCase(filter.concat("%")));
                        preds.add(alert.subtype.like(filter.concat("%")));
                        //add in joins on the feed or sla name
                        addOrFilter(feed, CommonFilterTranslations.feedFilters, preds, filter);
                        addOrFilter(sla, KyloEntityAwareAlertManager.alertSlaFilters, preds, filter);

                    }

                    booleanBuilder.andAnyOf(preds.toArray(new Predicate[preds.size()]));
                    globalFilter.and(booleanBuilder);
                }
            });


        }
        return globalFilter;
    }


    public JPAQuery<JpaAlert> createEntityQuery() {
        QJpaAlert alert = QJpaAlert.jpaAlert;

        JPAQuery<JpaAlert> query = queryFactory
            .select(alert)
            .from(alert)
            .limit(getLimit());

        List<Predicate> preds = filter(alert);
        addEntityFilter(alert, preds);
        if (entityCriteria == null || entityCriteria.isEmpty()) {
            log.warn("Unable to apply Entity Query.  No Entity Criteria was specified!");
            preds.add(Expressions.stringTemplate("1").eq(Expressions.stringTemplate("2")));
        }
        return super.addWhere(query, preds, new BooleanBuilder());

    }
}
