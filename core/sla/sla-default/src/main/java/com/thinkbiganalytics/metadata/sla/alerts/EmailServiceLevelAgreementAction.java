package com.thinkbiganalytics.metadata.sla.alerts;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 * Email the users specified in the incoming Configuration class about the SLA violation
 *
 * @see ServiceLevelAgreementActionAlertResponderFactory
 */
public class EmailServiceLevelAgreementAction implements ServiceLevelAgreementAction<EmailServiceLevelAgreementActionConfiguration> {


    @Override
    public boolean respond(EmailServiceLevelAgreementActionConfiguration actionConfiguration, ServiceLevelAssessment assessment, Alert a) {
        String desc = ServiceLevelAssessmentAlertUtil.getDescription(assessment);
        //mail it
        String email = actionConfiguration.getEmailAddresses();

        return true;
    }
}
