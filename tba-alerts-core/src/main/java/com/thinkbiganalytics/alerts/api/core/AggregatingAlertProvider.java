/**
 * 
 */
package com.thinkbiganalytics.alerts.api.core;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.ID;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.api.AlertListener;
import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.api.AlertResponder;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver;
import com.thinkbiganalytics.alerts.spi.AlertSource;

/**
 *
 * @author Sean Felten
 */
public class AggregatingAlertProvider implements AlertProvider, AlertNotifyReceiver {
    
    private static final Logger LOG = LoggerFactory.getLogger(AggregatingAlertProvider.class);
    
    private Set<AlertListener> listeners;
    private List<AlertResponder> responders;
    private Map<String, AlertSource> sources;
    private Map<String, AlertManager> managers;
//    private Map<SourceAlertID, AlertManager> pendingResponses;
    
    private volatile Executor listenersExecutor;
    private volatile Executor respondersExecutor;
    private volatile DateTime lastAlertsTime = DateTime.now();

    /**
     * 
     */
    public AggregatingAlertProvider() {
        this.listeners = Collections.synchronizedSet(new HashSet<AlertListener>());
        this.responders = Collections.synchronizedList(new ArrayList<AlertResponder>());
        this.sources = Collections.synchronizedMap(new HashMap<String, AlertSource>());
        this.managers = Collections.synchronizedMap(new HashMap<String, AlertManager>());
//        this.pendingResponses = Collections.synchronizedMap(new LinkedHashMap<SourceAlertID, AlertManager>());
    }
    
    public void setListenersExecutor(Executor listenersExecutor) {
        synchronized (this) {
            this.listenersExecutor = listenersExecutor;
        }
    }
    
