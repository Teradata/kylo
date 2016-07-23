package com.thinkbiganalytics.metadata.sla.alerts;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;

/**
 * create the JIRA issue when  SLA is violated
 *
 * @see ServiceLevelAgreementActionAlertResponderFactory
 */
public class JiraServiceLevelAgreementAction implements ServiceLevelAgreementAction<JiraServiceLevelAgreementActionConfiguration> {

    @Override
    public boolean respond(JiraServiceLevelAgreementActionConfiguration actionConfiguration, Alert a) {
        String desc = ServiceLevelAssessmentAlertUtil.getDescription(a);
        String projectKey = actionConfiguration.getProjectKey();
        String issueType = actionConfiguration.getIssueType();

        ///do call to JIRA
        return true;
    }
}
