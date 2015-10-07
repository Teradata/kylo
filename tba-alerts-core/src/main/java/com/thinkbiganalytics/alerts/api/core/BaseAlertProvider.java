/**
 * 
 */
package com.thinkbiganalytics.alerts.api.core;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.ID;
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
public class BaseAlertProvider implements AlertProvider, AlertNotifyReceiver {
    
    private Set<AlertListener> listeners;
    private List<AlertResponder> responders;
    private List<AlertSource> sources;
    private List<AlertManager> managers;
    private Map<Alert.ID, AlertManager> pendingResponses;
    
    private volatile Executor listenersExecutor;
    private volatile Executor respondersExecutor;
    private volatile DateTime lastAlertsTime = DateTime.now();

    /**
     * 
     */
    public BaseAlertProvider() {
        this.listeners = Collections.synchronizedSet(new HashSet<AlertListener>());
        this.responders = Collections.synchronizedList(new ArrayList<AlertResponder>());
        this.sources = Collections.synchronizedList(new ArrayList<AlertSource>());
        this.managers = Collections.synchronizedList(new ArrayList<AlertManager>());
        this.pendingResponses = Collections.synchronizedMap(new LinkedHashMap<Alert.ID, AlertManager>());
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
    
    public void setStartTime(DateTime time) {
        this.lastAlertsTime = time;
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
        this.sources.add(src);
    }
    
    public void addAlertManager(AlertManager mgr) {
        addAlertSource(mgr);
        this.managers.add(mgr);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#getAlerts(org.joda.time.DateTime)
     */
    @Override
    public Iterator<? extends Alert> getAlerts(DateTime since) {
        List<AlertSource> srcs = snapshotSources();
        return combineAlerts(since, srcs);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertProvider#getAlerts(com.thinkbiganalytics.alerts.api.Alert.ID)
     */
    @Override
    public Iterator<? extends Alert> getAlerts(ID since) {
        // TODO is this method needed?
        return Collections.<Alert>emptyList().iterator();
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
        Executor exec = this.listenersExecutor;
        
        exec.execute(new Runnable() {
            public void run() {
                DateTime last = BaseAlertProvider.this.lastAlertsTime;
                List<AlertSource> srcList = snapshotSources();
                Iterator<SimpleEntry<? extends Alert, AlertSource>> combinedAlerts 
                    = combineAlertsAndSources(last, srcList);
                
                while (combinedAlerts.hasNext()) {
                    SimpleEntry<? extends Alert, AlertSource> entry = combinedAlerts.next();
                    Alert alert = entry.getKey();
                    AlertSource src = entry.getValue();
                    
                    notifyListeners(alert);
                    
                    if (src instanceof AlertManager && alert.isActionable()) {
                        notifyRepsonders(alert, (AlertManager) src);
                    }
                }
            }
        });
    }
    
    protected Executor getListenersExecutor() {
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

    protected Executor getRespondersExecutor() {
        if (this.respondersExecutor == null) {
            synchronized (this) {
                if (this.respondersExecutor == null) {
                    this.respondersExecutor = Executors.newFixedThreadPool(1);
                }
            }
        }
        
        return respondersExecutor;
    }

    protected Iterator<SimpleEntry<? extends Alert, AlertSource>> combineAlertsAndSources(DateTime since, 
                                                                                          List<AlertSource> srcList) {
        List<Iterator<SimpleEntry<? extends Alert, AlertSource>>> iterators = new ArrayList<>();
        
        for (AlertSource src : srcList) {
            Function<Alert, SimpleEntry<? extends Alert, AlertSource>> func = combineFunction(src);
            Iterator<? extends Alert> itr = src.getAlerts(since);
            iterators.add(Iterators.transform(itr, func));
        }
        
        return Iterators.concat(iterators.iterator());
    }
    
    protected Iterator<? extends Alert> combineAlerts(DateTime since, List<AlertSource> srcList) {
        List<Iterator<? extends Alert>> iterators = new ArrayList<>();
        
        for (AlertSource src : srcList) {
            Iterator<? extends Alert> itr = src.getAlerts(since);
            iterators.add(itr);
        }
        
        return Iterators.concat(iterators.iterator());
    }

    private Function<Alert, SimpleEntry<? extends Alert, AlertSource>> combineFunction(final AlertSource src) {
        return new Function<Alert, SimpleEntry<? extends Alert,AlertSource>>() {
            @Override
            public SimpleEntry<? extends Alert, AlertSource> apply(Alert input) {
                return new SimpleEntry<Alert, AlertSource>(input, src);
            }
        };
    }
    
    private void notifyChanged(Alert alert, AlertManager manager) {
        notifyListeners(alert);
        notifyRepsonders(alert, manager);
    }

    protected void notifyListeners(final Alert alert) {
        Executor exec = getListenersExecutor();
        final List<? extends AlertListener> list;
    
        synchronized (this.listeners) {
            list = new ArrayList<>(this.listeners);
        }
        
        exec.execute(new Runnable() {
            @Override
            public void run() {
                for (AlertListener listener : list) {
                    listener.alertChange(alert);
                }
            }
        });
    }

    private void notifyRepsonders(Alert alert, AlertManager manager) {
        this.pendingResponses.put(alert.getId(), manager);
        signalResponders();
    }

    private void signalResponders() {
        Executor exec = getRespondersExecutor();
        final Map<Alert.ID, AlertManager> pending = new HashMap<>();
        
        synchronized (this.pendingResponses) {
            for (Map.Entry<Alert.ID, AlertManager> entry : this.pendingResponses.entrySet()) {
                pending.put(entry.getKey(), entry.getValue());
            }
        }
        
        if (pending.size() > 0) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    List<AlertResponder> respList = snapshotResponderts();

                    for (Map.Entry<Alert.ID, AlertManager> entry : pending.entrySet()) {
                        for (AlertResponder responder : respList) {
                            AlertManager manager = entry.getValue();
                            Alert alert = manager.getAlert(entry.getKey());

                            if (alert != null) {
                                alertChange(alert, responder, manager);
                            }
                        }
                    }
                }
            });
        }
    }
    
