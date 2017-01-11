/**
 * 
 */
package com.thinkbiganalytics.alerts.spi.defaults;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Iterator;
import java.util.Optional;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.Level;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver;
import com.thinkbiganalytics.alerts.spi.defaults.DefaultAlertManager;
import com.thinkbiganalytics.alerts.spi.defaults.DefaultAlertManagerConfig;
import com.thinkbiganalytics.metadata.persistence.MetadataPersistenceConfig;
import com.thinkbiganalytics.testing.jpa.TestPersistenceConfiguration;

/**
 *
 * @author Sean Felten
 */
@TestPropertySource(locations = "classpath:test-jpa-application.properties")
@SpringApplicationConfiguration(classes = { MetadataPersistenceConfig.class, TestPersistenceConfiguration.class, DefaultAlertManagerConfig.class })
public class DefaultAlertManagerTest extends AbstractTestNGSpringContextTests {
    
    @Mock
    private AlertNotifyReceiver alertReceiver;
    
    @Inject
    private DefaultAlertManager manager;
    
    private Alert.ID id1;
    private Alert.ID id2;
    private DateTime beforeTime;
    private DateTime middleTime;
    private DateTime afterTime;
    
    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
        manager.addReceiver(alertReceiver);
    }
    
    @AfterMethod
    public void afterMethod() {
        Mockito.reset(this.alertReceiver);
    }

    @Test(groups="create")
    public void testCreateAlert() throws Exception {
        this.beforeTime = DateTime.now().minusMillis(50);
        Alert alert1 = this.manager.create(URI.create("http://example.com/test/alert/1"), Level.MINOR, "1st description", "1st content");
        Thread.sleep(100);
        Alert alert2 = this.manager.create(URI.create("http://example.com/test/alert/2"), Level.CRITICAL, "2nd description", "2nd content");
        Thread.sleep(100);

        assertThat(alert1)
            .isNotNull()
            .extracting("type", "level", "description", "content")
            .contains(URI.create("http://example.com/test/alert/1"), Level.MINOR, "1st description", "1st content");
        assertThat(alert2)
            .isNotNull()
            .extracting("type", "level", "description", "content")
            .contains(URI.create("http://example.com/test/alert/2"), Level.CRITICAL, "2nd description", "2nd content");
        assertThat(alert2.getEvents())
            .hasSize(1)
            .extracting("state", "content")
            .contains(tuple(State.UNHANDLED, null));
        
        verify(this.alertReceiver, times(2)).alertsAvailable(anyInt());
        
        this.middleTime = alert2.getCreatedTime().minusMillis(50);
        this.afterTime = DateTime.now();
        this.id1 = alert1.getId();
        this.id2 = alert2.getId();
    }
    
    @Test(dependsOnGroups="create", groups="read1")
    public void testGetAlerts() {
        Iterator<Alert> itr = this.manager.getAlerts(null);
        
        assertThat(itr)
            .isNotNull()
            .hasSize(2)
            .extracting("type", "level", "description", "content")
            .contains(tuple(URI.create("http://example.com/test/alert/1"), Level.MINOR, "1st description", "1st content"),
                      tuple(URI.create("http://example.com/test/alert/2"), Level.CRITICAL, "2nd description", "2nd content"));
    }
    
    @Test(dependsOnGroups="create", groups="read1")
    public void testGetAlertById() {
        Optional<Alert> optional = this.manager.getAlert(id2);
        
        assertThat(optional.isPresent()).isTrue();
        assertThat(optional.get())
            .isNotNull()
            .extracting("id", "type", "level", "description", "content")
            .contains(this.id2, URI.create("http://example.com/test/alert/2"), Level.CRITICAL, "2nd description", "2nd content");
        assertThat(optional.get().getEvents())
            .hasSize(1)
            .extracting("state", "content")
            .contains(tuple(State.UNHANDLED, null));
    }
    
    @Test(dependsOnGroups="create", groups="read1")
    public void testGetAlertsSinceTime() {
        Iterator<Alert> itr = this.manager.getAlerts(this.manager.criteria().after(this.beforeTime));
        
        assertThat(itr).isNotNull().hasSize(2).extracting("id").contains(this.id1, this.id2);
        
        itr = this.manager.getAlerts(this.manager.criteria().after(this.middleTime));
        
        assertThat(itr).isNotNull().hasSize(1).extracting("id").contains(this.id2);
        
        itr = this.manager.getAlerts(this.manager.criteria().after(this.afterTime));
        
        assertThat(itr).isNotNull().hasSize(0);
    }
    
    @Test(dependsOnGroups="create", groups="read1")
    public void testGetAlertsByType() {
        Iterator<Alert> itr = this.manager.getAlerts(this.manager.criteria().type(URI.create("http://example.com/test/alert/1")));
        
        assertThat(itr).isNotNull().hasSize(1).extracting("id").contains(this.id1);
    }
    
    @Test(dependsOnGroups="create", groups="read1")
    public void testGetAlertsBySuperType() {
        Iterator<Alert> itr = this.manager.getAlerts(this.manager.criteria().type(URI.create("http://example.com/test/alert")));
        
        assertThat(itr).isNotNull().hasSize(2).extracting("id").contains(this.id1, this.id2);
    }
    
    @Test(dependsOnGroups="create", groups="update1")
    public void testAlertResponding() {
        Alert alert = this.manager.getAlert(id1).get();
        AlertResponse resp = this.manager.getResponse(alert);
        
        alert = resp.inProgress("Change in progress");
        
        assertThat(alert.getEvents())
            .hasSize(2)
            .extracting("state", "description", "content")
            .contains(tuple(State.UNHANDLED, null, null), tuple(State.IN_PROGRESS, "Change in progress", null));
        
        alert = resp.handle("Change handled", new Integer(42));
        
        assertThat(alert.getEvents())
            .hasSize(3)
            .extracting("state", "description", "content")
            .contains(tuple(State.UNHANDLED, null, null), tuple(State.IN_PROGRESS, "Change in progress", null), tuple(State.HANDLED, "Change handled", 42));
        
        verify(this.alertReceiver, times(2)).alertsAvailable(anyInt());
    }
    
    @Test(dependsOnGroups="update1", groups="read2")
    public void testGetAlertsByState() {
        Iterator<Alert> itr = this.manager.getAlerts(this.manager.criteria().state(Alert.State.UNHANDLED));
        
        assertThat(itr).isNotNull().hasSize(1).extracting("id").contains(this.id2);
        
        itr = this.manager.getAlerts(this.manager.criteria().state(Alert.State.HANDLED));
        
        assertThat(itr).isNotNull().hasSize(1);

        itr = this.manager.getAlerts(this.manager.criteria().state(Alert.State.CREATED));
        
        assertThat(itr).isNotNull().hasSize(0);
    }
    
    @Test(dependsOnGroups="read2", groups="update2")
    public void testClear() {
        Alert alert = this.manager.getAlert(id1).get();
        AlertResponse resp = this.manager.getResponse(alert);
        
        resp.clear();
        
        alert = this.manager.getAlert(id1).get();
        
        assertThat(alert).isNotNull();
        assertThat(alert.isCleared()).isTrue();
        verify(this.alertReceiver, times(0)).alertsAvailable(anyInt());
    }
    
    @Test(dependsOnGroups="update2", groups="read3")
    public void testGetAlertsAfterClear() {
        Iterator<Alert> itr = this.manager.getAlerts(null);
        
        assertThat(itr)
            .isNotNull()
            .hasSize(1)
            .extracting("id", "type", "level", "description", "content")
            .contains(tuple(this.id2, URI.create("http://example.com/test/alert/2"), Level.CRITICAL, "2nd description", "2nd content"));
    }
}
