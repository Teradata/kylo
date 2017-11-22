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

import com.google.common.collect.Sets;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.Level;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertNotfoundException;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.alerts.service.ServiceStatusAlerts;
import com.thinkbiganalytics.alerts.sla.AssessmentAlerts;
import com.thinkbiganalytics.alerts.spi.AlertDescriptor;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver;
import com.thinkbiganalytics.alerts.spi.AlertSource;
import com.thinkbiganalytics.alerts.spi.EntityIdentificationAlertContent;
import com.thinkbiganalytics.cluster.ClusterMessage;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.ClusterServiceMessageReceiver;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.alerts.OperationalAlerts;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert.AlertId;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertChangeEvent;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertRepository;
import com.thinkbiganalytics.security.role.SecurityRole;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 */
public class DefaultAlertManager extends QueryDslRepositorySupport implements AlertManager, ClusterServiceMessageReceiver {

    private static final Logger log = LoggerFactory.getLogger(DefaultAlertManager.class);


    @Inject
    private JPAQueryFactory queryFactory;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private ClusterService clusterService;

    private Set<AlertNotifyReceiver> alertReceivers = Collections.synchronizedSet(new HashSet<>());
    private JpaAlertRepository repository;

    private AlertSource.ID id = new AlertManagerId();

    private Long lastUpdatedTime;

    private Long previousUpdatedTime;

    /**
     * Map of the latest alerts summary for a given criteria
     */
    private Map<String, AlertSummaryCache> latestAlertsSummary = new ConcurrentHashMap<>();

    /**
     * Map of the latest alerts for a given criteria
     */
    private Map<String, AlertsCache> latestAlerts = new ConcurrentHashMap<>();


    @PostConstruct
    private void init() {
        clusterService.subscribe(this,AlertManagerChangedClusterMessage.TYPE);
    }

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
        return new DefaultAlertCriteria(queryFactory);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlert(com.thinkbiganalytics.alerts.api.Alert.ID)
     */
    @Override
    public Optional<Alert> getAlert(Alert.ID id) {
        return this.metadataAccess.read(() -> {
            return Optional.of(findAlert(id).map(a -> asValue(a)).orElseThrow(() -> new AlertNotfoundException(id)));
        });
    }

    public Optional<Alert> getAlertAsServiceAccount(Alert.ID id) {
        return this.metadataAccess.read(() -> {
            return Optional.of(findAlert(id).map(a -> asValue(a)).orElseThrow(() -> new AlertNotfoundException(id)));
        }, MetadataAccess.SERVICE);
    }

    protected DefaultAlertCriteria ensureAlertCriteriaType(AlertCriteria criteria) {
        return (DefaultAlertCriteria) (criteria == null ? criteria() : criteria);
    }

