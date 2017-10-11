package com.thinkbiganalytics.metadata.sla;

/*-
 * #%L
 * thinkbig-sla-email
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
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementEmailTemplate;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

/**
 * SLA action to allow users to send an email if an SLA is violated
 * This will be presented to the user as a possible SLA action in the UI since it is annotated with the {@link ServiceLevelAgreementActionConfig}
 */
@ServiceLevelAgreementActionConfig(
    name = "Email", description = "Email user(s) when the SLA is violated", actionClasses = {EmailServiceLevelAgreementAction.class}, velocityTemplateType = ServiceLevelAgreementEmailTemplate.EMAIL_TEMPLATE_TYPE
)
@ClassNameChange(classNames = {"com.thinkbiganalytics.metadata.sla.alerts.EmailServiceLevelAgreementActionConfiguration"})
public class EmailServiceLevelAgreementActionConfiguration extends BaseServiceLevelAgreementActionConfiguration {

    @PolicyProperty(name = "EmailAddresses", displayName = "Email addresses", hint = "comma separated email addresses", required = true, type = PolicyPropertyTypes.PROPERTY_TYPE.emails)
    private String emailAddresses;

    @PolicyProperty(name="EmailTemplate",displayName = "Email template",type = PolicyPropertyTypes.PROPERTY_TYPE.velocityTemplate)
    private String velocityTemplateId;


    public EmailServiceLevelAgreementActionConfiguration() {

    }

    public EmailServiceLevelAgreementActionConfiguration(@PolicyPropertyRef(name = "EmailAddresses") String emailAddresses) {
        this.emailAddresses = emailAddresses;
    }


    public EmailServiceLevelAgreementActionConfiguration(@PolicyPropertyRef(name = "EmailAddresses") String emailAddresses, @PolicyPropertyRef(name="EmailTemplate") String velocityTemplateId) {
        this.emailAddresses = emailAddresses;
        this.velocityTemplateId = velocityTemplateId;
    }


    public String getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(String emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public String getVelocityTemplateId() {
        return velocityTemplateId;
    }

    public void setVelocityTemplateId(String velocityTemplateId) {
        this.velocityTemplateId = velocityTemplateId;
    }
}
