package com.thinkbiganalytics.integration.sla;

/*-
 * #%L
 * kylo-service-app
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

import com.thinkbiganalytics.alerts.rest.model.Alert;
import com.thinkbiganalytics.alerts.rest.model.AlertRange;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementGroup;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.metadata.rest.model.sla.ObligationAssessment;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment.Result.FAILURE;


/**
 * Creates a feed, creates two SLAs for the feed, which are expected to succeed and to fail,
 * triggers SLA assessments, asserts SLA assessment results, asserts SLA failures appear in Alerts
 */
public class SlaIT extends IntegrationTestBase {

    private static final String TEST_FILE = "sla-assessment.txt";

    @Test
    public void testSla() throws IOException {
        copyDataToDropzone(TEST_FILE);

        LocalDateTime now = LocalDateTime.now();
        String systemName = now.format(DateTimeFormatter.ofPattern("HH_mm_ss_SSS"));
        FeedMetadata response = createSimpleFeed("sla_" + systemName, TEST_FILE);

        waitForFeedToComplete();

        ServiceLevelAgreementGroup sla = createOneHourAgoFeedProcessingDeadlineSla(response.getCategoryAndFeedName(), response.getFeedId());
        triggerSla(sla.getName());
        assertSLA(sla.getId(), FAILURE);
        assertFilterByFailuresContains(sla.getId());
        assertFilterBySuccessContainsNot(sla.getId());
        assertFailedSlaAppearsInAlerts(sla.getId());
    }

    @Override
    public void startClean() {
        super.startClean();
    }

//    @Test
    public void temp() {
        assertFilterByFailuresContains("696e5bc0-e206-40dc-8be1-72550264a90b");
    }

    private void assertFailedSlaAppearsInAlerts(String slaId) {
        AlertRange range = getAlerts();
        List<Alert> alerts = range.getAlerts();
        Assert.assertTrue(alerts.stream().anyMatch(alert -> slaId.equals(alert.getEntityId()) && "http://kylo.io/alert/alert/sla/violation".equals(alert.getType().toString())));
    }

    private void assertFilterBySuccessContainsNot(String slaId) {
        ServiceLevelAssessment[] array = getServiceLevelAssessments("result%3D%3DSUCCESS");
        Assert.assertFalse(Arrays.stream(array).anyMatch(assessment -> slaId.equals(assessment.getAgreement().getId())));
    }

    private void assertFilterByFailuresContains(String slaId) {
        ServiceLevelAssessment[] array = getServiceLevelAssessments("result%3D%3DFAILURE");
        Assert.assertTrue(Arrays.stream(array).anyMatch(assessment -> slaId.equals(assessment.getAgreement().getId())));
    }

    private void assertSLA(String slaId, ServiceLevelAssessment.Result status) {
        ServiceLevelAssessment[] array = getServiceLevelAssessments("slaId%3D%3D" + slaId);
        for (ServiceLevelAssessment sla : array) {
            List<ObligationAssessment> list = sla.getObligationAssessments();
            for (ObligationAssessment assessment : list) {
                Assert.assertEquals(status, assessment.getResult());
            }
        }
    }
}
