package com.thinkbiganalytics.metadata.sla.alerts;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 * create the JIRA issue when  SLA is violated
 *
 * @see ServiceLevelAgreementActionAlertResponderFactory
 */
public class JiraServiceLevelAgreementAction implements ServiceLevelAgreementAction<JiraServiceLevelAgreementActionConfiguration> {

    @Override
    public boolean respond(JiraServiceLevelAgreementActionConfiguration actionConfiguration, ServiceLevelAssessment assessment, Alert a) {
        String desc = ServiceLevelAssessmentAlertUtil.getDescription(assessment);
        String projectKey = actionConfiguration.getProjectKey();
        String issueType = actionConfiguration.getIssueType();

        ///do call to JIRA
        return true;
    }
}
