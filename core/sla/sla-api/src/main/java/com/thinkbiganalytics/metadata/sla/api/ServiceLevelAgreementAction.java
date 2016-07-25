package com.thinkbiganalytics.metadata.sla.api;

import com.thinkbiganalytics.alerts.api.Alert;

/**
 * Created by sr186054 on 7/20/16.
 */
public interface ServiceLevelAgreementAction<T extends ServiceLevelAgreementActionConfiguration> {

    boolean respond(T actionConfiguration, ServiceLevelAssessment assessment,Alert a);

}