    public Iterator<AlertSummary> getAlertsSummary(AlertCriteria criteria) {
        Long now = DateTime.now().getMillis();
        Principal[] principal = null;
        if (criteria != null && criteria.isAsServiceAccount()) {
            principal = new Principal[1];
            principal[0] = MetadataAccess.SERVICE;
        } else {
            principal = new Principal[0];
        }
        if (criteria.isOnlyIfChangesDetected() && !hasAlertsSummaryChanged(criteria)) {
            log.debug("Returning cached Alerts Summary data");
            return new ArrayList(latestAlertsSummary.get(criteria.toString()).getAlertSummaryList()).iterator();
        }
        log.debug("Query for Alerts Summary data");
        List<AlertSummary> latest = this.metadataAccess.read(() -> {
            DefaultAlertCriteria critImpl = ensureAlertCriteriaType(criteria);
            return critImpl.createSummaryQuery().fetch().stream()
                .collect(Collectors.toList());
        }, principal);
        if (criteria.isOnlyIfChangesDetected()) {
            latestAlertsSummary.put(criteria.toString(), new AlertSummaryCache(now, latest));
        }

        return latest.iterator();
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlerts()
     */
    @Override
    public Iterator<Alert> getAlerts(AlertCriteria criteria) {
        Long now = DateTime.now().getMillis();
        Principal[] principal = null;
        if (criteria != null && criteria.isAsServiceAccount()) {
            principal = new Principal[1];
            principal[0] = MetadataAccess.SERVICE;
        } else {
            principal = new Principal[0];
        }
        if (criteria.isOnlyIfChangesDetected() && !hasAlertsChanged(criteria)) {
            log.debug("Returning cached Alerts data");
            return new ArrayList(latestAlerts.get(criteria.toString()).getAlertList()).iterator();
        }
        log.debug("Query for Alerts data");
        List<Alert> alerts = this.metadataAccess.read(() -> {
            DefaultAlertCriteria critImpl = ensureAlertCriteriaType(criteria);
            return critImpl.createQuery().fetch().stream()
                .map(a -> asValue(a))
                .collect(Collectors.toList());
        }, principal);

        if (criteria.isOnlyIfChangesDetected()) {
            latestAlerts.put(criteria.toString(), new AlertsCache(now, alerts));
        }
        return alerts.iterator();
    }

    public Set<String> getAlertTypes() {
        Set<String> defaultAlertTypes = Sets.newHashSet(AssessmentAlerts.VIOLATION_ALERT_TYPE.toString(),
                                                        OperationalAlerts.JOB_FALURE_ALERT_TYPE.toString(),
                                                        ServiceStatusAlerts.SERVICE_STATUS_ALERT_TYPE.toString());
        Set<String> alertTypes = repository.findAlertTypes();
        if (alertTypes == null) {
            alertTypes = new HashSet<>();
        }
        //merge
        defaultAlertTypes.addAll(alertTypes);
        return defaultAlertTypes;
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
    public <C extends Serializable> Alert create(URI type, String subtype, Level level, String description, C content) {
        final Principal user = SecurityContextHolder.getContext().getAuthentication() != null
                               ? SecurityContextHolder.getContext().getAuthentication()
                               : null;
        //reset the subtype if the content is an Entity
        if (subtype == null) {
            subtype = "Other";
        }
        if (content != null && content instanceof EntityIdentificationAlertContent) {
            subtype = "Entity";
        }
        final String finalSubType = subtype;

        Alert created = this.metadataAccess.commit(() -> {
            JpaAlert alert = new JpaAlert(type, finalSubType, level, user, description, content);
            this.repository.save(alert);
            return asValue(alert);
        }, MetadataAccess.SERVICE);

        updateLastUpdatedTime();
        notifyReceivers(1);
        return created;
    }

    @Override
    public <C extends Serializable> Alert createEntityAlert(URI type, Level level, String description, EntityIdentificationAlertContent<C> content) {
        return create(type, null, level, description, content);
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

        JpaAlert jpaAlert = this.metadataAccess.commit(() -> {
            JpaAlert alert = repository.findOne(idImpl);
            this.repository.delete(id);
            return alert;
        }, MetadataAccess.SERVICE);
        updateLastUpdatedTime();
        return jpaAlert;
    }

    protected Optional<JpaAlert> findAlert(Alert.ID id) {
        JpaAlert.AlertId idImpl = (AlertId) resolve(id);
        return Optional.ofNullable(repository.findOne(idImpl));
    }

    protected Alert asValue(Alert alert) {
        return new ImmutableAlert(alert, this);
    }

    protected JpaAlert clearAlert(JpaAlert.AlertId id) {
        JpaAlert jpaAlert = this.metadataAccess.commit(() -> {
            JpaAlert alert = repository.findOne(id);
            alert.setCleared(true);
            return alert;
        }, MetadataAccess.SERVICE);
        updateLastUpdatedTime();
        return jpaAlert;
    }

    protected JpaAlert unclearAlert(JpaAlert.AlertId id) {
        JpaAlert jpaAlert = this.metadataAccess.commit(() -> {
            JpaAlert alert = repository.findOne(id);
            alert.setCleared(false);
            return alert;
        }, MetadataAccess.SERVICE);
        updateLastUpdatedTime();
        return jpaAlert;
    }


    protected <C extends Serializable> Alert updateAlertChangeEntry(JpaAlert.AlertId id, String descr, C content) {
        final Principal user = SecurityContextHolder.getContext().getAuthentication() != null
                               ? SecurityContextHolder.getContext().getAuthentication()
                               : null;

        Alert changed = this.metadataAccess.commit(() -> {
            JpaAlert alert = findAlert(id).orElseThrow(() -> new AlertNotfoundException(id));
            List<AlertChangeEvent> events = alert.getEvents();
            if (events != null && !events.isEmpty()) {
                JpaAlertChangeEvent event = (JpaAlertChangeEvent) events.get(0);
                event.setDescription(descr);
                event.setContent(content);
                event.setChangeTime(DateTime.now());
            }
            return asValue(alert);
        }, MetadataAccess.SERVICE);
        updateLastUpdatedTime();
        notifyReceivers(1);
        return changed;
    }

    protected <C extends Serializable> Alert changeAlert(JpaAlert.AlertId id, State state, String descr, C content) {
        final Principal user = SecurityContextHolder.getContext().getAuthentication() != null
                               ? SecurityContextHolder.getContext().getAuthentication()
                               : null;

        Alert changed = this.metadataAccess.commit(() -> {
            JpaAlert alert = findAlert(id).orElseThrow(() -> new AlertNotfoundException(id));
            JpaAlertChangeEvent event = new JpaAlertChangeEvent(state, user, descr, content);
            alert.addEvent(event);
            //need to save it
            repository.save(alert);
            return asValue(alert);
        }, MetadataAccess.SERVICE);
        updateLastUpdatedTime();
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

    @Override
    public void updateLastUpdatedTime() {
        previousUpdatedTime = lastUpdatedTime;
        lastUpdatedTime = DateTime.now().getMillis();
        if (previousUpdatedTime == null) {
            previousUpdatedTime = lastUpdatedTime;
        }
        clusterService.sendMessageToOthers(AlertManagerChangedClusterMessage.TYPE, new AlertManagerChangedClusterMessage(lastUpdatedTime));
    }

    private boolean hasAlertsSummaryChanged(AlertCriteria criteria) {
        if (latestAlertsSummary.containsKey(criteria.toString())) {
            return latestAlertsSummary.get(criteria.toString()).hasChanged(lastUpdatedTime);
        } else {
            return true;
        }
    }

    private boolean hasAlertsChanged(AlertCriteria criteria) {
        if (latestAlerts.containsKey(criteria.toString())) {
            return latestAlerts.get(criteria.toString()).hasChanged(lastUpdatedTime);
        } else {
            return true;
        }
    }

    private void resetChanged() {
        if (lastUpdatedTime == null) {
            lastUpdatedTime = DateTime.now().getMillis();
        }
        previousUpdatedTime = lastUpdatedTime;
    }

    private Long fetchLastUpdatedTime() {
        return metadataAccess.read(() -> {
            Long maxTime = repository.findMaxUpdatedTime();
            //it may be null if no alerts exist
            if (maxTime == null) {
                maxTime = DateTime.now().getMillis();
            }
            return maxTime;
        });
    }

    @Override
    public Long getLastUpdatedTime() {
        if (lastUpdatedTime == null) {
            //fetch it
            lastUpdatedTime = fetchLastUpdatedTime();
        }
        return lastUpdatedTime;
    }

    protected static class ImmutableAlert implements Alert {

        private final AlertManager source;
        private final Alert.ID id;
        private final String description;
        private final Level level;
        private final URI type;
        private final String subtype;
        private final DateTime createdTime;
        private final DateTime modifiedTime;
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
            this.modifiedTime = alert.getModifiedTime();
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
        public DateTime getModifiedTime() {
            return this.modifiedTime;
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

        @Override
        public <C extends Serializable> Alert updateAlertChange(String description, C content) {
            return updateAlertChangeEntry(this.id, description, content);
        }

        /* (non-Javadoc)
                 * @see com.thinkbiganalytics.alerts.api.AlertResponse#clear()
                 */
        @Override
        public void clear() {
            clearAlert(this.id);
        }

        @Override
        public void unclear() {
            unclearAlert(this.id);
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

    private class AlertSummaryCache {

        private Long lastUpdatedTime;
        private List<AlertSummary> alertSummaryList;

        public AlertSummaryCache(Long lastUpdatedTime, List<AlertSummary> alertSummaryList) {
            this.lastUpdatedTime = lastUpdatedTime;
            this.alertSummaryList = alertSummaryList;
        }

        public AlertSummaryCache(List<AlertSummary> alertSummaryList) {
            this.lastUpdatedTime = DateTime.now().getMillis();
            this.alertSummaryList = alertSummaryList;
        }

        public Long getLastUpdatedTime() {
            return lastUpdatedTime;
        }

        public List<AlertSummary> getAlertSummaryList() {
            return alertSummaryList;
        }

        public boolean hasChanged(Long time) {
            return time != null && time > lastUpdatedTime;
        }
    }


    private class AlertsCache {

        private Long lastUpdatedTime;
        private List<Alert> alertList;

        public AlertsCache(Long lastUpdatedTime, List<Alert> alertList) {
            this.lastUpdatedTime = lastUpdatedTime;
            this.alertList = alertList;
        }

        public Long getLastUpdatedTime() {
            return lastUpdatedTime;
        }

        public List<Alert> getAlertList() {
            return alertList;
        }

        public boolean hasChanged(Long time) {
            return time != null && time > lastUpdatedTime;
        }
    }

    public <C extends Serializable> EntityIdentificationAlertContent createEntityIdentificationAlertContent(String entityId, SecurityRole.ENTITY_TYPE entityType, C content) {
        if (content instanceof EntityIdentificationAlertContent) {
            ((EntityIdentificationAlertContent) content).setEntityId(entityId);
            ((EntityIdentificationAlertContent) content).setEntityType(entityType);
            return (EntityIdentificationAlertContent) content;
        } else {
            EntityIdentificationAlertContent c = new EntityIdentificationAlertContent();
            c.setEntityId(entityId);
            c.setEntityType(entityType);
            c.setContent(content);
            return c;
        }
    }

    @Override
    public void onMessageReceived(String from, ClusterMessage message) {

        if (AlertManagerChangedClusterMessage.TYPE.equalsIgnoreCase(message.getType())) {
            AlertManagerChangedClusterMessage msg = (AlertManagerChangedClusterMessage) message.getMessage();
            //set it equal to now to trigger any updates if needed for queries
            this.lastUpdatedTime = DateTime.now().getMillis();
        }
    }


}
