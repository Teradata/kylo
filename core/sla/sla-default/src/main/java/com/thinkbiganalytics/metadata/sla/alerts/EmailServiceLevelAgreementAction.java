package com.thinkbiganalytics.metadata.sla.alerts;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;

/**
 * Email the users specified in the incoming Configuration class about the SLA violation
 *
 * @see ServiceLevelAgreementActionAlertResponderFactory
 */
public class EmailServiceLevelAgreementAction implements ServiceLevelAgreementAction<EmailServiceLevelAgreementActionConfiguration> {

    @Override
    public boolean respond(EmailServiceLevelAgreementActionConfiguration actionConfiguration, Alert a) {
        String desc = ServiceLevelAssessmentAlertUtil.getDescription(a);
        //mail it
        String email = actionConfiguration.getEmailAddresses();

        return true;
    }
}
