package com.thinkbiganalytics.alerts.api.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertChangeEvent;
import com.thinkbiganalytics.alerts.api.AlertListener;
import com.thinkbiganalytics.alerts.api.AlertResponder;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.api.core.AggregatingAlertProvider.AlertInvocationHandler;
import com.thinkbiganalytics.alerts.api.core.AggregatingAlertProvider.SourceAlertID;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertSource;
import com.thinkbiganalytics.metadata.event.reactor.ReactorContiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ReactorContiguration.class, AggregatingAlertProviderTest.TestConfig.class })
public class AggregatingAlertProviderTest {
    
    @Inject
    private AggregatingAlertProvider provider;
    
    @Mock
    private AlertSource source;
    
    @Mock
    private AlertManager manager;
    
    @Mock
    private AlertResponse response;
    
    @Mock
    private AlertListener listener;
    
    @Mock
    private AlertResponder responder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
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
        
        when(this.source.getAlert(any(Alert.ID.class))).thenReturn(Optional.of(srcAlert));
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(Optional.of(mgrAlert));
        
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
        
        when(this.source.getAlert(any(Alert.ID.class))).thenReturn(Optional.of(sinceAlert));
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
        
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(Optional.of(mgrAlert));
        
        this.provider.respondTo(new SourceAlertID(mgrAlert.getId(), this.manager), this.responder);
        
        verify(this.responder).alertChange(any(Alert.class), any(AlertResponse.class));
    }
    
    @Test
    public void testRespondToNonActionable() {
        TestAlert mgrAlert = new TestAlert(this.manager, false);
        
        this.provider.addAlertManager(this.manager);
        
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(Optional.of(mgrAlert));
        
        this.provider.respondTo(new SourceAlertID(mgrAlert.getId(), this.manager), this.responder);
        
        verify(this.responder, never()).alertChange(any(Alert.class), any(AlertResponse.class));
    }
    
    @Test
    public void testRespondToChange() throws InterruptedException {
        TestAlert initMgrAlert = new TestAlert(this.manager, true);
        CountDownLatch latch = new CountDownLatch(1);

        this.provider.addAlertManager(this.manager);
        
        when(this.manager.getAlert(any(Alert.ID.class))).thenReturn(Optional.of(initMgrAlert));
        when(this.manager.getResponse(any(Alert.class))).thenReturn(this.response);
        when(this.response.handle(any(Serializable.class))).thenReturn(initMgrAlert);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                latch.countDown();
                return null;
            }
        }).when(this.listener).alertChange(any(Alert.class));
        
        this.provider.respondTo(new SourceAlertID(initMgrAlert.getId(), this.manager), new AlertResponder() {
            @Override
            public void alertChange(Alert alert, AlertResponse response) {
                response.handle("handled");
            }
        });
        
        latch.await(10, TimeUnit.SECONDS);
        
        verify(this.response).handle(eq("handled"));
        verify(this.listener, atLeastOnce()).alertChange(any(Alert.class));
    }

    @Test
    public void testAlertsAvailable() throws InterruptedException {
        TestAlert mgrAlert = new TestAlert(this.manager, true);
        TestAlert srcAlert = new TestAlert(this.source);
        CountDownLatch latch = new CountDownLatch(3);
        
        this.provider.addAlertSource(this.source);
        this.provider.addAlertManager(this.manager);
        
        when(this.source.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(srcAlert));
        when(this.manager.getAlerts(any(DateTime.class))).thenAnswer(iteratorAnswer(mgrAlert));
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                latch.countDown();
                return null;
            }
        }).when(this.listener).alertChange(any(Alert.class));
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                latch.countDown();
                return null;
            }
        }).when(this.responder).alertChange(any(Alert.class), any(AlertResponse.class));
        
        this.provider.alertsAvailable(2);
        
        latch.await(10, TimeUnit.SECONDS);
        
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
                if (Proxy.isProxyClass(input.getClass())) {
                    AlertInvocationHandler handler = (AlertInvocationHandler) Proxy.getInvocationHandler(input);
                    return handler.getWrappedAlert();
                } else {
                    return input;
                }
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
        public DateTime getCreatedTime() {
            return this.createdTime;
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
        public List<AlertChangeEvent> getEvents() {
            return Collections.singletonList(new TestChangeEvent(this));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C extends Serializable> C getContent() {
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
        public DateTime getChangeTime() {
            return this.time;
        }

        @Override
        public State getState() {
            return alert.isActionable() ? State.CREATED : State.UNHANDLED;
        }

        @Override
        public <C extends Serializable> C getContent() {
            return null;
        }
    }

    @Configuration
    public static class TestConfig {
        @Bean(name = "alertProvider")
        @Primary
        @Scope("prototype")
        public AggregatingAlertProvider alertProvider() {
            AggregatingAlertProvider provider = new AggregatingAlertProvider();
            provider.setAvailableAlertsExecutor(MoreExecutors.directExecutor());
            return provider;
        }
    }
}