    public void setRespondersExecutor(Executor respondersExecutor) {
        synchronized (this) {
            this.respondersExecutor = respondersExecutor;
        }
    }
    
    
    @Override
    public ID resolve(Serializable value) {
        if (value instanceof String) {
            return SourceAlertID.create((String) value, this.sources);
        } else if (value instanceof SourceAlertID) {
            return (SourceAlertID) value;
        } else {
            throw new IllegalArgumentException("Unrecognized alert ID format: " + value);
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#addListener(com.thinkbiganalytics.alerts.api.AlertListener)
     */
    @Override
    public void addListener(AlertListener listener) {
        this.listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#addResponder(com.thinkbiganalytics.alerts.api.AlertResponder)
     */
    @Override
    public void addResponder(AlertResponder responder) {
        this.responders.add(responder);
    }
    
    public void addAlertSource(AlertSource src) {
        this.sources.put(createAlertSourceId(src), src);
    }
    
    public void addAlertManager(AlertManager mgr) {
        addAlertSource(mgr);
        this.managers.put(createAlertSourceId(mgr), mgr);
        mgr.addReceiver(this);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#getAlert(com.thinkbiganalytics.alerts.api.Alert.ID)
     */
    @Override
    public Alert getAlert(ID id) {
        SourceAlertID srcId = asSourceAlertId(id);
        AlertSource src = this.sources.get(srcId.sourceId);
        
        if (src != null) {
            return getAlert(srcId.alertId, src);
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#getAlerts(org.joda.time.DateTime)
     */
    @Override
    public Iterator<? extends Alert> getAlerts(DateTime since) {
        Map<String, AlertSource> srcs = snapshotSources();
        return combineAlerts(since, srcs);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#getAlerts(com.thinkbiganalytics.alerts.api.Alert.ID)
     */
    @Override
    public Iterator<? extends Alert> getAlerts(ID since) {
        Alert sinceAlert = getAlert(since);
        
        if (sinceAlert != null) {
            DateTime created = getCreationTime(sinceAlert);
            Map<String, AlertSource> srcs = snapshotSources();
            
            return combineAlerts(created, srcs);
        } else {
            return Collections.<Alert>emptySet().iterator();
        }
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
        Executor exec = getListenersExecutor();
        
        LOG.info("Alert available: {}", count);
        
        exec.execute(new Runnable() {
            public void run() {
                DateTime sinceTime = AggregatingAlertProvider.this.lastAlertsTime;
                Map<String, AlertSource> srcList = snapshotSources();
                Iterator<Alert> combinedAlerts = combineAlerts(sinceTime, srcList);
                
                while (combinedAlerts.hasNext()) {
                    Alert alert = combinedAlerts.next();
                    AlertSource src = srcList.get(getSourceId(alert));
                    
                    LOG.info("Alert {} received from {}", alert.getId(), src);
                    
                    notifyListeners(alert);
                    
                    if (src instanceof AlertManager && alert.isActionable()) {
                        notifyResponders(alert.getId(), (AlertManager) src);
                    }
                    
                    sinceTime = getCreationTime(alert);
                }
                
                AggregatingAlertProvider.this.lastAlertsTime = sinceTime;
            }
        });
    }
    
    
    
    private Alert getAlert(Alert.ID id, AlertSource src) {
        Alert alert = src.getAlert(id);
        
        if (alert != null) {
            return wrapAlert(alert, src);
        } else {
            return null;
        }
    }

    private DateTime getCreationTime(Alert alert) {
        List<? extends AlertChangeEvent> events = alert.getEvents();
        // There should always be at least one creation event; the last one in the list
        return events.get(events.size() - 1).getChangeTime();
    }

    /**
     * Generates a unique, internal ID for this source
     */
    private static String createAlertSourceId(AlertSource src) {
        return Integer.toString(src.hashCode());
    }
    
    private static String getSourceId(Alert decorator) {
        SourceAlertID srcAlertId = (SourceAlertID) decorator.getId();
        return srcAlertId.sourceId;
    }
    
    private Executor getListenersExecutor() {
        if (this.listenersExecutor == null) {
            synchronized (this) {
                if (this.listenersExecutor == null) {
                    this.listenersExecutor = Executors.newCachedThreadPool(
                            new ThreadFactoryBuilder().setDaemon(true).build());
                }
            }
        }
        
        return listenersExecutor;
    }

    private Executor getRespondersExecutor() {
        if (this.respondersExecutor == null) {
            synchronized (this) {
                if (this.respondersExecutor == null) {
                    this.respondersExecutor = Executors.newFixedThreadPool(1);
                }
            }
        }
        
        return respondersExecutor;
    }

    private Iterator<Alert> combineAlerts(DateTime since, Map<String, AlertSource> srcs) {
        List<Iterator<Alert>> iterators = new ArrayList<>();
        
        for (AlertSource src : srcs.values()) {
            Function<Alert, Alert> func = wrapAlertFunction(src);
            Iterator<? extends Alert> itr = src.getAlerts(since);
            
            LOG.info("{} alerts available from {} since: {}", itr.hasNext() ? "There are " : "No", src, since);
            
            iterators.add(Iterators.transform(itr, func));
        }
        
        // TODO: This iterator produces the alerts of all sources grouped by source.  If we want to 
        // order by alert time then we will have to created a multiplexing iterator rather
        // than this one from Google collections.
        return Iterators.concat(iterators.iterator());
    }

    private Function<Alert, Alert> wrapAlertFunction(final AlertSource src) {
        return new Function<Alert, Alert>() {
            @Override
            public Alert apply(Alert input) {
                return wrapAlert(input, src);
            }
        };
    }
    
    private void notifyChanged(Alert alert, AlertManager manager) {
        notifyListeners(alert);
        notifyResponders(alert.getId(), manager);
    }

    private void notifyListeners(final Alert alert) {
        final List<? extends AlertListener> list = snapshotListeners();
        
        getListenersExecutor().execute(new Runnable() {
            @Override
            public void run() {
                for (AlertListener listener : list) {
                    listener.alertChange(alert);
                }
            }
        });
    }
        
    private void notifyResponders(final Alert.ID id, final AlertManager manager) {
        LOG.info("Notifying responders of change for alert ID: {}", id);

        final List<AlertResponder> respList = snapshotResponderts();
        
        getRespondersExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    
                    LOG.info("Invoking responders for alerts: {}", respList);

                    for (AlertResponder responder : respList) {
                        SourceAlertID srcId = asSourceAlertId(id);
                        Alert alert = getAlert(srcId.alertId, manager);

                        LOG.info("Alert change: {}  from source: {}  responder: {}", alert, manager, responder);
                        
                        if (alert != null) {
                            alertChange(alert, responder, manager);
                        }
                    }
                }
            });
    }

    private void alertChange(Alert alert, AlertResponder responder, AlertManager manager) {
        ManagerAlertResponse resp = new ManagerAlertResponse(alert, manager);
        
        responder.alertChange(alert, resp);
        
        if (resp.resultAlert != null) {
            notifyChanged(resp.resultAlert, resp.manager);
        }
    }
    
    private List<AlertListener> snapshotListeners() {
        List<AlertListener> listenerList;
        
        synchronized (AggregatingAlertProvider.this.listeners) {
            listenerList = new ArrayList<>(AggregatingAlertProvider.this.listeners);
        }
        return listenerList;
    }
    
    private List<AlertResponder> snapshotResponderts() {
        List<AlertResponder> respList;
        
        synchronized (AggregatingAlertProvider.this.responders) {
            respList = new ArrayList<>(AggregatingAlertProvider.this.responders);
        }
        return respList;
    }
    
    private Map<String, AlertSource> snapshotSources() {
        Map<String, AlertSource> srcList;
        
        synchronized (this.sources) {
            srcList = new HashMap<>(this.sources);
        }
        return srcList;
    }

    private SimpleEntry<Alert, AlertManager> findActionableAlert(ID id) {
        SourceAlertID srcId = asSourceAlertId(id);
        AlertManager mgr = this.managers.get(srcId.sourceId);
        
        if (mgr !=  null) {
            Alert alert = getAlert(srcId.alertId, mgr);
            
            if (alert != null && alert.isActionable()) {
                return new SimpleEntry<>(alert, mgr);
            } else {
                return null;
            } 
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
            InvocationHandler handler = new AlertInvocationHandler(alert, new SourceAlertID(id, alert.getSource()));
            return (Alert) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] {Alert.class}, handler);
        }
    }
    
    protected static class AlertInvocationHandler implements InvocationHandler {
        private Alert wrapped;
        private SourceAlertID proxyId;
        
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
    }

    /**
     * Decorates an alert ID with an internal identifier of its source.
     */
    protected static class SourceAlertID implements Alert.ID {
        private static final long serialVersionUID = -3799345314250454959L;

        private final Alert.ID alertId;
        private final String sourceId;
        
        public static SourceAlertID create(String str, Map<String, AlertSource> sources) {
            int sepIdx = str.lastIndexOf(":");
            String alertPart = str.substring(0, sepIdx);
            String srcId = str.substring(sepIdx + 1);
            AlertSource src = sources.get(srcId);
            
            if (src != null) {
                Alert.ID alertId = src.resolve(alertPart);
                return new SourceAlertID(alertId, src);
            } else {
                throw new IllegalArgumentException("Unrecognized alert ID: " + str);
            }
            
        }
        
        public SourceAlertID(ID alertId, AlertSource src) {
            super();
            this.alertId = alertId;
            this.sourceId = createAlertSourceId(src);
        }
        
        @Override
        public String toString() {
            return this.alertId.toString() + ":" + this.sourceId;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (! this.getClass().equals(obj.getClass()))
                return false;
            
            SourceAlertID that = (SourceAlertID) obj;
            
            return Objects.equals(this.alertId, that.alertId) &&
                    Objects.equals(this.sourceId, that.sourceId);
         }
        
        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.alertId, this.sourceId);
        }
    }
    
    
    protected class ManagerAlertResponse implements AlertResponse {
        
        private final Alert targetAlert;
        private final AlertManager manager;

        private Alert resultAlert = null;
        
        public ManagerAlertResponse(Alert alert, AlertManager mgr) {
            this.targetAlert = alert;
            this.manager = mgr;
        }
        
        @Override
        public <C> void inProgress() {
            inProgress(null);
        }

        @Override
        public <C> void inProgress(C content) {
            changed(this.manager.changeState(this.targetAlert, Alert.State.IN_PROGRESS, content));
        }
        
        @Override
        public <C> void handle() {
            handle(null);
        }

        @Override
        public <C> void handle(C content) {
            changed(this.manager.changeState(this.targetAlert, Alert.State.HANDLED, content));
        }

        @Override
        public <C> void unHandle() {
            unHandle(null);
        }
        
        @Override
        public <C> void unHandle(C content) {
            changed(this.manager.changeState(this.targetAlert, Alert.State.UNHANDLED, content));
        }

        @Override
        public void clear() {
            changed(this.manager.remove(this.targetAlert.getId()));
        }
        
        private void changed(Alert alert) {
            this.resultAlert = wrapAlert(alert, this.manager);
            notifyChanged(this.resultAlert, this.manager);
        }
    }
}
