/**
 *
 */
package com.thinkbiganalytics.alerts.api.core;

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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.ID;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertListener;
import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.api.AlertResponder;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.alerts.api.SourceAlert;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver;
import com.thinkbiganalytics.alerts.spi.AlertSource;
import com.thinkbiganalytics.alerts.spi.AlertSourceAggregator;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.bus.selector.MatchAllSelector;
import reactor.fn.Consumer;

/**
 *
 */
public class AggregatingAlertProvider implements AlertProvider, AlertSourceAggregator, AlertNotifyReceiver, Consumer<Event<Alert>> {

    private static final Logger LOG = LoggerFactory.getLogger(AggregatingAlertProvider.class);

    private List<AlertResponder> responders;
    private Registration<?, ?> respondersRegistration;
    private Map<AlertListener, Registration<?, ?>> listeners;
    private Map<String, AlertSource> sources;
    private Map<String, AlertManager> managers;
    private Executor availableAlertsExecutor;
    private volatile DateTime lastAlertsTime = DateTime.now();

    @Inject
    @Named("alertsEventBus")
    private EventBus alertsBus;

    @Inject
    @Named("respondableAlertsEventBus")
    private EventBus respondableAlertsBus;


    /**
     *
     */
    public AggregatingAlertProvider() {
        this.listeners = Collections.synchronizedMap(new HashMap<AlertListener, Registration<?, ?>>());
        this.responders = Collections.synchronizedList(new ArrayList<AlertResponder>());
        this.sources = Collections.synchronizedMap(new HashMap<String, AlertSource>());
        this.managers = Collections.synchronizedMap(new HashMap<String, AlertManager>());
        this.availableAlertsExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setDaemon(true).build());
    }

    /**
     * Generates a unique, internal ID for this source
     */
    private static String createAlertSourceId(AlertSource src) {
        return src.getId().toString();
    }

    private static String getSourceId(Alert decorator) {
        SourceAlertID srcAlertId = (SourceAlertID) decorator.getId();
        return srcAlertId.sourceId;
    }

    /**
     * @param availableAlertsExecutor the availableAlertsExecutor to set
     */
    public void setAvailableAlertsExecutor(Executor availableAlertsExecutor) {
        this.availableAlertsExecutor = availableAlertsExecutor;
    }

    /* (non-Javadoc)
     * @see reactor.fn.Consumer#accept(java.lang.Object)
     */
    @Override
    public void accept(Event<Alert> event) {
        final Alert alert = unwrapAlert(event.getData());
        final AlertManager mgr = (AlertManager) alert.getSource();
        final List<AlertResponder> responders = snapshotResponderts();

        responders.forEach(responder -> {
            AlertResponse resp = mgr.getResponse(alert);
            AlertResponseWrapper wrapper = new AlertResponseWrapper(resp);
            responder.alertChange(alert, wrapper);
        });
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#criteria()
     */
    @Override
    public AlertCriteria criteria() {
        return new Criteria();
    }

    @Override
    public ID resolve(Serializable value) {
        if (value instanceof String) {
            return SourceAlertID.create((String) value, this.sources, this.managers);
        } else if (value instanceof SourceAlertID) {
            return (SourceAlertID) value;
        } else {
            throw new IllegalArgumentException("Unrecognized alert ID format: " + value);
        }
    }


    @Override
    public ID resolve(ID id, AlertSource source) {
        if (id instanceof SourceAlertID) {
            return id;
        } else {
            return new SourceAlertID(id, source);
        }
    }

    /* (non-Javadoc)
         * @see com.thinkbiganalytics.alerts.api.AlertProvider#addListener(com.thinkbiganalytics.alerts.api.AlertListener)
         */
    @Override
    public void addListener(AlertListener listener) {
        // TODO matching all alerts for every listener.  Allow filtering at this level, such as by type?
        Registration<?, ?> reg = this.alertsBus.on(new MatchAllSelector(), new ListenerConsumer(listener));
        this.listeners.put(listener, reg);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#addResponder(com.thinkbiganalytics.alerts.api.AlertResponder)
     */
    @Override
    public void addResponder(AlertResponder responder) {
        this.responders.add(responder);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSourceAggregator#addAlertSource(com.thinkbiganalytics.alerts.spi.AlertSource)
     */
    @Override
    public boolean addAlertSource(AlertSource src) {
        return this.sources.put(createAlertSourceId(src), src) == null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSourceAggregator#removeAlertSource(com.thinkbiganalytics.alerts.spi.AlertSource)
     */
    @Override
    public boolean removeAlertSource(AlertSource src) {
        return this.sources.remove(createAlertSourceId(src)) != null;
    }

    @Override
    public boolean addAlertManager(AlertManager mgr) {
        if (this.managers.put(createAlertSourceId(mgr), mgr) == null) {
            mgr.addReceiver(this);
            return true;
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSourceAggregator#removeAlertManager(com.thinkbiganalytics.alerts.spi.AlertManager)
     */
    @Override
    public boolean removeAlertManager(AlertManager mgr) {
        if (this.managers.remove(createAlertSourceId(mgr)) != null) {
            mgr.removeReceiver(this);
            return true;
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#getAlert(com.thinkbiganalytics.alerts.api.Alert.ID)
     */
    @Override
    public Optional<Alert> getAlert(ID id) {
        SourceAlertID alertId = asSourceAlertId(id);
        AlertSource src = getSource(alertId.sourceId);

        if (src != null) {
            return getAlert(alertId.alertId, src, false);
        } else {
            return null;
        }
    }

    public Optional<Alert> getAlertAsServiceAccount(ID id) {
        SourceAlertID alertId = asSourceAlertId(id);
        AlertSource src = getSource(alertId.sourceId);

        if (src != null) {
            return getAlert(alertId.alertId, src, true);
        } else {
            return null;
        }
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#getAlerts(com.thinkbiganalytics.alerts.api.AlertCriteria)
     */
    @Override
    public Iterator<? extends Alert> getAlerts(AlertCriteria criteria) {
        Map<String, AlertSource> srcs = snapshotAllSources();
        return combineAlerts(criteria, srcs).iterator();
    }


    public Iterator<? extends AlertSummary> getAlertsSummary(AlertCriteria criteria) {
        Map<String, AlertSource> srcs = snapshotAllSources();
        return combineAlertSummary(criteria, srcs).iterator();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#getAlerts(org.joda.time.DateTime)
     */
    @Override
    public Iterator<? extends Alert> getAlertsAfter(DateTime time) {
        return getAlerts(criteria().after(time));
    }


    @Override
    public Iterator<? extends Alert> getAlertsModifiedAfter(DateTime time) {
        return getAlerts(criteria().after(time));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#getAlertsBefore(org.joda.time.DateTime)
     */
    @Override
    public Iterator<? extends Alert> getAlertsBefore(DateTime time) {
        return getAlerts(criteria().before(time));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#respondTo(com.thinkbiganalytics.alerts.api.Alert.ID, com.thinkbiganalytics.alerts.api.AlertResponder)
     */
    @Override
    public void respondTo(ID id, AlertResponder responder) {
        SimpleEntry<Alert, AlertManager> found = findActionableAlert(id);

        if (found != null) {
            alertChange(found.getKey(), responder, found.getValue());
        }
    }

    @Override
    public void alertsAvailable(int count) {
        LOG.debug("Alerts available: {}", count);

        this.availableAlertsExecutor.execute(() -> {
            final AtomicReference<DateTime> sinceTime = new AtomicReference<>(AggregatingAlertProvider.this.lastAlertsTime);
            Map<String, AlertSource> sources = snapshotAllSources();

            combineAlerts(criteria().asServiceAccount(true).after(sinceTime.get()), sources).forEach(alert -> {
                LOG.debug("Alert {} received from {}", alert.getId(), alert.getSource());

                notifyListeners(alert);
                if (alert.isActionable()) {
                    notifyResponders(alert);
                }

                sinceTime.set(alert.getCreatedTime());
            });

            AggregatingAlertProvider.this.lastAlertsTime = sinceTime.get();
        });
    }

    @PostConstruct
    private void createRegistrations() {
        this.respondersRegistration = this.respondableAlertsBus.on(new MatchAllSelector(), this);
    }

    @PreDestroy
    private void cancelRegistrations() {
        this.respondersRegistration.cancel();
        this.listeners.values().forEach(reg -> reg.cancel());
    }

    private AlertSource getSource(String srcId) {
        AlertSource src = this.sources.get(srcId);

        if (src == null) {
            return this.managers.get(srcId);
        } else {
            return src;
        }
    }

    public Optional<Alert> getAlert(Alert.ID id, AlertSource src, boolean asServiceAccount) {
        if (asServiceAccount) {
            return src.getAlertAsServiceAccount(id).map(alert -> wrapAlert(alert, src));
        } else {
            return src.getAlert(id).map(alert -> wrapAlert(alert, src));
        }
    }

    private Stream<Alert> combineAlerts(AlertCriteria criteria, Map<String, AlertSource> srcs) {
        Criteria critImpl = (Criteria) criteria;

        return srcs.values().stream()
            .map(src -> {
                AlertCriteria srcCrit = src.criteria();
                critImpl.transfer(srcCrit);
                Iterable<Alert> alerts = () -> src.getAlerts(srcCrit);
                return StreamSupport.stream(alerts.spliterator(), false);
            })
            .flatMap(s -> s)
            .map(alert -> wrapAlert(alert, alert.getSource()))
            .sorted((a1, a2) -> a2.getCreatedTime().compareTo(a1.getCreatedTime()));
    }

    private Stream<AlertSummary> combineAlertSummary(AlertCriteria criteria, Map<String, AlertSource> srcs) {
        Criteria critImpl = (Criteria) criteria;

        return srcs.values().stream()
            .map(src -> {
                AlertCriteria srcCrit = src.criteria();
                critImpl.transfer(srcCrit);
                Iterable<AlertSummary> alerts = () -> src.getAlertsSummary(srcCrit);
                return StreamSupport.stream(alerts.spliterator(), false);
            })
            .flatMap(s -> s);
    }


    private void notifyChanged(Alert alert) {
        notifyListeners(alert);
    }

    private void notifyResponders(Alert alert) {
        Event<Alert> event = Event.wrap(alert);
        this.respondableAlertsBus.notify(alert.getType(), event);
    }

    private void notifyListeners(Alert alert) {
        Event<Alert> event = Event.wrap(alert);
        this.alertsBus.notify(alert.getType(), event);
    }

//    private void notifyResponders(final Alert.ID id, final AlertManager manager) {
//        LOG.debug("Notifying responders of change for alert ID: {}", id);
//
//        final List<AlertResponder> respList = snapshotResponderts();
//        
//        getRespondersExecutor().execute(new Runnable() {
//                @Override
//                public void run() {
//                    
//                    LOG.debug("Invoking responders for alerts: {}", respList);
//
//                    for (AlertResponder responder : respList) {
//                        SourceAlertID srcId = asSourceAlertId(id);
//                        Alert alert = getAlert(srcId.alertId, manager);
//
//                        LOG.debug("Alert change: {}  from source: {}  responder: {}", alert, manager, responder);
//                        
//                        if (alert != null) {
//                            alertChange(alert, responder, manager);
//                        }
//                    }
//                }
//            });
//    }

    private Alert alertChange(Alert alert, AlertResponder responder, AlertManager manager) {
        AlertResponseWrapper response = new AlertResponseWrapper(manager.getResponse(alert));

        responder.alertChange(alert, response);
        return response.latestAlert;
    }

    private List<AlertListener> snapshotListeners() {
        synchronized (AggregatingAlertProvider.this.listeners) {
            return new ArrayList<>(AggregatingAlertProvider.this.listeners.keySet());
        }
    }

    private List<AlertResponder> snapshotResponderts() {
        synchronized (AggregatingAlertProvider.this.responders) {
            return new ArrayList<>(AggregatingAlertProvider.this.responders);
        }
    }

    private Map<String, AlertSource> snapshotSources() {
        synchronized (this.sources) {
            return new HashMap<>(this.sources);
        }
    }

    private Map<String, AlertSource> snapshotManagers() {
        synchronized (this.managers) {
            return new HashMap<>(this.managers);
        }
    }

    private Map<String, AlertSource> snapshotAllSources() {
        Map<String, AlertSource> srcs = snapshotSources();
        srcs.putAll(snapshotManagers());
        return srcs;
    }

    private SimpleEntry<Alert, AlertManager> findActionableAlert(ID id) {
        SourceAlertID srcId = asSourceAlertId(id);
        AlertManager mgr = this.managers.get(srcId.sourceId);

        if (mgr != null) {
            return getAlert(srcId.alertId, mgr, true)
                .filter(alert -> alert.isActionable())
                .map(alert -> new SimpleEntry<>(unwrapAlert(alert), mgr))
                .orElse(null);
        } else {
            return null;
        }
    }

    private SourceAlertID asSourceAlertId(ID id) {
        if (id instanceof SourceAlertID) {
            return (SourceAlertID) id;
        } else {
            // Can only happen if the client uses a different ID than was supplied by this provider.
            throw new IllegalArgumentException("Unrecognized sourceAlert ID type: " + id);
        }
    }

    private Alert wrapAlert(final Alert srcAlert, final AlertSource src) {
        return wrapAlert(new SourceAlertID(srcAlert.getId(), src), srcAlert);
    }

    private Alert wrapAlert(final SourceAlertID id, final Alert alert) {
        if (Proxy.isProxyClass(alert.getClass())) {
            return alert;
        } else {
            InvocationHandler handler = new AlertInvocationHandler(alert, id);
            return (Alert) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{Alert.class}, handler);
        }
    }

    public Alert unwrapAlert(Alert proxy) {
        if (Proxy.isProxyClass(proxy.getClass())) {
            AlertInvocationHandler handler = (AlertInvocationHandler) Proxy.getInvocationHandler(proxy);
            return handler.wrapped;
        } else {
            return proxy;  // not a proxy
        }
    }

    protected static class AlertInvocationHandler implements InvocationHandler, SourceAlert {

        private final Alert wrapped;
        private final SourceAlertID proxyId;

        public AlertInvocationHandler(Alert wrapped, SourceAlertID proxyId) {
            super();
            this.wrapped = wrapped;
            this.proxyId = proxyId;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("getId")) {
                return (Alert.ID) this.proxyId;
            } else {
                return method.invoke(this.wrapped, args);
            }
        }

        public Alert getWrappedAlert() {
            return this.wrapped;
        }

        @Override
        public ID getSourceAlertId() {
            return proxyId;
        }
    }

    /**
     * Decorates an alert ID with an internal identifier of its source.
     */
    protected static class SourceAlertID implements Alert.ID {

        private static final long serialVersionUID = -3799345314250454959L;

        private final Alert.ID alertId;
        private final String sourceId;

        public SourceAlertID(ID alertId, AlertSource src) {
            super();
            this.alertId = alertId;
            this.sourceId = createAlertSourceId(src);
        }

        public static SourceAlertID create(String str, Map<String, AlertSource> sources, Map<String, AlertManager> managers) {
            int sepIdx = str.lastIndexOf(":");
            String alertPart = str.substring(0, sepIdx);
            String srcId = str.substring(sepIdx + 1);
            AlertSource src = sources.get(srcId);
            src = src == null ? managers.get(srcId) : src;

            if (src != null) {
                Alert.ID alertId = src.resolve(alertPart);
                return new SourceAlertID(alertId, src);
            } else {
                throw new IllegalArgumentException("Unrecognized alert ID: " + str);
            }

        }

        @Override
        public String toString() {
            return this.alertId.toString() + ":" + this.sourceId;
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

            SourceAlertID that = (SourceAlertID) obj;

            return Objects.equals(this.alertId, that.alertId) &&
                   Objects.equals(this.sourceId, that.sourceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.alertId, this.sourceId);
        }
    }

    private static class ListenerConsumer implements Consumer<Event<Alert>> {

        private final AlertListener listener;

        public ListenerConsumer(AlertListener listener) {
            super();
            this.listener = listener;
        }

        @Override
        public void accept(Event<Alert> event) {
            this.listener.alertChange(event.getData());
        }
    }

    protected static class Criteria extends BaseAlertCriteria {


    }

    protected class AlertResponseWrapper implements AlertResponse {

        private final AlertResponse delegate;
        private Alert latestAlert;

        public AlertResponseWrapper(AlertResponse delegate) {
            this.delegate = delegate;
        }

        @Override
        public Alert inProgress(String description) {
            return inProgress(description, null);
        }

        @Override
        public <C extends Serializable> Alert inProgress(String description, C content) {
            return changed(this.delegate.inProgress(description, content));
        }

        @Override
        public Alert handle(String description) {
            return handle(description, null);
        }

        @Override
        public <C extends Serializable> Alert handle(String description, C content) {
            return changed(this.delegate.handle(description, content));
        }

        @Override
        public Alert unhandle(String description) {
            return unhandle(description, null);
        }

        @Override
        public <C extends Serializable> Alert unhandle(String description, C content) {
            return changed(this.delegate.unhandle(description, content));
        }

        @Override
        public void clear() {
            this.delegate.clear();
        }

        @Override
        public <C extends Serializable> Alert updateAlertChange(String description, C content) {
            return delegate.updateAlertChange(description, content);
        }

        @Override
        public void unclear() {
            this.delegate.unclear();
        }

        private Alert changed(Alert alert) {
            notifyChanged(alert);
            this.latestAlert = alert;
            return alert;
        }
    }
}
