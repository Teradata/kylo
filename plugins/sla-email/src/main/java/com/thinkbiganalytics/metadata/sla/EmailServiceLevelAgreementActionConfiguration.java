package com.thinkbiganalytics.metadata.sla;


import com.thinkbiganalytics.classnameregistry.ClassNameChange;
import com.thinkbiganalytics.metadata.sla.alerts.BaseServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfig;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

/**
 * Created by sr186054 on 7/20/16.
 */
@ServiceLevelAgreementActionConfig(
    name = "Email", description = "Email user(s) when the SLA is violated", actionClasses = {EmailServiceLevelAgreementAction.class}
)
@ClassNameChange(classNames = {"com.thinkbiganalytics.metadata.sla.alerts.EmailServiceLevelAgreementActionConfiguration"})
public class EmailServiceLevelAgreementActionConfiguration extends BaseServiceLevelAgreementActionConfiguration {

    @PolicyProperty(name = "EmailAddresses", displayName = "Email addresses", hint = "comma separated email addresses", required = true, type = PolicyPropertyTypes.PROPERTY_TYPE.email)
    private String emailAddresses;


    public EmailServiceLevelAgreementActionConfiguration() {

    }

    public EmailServiceLevelAgreementActionConfiguration(@PolicyPropertyRef(name = "EmailAddresses") String emailAddresses) {
        this.emailAddresses = emailAddresses;
    }


    public String getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(String emailAddresses) {
        this.emailAddresses = emailAddresses;
    }
}
