package com.thinkbiganalytics.feedmgr.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.policy.rest.model.BaseUiPolicyRule;

/**
 * Created by sr186054 on 7/18/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceLevelAgreementRule extends BaseUiPolicyRule {

    private ObligationGroup.Condition condition = ObligationGroup.Condition.REQUIRED;

    public ObligationGroup.Condition getCondition() {
        return condition;
    }

    public void setCondition(ObligationGroup.Condition condition) {
        this.condition = condition;
    }
}