    protected void alertChange(Alert alert, AlertResponder responder, AlertManager manager) {
        ManagerAlertResponse resp = new ManagerAlertResponse(alert, manager);
        
        responder.alertChange(alert, resp);
        
        if (resp.resultAlert != null) {
            notifyChanged(resp.resultAlert, resp.manager);
        }
    }
    
    protected List<AlertListener> snapshotListeners() {
        List<AlertListener> listenerList;
        
        synchronized (BaseAlertProvider.this.listeners) {
            listenerList = new ArrayList<>(BaseAlertProvider.this.listeners);
        }
        return listenerList;
    }
    
    protected List<AlertResponder> snapshotResponderts() {
        List<AlertResponder> respList;
        
        synchronized (BaseAlertProvider.this.responders) {
            respList = new ArrayList<>(BaseAlertProvider.this.responders);
        }
        return respList;
    }
    
    protected List<AlertSource> snapshotSources() {
        List<AlertSource> srcList;
        
        synchronized (BaseAlertProvider.this.sources) {
            srcList = new ArrayList<>(BaseAlertProvider.this.sources);
        }
        return srcList;
    }

    private SimpleEntry<Alert, AlertManager> findActionableAlert(ID id) {
        for (AlertManager src : this.managers) {
            Alert alert = src.getAlert(id);
            
            if (alert != null && alert.isActionable()) {
                return new SimpleEntry<>(alert, src);
            }
        }
        
        return null;
    }
    
    
    private class ManagerAlertResponse implements AlertResponse {
        
        private final Alert targetAlert;
        private final AlertManager manager;

        private Alert resultAlert = null;
        
        public ManagerAlertResponse(Alert alert, AlertManager mgr) {
            this.targetAlert = alert;
            this.manager = mgr;
        }

        @Override
        public <C> void inProgress(C content) {
            changed(this.manager.changeState(this.targetAlert, Alert.State.IN_PROCESS, content));
        }

        @Override
        public <C> void handle(C content) {
            changed(this.manager.changeState(this.targetAlert, Alert.State.HANDLED, content));
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
            this.resultAlert = alert;
            notifyChanged(alert, this.manager);
        }
    }
}
