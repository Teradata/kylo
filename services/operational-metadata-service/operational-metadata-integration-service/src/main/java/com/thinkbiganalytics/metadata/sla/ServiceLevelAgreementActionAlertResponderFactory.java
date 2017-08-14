package com.thinkbiganalytics.metadata.sla;

/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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
import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.api.AlertResponder;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.sla.AssessmentAlerts;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreementCheck;
import com.thinkbiganalytics.metadata.sla.alerts.ServiceLevelAgreementActionUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionValidation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessmentProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;

/**
 */
public class ServiceLevelAgreementActionAlertResponderFactory implements AlertResponder {

    private static final Logger log = LoggerFactory.getLogger(ServiceLevelAgreementActionAlertResponderFactory.class);

    @Inject
    JcrMetadataAccess metadataAccess;

    @Inject
    private ServiceLevelAssessmentProvider assessmentProvider;

    @Inject
    private ServiceLevelAgreementProvider agreementProvider;

    @Inject
    private AlertProvider provider;


    /**
     * (non-Javadoc)
     *
     * @see com.thinkbiganalytics.alerts.api.AlertResponder#alertChange(com.thinkbiganalytics.alerts.api.Alert, com.thinkbiganalytics.alerts.api.AlertResponse)
     */
    @Override
    public void alertChange(Alert alert, AlertResponse response) {
        if (alert.getEvents().get(0).getState() == Alert.State.UNHANDLED) {
            if (alert.getType().equals(AssessmentAlerts.VIOLATION_ALERT_TYPE)) {
                try {

                   boolean hasActions = handleViolation(alert);
                   //if we found additional actions mark the alert as being inProgress.
                    //otherwise keep it as unhandled and let an operator handle it
                   if(hasActions) {
                       response.inProgress("Handling SLA Alert");
                   }
                } catch (Exception e) {
                    log.error("ERROR Handling Alert Error {} ", e.getMessage());
                    response.unhandle("Failed to handle violation: " + e.getMessage());
                }
            }
        }
    }

    /**
     * handle the violation.  Return true if the sla has actions configured in additional generating an alert.
     * @param alert the alert
     * @return true if additional actions were triggered, false if not
     */
    private boolean handleViolation(Alert alert) {

        return metadataAccess.read(() -> {
            ServiceLevelAssessment.ID assessmentId = alert.getContent();
            ServiceLevelAssessment assessment = assessmentProvider.findServiceLevelAssessment(assessmentId);
            ServiceLevelAgreement agreement = assessment.getAgreement();
            assessmentProvider.ensureServiceLevelAgreementOnAssessment(assessment);
            agreement = assessment.getAgreement();
            boolean hasActions = false;
            if (agreement != null && agreement.getSlaChecks() != null && !agreement.getSlaChecks().isEmpty()) {

                for (ServiceLevelAgreementCheck check : agreement.getSlaChecks()) {

                    for (ServiceLevelAgreementActionConfiguration configuration : ((JcrServiceLevelAgreementCheck) check).getActionConfigurations(true)) {
                        List<Class<? extends ServiceLevelAgreementAction>> responders = configuration.getActionClasses();

                        if (responders != null) {
                            //first check to see if there is a Spring Bean configured for this class type... if so call that
                            for (Class<? extends ServiceLevelAgreementAction> responderClass : responders) {
                                ServiceLevelAgreementAction action = ServiceLevelAgreementActionUtil.instantiate(responderClass);
                                if (action != null) {
                                    hasActions = true;
                                    log.info("Found {} action", action.getClass().getName());
                                    //reassign the content of the alert to the ServiceLevelAssessment
                                    //validate the action is ok
                                    ServiceLevelAgreementActionValidation validation = ServiceLevelAgreementActionUtil.validateConfiguration(action);
                                    if (validation.isValid()) {
                                        action.respond(configuration, assessment, alert);
                                    } else {
                                        log.error("Unable to invoke SLA ImmutableAction {} while assessing {} due to Configuration error: {}.  Please fix.", action.getClass(), agreement.getName(),
                                                  validation.getValidationMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return hasActions;
        }, MetadataAccess.SERVICE);
    }


}
