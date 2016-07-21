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

    private List<Class<? extends ServiceLevelAgreementAction>> actionClasses;

    public List<Class<? extends ServiceLevelAgreementAction>> getActionClasses() {
        return actionClasses;
    }

    public void setActionClasses(List<Class<? extends ServiceLevelAgreementAction>> actionClasses) {
        this.actionClasses = actionClasses;
    }
}
