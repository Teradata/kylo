/**
 *
 */
package com.thinkbiganalytics.alerts.spi.defaults;

/*-
 * #%L
 * thinkbig-alerts-default
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
import com.thinkbiganalytics.alerts.api.Alert.Level;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver;
import com.thinkbiganalytics.metadata.persistence.MetadataPersistenceConfig;
import com.thinkbiganalytics.testing.jpa.TestPersistenceConfiguration;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 *
 */
@TestPropertySource(locations = "classpath:test-jpa-application.properties")
@SpringApplicationConfiguration(classes = {MetadataPersistenceConfig.class, TestPersistenceConfiguration.class, KyloAlertManagerConfig.class, SpringOperationalMetadataTestConfiguration.class})
public class DefaultAlertManagerTest extends AbstractTestNGSpringContextTests {

    private static final String LONG_DESCR;
    private static final String TRUNK_DESCR;

    static {
        char[] descr = new char[257];
        Arrays.fill(descr, 'x');
        LONG_DESCR = new String(descr);
        TRUNK_DESCR = LONG_DESCR.substring(0, 252) + "...";
    }

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

    @Test(groups = "create")
    public void testCreateAlert() throws Exception {
        this.beforeTime = DateTime.now().minusMillis(50);
        Alert alert1 = this.manager.create(URI.create("http://example.com/test/alert/1"), "subtype",Level.MINOR, LONG_DESCR, "1st content");
        Thread.sleep(100);
        Alert alert2 = this.manager.create(URI.create("http://example.com/test/alert/2"), "subtype",Level.CRITICAL, "2nd description", "2nd content");
        Thread.sleep(100);

        Assertions.assertThat(alert1)
            .isNotNull()
            .extracting("type", "level", "description", "content")
            .contains(URI.create("http://example.com/test/alert/1"), Level.MINOR, TRUNK_DESCR, "1st content");
        Assertions.assertThat(alert2)
            .isNotNull()
            .extracting("type", "level", "description", "content")
            .contains(URI.create("http://example.com/test/alert/2"), Level.CRITICAL, "2nd description", "2nd content");
        Assertions.assertThat(alert2.getEvents())
            .hasSize(1)
            .extracting("state", "content")
            .contains(Assertions.tuple(State.UNHANDLED, null));

        Mockito.verify(this.alertReceiver, Mockito.times(2)).alertsAvailable(Matchers.anyInt());

        this.middleTime = alert2.getCreatedTime().minusMillis(50);
        this.afterTime = DateTime.now();
        this.id1 = alert1.getId();
        this.id2 = alert2.getId();
    }

    @Test(dependsOnGroups = "create", groups = "read1")
    public void testGetAlerts() {
        Iterator<Alert> itr = this.manager.getAlerts(this.manager.criteria().asServiceAccount(true));

        Assertions.assertThat(itr)
            .isNotNull()
            .hasSize(2)
            .extracting("type", "level", "description", "content")
            .contains(Assertions.tuple(URI.create("http://example.com/test/alert/1"), Level.MINOR, TRUNK_DESCR, "1st content"),
                      Assertions.tuple(URI.create("http://example.com/test/alert/2"), Level.CRITICAL, "2nd description", "2nd content"));
    }

    @Test(dependsOnGroups = "create", groups = "read1")
    public void testGetAlertById() {
        Optional<Alert> optional = this.manager.getAlertAsServiceAccount(id2);

        Assertions.assertThat(optional.isPresent()).isTrue();
        Assertions.assertThat(optional.get())
            .isNotNull()
            .extracting("id", "type", "level", "description", "content")
            .contains(this.id2, URI.create("http://example.com/test/alert/2"), Level.CRITICAL, "2nd description", "2nd content");
        Assertions.assertThat(optional.get().getEvents())
            .hasSize(1)
            .extracting("state", "content")
            .contains(Assertions.tuple(State.UNHANDLED, null));
    }

