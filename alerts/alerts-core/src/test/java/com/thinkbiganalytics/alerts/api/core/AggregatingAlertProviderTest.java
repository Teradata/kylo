package com.thinkbiganalytics.alerts.api.core;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.ID;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.api.AlertListener;
import com.thinkbiganalytics.alerts.api.AlertResponder;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.api.core.AggregatingAlertProvider.AlertInvocationHandler;
import com.thinkbiganalytics.alerts.api.core.AggregatingAlertProvider.SourceAlertID;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertSource;

public class AggregatingAlertProviderTest {
    
    private AggregatingAlertProvider provider = new AggregatingAlertProvider();
    
    @Mock
    private AlertSource source;
    
    @Mock
    private AlertManager manager;
    
    @Mock
    private AlertListener listener;
    
    @Mock
    private AlertResponder responder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        provider.setListenersExecutor(MoreExecutors.directExecutor());
        provider.setRespondersExecutor(MoreExecutors.directExecutor());
        provider.addListener(this.listener);
        provider.addResponder(this.responder);
    }
    
    @Test
    public void testResolve() {
        TestAlert mgrAlert = new TestAlert(this.manager);
        TestAlert srcAlert = new TestAlert(this.source);
        SourceAlertID mgrId = new SourceAlertID(mgrAlert.getId(), this.manager);
        SourceAlertID srcId = new SourceAlertID(srcAlert.getId(), this.source);
        String mgrIdStr = mgrId.toString();
        String srcIdStr = srcId.toString();
        
        this.provider.addAlertSource(this.source);
        this.provider.addAlertManager(this.manager);

        when(this.source.resolve(any(TestID.class))).thenReturn(srcAlert.getId());
        when(this.manager.resolve(any(TestID.class))).thenReturn(mgrAlert.getId());
        when(this.source.resolve(any(String.class))).thenReturn(srcAlert.getId());
        when(this.manager.resolve(any(String.class))).thenReturn(mgrAlert.getId());
        
        Alert.ID mgrObjResolved = this.provider.resolve(mgrId);
        Alert.ID srcObjResolved = this.provider.resolve(srcId);
        Alert.ID mgrStrResolved = this.provider.resolve(mgrIdStr);
        Alert.ID srcStrResolved = this.provider.resolve(srcIdStr);
        
        assertThat(mgrObjResolved).isInstanceOf(SourceAlertID.class).isEqualTo(mgrId).isEqualTo(mgrStrResolved);
        assertThat(srcObjResolved).isInstanceOf(SourceAlertID.class).isEqualTo(srcId).isEqualTo(srcStrResolved);
    }
    
    @Test
    public void testGetAlert() {
        TestAlert mgrAlert = new TestAlert(this.manager);
        TestAlert srcAlert = new TestAlert(this.source);
        SourceAlertID mgrId = new SourceAlertID(mgrAlert.getId(), this.manager);
        SourceAlertID srcId = new SourceAlertID(srcAlert.getId(), this.source);
        
        this.provider.addAlertSource(this.source);
        this.provider.addAlertManager(this.manager);
        
        when(this.source.getAlert(any(Alert.ID.class))).thenReturn(srcAlert);
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(mgrAlert);
        
        Alert getMgrAlert = providerToSourceAlertFunction().apply(this.provider.getAlert(mgrId));
        Alert getSrcAlert = providerToSourceAlertFunction().apply(this.provider.getAlert(srcId));
        
        assertThat(getMgrAlert).isEqualTo(mgrAlert);
        assertThat(getSrcAlert).isEqualTo(srcAlert);
    }

    @Test
    public void testGetAlertsSinceTimeSourceOnly() {
        DateTime since = DateTime.now().minusSeconds(1);
        TestAlert srcAlert = new TestAlert(this.source);
        
        this.provider.addAlertSource(this.source);
        
        when(this.source.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(srcAlert));
        
        Iterator<? extends Alert> results = this.provider.getAlerts(since);
        Iterator<? extends Alert> itr = Iterators.transform(results, providerToSourceAlertFunction());
        Alert alert = itr.next();
        
        assertThat(alert).isEqualTo(srcAlert);
    }

    @Test
    public void testGetAlertsSinceTimeManagerOnly() {
        DateTime since = DateTime.now().minusSeconds(1);
        TestAlert mgrAlert = new TestAlert(this.manager);
        
        this.provider.addAlertManager(this.manager);
        
        when(this.manager.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(mgrAlert));
        
        Iterator<? extends Alert> results = this.provider.getAlerts(since);
        Iterator<? extends Alert> itr = Iterators.transform(results, providerToSourceAlertFunction());
        Alert alert = itr.next();
        
        assertThat(alert).isEqualTo(mgrAlert);
    }

    @Test
    public void testGetAlertsSinceTimeAllSources() {
        DateTime since = DateTime.now().minusSeconds(1);
        TestAlert mgrAlert = new TestAlert(this.manager);
        TestAlert srcAlert = new TestAlert(this.source);
        
        this.provider.addAlertSource(this.source);
        this.provider.addAlertManager(this.manager);
        
        when(this.source.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(srcAlert));
        when(this.manager.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(mgrAlert));
        
        Iterator<? extends Alert> results = this.provider.getAlerts(since);
        Iterator<? extends Alert> itr = Iterators.transform(results, providerToSourceAlertFunction());
        List<Alert> alerts = Lists.newArrayList(itr);
        
        assertThat(alerts).hasSize(2).contains(srcAlert, mgrAlert);
    }
    
    @Test
    public void testGetAlertsIdAllSources() {
        DateTime since = DateTime.now().minusSeconds(1);
        TestAlert sinceAlert = new TestAlert(this.source, since);
        TestAlert srcAlert = new TestAlert(this.source);
        TestAlert mgrAlert = new TestAlert(this.manager);
        
        this.provider.addAlertSource(this.source);
        this.provider.addAlertManager(this.manager);
        
        when(this.source.getAlert(any(Alert.ID.class))).thenReturn(sinceAlert);
        when(this.source.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(srcAlert));
        when(this.manager.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(mgrAlert));
        
        Iterator<? extends Alert> results = this.provider.getAlerts(new SourceAlertID(sinceAlert.getId(), this.source));
        Iterator<? extends Alert> itr = Iterators.transform(results, providerToSourceAlertFunction());
        List<Alert> alerts = Lists.newArrayList(itr);
        
        assertThat(alerts).hasSize(2).contains(srcAlert, mgrAlert);
    }
    
    @Test
    public void testRespondToActionable() {
        TestAlert mgrAlert = new TestAlert(this.manager, true);
        
        this.provider.addAlertManager(this.manager);
        
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(mgrAlert);
        
        this.provider.respondTo(new SourceAlertID(mgrAlert.getId(), this.manager), this.responder);
        
        verify(this.responder).alertChange(any(Alert.class), any(AlertResponse.class));
    }
    
    @Test
    public void testRespondToNonActionable() {
        TestAlert mgrAlert = new TestAlert(this.manager, false);
        
        this.provider.addAlertManager(this.manager);
        
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(mgrAlert);
        
        this.provider.respondTo(new SourceAlertID(mgrAlert.getId(), this.manager), this.responder);
        
        verify(this.responder, never()).alertChange(any(Alert.class), any(AlertResponse.class));
    }
    
    @Test
    public void testRespondToChange() {
        TestAlert initMgrAlert = new TestAlert(this.manager, true);
        TestAlert resultMgrAlert = new TestAlert(this.manager, true);
        
        this.provider.addAlertManager(this.manager);
        
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(initMgrAlert);
        when(this.manager.changeState(any(Alert.class), any(Alert.State.class), any())).thenReturn(resultMgrAlert);
        
        this.provider.respondTo(new SourceAlertID(initMgrAlert.getId(), this.manager), new AlertResponder() {
            @Override
            public void alertChange(Alert alert, AlertResponse response) {
                response.handle("handled");
            }
        });
        
        verify(this.manager).changeState(any(Alert.class), eq(Alert.State.HANDLED), eq("handled"));
        verify(this.listener, atLeastOnce()).alertChange(any(Alert.class));
        verify(this.responder, atLeastOnce()).alertChange(any(Alert.class), any(AlertResponse.class));
    }

    @Test
    public void testAlertsAvailable() {
        TestAlert mgrAlert = new TestAlert(this.manager, true);
        TestAlert srcAlert = new TestAlert(this.source);
        
        this.provider.addAlertSource(this.source);
        this.provider.addAlertManager(this.manager);
        
        when(this.source.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(srcAlert));
        when(this.manager.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(mgrAlert));
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(mgrAlert);
        
        this.provider.alertsAvailable(2);
        
        verify(this.listener, times(2)).alertChange(any(Alert.class));
        verify(this.responder, times(1)).alertChange(any(Alert.class), any(AlertResponse.class));
    }
    
    
    private Answer<Iterator<? extends Alert>> iteratorAnswer(final Alert... alerts) {
        return new Answer<Iterator<? extends Alert>>() {
            @Override
            public Iterator<? extends Alert> answer(InvocationOnMock invocation) throws Throwable {
                return interator(alerts);
            }
        };
    }

    private Iterator<? extends Alert> interator(Alert... alerts) {
        return Arrays.asList(alerts).iterator();
    }

    private Function<Alert, Alert> providerToSourceAlertFunction() {
        return new Function<Alert, Alert>() {
            @Override
            public Alert apply(Alert input) {
                Proxy.isProxyClass(input.getClass());
                AlertInvocationHandler handler = (AlertInvocationHandler) Proxy.getInvocationHandler(input);
                return handler.getWrappedAlert();
            }
        };
    }
    
    
    private static class TestID implements Alert.ID { 
        @Override
        public String toString() {
            return "TestID:" + hashCode();
        }
    }
    
    private class TestAlert implements Alert {
        
        private TestID id = new TestID();
        private AlertSource source;
        private boolean actionable;
        private DateTime createdTime;
        
        public TestAlert(AlertSource src) {
            this(src, src instanceof AlertManager);
        }
        
        public TestAlert(AlertSource src, boolean actionable) {
            this(src, actionable, DateTime.now());
        }
        
        public TestAlert(AlertSource src, DateTime created) {
            this(src, src instanceof AlertManager, created);
        }

        public TestAlert(AlertSource src, boolean actionable, DateTime created) {
            this.source =  src;
            this.actionable = actionable;
            this.createdTime = created;
        }
        
        @Override
        @SuppressWarnings("serial")
        public ID getId() {
            return this.id;
        }

        @Override
        public URI getType() {
            return URI.create("http://com.example/alert/test");
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public Level getLevel() {
            return Level.INFO;
        }

        @Override
        public AlertSource getSource() {
            return this.source;
        }

        @Override
        public boolean isActionable() {
            return this.actionable;
        }

        @Override
        public List<? extends AlertChangeEvent> getEvents() {
            return Collections.singletonList(new TestChangeEvent(this));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C> C getContent() {
            return (C) new Boolean(this.actionable);
        }
    }
    
    private class TestChangeEvent implements AlertChangeEvent {
        
        private TestAlert alert;
        private DateTime time = DateTime.now();

        public TestChangeEvent(TestAlert alert) {
            this.alert = alert;
            this.time = alert.createdTime;
        }

        @Override
        public ID getAlertId() {
            return this.alert.getId();
        }

        @Override
        public DateTime getChangeTime() {
            return this.time;
        }

        @Override
        public State getState() {
            return alert.isActionable() ? State.CREATED : State.UNHANDLED;
        }

        @Override
        public <C> C getContent() {
            return null;
        }
    }

}
