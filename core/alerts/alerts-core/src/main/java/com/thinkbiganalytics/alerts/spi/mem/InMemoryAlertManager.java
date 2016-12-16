/**
 * 
 */
package com.thinkbiganalytics.alerts.spi.mem;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.ID;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.spi.AlertDescriptor;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver;
import com.thinkbiganalytics.alerts.spi.AlertSource;

/**
 *
 * @author Sean Felten
 */
public class InMemoryAlertManager implements AlertManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryAlertManager.class);
    
    public static final int MAX_ALERTS = 2000;
    
    private final Set<AlertDescriptor> descriptors;
    private final Set<AlertNotifyReceiver> alertReceivers;
    private final Map<Alert.ID, AtomicReference<Alert>> alertsById;
    private final NavigableMap<DateTime, AtomicReference<Alert>> alertsByTime;
    private final ReadWriteLock alertsLock = new ReentrantReadWriteLock();

    private volatile Executor receiversExecutor;
    private AtomicInteger changeCount = new AtomicInteger(0);

    /**
     * 
     */
    public InMemoryAlertManager() {
        this.descriptors = Collections.synchronizedSet(new HashSet<AlertDescriptor>());
        this.alertReceivers = Collections.synchronizedSet(new HashSet<AlertNotifyReceiver>());
        this.alertsById = new ConcurrentHashMap<>();
        this.alertsByTime = new ConcurrentSkipListMap<>();
    }
    
    public void setReceiversExecutor(Executor receiversExecutor) {
        synchronized (this) {
            this.receiversExecutor = receiversExecutor;
        }
    }

    protected Executor getRespondersExecutor() {
        if (this.receiversExecutor == null) {
            synchronized (this) {
                if (this.receiversExecutor == null) {
                    this.receiversExecutor = Executors.newFixedThreadPool(1);
                }
            }
        }
        
        return receiversExecutor;
    }

    @Override
    public Set<AlertDescriptor> getAlertDescriptors() {
        synchronized (this.descriptors) {
            return new HashSet<>(this.descriptors);
        }
    }
    
    @Override
    public boolean addDescriptor(AlertDescriptor descriptor) {
        return this.descriptors.add(descriptor);
    }
    
    public void setAlertDescriptors(Collection<AlertDescriptor> types) {
        synchronized (this.descriptors) {
            this.descriptors.addAll(types);
        }
    }

    @Override
    public void addReceiver(AlertNotifyReceiver receiver) {
        this.alertReceivers.add(receiver);
    }

    @Override
    public Alert getAlert(ID id) {
        AtomicReference<Alert> ref = this.alertsById.get(id);
        
        if (ref != null) {
            return ref.get();
        } else {
            return null;
        }
    }
    
    @Override
    public ID resolve(Serializable ser) {
        if (ser instanceof String) {
            return new AlertID((String) ser);
        } else if (ser instanceof UUID) {
            return new AlertID((UUID) ser);
        } else if (ser instanceof AlertID) { 
            return (AlertID) ser;
        } else {
            throw new IllegalArgumentException("Invalid ID source format: " + ser.getClass());
        }
    }

    @Override
    public Iterator<? extends Alert> getAlerts() {
        return Iterators.transform(this.alertsByTime.values().iterator(), 
                new Function<AtomicReference<Alert>, Alert>() { 
                    @Override
                    public Alert apply(AtomicReference<Alert> ref) {
                        return ref.get();
                    }
                });
    }

    @Override
    public Iterator<? extends Alert> getAlertsSince(DateTime since) {
        this.alertsLock.readLock().lock();
        try {
            DateTime higher = this.alertsByTime.higherKey(since);
            
            if (higher != null) {
                SortedMap<DateTime, AtomicReference<Alert>> submap = this.alertsByTime.subMap(higher, DateTime.now());
                
                return Iterators.transform(submap.values().iterator(),
                        new Function<AtomicReference<Alert>, Alert>() {
                            @Override
                            public Alert apply(AtomicReference<Alert> ref) {
                                return ref.get();
                            }
                        });
            } else {
                return Collections.<Alert>emptySet().iterator();
            }
        } finally {
            this.alertsLock.readLock().unlock();
        }
    }

    @Override
    public Iterator<? extends Alert> getAlertsSince(ID since) {
        AtomicReference<Alert> ref = this.alertsById.get(since);
        
        if (ref == null) {
            return Collections.<Alert>emptySet().iterator();
        } else {
            DateTime createdTime = DateTime.now();
            for (AlertChangeEvent event : ref.get().getEvents()) {
                if (event.getState() == State.CREATED || event.getState() == State.UNHANDLED) {
                    createdTime = event.getChangeTime();
                    break;
                }
            }
        
            return getAlertsSince(createdTime);
        }
    }

    @Override
    public <C> Alert create(URI type, Alert.Level level, String description, C content) {
        GenericAlert alert = new GenericAlert(type, level, description, content);
        DateTime createdTime = alert.getEvents().get(0).getChangeTime();
        
        addAlert(alert, createdTime);
        return alert;
    }

    @Override
    public <C> Alert changeState(Alert alert, State newState, C content) {
        AtomicReference<Alert> ref = this.alertsById.get(alert.getId());
        
        if (ref != null) {
            GenericAlert oldAlert = (GenericAlert) alert;
            GenericAlert newAlert = new GenericAlert(oldAlert, newState, content);
            boolean changed = ref.compareAndSet(oldAlert, newAlert);
            
            if (changed) {
                this.changeCount.incrementAndGet();
                signalReceivers();
                return newAlert;
            } else {
                throw new ConcurrentModificationException("The state of this alert has aready been changed");
            }
        } else {
            return null;
        }
    }

    @Override
    public Alert remove(ID id) {
        this.alertsLock.writeLock().lock();
        try {
            AtomicReference<Alert> ref = this.alertsById.get(id);
            
            if (ref != null) {
                this.alertsByTime.values().remove(ref);
                return this.alertsById.remove(id).get();
            } else {
                return null;
            }
        } finally {
            this.alertsLock.writeLock().unlock();
        }
    }


    protected void addAlert(GenericAlert alert, DateTime createdTime) {
        AtomicReference<Alert> ref = new AtomicReference<Alert>(alert);
        int count = 0;
        
        this.alertsLock.writeLock().lock();
        try {
            this.alertsByTime.put(createdTime, ref);
            this.alertsById.put(alert.getId(), ref);
            count = this.changeCount.incrementAndGet();
        } finally {
            this.alertsLock.writeLock().unlock();
        }
        
        LOG.info("Alert added - pending notifications: {}", count);
        
        if (count > 0) {
            signalReceivers();
        }
    }
    
    
    private void signalReceivers() {
        Executor exec = getRespondersExecutor();
        final Set<AlertNotifyReceiver> receivers;
        
        synchronized (this.alertReceivers) {
            receivers = new HashSet<>(this.alertReceivers);
        }
        
        exec.execute(new Runnable() {
            @Override
            public void run() {
                int count = InMemoryAlertManager.this.changeCount.get();
                
                LOG.info("Notifying receivers: {} about events: {}", receivers.size(), count);
                
                for (AlertNotifyReceiver receiver : receivers) {
                    receiver.alertsAvailable(count);
                }
                
                InMemoryAlertManager.this.changeCount.getAndAdd(-count);
            }
        });
    }


    private class AlertByIdMap extends LinkedHashMap<Alert.ID, AtomicReference<Alert>> {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<ID, AtomicReference<Alert>> eldest) {
            if (this.size() > MAX_ALERTS) {
                InMemoryAlertManager.this.alertsByTime.values().remove(eldest.getValue());
                return true;
            } else {
                return false;
            }
        }
    }
    
    private static class AlertID implements Alert.ID {
        private final UUID uuid;
        
        public AlertID() {
            this(UUID.randomUUID());
        }
        
        public AlertID(String str) {
            this(UUID.fromString(str));
        }
        
        public AlertID(UUID id) {
            this.uuid = id;
        }
        
        @Override
        public String toString() {
            return this.uuid.toString();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (! this.getClass().equals(obj.getClass()))
                return false;
            
            return Objects.equals(this.uuid, ((AlertID) obj).uuid);
         }
        
        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }
    }
    
    protected class GenericAlert implements Alert {
        
        private final AlertID id;
        private final URI type;
        private final Level level;
        private final String description;
        private final AlertSource source;
        private final Object content;
        private final List<? extends AlertChangeEvent> events;

        public GenericAlert(URI type, Level level, String description, Object content) {
            this.id = new AlertID();
            this.type = type;
            this.level = level;
            this.description = description;
            this.content = content;
            this.source = InMemoryAlertManager.this;
            this.events = Collections.unmodifiableList(Collections.singletonList(
                    new GenericChangeEvent(this.id, State.UNHANDLED)));
        }

        public GenericAlert(URI type, Level level, Object content) {
            this(type, level, "", content);
        }
        
        public GenericAlert(GenericAlert alert, State newState, Object eventContent) {
            this.id = alert.id;
            this.type = alert.type;
            this.level = alert.level;
            this.description = alert.description;
            this.source = alert.source;
            this.content = alert.content;
            
            ArrayList<AlertChangeEvent> evList = new ArrayList<>(alert.events);
            evList.add(0, new GenericChangeEvent(this.id, newState, eventContent));
            this.events = Collections.unmodifiableList(evList);
        }

        @Override
        public ID getId() {
            return this.id;
        }

        @Override
        public URI getType() {
            return this.type;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public Level getLevel() {
            return this.level;
        }

        @Override
        public AlertSource getSource() {
            return this.source;
        }

        @Override
        public boolean isActionable() {
            return true;
        }

        @Override
        public List<? extends AlertChangeEvent> getEvents() {
            return this.events;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C> C getContent() {
            return (C) this.content;
        }
    }
    
    private static class GenericChangeEvent implements AlertChangeEvent {
        
        private final AlertID alertId;
        private final DateTime changeTime;
        private final State state;
        private final Object content;

        
        public GenericChangeEvent(AlertID id, State state) {
            this(id, state, null);
        }

        public GenericChangeEvent(AlertID alertId, State state, Object content) {
            super();
            this.alertId = alertId;
            this.state = state;
            this.content = content;
            this.changeTime = DateTime.now();
        }

        @Override
        public AlertID getAlertId() {
            return alertId;
        }

        @Override
        public DateTime getChangeTime() {
            return changeTime;
        }

        @Override
        public State getState() {
            return state;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C> C getContent() {
            return (C) this.content;
        }
        
    }
}
