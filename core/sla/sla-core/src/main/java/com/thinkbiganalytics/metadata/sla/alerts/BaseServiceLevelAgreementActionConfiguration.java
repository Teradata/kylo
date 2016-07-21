package com.thinkbiganalytics.metadata.sla.alerts;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;

import java.util.List;

/**
 * Created by sr186054 on 7/20/16.
 */
public class BaseServiceLevelAgreementActionConfiguration implements ServiceLevelAgreementActionConfiguration {

    private List<Class<? extends ServiceLevelAgreementAction>> actionClasses;

    public List<Class<? extends ServiceLevelAgreementAction>> getActionClasses() {
        return actionClasses;
    }

    public void setActionClasses(List<Class<? extends ServiceLevelAgreementAction>> actionClasses) {
        this.actionClasses = actionClasses;
    }

}
