/**
 * 
 */
package com.thinkbiganalytics.alerts.spi.kylo;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.ID;
import com.thinkbiganalytics.alerts.api.Alert.Level;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.api.AlertNotfoundException;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.spi.AlertDescriptor;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert.AlertId;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertChangeEvent;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertRepository;

/**
 *
 * @author Sean Felten
 */
public class KyloAlertManager implements AlertManager {
    
    @Inject
    private MetadataAccess metadataAccess;

    private Set<AlertNotifyReceiver> alertReceivers = Collections.synchronizedSet(new HashSet<>());
    private JpaAlertRepository repository;
    
    /**
     * @param repo
     */
    public KyloAlertManager(JpaAlertRepository repo) {
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
    public Iterator<Alert> getAlerts() {
        return this.metadataAccess.read(() -> {
            return repository.findAll().stream()
                            .map(a -> (Alert) a)
                            .collect(Collectors.toList()) // Need to terminate the stream while still in a transaction
                            .iterator();
        }, MetadataAccess.SERVICE);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlerts(org.joda.time.DateTime)
     */
    @Override
    public Iterator<Alert> getAlerts(DateTime since) {
        return this.metadataAccess.read(() -> {
            return repository.findAlertsSince(since).stream()
                            .map(a -> asValue(a))
                            .collect(Collectors.toList()) // Need to terminate the stream while still in a transaction
                            .iterator();
        }, MetadataAccess.SERVICE);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlerts(com.thinkbiganalytics.alerts.api.Alert.ID)
     */
    @Override
    public Iterator<Alert> getAlerts(ID since) {
        return this.metadataAccess.read(() -> {
            return getAlert(since)
                            .map(a -> getAlerts(a.getCreatedTime()))
                            .orElseThrow(() -> new AlertNotfoundException(since));
        }, MetadataAccess.SERVICE);
    }

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
        Alert created = this.metadataAccess.commit(() -> {
            JpaAlert alert = new JpaAlert(type, level, description, content);
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
        return deleteAlert(idImpl);
    }
    
    protected Optional<JpaAlert> findAlert(Alert.ID id) {
        JpaAlert.AlertId idImpl = (AlertId) resolve(id);
        return Optional.ofNullable(repository.findOne(idImpl));
    }

    protected Alert asValue(Alert alert) {
        return new ImmutableAlert(alert, this);
    }

    protected JpaAlert deleteAlert(JpaAlert.AlertId id) {
        return this.metadataAccess.commit(() -> {
            JpaAlert alert = repository.findOne(id);
            this.repository.delete(id);
            return alert;
        }, MetadataAccess.SERVICE);
    }

    protected <C extends Serializable> Alert changeAlert(JpaAlert.AlertId id, State state, C content) {
        Alert changed = this.metadataAccess.commit(() -> {
            JpaAlert alert = findAlert(id).orElseThrow(() -> new AlertNotfoundException(id));
            JpaAlertChangeEvent event = new JpaAlertChangeEvent(state, content);
            alert.getEvents().add(event);
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
        public Alert inProgress() {
            return inProgress(null);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#inProgress(java.io.Serializable)
         */
        @Override
        public <C extends Serializable> Alert inProgress(C content) {
            return changeAlert(this.id, State.IN_PROGRESS, content);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#handle()
         */
        @Override
        public Alert handle() {
            return handle(null);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#handle(java.io.Serializable)
         */
        @Override
        public <C extends Serializable> Alert handle(C content) {
            return changeAlert(this.id, State.HANDLED, content);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#unHandle()
         */
        @Override
        public Alert unHandle() {
            return unhandle(null);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#unhandle(java.io.Serializable)
         */
        @Override
        public <C extends Serializable> Alert unhandle(C content) {
            return changeAlert(this.id, State.UNHANDLED, content);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertResponse#clear()
         */
        @Override
        public void clear() {
            deleteAlert(this.id);
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
        private final List<AlertChangeEvent> events;
        
        public ImmutableAlert(Alert alert, AlertManager mgr) {
            this.source = mgr;
            this.id = alert.getId();
            this.content = alert.getContent();
            this.description = alert.getDescription();
            this.level = alert.getLevel();
            this.type = alert.getType();
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
//            return this.events.get(0).getChangeTime();
        }

        @Override
        public boolean isActionable() {
            return true;
        }
    }
    
    private static class ImmutableAlertChangeEvent implements AlertChangeEvent {
        
        private final DateTime changeTime;
        private final State state;
        private final Serializable content;
        
        public ImmutableAlertChangeEvent(AlertChangeEvent event) {
            this.changeTime = event.getChangeTime();
            this.state = event.getState();
            this.content = event.getContent();
        }

        public DateTime getChangeTime() {
            return changeTime;
        }

        public State getState() {
            return state;
        }

        public Serializable getContent() {
            return content;
        }
    }
}
