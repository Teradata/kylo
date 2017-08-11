/**
 *
 */
package com.thinkbiganalytics.metadata.sla.alerts;

/*-
 * #%L
 * thinkbig-sla-core
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
import com.thinkbiganalytics.alerts.sla.AssessmentAlerts;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 */
public class AssessmentAlertGenerator {

    @Inject
    private AlertManager alertManager;

    /**
     *
     */
    public AssessmentAlertGenerator() {
    }

    @PostConstruct
    public void initAlertTypes() {
        this.alertManager.addDescriptor(AssessmentAlerts.VIOLATION_ALERT);
    }

    public Alert generateViolationAlert(Alert.Level level, ServiceLevelAssessment assessment) {
        String description = "";

        return this.alertManager.create(AssessmentAlerts.VIOLATION_ALERT_TYPE, "SLA",level, description, assessment);
    }

}
