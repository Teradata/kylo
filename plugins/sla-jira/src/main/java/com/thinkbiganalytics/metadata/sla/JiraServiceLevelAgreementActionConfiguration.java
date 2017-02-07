package com.thinkbiganalytics.metadata.sla;

/*-
 * #%L
 * thinkbig-sla-jira
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

import com.thinkbiganalytics.classnameregistry.ClassNameChange;
import com.thinkbiganalytics.metadata.sla.alerts.BaseServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfig;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.policy.PropertyLabelValue;

/**
 */
@ServiceLevelAgreementActionConfig(
    name = "Jira",
    description = "Create a Jira when the SLA is violated",
    actionClasses = {JiraServiceLevelAgreementAction.class}
)
@ClassNameChange(classNames = {"com.thinkbiganalytics.metadata.sla.alerts.JiraServiceLevelAgreementActionConfiguration"})
public class JiraServiceLevelAgreementActionConfiguration extends BaseServiceLevelAgreementActionConfiguration {

    @PolicyProperty(name = "JiraProjectKey",
                    displayName = "Jira Project Key",
                    hint = "The Jira Project Key ",
                    required = true)
    private String projectKey;
    @PolicyProperty(name = "JiraIssueType",
                    displayName = "Issue Type",
                    hint = "The Jira Type ",
                    value = "Bug", type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                    labelValues = {@PropertyLabelValue(label = "Bug", value = "Bug"),
                                   @PropertyLabelValue(label = "Task", value = "Task")},
                    required = true)
    private String issueType;
    @PolicyProperty(name = "JiraAssignee", displayName = "Assignee", hint = "Who should get assigned this Jira? ", required = true)
    private String assignee;

    public JiraServiceLevelAgreementActionConfiguration() {

    }

    public JiraServiceLevelAgreementActionConfiguration(@PolicyPropertyRef(name = "JiraProjectKey") String projectKey, @PolicyPropertyRef(name = "JiraIssueType") String issueType,
                                                        @PolicyPropertyRef(name = "JiraAssignee") String assignee) {
        this.projectKey = projectKey;
        this.issueType = issueType;
        this.assignee = assignee;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
