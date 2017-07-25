/**
 *
 */
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.ID;
import com.thinkbiganalytics.alerts.api.Alert.Level;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertNotfoundException;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.alerts.api.core.BaseAlertCriteria;
import com.thinkbiganalytics.alerts.spi.AlertDescriptor;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver;
import com.thinkbiganalytics.alerts.spi.AlertSource;
import com.thinkbiganalytics.alerts.spi.EntityIdentificationAlertContent;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.sla.QServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.jpa.alerts.DefaultAlertSummary;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert.AlertId;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertChangeEvent;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertRepository;
import com.thinkbiganalytics.metadata.jpa.alerts.QJpaAlert;
import com.thinkbiganalytics.metadata.jpa.feed.FeedAclIndexQueryAugmentor;
import com.thinkbiganalytics.metadata.jpa.feed.JpaOpsManagerFeed;
import com.thinkbiganalytics.metadata.jpa.feed.QJpaOpsManagerFeed;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiFeedProcessorStats;
import com.thinkbiganalytics.metadata.jpa.sla.QJpaServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.jpa.support.CommonFilterTranslations;
import com.thinkbiganalytics.metadata.jpa.support.GenericQueryDslFilter;
import com.thinkbiganalytics.metadata.jpa.support.QueryDslPathInspector;
import com.thinkbiganalytics.security.role.SecurityRole;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 *
 */
public class DefaultAlertManager extends QueryDslRepositorySupport implements AlertManager {

    @Inject
    private JPAQueryFactory queryFactory;

    @Inject
    private MetadataAccess metadataAccess;

    private Set<AlertNotifyReceiver> alertReceivers = Collections.synchronizedSet(new HashSet<>());
    private JpaAlertRepository repository;


    static final ImmutableMap<String, String> alertSlaFilters =
        new ImmutableMap.Builder<String, String>()
            .put("sla", "name")
            .put("slaDescription", "description").build();

    private AlertSource.ID id = new AlertManagerId();

    @Override
    public AlertSource.ID getId() {
       return id;
    }

