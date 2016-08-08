package com.thinkbiganalytics.feedmgr.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.policy.rest.model.BaseUiPolicyRule;

import java.util.List;

/**
 * Created by sr186054 on 7/18/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceLevelAgreementActionUiConfigurationItem extends BaseUiPolicyRule {

    private boolean validConfiguration = false;

    private String validationMessage;

    private List<Class<? extends ServiceLevelAgreementAction>> actionClasses;

    public List<Class<? extends ServiceLevelAgreementAction>> getActionClasses() {
        return actionClasses;
    }

    public void setActionClasses(List<Class<? extends ServiceLevelAgreementAction>> actionClasses) {
        this.actionClasses = actionClasses;
    }


    public boolean isValidConfiguration() {
        return validConfiguration;
    }

    public void setValidConfiguration(boolean validConfiguration) {
        this.validConfiguration = validConfiguration;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
}
