package com.thinkbiganalytics.alerts.api.core;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

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

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.ID;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.api.AlertListener;
import com.thinkbiganalytics.alerts.api.AlertResponder;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertSource;

public class BaseAlertProviderTest {
    
    private BaseAlertProvider provider = new BaseAlertProvider();
    
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
        
        provider.setListenersExecutor(MoreExecutors.sameThreadExecutor());
        provider.setRespondersExecutor(MoreExecutors.sameThreadExecutor());
        provider.addListener(this.listener);
        provider.addResponder(this.responder);
    }

    @Test
    public void testGetAlertsSourceOnly() {
        final TestAlert srcAlert = new TestAlert(this.source);
        
        this.provider.addAlertSource(this.source);
        
        when(this.source.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(srcAlert));
        
        Iterator<? extends Alert> itr = this.provider.getAlerts(DateTime.now().minusSeconds(1));
        Alert alert = itr.next();
        
        assertThat(alert).isEqualTo(srcAlert);
    }

    @Test
    public void testGetAlertsManagerOnly() {
        final TestAlert mgrAlert = new TestAlert(this.manager);
        
        this.provider.addAlertManager(this.manager);
        
        when(this.manager.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(mgrAlert));
        
        Iterator<? extends Alert> itr = this.provider.getAlerts(DateTime.now().minusSeconds(1));
        Alert alert = itr.next();
        
        assertThat(alert).isEqualTo(mgrAlert);
    }

    @Test
    public void testGetAlertsAllSources() {
        final TestAlert mgrAlert = new TestAlert(this.manager);
        final TestAlert srcAlert = new TestAlert(this.source);
        
        this.provider.addAlertSource(this.source);
        this.provider.addAlertManager(this.manager);
        
        when(this.source.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(srcAlert));
        when(this.manager.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(mgrAlert));
        
        Iterator<? extends Alert> itr = this.provider.getAlerts(DateTime.now().minusSeconds(1));
        List<Alert> alerts = Lists.newArrayList(itr);
        
        assertThat(alerts).contains(srcAlert, mgrAlert);
    }
    
    @Test
    public void testRespondToActionable() {
        final TestAlert mgrAlert = new TestAlert(this.manager, true);
        
        this.provider.addAlertManager(this.manager);
        
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(mgrAlert);
        
        this.provider.respondTo(mgrAlert.getId(), this.responder);
        
        verify(this.responder).alertChange(eq(mgrAlert), any(AlertResponse.class));
    }
    
    @Test
    public void testRespondToNonActionable() {
        final TestAlert mgrAlert = new TestAlert(this.manager, false);
        
        this.provider.addAlertManager(this.manager);
        
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(mgrAlert);
        
        this.provider.respondTo(mgrAlert.getId(), this.responder);
        
        verify(this.responder, never()).alertChange(eq(mgrAlert), any(AlertResponse.class));
    }
    
    @Test
    public void testRespondToChange() {
        final TestAlert initAlert = new TestAlert(this.manager, true);
        final TestAlert resultAlert = new TestAlert(this.manager, true);
        
        this.provider.addAlertManager(this.manager);
        
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(initAlert);
        when(this.manager.changeState(any(Alert.class), any(Alert.State.class), any())).thenReturn(resultAlert);
        
        this.provider.respondTo(initAlert.getId(), new AlertResponder() {
            @Override
            public void alertChange(Alert alert, AlertResponse response) {
                response.handle("handled");
            }
        });
        
        verify(this.manager).changeState(initAlert, Alert.State.HANDLED, "handled");
        verify(this.listener, atLeastOnce()).alertChange(resultAlert);
        verify(this.responder, atLeastOnce()).alertChange(any(Alert.class), any(AlertResponse.class));
    }

    @Test
    public void testAlertsAvailable() {
        final TestAlert mgrAlert = new TestAlert(this.manager, true);
        final TestAlert srcAlert = new TestAlert(this.source);
        
        this.provider.addAlertSource(this.source);
        this.provider.addAlertManager(this.manager);
        
        when(this.source.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(srcAlert));
        when(this.manager.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(mgrAlert));
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(mgrAlert);
        
        this.provider.alertsAvailable(2);
        
        verify(this.listener).alertChange(srcAlert);
        verify(this.listener).alertChange(mgrAlert);
        verify(this.responder, never()).alertChange(eq(srcAlert), any(AlertResponse.class));
        verify(this.responder).alertChange(eq(mgrAlert), any(AlertResponse.class));
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

    
    private class TestAlert implements Alert {
        
        private AlertSource source;
        private boolean actionable;
        
        public TestAlert(AlertSource src) {
            this(src, src instanceof AlertManager);
        }
        
        public TestAlert(AlertSource src, boolean actionable) {
            this.source =  src;
            this.actionable = actionable;
        }

        @Override
        @SuppressWarnings("serial")
        public ID getId() {
            return new ID() { };
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
