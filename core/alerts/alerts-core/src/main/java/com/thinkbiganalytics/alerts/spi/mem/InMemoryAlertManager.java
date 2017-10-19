/**
 *
 */
package com.thinkbiganalytics.alerts.spi.mem;

/*-
 * #%L
 * thinkbig-alerts-core
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

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.alerts.api.core.BaseAlertCriteria;
import com.thinkbiganalytics.alerts.spi.AlertDescriptor;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver;
import com.thinkbiganalytics.alerts.spi.AlertSource;
import com.thinkbiganalytics.alerts.spi.EntityIdentificationAlertContent;
import com.thinkbiganalytics.security.role.SecurityRole;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 *
 */
public class InMemoryAlertManager implements AlertManager {

    public static final int MAX_ALERTS = 2000;
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryAlertManager.class);
    private static AtomicReference<GenericAlert> NULL_REF = new AtomicReference<GenericAlert>(null);

    private final Set<AlertDescriptor> descriptors;
    private final Set<AlertNotifyReceiver> alertReceivers;
    private final Map<Alert.ID, AtomicReference<GenericAlert>> alertsById;
    private final NavigableMap<DateTime, AtomicReference<GenericAlert>> alertsByTime;
    private final ReadWriteLock alertsLock = new ReentrantReadWriteLock();

    private volatile Executor receiversExecutor;
    private AtomicInteger changeCount = new AtomicInteger(0);

    private AlertManagerId id = new AlertManagerId();

    private DateTime lastUpdated;

    @Override
    public ID getId() {
        return id;
    }

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

    public void setAlertDescriptors(Collection<AlertDescriptor> types) {
        synchronized (this.descriptors) {
            this.descriptors.addAll(types);
        }
    }

    @Override
    public void updateLastUpdatedTime() {
        this.lastUpdated = DateTime.now();
    }

    @Override
    public boolean addDescriptor(AlertDescriptor descriptor) {
        return this.descriptors.add(descriptor);
    }

    @Override
    public void addReceiver(AlertNotifyReceiver receiver) {
        this.alertReceivers.add(receiver);
    }

    @Override
    public void removeReceiver(AlertNotifyReceiver receiver) {
        this.alertReceivers.remove(receiver);
    }

    @Override
    public Optional<Alert> getAlert(Alert.ID id) {
        return Optional.ofNullable(this.alertsById.getOrDefault(id, NULL_REF).get());
    }

    @Override
    public Optional<Alert> getAlertAsServiceAccount(Alert.ID id) {
        return getAlert(id);
    }

    @Override
    public AlertCriteria criteria() {
        return new BaseAlertCriteria();
    }

    @Override
    public Alert.ID resolve(Serializable ser) {
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
    public Iterator<Alert> getAlerts(AlertCriteria criteria) {
        BaseAlertCriteria predicate = (BaseAlertCriteria) (criteria == null ? criteria() : criteria);
        // TODO Grab a partition of the map first based on before/after times of criteria
        return this.alertsByTime.values().stream()
            .map(ref -> (Alert) ref.get())
            .filter(predicate)
            .iterator();
    }

    @Override
    public Iterator<AlertSummary> getAlertsSummary(AlertCriteria criteria) {
        BaseAlertCriteria predicate = (BaseAlertCriteria) (criteria == null ? criteria() : criteria);
        // TODO Grab a partition of the map first based on before/after times of criteria
        List<Alert> alerts = this.alertsByTime.values().stream()
            .map(ref -> (Alert) ref.get())
            .filter(predicate).collect(Collectors.toList());
        List<AlertSummary> summaryList = new ArrayList<>();
        Map<String,AlertSummary> groupedAlerts = new HashMap<>();

        alerts.stream().forEach(alert -> {
            String key = alert.getType()+":"+alert.getSubtype()+":"+alert.getLevel();
            groupedAlerts.computeIfAbsent(key, groupKey -> new GenericAlertSummary(alert.getType().toString(),alert.getSubtype(),alert.getLevel()));
            ((GenericAlertSummary)groupedAlerts.get(key)).incrementCount();
        });
        return groupedAlerts.values().iterator();

    }
//
//    @Override
//    public Iterator<Alert> getAlerts(DateTime since) {
//        this.alertsLock.readLock().lock();
//        try {
//            DateTime higher = this.alertsByTime.higherKey(since);
//            
//            if (higher != null) {
//                SortedMap<DateTime, AtomicReference<GenericAlert>> submap = this.alertsByTime.subMap(higher, DateTime.now());
//                return submap.values().stream().map(ref -> (Alert) ref.get()).iterator();
//            } else {
//                return Collections.<Alert>emptySet().iterator();
//            }
//        } finally {
//            this.alertsLock.readLock().unlock();
//        }
//    }
//
//    @Override
//    public Iterator<Alert> getAlerts(ID since) {
//        AtomicReference<GenericAlert> ref = this.alertsById.get(since);
//        
//        if (ref == null) {
//            return Collections.<Alert>emptySet().iterator();
//        } else {
//            GenericAlert alert = ref.get();
//            int index = alert.getEvents().size() - 1;
//            DateTime createdTime = alert.getEvents().get(index).getChangeTime();
//            
//            return getAlerts(createdTime);
//        }
//    }

    @Override
    public <C extends Serializable> Alert create(URI type,String subtype, Alert.Level level, String description, C content) {
        GenericAlert alert = new GenericAlert(type, subtype,level, description, content);
        DateTime createdTime = alert.getEvents().get(0).getChangeTime();

        addAlert(alert, createdTime);
        return alert;
    }

    @Override
    public <C extends Serializable> Alert createEntityAlert(URI type, Alert.Level level, String description, EntityIdentificationAlertContent<C> content) {
        return create(type,null,level,description,content);
    }

    private void updated(GenericAlert alert) {
        this.alertsById.computeIfPresent(alert.getId(),
                                         (id, ref) -> {
                                             ref.set(alert);
                                             return ref;
                                         });
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertManager#getUpdator(com.thinkbiganalytics.alerts.api.Alert)
     */
    @Override
    public AlertResponse getResponse(Alert alert) {
        return new InternalAlertResponse((GenericAlert) alert);
    }

    @Override
    public Alert remove(Alert.ID id) {
        this.alertsLock.writeLock().lock();
        try {
            AtomicReference<GenericAlert> ref = this.alertsById.remove(id);

            if (ref != null) {
                this.alertsByTime.values().remove(ref);
                return ref.get();
            } else {
                return null;
            }
        } finally {
            this.alertsLock.writeLock().unlock();
        }
    }


    protected void addAlert(GenericAlert alert, DateTime createdTime) {
        AtomicReference<GenericAlert> ref = new AtomicReference<>(alert);
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
        lastUpdated = DateTime.now();

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

        exec.execute(() -> {
            int count = InMemoryAlertManager.this.changeCount.get();

            LOG.info("Notifying receivers: {} about events: {}", receivers.size(), count);

            for (AlertNotifyReceiver receiver : receivers) {
                receiver.alertsAvailable(count);
            }

            InMemoryAlertManager.this.changeCount.getAndAdd(-count);
        });
    }

    @Override
    public Long getLastUpdatedTime() {
        return lastUpdated != null ? lastUpdated.getMillis() : null;
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
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!this.getClass().equals(obj.getClass())) {
                return false;
            }

            return Objects.equals(this.uuid, ((AlertID) obj).uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }
    }

    private static class GenericChangeEvent implements AlertChangeEvent {

        private final AlertID alertId;
        private final DateTime changeTime;
        private final State state;
        private final Principal user;
        private final String description;
        private final Object content;


        public GenericChangeEvent(AlertID id, State state) {
            this(id, state, null, null, null);
        }

        public GenericChangeEvent(AlertID alertId, State state, Principal user, String descr, Object content) {
            super();
            this.alertId = alertId;
            this.state = state;
            this.user = user;
            this.description = descr;
            this.content = content;
            this.changeTime = DateTime.now();
        }

        @Override
        public DateTime getChangeTime() {
            return changeTime;
        }

        @Override
        public State getState() {
            return state;
        }

        public AlertID getAlertId() {
            return alertId;
        }

        public Principal getUser() {
            return user;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C extends Serializable> C getContent() {
            return (C) this.content;
        }
    }

    private class InternalAlertResponse implements AlertResponse {

        private GenericAlert current;

        public InternalAlertResponse(GenericAlert alert) {
            this.current = alert;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.#inProgress()
         */
        @Override
        public Alert inProgress(String description) {
            return inProgress(description, null);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.#inProgress(java.io.Serializable)
         */
        @Override
        public <C extends Serializable> Alert inProgress(String description, C content) {
            checkCleared();
            this.current = new GenericAlert(this.current, State.IN_PROGRESS, content);
            updated(current);
            return this.current;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.#handle()
         */
        @Override
        public Alert handle(String description) {
            return handle(null);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.#handle(java.io.Serializable)
         */
        @Override
        public <C extends Serializable> Alert handle(String description, C content) {
            checkCleared();
            this.current = new GenericAlert(this.current, State.HANDLED, content);
            updated(current);
            return this.current;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.#unHandle()
         */
        @Override
        public Alert unhandle(String description) {
            return unhandle(description, null);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.#unHandle(java.io.Serializable)
         */
        @Override
        public <C extends Serializable> Alert unhandle(String description, C content) {
            checkCleared();
            this.current = new GenericAlert(this.current, State.UNHANDLED, content);
            updated(current);
            return this.current;
        }

        @Override
        public <C extends Serializable> Alert updateAlertChange(String description, C content) {
            this.current = new GenericAlert(this.current, this.current.getState(),content);
            return this.current;
        }

        @Override
        public void unclear() {
            this.current = new GenericAlert(this.current, false);
        }

        /* (non-Javadoc)
                 * @see com.thinkbiganalytics.alerts.api.AlertResponse#clear()
                 */
        @Override
        public void clear() {
            checkCleared();
            this.current = new GenericAlert(this.current, true);
            this.current = null;
        }

        private void checkCleared() {
            if (this.current == null) {
                throw new IllegalStateException("The alert cannot be updated as it has been already cleared.");
            }
        }
    }

    @Override
    public <C extends Serializable> EntityIdentificationAlertContent createEntityIdentificationAlertContent(String entityId, SecurityRole.ENTITY_TYPE entityType, C content) {
        EntityIdentificationAlertContent c = new EntityIdentificationAlertContent();
        c.setContent(content);
        return c;
    }

    private class AlertByIdMap extends LinkedHashMap<Alert.ID, AtomicReference<Alert>> {

        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<Alert.ID, AtomicReference<Alert>> eldest) {
            if (this.size() > MAX_ALERTS) {
                InMemoryAlertManager.this.alertsByTime.values().remove(eldest.getValue());
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Immutable implementation of an Alert.
     */
    protected class GenericAlert implements Alert {

        private final AlertID id;
        private final URI type;
        private String subtype;
        private final Level level;
        private final String description;
        private final boolean cleared;
        private final AlertSource source;
        private final Object content;
        private final List<AlertChangeEvent> events;

        public GenericAlert(URI type, String subtype, Level level, String description, Object content) {
            this.id = new AlertID();
            this.type = type;
            this.subtype = subtype;
            this.level = level;
            this.cleared = false;
            this.description = description;
            this.content = content;
            this.source = InMemoryAlertManager.this;

            if (content != null) {
                this.events = Collections.unmodifiableList(Collections.singletonList(new GenericChangeEvent(this.id, State.UNHANDLED, null, null, content)));
            } else {
                this.events = Collections.emptyList();
            }
        }

        public GenericAlert(URI type, Level level, Object content) {
            this(type, null,level, "", content);
        }

        public GenericAlert(GenericAlert alert, State newState, Object eventContent) {
            this.id = alert.id;
            this.type = alert.type;
            this.subtype = alert.subtype;
            this.level = alert.level;
            this.description = alert.description;
            this.source = alert.source;
            this.content = alert.content;
            this.cleared = false;

            ArrayList<AlertChangeEvent> evList = new ArrayList<>(alert.events);
            evList.add(0, new GenericChangeEvent(this.id, newState, null, null, eventContent));
            this.events = Collections.unmodifiableList(evList);
        }

        public GenericAlert(GenericAlert alert, boolean cleared) {
            this.id = alert.id;
            this.type = alert.type;
            this.subtype = alert.subtype;
            this.level = alert.level;
            this.description = alert.description;
            this.source = alert.source;
            this.content = alert.content;
            this.events = Collections.unmodifiableList(alert.events);
            this.cleared = cleared;
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
        public String getSubtype() {
            return subtype;
        }

        public void setSubtype(String subtype) {
            this.subtype = subtype;
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
        public State getState() {
            return this.events.get(0).getState();
        }

        @Override
        public DateTime getCreatedTime() {
            return this.events.get(this.events.size() - 1).getChangeTime();
        }

        @Override
        public DateTime getModifiedTime() {
            return this.events.get(0).getChangeTime();
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
        public boolean isCleared() {
            return this.cleared;
        }

        @Override
        public List<AlertChangeEvent> getEvents() {
            return this.events;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C extends Serializable> C getContent() {
            return (C) this.content;
        }
    }

    public static class AlertManagerId implements ID {


        private static final long serialVersionUID = 7691516770322504702L;

        private String idValue = InMemoryAlertManager.class.getSimpleName();


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

    private class GenericAlertSummary implements AlertSummary {

        private String type;

        private String subtype;

        private Alert.Level level;

        private Long count;

        private Long lastAlertTimestamp;

        public GenericAlertSummary(){

        }

        public GenericAlertSummary(String type, String subtype, Alert.Level level){
            this.type = type;
            this.subtype = subtype;
            this.level = level;
        }

        @Override
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String getSubtype() {
            return subtype;
        }

        public void setSubtype(String subtype) {
            this.subtype = subtype;
        }

        @Override
        public Alert.Level getLevel() {
            return level;
        }

        public void setLevel(Alert.Level level) {
            this.level = level;
        }


        @Override
        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public void incrementCount(){
            count +=1;
        }

        @Override
        public Long getLastAlertTimestamp() {
            return DateTime.now().getMillis();
        }
    }

}
