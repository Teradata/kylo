package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;

import java.util.List;

/**
 * Created by sr186054 on 7/22/16.
 */
public interface ServiceLevelAgreementCheckBuilder {

    ServiceLevelAgreementCheckBuilder actionConfiguration(ServiceLevelAgreementActionConfiguration configuration);

    ServiceLevelAgreementCheckBuilder actionConfigurations(List<ServiceLevelAgreementActionConfiguration> configurations);

    ServiceLevelAgreementCheckBuilder cronExpression(String cronExpression);

    /**
     * Removes all slaCheck objects under this SLA
     */
    ServiceLevelAgreementCheckBuilder removeSlaChecks();

    ServiceLevelAgreementCheck build();
}
