/**
 * 
 */
package com.thinkbiganalytics.alerts.spi.defaults;

import java.io.Serializable;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
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
import com.thinkbiganalytics.alerts.api.core.BaseAlertCriteria;
import com.thinkbiganalytics.alerts.spi.AlertDescriptor;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert.AlertId;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertChangeEvent;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertRepository;
import com.thinkbiganalytics.metadata.jpa.alerts.QJpaAlert;
import com.thinkbiganalytics.security.UsernamePrincipal;

/**
 *
 * @author Sean Felten
 */
public class DefaultAlertManager extends QueryDslRepositorySupport implements AlertManager {
    
    @Inject
    private JPAQueryFactory queryFactory;

    @Inject
    private MetadataAccess metadataAccess;

    private Set<AlertNotifyReceiver> alertReceivers = Collections.synchronizedSet(new HashSet<>());
    private JpaAlertRepository repository;
    

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
    public ID resolve(Serializable id) {
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
    public Optional<Alert> getAlert(ID id) {
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
    public <C extends Serializable> Alert create(URI type, Level level, String description, C content) {
        final Principal user = SecurityContextHolder.getContext().getAuthentication() != null 
                        ? SecurityContextHolder.getContext().getAuthentication() 
                        : null;
                        
        Alert created = this.metadataAccess.commit(() -> {
            JpaAlert alert = new JpaAlert(type, level, user, description, content);
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
    public Alert remove(ID id) {
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final Principal user = auth != null 
                        ? new UsernamePrincipal(auth.getPrincipal().toString())
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
        public Alert unHandle(String descr) {
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
    
    private static class ImmutableAlert implements Alert {
        
        private final AlertManager source;
        private final Alert.ID id;
        private final String description;
        private final Level level;
        private final URI type;
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
    
    private class Criteria extends BaseAlertCriteria {
        
        public JPAQuery<JpaAlert> createQuery() {
            List<Predicate> preds = new ArrayList<>();
            QJpaAlert alert = QJpaAlert.jpaAlert;
            
            JPAQuery<JpaAlert> query = queryFactory
                            .select(alert)
                            .from(alert)
                            .limit(getLimit());
            
            // The "state" criteria now means filter by an alert's current state.
            // To support filtering by any state the alert has transitioned through
            // we can add the commented out code below (the old state behavior)
//            if (getTransitions().size() > 0) {
//                QAlertChangeEvent event = QAlertChangeEvent.alertChangeEvent;
//                query.join(alert.events, event);
//                preds.add(event.state.in(getTransitions()));
//            }

            if (getStates().size() > 0) preds.add(alert.state.in(getStates()));
            if (getLevels().size() > 0) preds.add(alert.level.in(getLevels()));
            if (getAfterTime() != null) preds.add(alert.createdTime.gt(getAfterTime()));
            if (getBeforeTime() != null) preds.add(alert.createdTime.lt(getBeforeTime()));
            if (! isIncludeCleared()) preds.add(alert.cleared.isFalse());

            if (getTypes().size() > 0) {
                BooleanBuilder likes = new BooleanBuilder();
                getTypes().stream()
                    .map(uri -> alert.typeString.like(uri.toASCIIString().concat("%")))
                    .forEach(pred -> likes.or(pred));
                preds.add(likes);
            }
            
            // When limiting and using "after" criteria only, we need to sort ascending to get the next n values after the given id/time.
            // In all other cases sort descending. The results will be ordered correctly when aggregated by the provider.
            if (getLimit() != Integer.MAX_VALUE && getAfterTime() != null && getBeforeTime() == null) {
                query.orderBy(alert.createdTime.asc());
            } else {
                query.orderBy(alert.createdTime.desc());
            }

            if (preds.isEmpty()) {
                return query;
            } else {
                return query.where(preds.toArray(new Predicate[preds.size()]));
            }
        }
    }
    
}
