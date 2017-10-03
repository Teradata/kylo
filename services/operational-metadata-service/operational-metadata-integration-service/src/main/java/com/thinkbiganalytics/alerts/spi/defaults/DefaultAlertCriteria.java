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
 *
 */
import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.support.QueryBase;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.alerts.api.core.BaseAlertCriteria;
import com.thinkbiganalytics.metadata.jpa.alerts.DefaultAlertSummary;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert;
import com.thinkbiganalytics.metadata.jpa.alerts.QJpaAlert;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/3/17.
 */
public class DefaultAlertCriteria extends BaseAlertCriteria {

    protected JPAQueryFactory queryFactory;

    public DefaultAlertCriteria(JPAQueryFactory queryFactory){
        this.queryFactory = queryFactory;
    }

    public JPAQuery<JpaAlert> createQuery() {
        QJpaAlert alert = QJpaAlert.jpaAlert;

        JPAQuery<JpaAlert> query = queryFactory
            .select(alert)
            .from(alert)
            .limit(getLimit());

        List<Predicate> preds = filter(alert);
        BooleanBuilder orFilter = orFilter(alert);

        // When limiting and using "after" criteria only, we need to sort ascending to get the next n values after the given id/time.
        // In all other cases sort descending. The results will be ordered correctly when aggregated by the provider.
        if (getLimit() != Integer.MAX_VALUE && getAfterTime() != null && getBeforeTime() == null) {
            query.orderBy(alert.createdTime.asc());
        } else {
            query.orderBy(alert.createdTime.desc());
        }

        return addWhere(query,preds,orFilter);

    }



    public JPAQuery<AlertSummary> createSummaryQuery() {
        QJpaAlert alert = QJpaAlert.jpaAlert;

        JPAQuery
            query = queryFactory.select(
            Projections.bean(DefaultAlertSummary.class,
                             alert.typeString.as("type"),
                             alert.subtype.as("subtype"),
                             alert.level.as("level"),
                             alert.count().as("count"),
                             alert.createdTimeMillis.max().as("lastAlertTimestamp"))
        )
            .from(alert)
            .groupBy(alert.typeString, alert.subtype, alert.level);
        List<Predicate> preds = filter(alert);

        BooleanBuilder orFilter = orFilter(alert);

        return (JPAQuery<AlertSummary>)  addWhere(query, preds, orFilter);

    }


    protected JPAQuery addWhere(QueryBase query, List<Predicate> preds, BooleanBuilder orFilter){
        if (preds.isEmpty() && !orFilter.hasValue()) {
            return (JPAQuery)query;
        } else {
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            preds.forEach(p -> booleanBuilder.and(p));
            if (orFilter.hasValue()) {
                booleanBuilder.and(orFilter);
            }
            return (JPAQuery)query.where(booleanBuilder);
        }
    }


    protected BooleanBuilder orFilter(QJpaAlert alert){
        BooleanBuilder globalFilter = new BooleanBuilder();
        if(StringUtils.isNotBlank(getOrFilter())) {
            Lists.newArrayList(StringUtils.split(getOrFilter(), ",")).stream().forEach(filter -> {
                filter = StringUtils.trim(filter);
                if(filter != null) {
                    BooleanBuilder booleanBuilder = new BooleanBuilder();
                    List<Predicate> preds = new ArrayList<>();
                    try {
                        Alert.State state = Alert.State.valueOf(filter.toUpperCase());
                        preds.add(alert.state.eq(state));
                    }catch (IllegalArgumentException e){

                    }

                    preds.add( alert.typeString.like(filter.concat("%")));
                    preds.add(alert.subtype.like(filter.concat("%")));
                    booleanBuilder.andAnyOf(preds.toArray(new Predicate[preds.size()]));
                    globalFilter.and(booleanBuilder);
                }
            });


        }
        return globalFilter;
    }

    protected List<Predicate> filter(QJpaAlert alert){
        List<Predicate> preds = new ArrayList<>();
        // The "state" criteria now means filter by an alert's current state.
        // To support filtering by any state the alert has transitioned through
        // we can add the commented out code below (the old state behavior)
//            if (getTransitions().size() > 0) {
//                QAlertChangeEvent event = QAlertChangeEvent.alertChangeEvent;
//                query.join(alert.events, event);
//                preds.add(event.state.in(getTransitions()));
//            }

        if (getStates().size() > 0) {
            preds.add(alert.state.in(getStates()));
        }
        if (getLevels().size() > 0) {
            preds.add(alert.level.in(getLevels()));
        }
        if (getAfterTime() != null) {
            preds.add(alert.createdTime.gt(getAfterTime()));
        }
        if (getBeforeTime() != null) {
            preds.add(alert.createdTime.lt(getBeforeTime()));
        }
        if (getAfterTime() != null) {
            preds.add(alert.createdTime.gt(getAfterTime()));
        }
        if (getBeforeTime() != null) {
            preds.add(alert.createdTime.lt(getBeforeTime()));
        }
        if (!isIncludeCleared()) {
            preds.add(alert.cleared.isFalse());
        }

        if (getTypes().size() > 0) {
            BooleanBuilder likes = new BooleanBuilder();
            getTypes().stream()
                .map(uri -> alert.typeString.like(uri.toASCIIString().concat("%")))
                .forEach(pred -> likes.or(pred));
            preds.add(likes);
        }

        if (getSubtypes().size() > 0) {
            preds.add(alert.subtype.in(getSubtypes()));
        }
        return preds;
    }


}