    @Test(dependsOnGroups = "create", groups = "read1")
    public void testGetAlertsSinceTime() {
        Iterator<Alert> itr = this.manager.getAlerts(this.manager.criteria().asServiceAccount(true).after(this.beforeTime));

        Assertions.assertThat(itr).isNotNull().hasSize(2).extracting("id").contains(this.id1, this.id2);

        itr = this.manager.getAlerts(this.manager.criteria().asServiceAccount(true).after(this.middleTime));

        Assertions.assertThat(itr).isNotNull().hasSize(1).extracting("id").contains(this.id2);

        itr = this.manager.getAlerts(this.manager.criteria().asServiceAccount(true).after(this.afterTime));

        Assertions.assertThat(itr).isNotNull().hasSize(0);
    }

    @Test(dependsOnGroups = "create", groups = "read1")
    public void testGetAlertsByType() {
        Iterator<Alert> itr = this.manager.getAlerts(this.manager.criteria().asServiceAccount(true).type(URI.create("http://example.com/test/alert/1")));

        Assertions.assertThat(itr).isNotNull().hasSize(1).extracting("id").contains(this.id1);
    }

    @Test(dependsOnGroups = "create", groups = "read1")
    public void testGetAlertsBySuperType() {
        Iterator<Alert> itr = this.manager.getAlerts(this.manager.criteria().asServiceAccount(true).type(URI.create("http://example.com/test/alert")));

        Assertions.assertThat(itr).isNotNull().hasSize(2).extracting("id").contains(this.id1, this.id2);
    }

    @Test(dependsOnGroups = "create", groups = "update1")
    public void testAlertResponding() {
        Alert alert = this.manager.getAlertAsServiceAccount(id1).get();
        AlertResponse resp = this.manager.getResponse(alert);

        alert = resp.inProgress(LONG_DESCR);

        Assertions.assertThat(alert.getEvents())
            .hasSize(2)
            .extracting("state", "description", "content")
            .contains(Assertions.tuple(State.UNHANDLED, null, null), Assertions.tuple(State.IN_PROGRESS, TRUNK_DESCR, null));

        alert = resp.handle("Change handled", new Integer(42));

        Assertions.assertThat(alert.getEvents())
            .hasSize(3)
            .extracting("state", "description", "content")
            .contains(Assertions.tuple(State.UNHANDLED, null, null), Assertions.tuple(State.IN_PROGRESS, TRUNK_DESCR, null), Assertions.tuple(State.HANDLED, "Change handled", 42));

        Mockito.verify(this.alertReceiver, Mockito.times(2)).alertsAvailable(Matchers.anyInt());
    }

    @Test(dependsOnGroups = "update1", groups = "read2")
    public void testGetAlertsByState() {
        Iterator<Alert> itr = this.manager.getAlerts(this.manager.criteria().asServiceAccount(true).state(Alert.State.UNHANDLED));

        Assertions.assertThat(itr).isNotNull().hasSize(1).extracting("id").contains(this.id2);

        itr = this.manager.getAlerts(this.manager.criteria().asServiceAccount(true).state(Alert.State.HANDLED));

        Assertions.assertThat(itr).isNotNull().hasSize(1);

        itr = this.manager.getAlerts(this.manager.criteria().asServiceAccount(true).state(Alert.State.CREATED));

        Assertions.assertThat(itr).isNotNull().hasSize(0);
    }

    @Test(dependsOnGroups = "read2", groups = "update2")
    public void testClear() {
        Alert alert = this.manager.getAlertAsServiceAccount(id1).get();
        AlertResponse resp = this.manager.getResponse(alert);

        resp.clear();

        alert = this.manager.getAlertAsServiceAccount(id1).get();

        Assertions.assertThat(alert).isNotNull();
        Assertions.assertThat(alert.isCleared()).isTrue();
        Mockito.verify(this.alertReceiver, Mockito.times(0)).alertsAvailable(Matchers.anyInt());
    }

    @Test(dependsOnGroups = "update2", groups = "read3")
    public void testGetAlertsAfterClear() {
        Iterator<Alert> itr = this.manager.getAlerts(this.manager.criteria().asServiceAccount(true));

        Assertions.assertThat(itr)
            .isNotNull()
            .hasSize(1)
            .extracting("id", "type", "level", "description", "content")
            .contains(Assertions.tuple(this.id2, URI.create("http://example.com/test/alert/2"), Level.CRITICAL, "2nd description", "2nd content"));
    }
}