    /**
     * @param repo
     */
    public DefaultAlertManager(JpaAlertRepository repo) {
        super(JpaAlert.class);
        this.repository = repo;
        CommonFilterTranslations.addFilterTranslations(QJpaServiceLevelAgreementDescription.class,alertSlaFilters);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#resolve(java.io.Serializable)
     */
    @Override
    public Alert.ID resolve(Serializable id) {
        if (id instanceof JpaAlert.AlertId) {
            return (JpaAlert.AlertId) id;
        } else {
            return new JpaAlert.AlertId(id);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlertDescriptors()
     */
    @Override
    public Set<AlertDescriptor> getAlertDescriptors() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#addReceiver(com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver)
     */
    @Override
    public void addReceiver(AlertNotifyReceiver receiver) {
        this.alertReceivers.add(receiver);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#removeReceiver(com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver)
     */
    @Override
    public void removeReceiver(AlertNotifyReceiver receiver) {
        this.alertReceivers.remove(receiver);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#criteria()
     */
    @Override
    public AlertCriteria criteria() {
        return new Criteria();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlert(com.thinkbiganalytics.alerts.api.Alert.ID)
     */
    @Override
    public Optional<Alert> getAlert(Alert.ID id) {
        return this.metadataAccess.read(() -> {
            return Optional.of(findAlert(id).map(a -> asValue(a)).orElseThrow(() -> new AlertNotfoundException(id)));
        }, MetadataAccess.SERVICE);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlerts()
     */
    @Override
    public Iterator<Alert> getAlerts(AlertCriteria criteria) {
        return this.metadataAccess.read(() -> {
            Criteria critImpl = (Criteria) (criteria == null ? criteria() : criteria);
            return critImpl.createQuery().fetch().stream()
                .map(a -> asValue(a))
                .collect(Collectors.toList()) // Need to terminate the stream while still in a transaction
                .iterator();
        }, MetadataAccess.SERVICE);
    }

    public Iterator<AlertSummary> getAlertsSummary(AlertCriteria criteria) {
        return this.metadataAccess.read(() -> {
            Criteria critImpl = (Criteria) (criteria == null ? criteria() : criteria);
            return critImpl.createSummaryQuery().fetch().stream()
                .collect(Collectors.toList()) // Need to terminate the stream while still in a transaction
                .iterator();
        }, MetadataAccess.SERVICE);
    }
//
//    /* (non-Javadoc)
//     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlerts(org.joda.time.DateTime)
//     */
//    @Override
//    public Iterator<Alert> getAlerts(DateTime since) {
//        return this.metadataAccess.read(() -> {
//            return repository.findAlertsAfter(since).stream()
//                            .map(a -> asValue(a))
//                            .collect(Collectors.toList()) // Need to terminate the stream while still in a transaction
//                            .iterator();
//        }, MetadataAccess.SERVICE);
//    }
//
//    /* (non-Javadoc)
//     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlerts(com.thinkbiganalytics.alerts.api.Alert.ID)
//     */
//    @Override
//    public Iterator<Alert> getAlerts(ID since) {
//        return this.metadataAccess.read(() -> {
//            return getAlert(since)
//                            .map(a -> getAlerts(a.getCreatedTime()))
//                            .orElseThrow(() -> new AlertNotfoundException(since));
//        }, MetadataAccess.SERVICE);
//    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertManager#addDescriptor(com.thinkbiganalytics.alerts.spi.AlertDescriptor)
     */
    @Override
    public boolean addDescriptor(AlertDescriptor descriptor) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertManager#create(java.net.URI, com.thinkbiganalytics.alerts.api.Alert.Level, java.lang.String, java.io.Serializable)
     */
    @Override
    public <C extends Serializable> Alert create(URI type, String subtype,Level level, String description, C content) {
        final Principal user = SecurityContextHolder.getContext().getAuthentication() != null
                               ? SecurityContextHolder.getContext().getAuthentication()
                               : null;

        Alert created = this.metadataAccess.commit(() -> {
            JpaAlert alert = new JpaAlert(type, subtype,level, user, description, content);
            this.repository.save(alert);
            return asValue(alert);
        }, MetadataAccess.SERVICE);

        notifyReceivers(1);
        return created;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertManager#getResponse(com.thinkbiganalytics.alerts.api.Alert)
     */
    @Override
    public AlertResponse getResponse(Alert alert) {
        JpaAlert.AlertId idImpl = (JpaAlert.AlertId) resolve(alert.getId());
        return new TransactionalResponse(idImpl);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertManager#remove(com.thinkbiganalytics.alerts.api.Alert.ID)
     */
    @Override
    public Alert remove(Alert.ID id) {
        JpaAlert.AlertId idImpl = (JpaAlert.AlertId) resolve(id);

        return this.metadataAccess.commit(() -> {
            JpaAlert alert = repository.findOne(idImpl);
            this.repository.delete(id);
            return alert;
        }, MetadataAccess.SERVICE);
    }

    protected Optional<JpaAlert> findAlert(Alert.ID id) {
        JpaAlert.AlertId idImpl = (AlertId) resolve(id);
        return Optional.ofNullable(repository.findOne(idImpl));
    }

    protected Alert asValue(Alert alert) {
        return new ImmutableAlert(alert, this);
    }

    protected JpaAlert clearAlert(JpaAlert.AlertId id) {
        return this.metadataAccess.commit(() -> {
            JpaAlert alert = repository.findOne(id);
            alert.setCleared(true);
            return alert;
        }, MetadataAccess.SERVICE);
    }

    protected <C extends Serializable> Alert changeAlert(JpaAlert.AlertId id, State state, String descr, C content) {
        final Principal user = SecurityContextHolder.getContext().getAuthentication() != null
                               ? SecurityContextHolder.getContext().getAuthentication()
                               : null;

        Alert changed = this.metadataAccess.commit(() -> {
            JpaAlert alert = findAlert(id).orElseThrow(() -> new AlertNotfoundException(id));
            JpaAlertChangeEvent event = new JpaAlertChangeEvent(state, user, descr, content);
            alert.addEvent(event);
            return asValue(alert);
        }, MetadataAccess.SERVICE);

        notifyReceivers(1);
        return changed;
    }

    protected void notifyReceivers(int count) {
        Set<AlertNotifyReceiver> receivers;
        synchronized (this.alertReceivers) {
            receivers = new HashSet<>(this.alertReceivers);
        }
        receivers.forEach(a -> a.alertsAvailable(count));
    }

    private static class ImmutableAlert implements Alert {

        private final AlertManager source;
        private final Alert.ID id;
        private final String description;
        private final Level level;
        private final URI type;
        private final String subtype;
        private final DateTime createdTime;
        private final Serializable content;
        private final boolean cleared;
        private final List<AlertChangeEvent> events;

        public ImmutableAlert(Alert alert, AlertManager mgr) {
            this.source = mgr;
            this.id = alert.getId();
            this.content = alert.getContent();
            this.description = alert.getDescription();
            this.level = alert.getLevel();
            this.type = alert.getType();
            this.subtype = alert.getSubtype();
            this.cleared = alert.isCleared();
            this.createdTime = alert.getCreatedTime();
            this.events = Collections.unmodifiableList(alert.getEvents().stream()
                                                           .map(a -> new ImmutableAlertChangeEvent(a))
                                                           .collect(Collectors.toList()));
        }

        @Override
        public AlertManager getSource() {
            return source;
        }

        @Override
        public Alert.ID getId() {
            return id;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Level getLevel() {
            return level;
        }

        @Override
        public URI getType() {
            return type;
        }

        @Override
        public String getSubtype() {
            return subtype;
        }

        @Override
        public State getState() {
            return this.events.get(0).getState();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Serializable getContent() {
            return content;
        }

        @Override
        public List<AlertChangeEvent> getEvents() {
            return events;
        }

        @Override
        public DateTime getCreatedTime() {
            return this.createdTime;
        }

        @Override
        public boolean isCleared() {
            return this.cleared;
        }

        @Override
        public boolean isActionable() {
            return true;
        }
    }

    private static class ImmutableAlertChangeEvent implements AlertChangeEvent {

        private final DateTime changeTime;
        private final State state;
        private final Principal user;
        private final String description;
        private final Serializable content;

        public ImmutableAlertChangeEvent(AlertChangeEvent event) {
            this.changeTime = event.getChangeTime();
            this.state = event.getState();
            this.user = event.getUser();
            this.description = event.getDescription();
            this.content = event.getContent();
        }

        public DateTime getChangeTime() {
            return changeTime;
        }

        public State getState() {
            return state;
        }

        @Override
        public Principal getUser() {
            return user;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        public Serializable getContent() {
            return this.content;
        }
    }

    private class TransactionalResponse implements AlertResponse {

        private final JpaAlert.AlertId id;

        public TransactionalResponse(JpaAlert.AlertId id) {
            super();
            this.id = id;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#inProgress()
         */
        @Override
        public Alert inProgress(String descr) {
            return inProgress(descr, null);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#inProgress(java.io.Serializable)
         */
        @Override
        public <C extends Serializable> Alert inProgress(String descr, C content) {
            return changeAlert(this.id, State.IN_PROGRESS, descr, content);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#handle()
         */
        @Override
        public Alert handle(String descr) {
            return handle(descr, null);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#handle(java.io.Serializable)
         */
        @Override
        public <C extends Serializable> Alert handle(String descr, C content) {
            return changeAlert(this.id, State.HANDLED, descr, content);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#unHandle()
         */
        @Override
        public Alert unhandle(String descr) {
            return unhandle(descr, null);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#unhandle(java.io.Serializable)
         */
        @Override
        public <C extends Serializable> Alert unhandle(String descr, C content) {
            return changeAlert(this.id, State.UNHANDLED, descr, content);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#clear()
         */
        @Override
        public void clear() {
            clearAlert(this.id);
        }
    }

    private class Criteria extends BaseAlertCriteria {


        public JPAQuery<JpaAlert> createQuery() {
            QJpaAlert alert = QJpaAlert.jpaAlert;
            QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
            QJpaServiceLevelAgreementDescription sla = QJpaServiceLevelAgreementDescription.jpaServiceLevelAgreementDescription;

            JPAQuery<JpaAlert> query = queryFactory
                .select(alert)
                .from(alert)
                .leftJoin(feed).on(feed.id.uuid.eq(alert.entityId.value).and(alert.entityType.eq(Expressions.stringPath("'FEED'"))))
                .leftJoin(sla).on(sla.slaId.uuid.eq(alert.entityId.value).and(alert.entityType.eq(Expressions.stringPath("'SLA'"))))
                .limit(getLimit());

            List<Predicate> preds = filter(alert);
            BooleanBuilder orFilter = orFilter(alert,feed,sla);

            // When limiting and using "after" criteria only, we need to sort ascending to get the next n values after the given id/time.
            // In all other cases sort descending. The results will be ordered correctly when aggregated by the provider.
            if (getLimit() != Integer.MAX_VALUE && getAfterTime() != null && getBeforeTime() == null) {
                query.orderBy(alert.createdTime.asc());
            } else {
                query.orderBy(alert.createdTime.desc());
            }

            if (preds.isEmpty() && !orFilter.hasValue()) {
                return query;
            } else {
                BooleanBuilder booleanBuilder = new BooleanBuilder();
                preds.forEach(p -> booleanBuilder.and(p));
                if(orFilter.hasValue()) {
                    booleanBuilder.and(orFilter);
                }
                return query.where(booleanBuilder);
            }

        }

        public JPAQuery<AlertSummary> createSummaryQuery() {
            QJpaAlert alert = QJpaAlert.jpaAlert;
            QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
            QJpaServiceLevelAgreementDescription sla = QJpaServiceLevelAgreementDescription.jpaServiceLevelAgreementDescription;

            JPAQuery
                query = queryFactory.select(
                Projections.bean(DefaultAlertSummary.class,
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
                .leftJoin(feed).on(feed.id.uuid.eq(alert.entityId.value).and(alert.entityType.endsWithIgnoreCase(Expressions.stringPath("'FEED'"))))
                .leftJoin(sla).on(sla.slaId.uuid.eq(alert.entityId.value).and(alert.entityType.endsWithIgnoreCase(Expressions.stringPath("'SLA'"))))
                .groupBy(alert.typeString, alert.subtype);
            List<Predicate> preds = filter(alert);

            BooleanBuilder orFilter = orFilter(alert,feed,sla);

            if (preds.isEmpty() && !orFilter.hasValue()) {
                return query;
            } else {
                BooleanBuilder booleanBuilder = new BooleanBuilder();
                preds.forEach(p -> booleanBuilder.and(p));
                if(orFilter.hasValue()) {
                    booleanBuilder.and(orFilter);
                }
                return (JPAQuery<AlertSummary>) query.where(booleanBuilder);
            }
        }
        private String filterStringForFeedAlertEntities(String keyword){
            return CommonFilterTranslations.feedFilters.keySet().stream().map(key -> key+"=~"+keyword).collect(Collectors.joining(","));
        }

        private String filterStringForSlaAlertEntities(String keyword){
            return alertSlaFilters.keySet().stream().map(key -> key+"=~"+keyword).collect(Collectors.joining(","));
        }

        private BooleanBuilder orFilter(QJpaAlert alert, QJpaOpsManagerFeed feed,QJpaServiceLevelAgreementDescription sla){
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
                    preds.add( alert.description.likeIgnoreCase(filter.concat("%")));
                    preds.add( alert.entityType.likeIgnoreCase(filter.concat("%")));
                    preds.add( alert.typeString.likeIgnoreCase(filter.concat("%")));
                    preds.add(alert.subtype.like(filter.concat("%")));
                    //add in joins on the feed or sla name
                    preds.add(GenericQueryDslFilter.buildOrFilter(feed,filterStringForFeedAlertEntities(filter)));
                        preds.add(GenericQueryDslFilter.buildOrFilter(sla,filterStringForSlaAlertEntities(filter)));

                    booleanBuilder.andAnyOf(preds.toArray(new Predicate[preds.size()]));
                    globalFilter.and(booleanBuilder);
                    }
                });


            }
            return globalFilter;
        }

        private List<Predicate> filter(QJpaAlert alert){
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



    public static class AlertManagerId implements ID {


        private static final long serialVersionUID = 7691516770322504702L;

        private String idValue = DefaultAlertManager.class.getSimpleName();


        public AlertManagerId() {
        }


        public String getIdValue() {
            return idValue;
        }

        @Override
        public String toString() {
            return idValue;
        }

    }

    public <C extends Serializable>  EntityIdentificationAlertContent createEntityIdentificationAlertContent(String entityId, SecurityRole.ENTITY_TYPE entityType, C content) {
        if(content instanceof EntityIdentificationAlertContent){
           ((EntityIdentificationAlertContent) content).setEntityId(entityId);
           ((EntityIdentificationAlertContent) content).setEntityType(entityType);
           return (EntityIdentificationAlertContent) content;
        }
        else {
            EntityIdentificationAlertContent c = new EntityIdentificationAlertContent();
            c.setEntityId(entityId);
            c.setEntityType(entityType);
            c.setContent(content);
            return c;
        }
    }

}
